package me.matsumo.agentguiplugin.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
    Error;

    /** 許可された遷移先を定義 */
    fun canTransitionTo(next: SessionState): Boolean = when (this) {
        Disconnected -> next in setOf(Connecting)
        Connecting -> next in setOf(Ready, AuthRequired, Error)
        AuthRequired -> next in setOf(Disconnected)
        Ready -> next in setOf(Processing, Disconnected, Error)
        Processing -> next in setOf(WaitingForInput, Error)
        WaitingForInput -> next in setOf(Processing, Connecting, Disconnected, Error)
        Error -> next in setOf(Disconnected)
    }
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

@Immutable
data class SessionStatus(
    val state: SessionState = SessionState.Disconnected,
    val sessionId: String? = null,
    val errorMessage: String? = null,
)

@Immutable
data class UsageInfo(
    val contextUsage: Float = 0f,
    val totalInputTokens: Long = 0L,
    val totalCostUsd: Double = 0.0,
)

@Stable
data class ChatUiState(
    val conversationTree: ConversationTree = ConversationTree(),
    val conversationCursor: ConversationCursor = ConversationCursor(),
    val session: SessionStatus = SessionStatus(),
    val usage: UsageInfo = UsageInfo(),
    val subAgentTasks: ImmutableMap<String, SubAgentTask> = persistentMapOf(),
    val attachedFiles: ImmutableList<AttachedFile> = persistentListOf(),
    val model: Model = Model.SONNET,
    val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    val permissionModeBeforePlan: PermissionMode? = null,
    val pendingPermission: PendingPermission? = null,
    val pendingQuestion: PendingQuestion? = null,
    val toolResults: ImmutableMap<String, ToolResultInfo> = persistentMapOf(),
    val authOutputLines: ImmutableList<String> = persistentListOf(),
) {
    // 後方互換のための convenience accessor
    val sessionState: SessionState get() = session.state
    val sessionId: String? get() = session.sessionId
    val errorMessage: String? get() = session.errorMessage
    val contextUsage: Float get() = usage.contextUsage
    val totalInputTokens: Long get() = usage.totalInputTokens
    val totalCostUsd: Double get() = usage.totalCostUsd

    /** アクティブパスのフラットメッセージリスト（UI 互換用） */
    val activeMessages: List<ChatMessage>
        get() = conversationTree.getActiveMessages()

    /** 現在のアクティブパスが編集ブランチ（コンテキスト復元済み）かどうか */
    val isReconstructedContext: Boolean
        get() = conversationCursor.activeLeafPath.any { it.timelineIndex > 0 }
}

sealed interface ChatMessage {
    val id: String

    @Immutable
    data class User(
        override val id: String,
        val editGroupId: String = id,
        val text: String,
        val attachedFiles: ImmutableList<AttachedFile> = persistentListOf(),
    ) : ChatMessage

    @Immutable
    data class Assistant(
        override val id: String,
        val blocks: ImmutableList<UiContentBlock> = persistentListOf(),
        val timestamp: Long = System.currentTimeMillis(),
    ) : ChatMessage

    @Immutable
    data class Interrupted(
        override val id: String,
        val timestamp: Long = System.currentTimeMillis(),
    ) : ChatMessage
}

@Immutable
data class SubAgentTask(
    val id: String,                                                // = parentToolUseId
    val timelineSessionId: String? = null,                         // どのブランチのタスクか
    val spawnedByToolName: String? = null,                         // 呼び出し元ツール名
    val messages: ImmutableList<ChatMessage> = persistentListOf(), // サブエージェントのメッセージ列
    val startedAt: Long? = null,                                   // 開始時刻 (epochMillis)
    val completedAt: Long? = null,                                 // 終了時刻 (epochMillis)
)

@Immutable
data class ToolResultInfo(
    val content: String,
    val isError: Boolean = false,
)

@Immutable
data class EditDiffInfo(
    val filePath: String,
    val oldString: String,
    val newString: String,
)

sealed interface UiContentBlock {
    @Immutable
    data class Text(val text: String) : UiContentBlock
    @Immutable
    data class Thinking(val text: String, val isExpanded: Boolean = false) : UiContentBlock
    @Immutable
    data class ToolUse(
        val toolName: String,
        val inputJson: JsonObject,
        val toolUseId: String? = null,
        val diffInfo: EditDiffInfo? = null,
    ) : UiContentBlock
}
