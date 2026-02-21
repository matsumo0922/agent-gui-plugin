package me.matsumo.agentguiplugin.viewmodel

import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import me.matsumo.agentguiplugin.viewmodel.mapper.ParsedStreamEvent
import me.matsumo.agentguiplugin.viewmodel.mapper.parseStreamEvent
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private var streamingBuffer: StreamingBuffer? = null
    private var lastUiUpdateTime = 0L
    private var activeTurnJob: Job? = null
    private var activeTurnId = 0L
    private var streamingMessageId: String? = null

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

    fun sendMessage(text: String) {
        val session = client ?: return

        if (text.isEmpty()) return

        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                sessionState = SessionState.Streaming,
                isStreaming = true,
            )
        }

        activeTurnJob?.cancel()
        val turnId = ++activeTurnId
        resetStreamingState()

        activeTurnJob = scope.launch {
            try {
                session.send(text)
                session.receiveResponse().collect { msg ->
                    if (turnId != activeTurnId) return@collect

                    when (msg) {
                        is SystemMessage -> {
                            if (msg.isInit) {
                                _uiState.update { it.copy(sessionId = msg.sessionId) }
                            }
                        }

                        is StreamEvent -> handleStreamEvent(msg)
                        is AssistantMessage -> handleAssistantMessage(msg)
                        is ResultMessage -> handleResultMessage(msg)
                        is UserMessage -> { /* tool results - ignore */ }
                    }
                }
            } catch (e: CancellationException) {
                // Normal cancellation (e.g. new message sent, abort) — don't treat as error.
                // Streaming state is already reset by the caller before cancellation.
                throw e
            } catch (e: Exception) {
                resetStreamingState()
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        isStreaming = false,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    private fun handleStreamEvent(event: StreamEvent) {
        val parsed = parseStreamEvent(event.event) ?: return

        when (parsed) {
            is ParsedStreamEvent.MessageStart -> {
                streamingBuffer = StreamingBuffer()
                streamingMessageId = UUID.randomUUID().toString()
                val assistantMsg = ChatMessage.Assistant(
                    id = streamingMessageId!!,
                    blocks = emptyList(),
                    isComplete = false,
                )
                _uiState.update {
                    it.copy(messages = it.messages + assistantMsg)
                }
            }

            is ParsedStreamEvent.ContentBlockStart -> {
                val buffer = streamingBuffer ?: return
                buffer.startBlock(parsed.index, parsed.blockType, parsed.toolName, parsed.toolUseId)
                updateStreamingMessage()
            }

            is ParsedStreamEvent.ContentBlockDelta -> {
                val buffer = streamingBuffer ?: return
                buffer.appendDelta(parsed.index, parsed.deltaType, parsed.text)
                throttledUpdateStreamingMessage()
            }

            is ParsedStreamEvent.ContentBlockStop -> {
                val buffer = streamingBuffer ?: return
                buffer.stopBlock(parsed.index)
                updateStreamingMessage()
            }

            is ParsedStreamEvent.MessageStop -> {
                // AssistantMessage will follow to finalize
            }
        }
    }

    private fun handleAssistantMessage(msg: AssistantMessage) {
        val blocks = msg.content.map { it.toUiBlock() }
        val msgId = streamingMessageId

        if (msgId != null) {
            replaceStreamingMessage(msgId, blocks, isComplete = true)
            resetStreamingState()
        } else {
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
                isComplete = true,
            )
            _uiState.update { it.copy(messages = it.messages + assistantMsg) }
        }
    }

    private fun handleResultMessage(msg: ResultMessage) {
        val buffer = streamingBuffer
        val msgId = streamingMessageId
        if (buffer != null && msgId != null && buffer.hasContent()) {
            replaceStreamingMessage(msgId, buffer.toUiBlocks(), isComplete = true)
        }
        resetStreamingState()

        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                isStreaming = false,
                totalCostUsd = msg.totalCostUsd ?: it.totalCostUsd,
                errorMessage = if (msg.isError) "Turn ended with error: ${msg.subtype}" else null,
            )
        }
    }

    private fun throttledUpdateStreamingMessage() {
        val now = System.currentTimeMillis()
        if (now - lastUiUpdateTime < UI_UPDATE_THROTTLE_MS) return
        lastUiUpdateTime = now
        updateStreamingMessage()
    }

    private fun updateStreamingMessage() {
        val buffer = streamingBuffer ?: return
        val msgId = streamingMessageId ?: return

        lastUiUpdateTime = System.currentTimeMillis()
        replaceStreamingMessage(msgId, buffer.toUiBlocks(), isComplete = false)
    }

    private fun replaceStreamingMessage(
        messageId: String,
        blocks: List<UiContentBlock>,
        isComplete: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { existing ->
                    if (existing.id == messageId) {
                        ChatMessage.Assistant(
                            id = messageId,
                            blocks = blocks,
                            isComplete = isComplete,
                        )
                    } else {
                        existing
                    }
                },
            )
        }
    }

    private fun resetStreamingState() {
        streamingBuffer = null
        streamingMessageId = null
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
        resetStreamingState()
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput, isStreaming = false)
        }
    }

    fun reconnect() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        resetStreamingState()
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

    companion object {
        private const val UI_UPDATE_THROTTLE_MS = 50L
    }
}
