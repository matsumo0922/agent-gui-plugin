package me.matsumo.agentguiplugin.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.json.JsonObject
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode

enum class SessionState {
    Disconnected,
    Connecting,
    AuthRequired,
    Ready,
    Processing,
    WaitingForInput,
    Error,
}

@Immutable
data class PendingPermission(
    val toolName: String,
    val toolInput: Map<String, Any?>,
)

@Immutable
data class PendingQuestion(
    val toolName: String,
    val toolInput: Map<String, Any?>,
)

@Stable
data class ChatUiState(
    val conversationTree: ConversationTree = ConversationTree(),
    val conversationCursor: ConversationCursor = ConversationCursor(),
    val subAgentTasks: Map<String, SubAgentTask> = emptyMap(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val sessionState: SessionState = SessionState.Disconnected,
    val sessionId: String? = null,
    val model: Model = Model.SONNET,
    val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    val contextUsage: Float = 0f,
    val totalInputTokens: Long = 0L,
    val totalCostUsd: Double = 0.0,
    val pendingPermission: PendingPermission? = null,
    val pendingQuestion: PendingQuestion? = null,
    val errorMessage: String? = null,
    val authOutputLines: List<String> = emptyList(),
) {
    /** アクティブパスのフラットメッセージリスト（UI 互換用） */
    val activeMessages: List<ChatMessage>
        get() = conversationTree.getActiveMessages()

    /** 現在のアクティブパスが編集ブランチ（コンテキスト復元済み）かどうか */
    val isReconstructedContext: Boolean
        get() = conversationCursor.activeLeafPath.any { it.timelineIndex > 0 }
}

sealed interface ChatMessage {
    val id: String

    data class User(
        override val id: String,
        val editGroupId: String = id,
        val text: String,
        val attachedFiles: List<AttachedFile> = emptyList(),
    ) : ChatMessage

    data class Assistant(
        override val id: String,
        val blocks: List<UiContentBlock> = emptyList(),
        val timestamp: Long = System.currentTimeMillis(),
    ) : ChatMessage

    data class Interrupted(
        override val id: String,
        val timestamp: Long = System.currentTimeMillis(),
    ) : ChatMessage
}

@Immutable
data class SubAgentTask(
    val id: String,                                    // = parentToolUseId
    val timelineSessionId: String? = null,             // どのブランチのタスクか
    val spawnedByToolName: String? = null,             // 呼び出し元ツール名
    val messages: List<ChatMessage> = emptyList(),     // サブエージェントのメッセージ列
    val startedAt: Long? = null,                       // 開始時刻 (epochMillis)
    val completedAt: Long? = null,                     // 終了時刻 (epochMillis)
)

@Immutable
data class EditDiffInfo(
    val filePath: String,
    val oldString: String,
    val newString: String,
)

sealed interface UiContentBlock {
    data class Text(val text: String) : UiContentBlock
    data class Thinking(val text: String, val isExpanded: Boolean = false) : UiContentBlock
    data class ToolUse(
        val toolName: String,
        val inputJson: JsonObject,
        val toolUseId: String? = null,
        val diffInfo: EditDiffInfo? = null,
    ) : UiContentBlock
}
