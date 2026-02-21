package me.matsumo.agentguiplugin.viewmodel

import kotlinx.serialization.json.JsonObject

enum class SessionState {
    Disconnected,
    Connecting,
    Ready,
    Streaming,
    WaitingForInput,
    Error,
}

data class PendingPermission(
    val toolName: String,
    val toolInput: Map<String, Any?>,
)

data class PendingQuestion(
    val toolName: String,
    val toolInput: Map<String, Any?>,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val sessionState: SessionState = SessionState.Disconnected,
    val isStreaming: Boolean = false,
    val sessionId: String? = null,
    val model: String? = null,
    val totalCostUsd: Double = 0.0,
    val pendingPermission: PendingPermission? = null,
    val pendingQuestion: PendingQuestion? = null,
    val errorMessage: String? = null,
)

sealed interface ChatMessage {
    val id: String

    data class User(
        override val id: String,
        val text: String,
    ) : ChatMessage

    data class Assistant(
        override val id: String,
        val blocks: List<UiContentBlock> = emptyList(),
        val isComplete: Boolean = false,
    ) : ChatMessage
}

data class SubAgentTask(
    val id: String,                                    // = parentToolUseId
    val spawnedByToolName: String? = null,             // 呼び出し元ツール名
    val messages: List<ChatMessage> = emptyList(),     // サブエージェントのメッセージ列
    val isComplete: Boolean = false,
)

sealed interface UiContentBlock {
    data class Text(val text: String) : UiContentBlock
    data class Thinking(val text: String, val isExpanded: Boolean = false) : UiContentBlock
    data class ToolUse(
        val toolName: String,
        val inputJson: JsonObject,
        val toolUseId: String? = null,
        val elapsed: Double? = null,
        val isStreaming: Boolean = false,
        val subAgentTask: SubAgentTask? = null,
    ) : UiContentBlock
}
