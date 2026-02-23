package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import me.matsumo.agentguiplugin.viewmodel.transcript.TranscriptTailer
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.HookEvent
import me.matsumo.claude.agent.types.HookOutput
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SubagentStartHookInput
import me.matsumo.claude.agent.types.SubagentStopHookInput
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val permissionHandler = PermissionHandler(
        currentState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
    )

    private var client: ClaudeSDKClient? = null
    private var activeTurnJob: Job? = null
    private var activeTurnId = 0L

    // Sub-agent transcript tailing: agentId -> tailer
    private val activeTailers = mutableMapOf<String, TranscriptTailer>()

    // Hook toolUseId (CLI internal UUID) → mutable key reference for tailer callback.
    // Initially points to hookToolUseId, updated to real parentToolUseId (toolu_...) when resolved.
    private val tailerKeyRefs = mutableMapOf<String, AtomicReference<String>>()

    // Ordered list of hookToolUseIds not yet mapped to real parentToolUseId
    private val unresolvedHookIds = mutableListOf<String>()

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                createSession {
                    model = Model.HAIKU
                    cwd = projectBasePath
                    cliPath = claudeCodePath
                    includePartialMessages = true
                    canUseTool { toolName, input, _ ->
                        permissionHandler.request(toolName, input)
                    }
                    hooks {
                        on(HookEvent.SUBAGENT_START) { input, toolUseId, _ ->
                            val si = input as SubagentStartHookInput
                            startTailing(si.agentId, si.transcriptPath, toolUseId)
                            HookOutput.proceed()
                        }
                        on(HookEvent.SUBAGENT_STOP) { input, _, _ ->
                            val si = input as SubagentStopHookInput
                            stopTailing(si.agentId)
                            HookOutput.proceed()
                        }
                    }
                }.also {
                    it.connect()
                    client = it
                }

                _uiState.update { it.copy(sessionState = SessionState.Ready) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    fun attachFile(file: AttachedFile) {
        _uiState.update { state ->
            if (state.attachedFiles.any { it.id == file.id }) state
            else state.copy(attachedFiles = state.attachedFiles + file)
        }
    }

    fun detachFile(file: AttachedFile) {
        _uiState.update { state ->
            state.copy(attachedFiles = state.attachedFiles.filter { it.id != file.id })
        }
    }

    fun sendMessage(text: String) {
        if (text.isEmpty()) return

        val session = client ?: return
        val files = _uiState.value.attachedFiles

        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
            attachedFiles = files,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                attachedFiles = emptyList(),
                sessionState = SessionState.Processing,
            )
        }

        activeTurnJob?.cancel()
        activeTurnJob = scope.launch {
            try {
                val turnId = ++activeTurnId

                if (files.isEmpty()) {
                    session.send(text)
                } else {
                    session.send(buildContentBlocks(text, files))
                }

                session.receiveResponse().collect { message ->
                    if (turnId != activeTurnId) return@collect

                    when (message) {
                        is SystemMessage -> handleSystemMessage(message)

                        is StreamEvent -> {
                            /* wait for completion */
                        }

                        is AssistantMessage -> {
                            handleAssistantMessage(message)
                        }
                        is ResultMessage -> handleResultMessage(message)
                        is UserMessage -> {
                            /* tool results - ignore */
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    private fun buildContentBlocks(text: String, files: List<AttachedFile>): List<JsonObject> {
        val blocks = mutableListOf<JsonObject>()

        blocks.add(
            buildJsonObject {
                put("type", "text")
                put("text", text)
            },
        )

        for (file in files) {
            if (file.isImage) {
                blocks.add(file.toImageBlock())
            } else {
                blocks.add(file.toDocumentBlock())
            }
        }

        return blocks
    }

    private fun handleSystemMessage(message: SystemMessage) {
        if (message.isInit) {
            _uiState.update { it.copy(sessionId = message.sessionId) }
        }
    }

    private fun handleAssistantMessage(message: AssistantMessage) {
        val pid = message.parentToolUseId
        if (pid != null) {
            // Sub-agent message from stream-json.
            // Resolve hookToolUseId → real parentToolUseId (toolu_...) if needed.
            if (!_uiState.value.subAgentTasks.containsKey(pid) && unresolvedHookIds.isNotEmpty()) {
                val hookId = unresolvedHookIds.removeFirst()

                // Update the tailer's key so new messages go to the correct key
                tailerKeyRefs[hookId]?.set(pid)

                // Re-key the SubAgentTask from hookId to pid
                _uiState.update { state ->
                    val task = state.subAgentTasks[hookId] ?: return@update state
                    val newTasks = state.subAgentTasks.toMutableMap()
                    newTasks.remove(hookId)
                    // Merge with any messages that may have arrived at pid during the race window
                    val existingAtPid = newTasks[pid]
                    val mergedMessages = task.messages + (existingAtPid?.messages ?: emptyList())
                    newTasks[pid] = task.copy(id = pid, messages = mergedMessages)
                    state.copy(subAgentTasks = newTasks)
                }
            }

            // Update spawnedByToolName from stream-json if not yet set
            _uiState.update { state ->
                val existing = state.subAgentTasks[pid]
                if (existing == null || existing.spawnedByToolName != null) state
                else {
                    val toolName = message.parentToolName
                    if (toolName != null) {
                        state.copy(
                            subAgentTasks = state.subAgentTasks + (
                                pid to existing.copy(spawnedByToolName = toolName)
                            ),
                        )
                    } else {
                        state
                    }
                }
            }
        } else {
            val blocks = message.content.map { it.toUiBlock() }
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
            )

            _uiState.update { it.copy(messages = it.messages + assistantMsg) }
        }
    }

    private fun handleResultMessage(message: ResultMessage) {
        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                totalCostUsd = message.totalCostUsd ?: it.totalCostUsd,
                errorMessage = if (message.isError) "Turn ended with error: ${message.subtype}" else null,
            )
        }
    }

    // --- Sub-agent transcript tailing ---

    private fun startTailing(agentId: String, transcriptPath: String, hookToolUseId: String?) {
        if (hookToolUseId == null) return

        val jsonlPath = "${transcriptPath.removeSuffix(".jsonl")}/subagents/agent-$agentId.jsonl"

        // Mutable key: starts as hookToolUseId (CLI UUID), updated to real parentToolUseId (toolu_...)
        // when handleAssistantMessage resolves it.
        val keyRef = AtomicReference(hookToolUseId)
        tailerKeyRefs[hookToolUseId] = keyRef
        unresolvedHookIds.add(hookToolUseId)

        // Create initial SubAgentTask under hookToolUseId
        _uiState.update { state ->
            if (state.subAgentTasks.containsKey(hookToolUseId)) state
            else state.copy(
                subAgentTasks = state.subAgentTasks + (
                    hookToolUseId to SubAgentTask(id = hookToolUseId)
                ),
            )
        }

        val tailer = TranscriptTailer(scope)
        activeTailers[agentId] = tailer

        tailer.start(jsonlPath) { assistantMsg ->
            val currentKey = keyRef.get()
            _uiState.update { state ->
                val existing = state.subAgentTasks[currentKey]
                val oldMessages = existing?.messages ?: emptyList()

                // Same id → replace (partial update); new id → append
                val existingIndex = oldMessages.indexOfFirst { it.id == assistantMsg.id }
                val newMessages = if (existingIndex >= 0) {
                    oldMessages.toMutableList().apply { set(existingIndex, assistantMsg) }
                } else {
                    oldMessages + assistantMsg
                }

                val task = (existing ?: SubAgentTask(id = currentKey)).copy(messages = newMessages)
                state.copy(subAgentTasks = state.subAgentTasks + (currentKey to task))
            }
        }
    }

    private fun stopTailing(agentId: String) {
        activeTailers.remove(agentId)?.stop()
    }

    private fun stopAllTailing() {
        activeTailers.values.forEach { it.stop() }
        activeTailers.clear()
        tailerKeyRefs.clear()
        unresolvedHookIds.clear()
    }

    // --- Public API ---

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        stopAllTailing()
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput)
        }
    }

    fun reconnect() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        stopAllTailing()
        client?.close()
        client = null
        _uiState.update { ChatUiState(sessionState = SessionState.Connecting) }
        initialize()
    }

    fun dispose() {
        activeTurnJob?.cancel()
        stopAllTailing()
        client?.close()
        client = null
    }
}
