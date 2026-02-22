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
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import java.io.File
import java.util.*

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

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                createSession {
                    cwd = projectBasePath
                    cliPath = claudeCodePath
                    includePartialMessages = true
                    canUseTool { toolName, input, _ ->
                        permissionHandler.request(toolName, input)
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
        val session = client ?: return

        if (text.isEmpty()) return

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
        val turnId = ++activeTurnId

        activeTurnJob = scope.launch {
            try {
                if (files.isEmpty()) {
                    session.send(text)
                } else {
                    session.send(buildContentBlocks(text, files))
                }

                session.receiveResponse().collect { msg ->
                    if (turnId != activeTurnId) return@collect

                    when (msg) {
                        is SystemMessage -> {
                            if (msg.isInit) {
                                _uiState.update { it.copy(sessionId = msg.sessionId) }
                            }
                        }

                        is StreamEvent -> { /* wait for completion */ }
                        is AssistantMessage -> handleAssistantMessage(msg)
                        is ResultMessage -> handleResultMessage(msg)
                        is UserMessage -> { /* tool results - ignore */ }
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

        // Text block
        blocks.add(buildJsonObject {
            put("type", "text")
            put("text", text)
        })

        // File blocks
        for (file in files) {
            if (file.isImage) {
                blocks.add(file.toImageBlock())
            } else {
                blocks.add(file.toDocumentBlock())
            }
        }

        return blocks
    }

    private fun AttachedFile.toImageBlock(): JsonObject = buildJsonObject {
        put("type", "image")
        put("source", buildJsonObject {
            put("type", "base64")
            put("data", Base64.getEncoder().encodeToString(File(path).readBytes()))
            put("media_type", inferMediaType(path))
        })
    }

    private fun AttachedFile.toDocumentBlock(): JsonObject = buildJsonObject {
        put("type", "document")
        put("source", buildJsonObject {
            put("type", "text")
            put("media_type", "text/plain")
            put("data", File(path).readText())
        })
    }

    private fun inferMediaType(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }

    private fun handleAssistantMessage(msg: AssistantMessage) {
        val pid = msg.parentToolUseId
        if (pid != null) {
            // サブエージェントのメッセージ → subAgentTasks map を O(1) 更新
            val blocks = msg.content.map { it.toUiBlock() }
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
            )
            _uiState.update { state ->
                val existing = state.subAgentTasks[pid]
                val toolName = existing?.spawnedByToolName ?: msg.parentToolName
                val task = existing?.copy(messages = existing.messages + assistantMsg)
                    ?: SubAgentTask(
                        id = pid,
                        spawnedByToolName = toolName,
                        messages = listOf(assistantMsg),
                    )
                state.copy(subAgentTasks = state.subAgentTasks + (pid to task))
            }
        } else {
            // メインのアシスタントメッセージ → messages に直接追加
            val blocks = msg.content.map { it.toUiBlock() }
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
            )
            _uiState.update { it.copy(messages = it.messages + assistantMsg) }
        }
    }

    private fun handleResultMessage(msg: ResultMessage) {
        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                totalCostUsd = msg.totalCostUsd ?: it.totalCostUsd,
                errorMessage = if (msg.isError) "Turn ended with error: ${msg.subtype}" else null,
            )
        }
    }

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput)
        }
    }

    fun reconnect() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        client?.close()
        client = null
        _uiState.update { ChatUiState(sessionState = SessionState.Connecting) }
        initialize()
    }

    fun dispose() {
        activeTurnJob?.cancel()
        client?.close()
        client = null
    }
}
