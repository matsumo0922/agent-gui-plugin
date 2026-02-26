package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightChecker
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightResult
import me.matsumo.agentguiplugin.viewmodel.transcript.TranscriptTailer
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.resumeSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.HookEvent
import me.matsumo.claude.agent.types.HookOutput
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.SessionOptionsBuilder
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SubagentStartHookInput
import me.matsumo.claude.agent.types.SubagentStopHookInput
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    /** VM が所有する CoroutineScope。dispose() で cancel される。 */
    private val vmScope = CoroutineScope(SupervisorJob())

    private val _uiState = MutableStateFlow(
        ChatUiState(
            model = initialModel,
            permissionMode = initialPermissionMode,
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val preflightChecker = PreflightChecker()
    private var authProcess: Process? = null
    private var authStdout: BufferedReader? = null
    private var authStdin: OutputStreamWriter? = null
    private var authReaderJob: Job? = null

    private val permissionHandler = PermissionHandler(
        currentState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
    )

    val branchSessionManager = BranchSessionManager(
        applyCommonConfig = { model, permissionMode -> applyCommonConfig(model, permissionMode) },
    )

    private val startMutex = Mutex()
    private var startJob: Job? = null

    @Volatile
    private var disposed = false

    private var client: ClaudeSDKClient? = null
    private var activeTurnJob: Job? = null
    private var activeTurnId = 0L

    // Last per-turn context token count from message_start stream event (not cumulative)
    private var lastTurnInputTokens = 0L

    // Sub-agent transcript tailing: agentId -> tailer
    private val activeTailers = mutableMapOf<String, TranscriptTailer>()

    // Hook toolUseId (CLI internal UUID) → mutable key reference for tailer callback.
    // Initially points to hookToolUseId, updated to real parentToolUseId (toolu_...) when resolved.
    private val tailerKeyRefs = mutableMapOf<String, AtomicReference<String>>()

    // agentId → keyRef for resolving SubAgentTask key on stopTailing
    private val agentKeyRefs = mutableMapOf<String, AtomicReference<String>>()

    // Ordered list of hookToolUseIds not yet mapped to real parentToolUseId
    private val unresolvedHookIds = mutableListOf<String>()

    /**
     * セッションを開始する。resumeSessionId が指定された場合はセッション再開。
     * Mutex により多重起動を防止。dispose 済みの場合は何もしない。
     *
     * Idempotent: Disconnected 以外の状態 or startJob がアクティブなら何もしない。
     */
    suspend fun start(resumeSessionId: String? = null) {
        startMutex.withLock {
            if (disposed) return
            if (startJob?.isActive == true) return
            if (_uiState.value.sessionState != SessionState.Disconnected) return

            // ロック内で先に Connecting に更新。これにより次の start() 呼び出しを確実にブロック。
            _uiState.update { it.copy(sessionState = SessionState.Connecting) }

            startJob = vmScope.launch {
                val result = preflightChecker.check(claudeCodePath)
                when (result) {
                    is PreflightResult.Ready -> connectSession(resumeSessionId)
                    is PreflightResult.AuthRequired -> {
                        authProcess = result.process
                        authStdout = result.stdout
                        authStdin = result.stdin
                        _uiState.update {
                            it.copy(
                                sessionState = SessionState.AuthRequired,
                                authOutputLines = result.initialOutput,
                            )
                        }
                        startAuthOutputReader()
                    }
                    is PreflightResult.Error -> {
                        _uiState.update {
                            it.copy(
                                sessionState = SessionState.Error,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 現在のセッションをクリアし、初期状態に戻す。
     * 進行中の turn、permission 待ち、sub-agent tailer も安全に中断する。
     * dispose() とは異なり、VM 自体は再利用可能。
     */
    suspend fun clear() {
        startMutex.withLock {
            startJob?.cancel()
            startJob = null
            activeTurnJob?.cancel()
            activeTurnJob = null
            cleanupAuthProcess()
            stopAllTailing()
            permissionHandler.cancelPending()
            branchSessionManager.closeAll()
            client?.close()
            client = null
            _uiState.value = ChatUiState(
                model = _uiState.value.model,
                permissionMode = _uiState.value.permissionMode,
            )
        }
    }

    /**
     * VM を完全に破棄する。再利用不可。
     * 所有する CoroutineScope を cancel し、全リソースを解放する。
     */
    fun dispose() {
        disposed = true
        vmScope.cancel() // startJob, activeTurnJob, tailer jobs もすべて cancel される
        cleanupAuthProcess()
        stopAllTailing()
        permissionHandler.cancelPending()
        branchSessionManager.closeAll()
        client?.close()
        client = null
    }

    /**
     * 履歴メッセージを UI に投入する（resume 時の過去メッセージ表示用）。
     * start() の前に呼び出すこと。
     */
    fun importHistory(messages: List<ChatMessage>, branchSessionId: String? = null) {
        val tree = buildConversationTreeFromFlatList(messages, branchSessionId)
        _uiState.update {
            it.copy(
                messages = messages,
                conversationTree = tree,
                conversationCursor = ConversationCursor(activeLeafPath = tree.getActiveLeafPath()),
            )
        }
    }

    private suspend fun connectSession(resumeSessionId: String?) {
        // 防御的チェック: dispose 済みなら何もしない
        if (disposed) return

        try {
            val state = _uiState.value
            val localClient = if (resumeSessionId != null) {
                resumeSession(resumeSessionId) {
                    applyCommonConfig(state.model, state.permissionMode)
                    forkSession = true // 元セッション保全
                }
            } else {
                createSession {
                    applyCommonConfig(state.model, state.permissionMode)
                }
            }

            // SDK client 作成後、代入前に再チェック
            if (disposed || !currentCoroutineContext().isActive) {
                localClient.close()
                return
            }

            localClient.connect()
            client = localClient

            _uiState.update {
                it.copy(sessionState = SessionState.Ready)
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

    /**
     * 全セッション（初期・ブランチ・再接続）で共通の設定を適用。
     * セッション設定要件を一元化し、設定漏れによる回帰を防ぐ。
     */
    private fun SessionOptionsBuilder.applyCommonConfig(
        model: Model,
        permissionMode: PermissionMode,
    ) {
        this.model = model
        this.permissionMode = permissionMode
        this.cwd = projectBasePath
        this.cliPath = claudeCodePath
        this.includePartialMessages = true

        if (claudeCodePath != null) {
            env { put("CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK", "1") }
        }

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
        if (disposed) return

        val session = client ?: return
        val files = _uiState.value.attachedFiles

        val editGroupId = UUID.randomUUID().toString()
        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            editGroupId = editGroupId,
            text = text,
            attachedFiles = files,
        )

        _uiState.update { state ->
            val (newTree, newPath) = state.conversationTree.appendUserMessage(
                userMessage = userMsg,
                branchSessionId = state.sessionId,
            )
            state.copy(
                messages = state.messages + userMsg,
                conversationTree = newTree,
                conversationCursor = ConversationCursor(activeLeafPath = newPath),
                attachedFiles = emptyList(),
                sessionState = SessionState.Processing,
            )
        }

        activeTurnJob?.cancel()
        activeTurnJob = vmScope.launch {
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

                        is StreamEvent -> handleStreamEvent(message)

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
            handleSubAgentAssistantMessage(message, pid)
        } else {
            val blocks = message.content.map { it.toUiBlock() }
            val messageId = message.uuid ?: UUID.randomUUID().toString()
            val assistantMsg = ChatMessage.Assistant(
                id = messageId,
                blocks = blocks,
            )

            _uiState.update { state ->
                val cursor = state.conversationCursor
                val targetPath = cursor.activeLeafPath
                val isSameStreaming = cursor.activeStreamingMessageId == messageId

                val newTree = if (isSameStreaming) {
                    // 同一メッセージの partial update: 最後の応答を差し替え
                    state.conversationTree.updateLastResponse(targetPath) { last ->
                        if (last is ChatMessage.Assistant && last.id == messageId) assistantMsg else last
                    }
                } else {
                    // 新しいメッセージ: 追加
                    state.conversationTree.appendResponse(targetPath, assistantMsg)
                }

                state.copy(
                    // Legacy: flat list (並行書込み、Step 11 で削除)
                    messages = if (isSameStreaming) {
                        val idx = state.messages.indexOfLast { it.id == messageId }
                        if (idx >= 0) state.messages.toMutableList().apply { set(idx, assistantMsg) }
                        else state.messages + assistantMsg
                    } else {
                        state.messages + assistantMsg
                    },
                    conversationTree = newTree,
                    conversationCursor = cursor.copy(activeStreamingMessageId = messageId),
                )
            }
        }
    }

    private fun handleSubAgentAssistantMessage(message: AssistantMessage, pid: String) {
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
    }

    private fun handleStreamEvent(event: StreamEvent) {
        if (event.parentToolUseId != null) return // サブエージェントのイベントは無視
        val type = event.event["type"]?.jsonPrimitive?.contentOrNull ?: return
        if (type != "message_start") return

        val usage = event.event["message"]?.jsonObject?.get("usage")?.jsonObject ?: return
        val inputTokens = usage["input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        val cacheCreation = usage["cache_creation_input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        val cacheRead = usage["cache_read_input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        lastTurnInputTokens = inputTokens + cacheCreation + cacheRead
    }

    private fun handleResultMessage(message: ResultMessage) {
        // lastTurnInputTokens は最後の message_start イベントの値 (単一API呼び出し分)
        // ResultMessage.usage は全API呼び出しの累積値なので使わない
        val contextWindow = 200_000L
        val usage = (lastTurnInputTokens.toFloat() / contextWindow).coerceIn(0f, 1f)

        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                conversationCursor = it.conversationCursor.copy(activeStreamingMessageId = null),
                totalCostUsd = message.totalCostUsd ?: it.totalCostUsd,
                contextUsage = usage,
                totalInputTokens = lastTurnInputTokens,
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
                    hookToolUseId to SubAgentTask(id = hookToolUseId, startedAt = System.currentTimeMillis())
                ),
            )
        }

        agentKeyRefs[agentId] = keyRef

        val tailer = TranscriptTailer(vmScope)
        activeTailers[agentId] = tailer

        tailer.start(jsonlPath) { message ->
            val currentKey = keyRef.get()
            _uiState.update { state ->
                val existing = state.subAgentTasks[currentKey]
                val oldMessages = existing?.messages ?: emptyList()

                // Same id → replace (partial update); new id → append
                val existingIndex = oldMessages.indexOfFirst { it.id == message.id }
                val newMessages = if (existingIndex >= 0) {
                    oldMessages.toMutableList().apply { set(existingIndex, message) }
                } else {
                    oldMessages + message
                }

                val task = (existing ?: SubAgentTask(id = currentKey)).copy(messages = newMessages)
                state.copy(subAgentTasks = state.subAgentTasks + (currentKey to task))
            }
        }
    }

    private fun stopTailing(agentId: String) {
        activeTailers.remove(agentId)?.stop()

        val keyRef = agentKeyRefs.remove(agentId) ?: return
        val taskKey = keyRef.get()
        val now = System.currentTimeMillis()

        _uiState.update { state ->
            val task = state.subAgentTasks[taskKey] ?: return@update state
            state.copy(
                subAgentTasks = state.subAgentTasks + (taskKey to task.copy(completedAt = now)),
            )
        }
    }

    private fun stopAllTailing() {
        activeTailers.values.forEach { it.stop() }
        activeTailers.clear()
        tailerKeyRefs.clear()
        agentKeyRefs.clear()
        unresolvedHookIds.clear()
    }

    // --- Public API ---

    fun changeModel(model: Model) {
        vmScope.launch { client?.setModel(model.modelId) }
        _uiState.update { it.copy(model = model) }
    }

    fun changePermissionMode(mode: PermissionMode) {
        vmScope.launch { client?.setPermissionMode(mode) }
        _uiState.update { it.copy(permissionMode = mode) }
    }

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        // activeTurnId をインクリメントして現在のターンのメッセージ処理を無効化する。
        // activeTurnJob はキャンセルしない。interrupt 後に CLI が送る ResultMessage を
        // 既存の receiveResponse() Flow に消費させ、messageChannel に残留させないため。
        // 残留すると次の receiveResponse() が古い ResultMessage を拾って即終了してしまう。
        activeTurnId++
        stopAllTailing()
        vmScope.launch { client?.interrupt() }

        val interruptedMsg = ChatMessage.Interrupted(
            id = UUID.randomUUID().toString(),
        )
        _uiState.update { state ->
            val path = state.conversationCursor.activeLeafPath
            state.copy(
                sessionState = SessionState.WaitingForInput,
                messages = state.messages + interruptedMsg,
                conversationTree = state.conversationTree.appendResponse(path, interruptedMsg),
                conversationCursor = state.conversationCursor.copy(activeStreamingMessageId = null),
            )
        }
    }

    fun reconnect() {
        vmScope.launch { clear(); start() }
    }

    // --- Auth process management ---

    fun sendAuthInput(text: String) {
        val writer = authStdin ?: return
        vmScope.launch(Dispatchers.IO) {
            try {
                writer.write(text + "\n")
                writer.flush()
            } catch (_: IOException) {
                // Process may have exited
            }
        }
    }

    fun confirmAuthComplete() {
        vmScope.launch {
            cleanupAuthProcess()
            _uiState.update {
                it.copy(
                    sessionState = SessionState.Disconnected,
                    authOutputLines = emptyList(),
                )
            }
            start()
        }
    }

    private fun startAuthOutputReader() {
        val reader = authStdout ?: return
        authReaderJob = vmScope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    _uiState.update {
                        it.copy(authOutputLines = it.authOutputLines + line)
                    }
                }
            } catch (_: IOException) {
                // Stream closed
            }
            // Process exited — auto-proceed to re-preflight
            confirmAuthComplete()
        }
    }

    private fun cleanupAuthProcess() {
        // プロセス破棄 → ストリーム close を先に行い、readLine() のブロッキングを解除してから
        // ジョブを cancel する。逆順だと readLine() が永久に suspend する。
        authProcess?.destroyForcibly()
        authProcess = null
        runCatching { authStdin?.close() }
        runCatching { authStdout?.close() }
        authStdin = null
        authStdout = null
        authReaderJob?.cancel()
        authReaderJob = null
    }
}
