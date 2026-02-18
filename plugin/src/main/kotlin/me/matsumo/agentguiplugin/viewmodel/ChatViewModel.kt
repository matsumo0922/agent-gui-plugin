package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import me.matsumo.agentguiplugin.bridge.client.BridgeClient
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.ContentBlock
import me.matsumo.agentguiplugin.bridge.model.PermissionResult
import me.matsumo.agentguiplugin.bridge.model.SessionOptions
import java.util.*

class ChatViewModel(
    private val client: BridgeClient,
    private val projectBasePath: String,
    private val claudeCodePath: String? = null,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Streaming accumulation
    private val streamingBlocks = mutableMapOf<Int, StringBuilder>()
    private val streamingBlockTypes = mutableMapOf<Int, String>()
    private val streamingToolNames = mutableMapOf<Int, String>()
    private var currentAssistantId: String? = null

    fun initialize() {
        scope.launch {
            _uiState.update { it.copy(sessionState = SessionState.Connecting) }
            try {
                client.connect(scope)
                collectEvents()
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

    private fun collectEvents() {
        scope.launch {
            client.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun handleEvent(event: BridgeEvent) {
        when (event) {
            is BridgeEvent.Ready -> {
                _uiState.update { it.copy(sessionState = SessionState.Ready) }
            }

            is BridgeEvent.SessionInit -> {
                _uiState.update {
                    it.copy(
                        sessionId = event.sessionId,
                        model = event.model,
                        sessionState = SessionState.Streaming,
                    )
                }
            }

            is BridgeEvent.StreamMessageStart -> {
                streamingBlocks.clear()
                streamingBlockTypes.clear()
                streamingToolNames.clear()
                currentAssistantId = UUID.randomUUID().toString()
                _uiState.update { it.copy(isStreaming = true, sessionState = SessionState.Streaming) }

                addOrUpdateAssistantMessage(emptyList(), isComplete = false)
            }

            is BridgeEvent.StreamContentStart -> {
                streamingBlocks[event.index] = StringBuilder()
                streamingBlockTypes[event.index] = event.blockType
                val toolName = event.toolName
                if (toolName != null) {
                    streamingToolNames[event.index] = toolName
                }
            }

            is BridgeEvent.StreamContentDelta -> {
                streamingBlocks[event.index]?.append(event.text)
                updateStreamingMessage()
            }

            is BridgeEvent.StreamContentStop -> {
                // Block complete, keep data for final assembly
            }

            is BridgeEvent.StreamMessageStop -> {
                // Wait for assistant_message to finalize
            }

            is BridgeEvent.AssistantMessage -> {
                val blocks = event.content.map { it.toUiBlock() }
                addOrUpdateAssistantMessage(blocks, isComplete = true)
                _uiState.update { it.copy(isStreaming = false) }
                streamingBlocks.clear()
                streamingBlockTypes.clear()
                streamingToolNames.clear()
            }

            is BridgeEvent.TurnResult -> {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.WaitingForInput,
                        isStreaming = false,
                        totalCostUsd = event.totalCostUsd,
                        errorMessage = if (event.isError) "Turn ended with error: ${event.subtype}" else null,
                    )
                }
            }

            is BridgeEvent.PermissionRequest -> {
                if (event.toolName == "AskUserQuestion") {
                    _uiState.update { it.copy(pendingQuestion = event) }
                } else {
                    _uiState.update { it.copy(pendingPermission = event) }
                }
            }

            is BridgeEvent.ToolProgress -> {
                // Update elapsed time for tool use blocks if needed
            }

            is BridgeEvent.Status -> {
                // Could show compacting status, etc.
            }

            is BridgeEvent.Error -> {
                _uiState.update {
                    it.copy(
                        sessionState = if (event.fatal) SessionState.Error else it.sessionState,
                        errorMessage = event.message,
                        isStreaming = false,
                    )
                }
            }

            is BridgeEvent.Unknown -> {
                // Ignore unknown events
            }
        }
    }

    private fun updateStreamingMessage() {
        val blocks = streamingBlocks.entries.sortedBy { it.key }.map { (index, sb) ->
            val text = sb.toString()
            when (streamingBlockTypes[index]) {
                "thinking" -> UiContentBlock.Thinking(text)
                "tool_use" -> UiContentBlock.ToolUse(
                    toolName = streamingToolNames[index] ?: "unknown",
                    inputJson = buildJsonObject {},
                )
                else -> UiContentBlock.Text(text)
            }
        }
        addOrUpdateAssistantMessage(blocks, isComplete = false)
    }

    private fun addOrUpdateAssistantMessage(blocks: List<UiContentBlock>, isComplete: Boolean) {
        val assistantId = currentAssistantId ?: return
        _uiState.update { state ->
            val messages = state.messages.toMutableList()
            val existingIndex = messages.indexOfLast { it.id == assistantId }
            val msg = ChatMessage.Assistant(id = assistantId, blocks = blocks, isComplete = isComplete)

            if (existingIndex >= 0) {
                messages[existingIndex] = msg
            } else {
                messages.add(msg)
            }
            state.copy(messages = messages)
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val userMessage = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                sessionState = SessionState.Streaming,
            )
        }

        scope.launch {
            val state = _uiState.value
            if (state.sessionId == null) {
                // First message - start session
                client.startSession(
                    prompt = text,
                    options = SessionOptions(
                        cwd = projectBasePath,
                        claudeCodePath = claudeCodePath,
                    ),
                )
            } else {
                // Follow-up message
                client.sendUserMessage(text)
            }
        }
    }

    fun respondPermission(allow: Boolean) {
        val request = _uiState.value.pendingPermission ?: return
        _uiState.update { it.copy(pendingPermission = null) }

        scope.launch {
            val result = if (allow) {
                PermissionResult(behavior = "allow")
            } else {
                PermissionResult(behavior = "deny", message = "Denied by user")
            }
            client.respondPermission(request.requestId, result)
        }
    }

    fun respondQuestion(answers: Map<String, String>) {
        val request = _uiState.value.pendingQuestion ?: return
        _uiState.update { it.copy(pendingQuestion = null) }

        scope.launch {
            val updatedInput = buildJsonObject {
                // Copy original questions
                request.toolInput["questions"]?.let { put("questions", it) }
                // Add answers
                put("answers", kotlinx.serialization.json.JsonObject(
                    answers.mapValues { (_, v) -> kotlinx.serialization.json.JsonPrimitive(v) }
                ))
            }
            val result = PermissionResult(
                behavior = "allow",
                updatedInput = updatedInput,
            )
            client.respondPermission(request.requestId, result)
        }
    }

    fun abortSession() {
        scope.launch {
            client.abort()
        }
        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                isStreaming = false,
            )
        }
    }

    fun reconnect() {
        _uiState.update { ChatUiState(sessionState = SessionState.Connecting) }
        initialize()
    }

    private fun ContentBlock.toUiBlock(): UiContentBlock = when (this) {
        is ContentBlock.Text -> UiContentBlock.Text(text)
        is ContentBlock.Thinking -> UiContentBlock.Thinking(thinking)
        is ContentBlock.ToolUse -> UiContentBlock.ToolUse(
            toolName = name,
            inputJson = input,
        )
        is ContentBlock.Unknown -> UiContentBlock.Text("[Unknown block]")
    }
}
