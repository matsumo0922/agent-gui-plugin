package me.matsumo.agentguiplugin.viewmodel

import com.anthropic.sdk.ClaudeSDKClient
import com.anthropic.sdk.createSession
import com.anthropic.sdk.types.AssistantMessage
import com.anthropic.sdk.types.ContentBlock
import com.anthropic.sdk.types.PermissionResult
import com.anthropic.sdk.types.PermissionResultAllow
import com.anthropic.sdk.types.PermissionResultDeny
import com.anthropic.sdk.types.ResultMessage
import com.anthropic.sdk.types.StreamEvent
import com.anthropic.sdk.types.SystemMessage
import com.anthropic.sdk.types.TextBlock
import com.anthropic.sdk.types.ThinkingBlock
import com.anthropic.sdk.types.ToolResultBlock
import com.anthropic.sdk.types.ToolUseBlock
import com.anthropic.sdk.types.UserMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.*

class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var permissionDeferred: CompletableDeferred<PermissionResult>? = null
    private var client: ClaudeSDKClient? = null

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                val session = createSession {
                    cwd = projectBasePath
                    cliPath = claudeCodePath
                    canUseTool { toolName, input, _ ->
                        handlePermissionRequest(toolName, input)
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

    private suspend fun handlePermissionRequest(
        toolName: String,
        input: Map<String, Any?>,
    ): PermissionResult {
        val deferred = CompletableDeferred<PermissionResult>()
        permissionDeferred = deferred

        if (toolName == "AskUserQuestion") {
            _uiState.update {
                it.copy(pendingQuestion = PendingQuestion(toolName, input))
            }
        } else {
            _uiState.update {
                it.copy(pendingPermission = PendingPermission(toolName, input))
            }
        }

        return deferred.await()
    }

    fun respondPermission(allow: Boolean) {
        _uiState.update { it.copy(pendingPermission = null) }
        val result: PermissionResult = if (allow) {
            PermissionResultAllow()
        } else {
            PermissionResultDeny(message = "Denied by user")
        }
        permissionDeferred?.complete(result)
        permissionDeferred = null
    }

    fun respondQuestion(answers: Map<String, String>) {
        _uiState.update { it.copy(pendingQuestion = null) }
        val updatedInput = JsonObject(
            answers.mapValues { (_, v) -> JsonPrimitive(v) }
        )
        permissionDeferred?.complete(PermissionResultAllow(updatedInput = updatedInput))
        permissionDeferred = null
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

    private fun ContentBlock.toUiBlock(): UiContentBlock = when (this) {
        is TextBlock -> UiContentBlock.Text(text)
        is ThinkingBlock -> UiContentBlock.Thinking(thinking)
        is ToolUseBlock -> UiContentBlock.ToolUse(
            toolName = name,
            inputJson = input,
        )
        is ToolResultBlock -> UiContentBlock.Text("[Tool result]")
    }
}
