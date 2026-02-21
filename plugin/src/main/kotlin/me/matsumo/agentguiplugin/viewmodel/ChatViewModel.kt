package me.matsumo.agentguiplugin.viewmodel

import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import kotlinx.coroutines.CoroutineScope
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

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                val session = createSession {
                    cwd = projectBasePath
                    cliPath = claudeCodePath
                    canUseTool { toolName, input, _ ->
                        permissionHandler.request(toolName, input)
                    }
                }
                session.connect()
                client = session
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

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return
        val session = client ?: return

        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = "",
                sessionState = SessionState.Streaming,
                isStreaming = true,
            )
        }

        scope.launch {
            try {
                session.send(text)
                session.receiveResponse().collect { msg ->
                    when (msg) {
                        is SystemMessage -> {
                            if (msg.isInit) {
                                _uiState.update {
                                    it.copy(sessionId = msg.sessionId)
                                }
                            }
                        }

                        is AssistantMessage -> {
                            val blocks = msg.content.map { it.toUiBlock() }
                            val assistantMsg = ChatMessage.Assistant(
                                id = UUID.randomUUID().toString(),
                                blocks = blocks,
                                isComplete = true,
                            )
                            _uiState.update {
                                it.copy(messages = it.messages + assistantMsg)
                            }
                        }

                        is ResultMessage -> {
                            _uiState.update {
                                it.copy(
                                    sessionState = SessionState.WaitingForInput,
                                    isStreaming = false,
                                    totalCostUsd = msg.totalCostUsd ?: it.totalCostUsd,
                                    errorMessage = if (msg.isError) "Turn ended with error: ${msg.subtype}" else null,
                                )
                            }
                        }

                        is UserMessage -> { /* tool results - ignore */ }
                        is StreamEvent -> { /* not used */ }
                    }
                }
            } catch (e: Exception) {
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

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput, isStreaming = false)
        }
    }

    fun reconnect() {
        client?.close()
        client = null
        _uiState.update { ChatUiState(sessionState = SessionState.Connecting) }
        initialize()
    }

    fun dispose() {
        client?.close()
        client = null
    }
}
