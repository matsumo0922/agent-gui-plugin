package me.matsumo.agentguiplugin.viewmodel

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode

/**
 * ChatUiState に対する全アクションを定義。
 * ChatViewModel 内の全 _uiState.update を置き換える。
 */
sealed interface StateAction {
    // --- Session lifecycle ---
    data object StartConnecting : StateAction
    data class SessionReady(val sessionId: String?) : StateAction
    data object SessionAuthRequired : StateAction
    data class SessionError(val message: String?) : StateAction
    data object SessionDisconnected : StateAction
    data class SessionIdUpdated(val sessionId: String) : StateAction

    // --- Turn lifecycle ---
    data class TurnStarted(
        val newTree: ConversationTree,
        val newPath: SlotPath,
    ) : StateAction

    data class AssistantMessageReceived(
        val assistantMsg: ChatMessage.Assistant,
    ) : StateAction

    data class TurnCompleted(
        val isError: Boolean,
        val errorMessage: String?,
    ) : StateAction

    data class TurnError(val message: String?, val toState: SessionState = SessionState.Error) : StateAction

    data class Aborted(
        val newTree: ConversationTree,
    ) : StateAction

    // --- Edit / Navigate ---
    data class EditBranchCreated(
        val newTree: ConversationTree,
        val newPath: SlotPath,
    ) : StateAction

    data class EditSessionSynced(val sessionId: String?) : StateAction

    data class VersionNavigated(
        val newTree: ConversationTree,
        val newPath: SlotPath,
        val newSessionId: String?,
        val needsSessionSwitch: Boolean,
    ) : StateAction

    data class BranchSwitchCompleted(val sessionId: String) : StateAction
    data class BranchSwitchFailed(val message: String?) : StateAction

    // --- File management ---
    data class FileAttached(val file: AttachedFile) : StateAction
    data class FileDetached(val file: AttachedFile) : StateAction

    // --- Config ---
    data class ModelChanged(val model: Model) : StateAction
    data class PermissionModeChanged(val mode: PermissionMode) : StateAction

    // --- Tool results ---
    data class ToolResultReceived(val results: Map<String, ToolResultInfo>) : StateAction

    // --- External sync (from delegates) ---
    data class SubAgentTasksUpdated(val tasks: Map<String, SubAgentTask>) : StateAction
    data class UsageUpdated(val usage: UsageInfo) : StateAction
    data class AuthOutputUpdated(val lines: List<String>) : StateAction

    // --- History ---
    data class HistoryImported(
        val tree: ConversationTree,
        val cursor: ConversationCursor,
        val toolResults: Map<String, ToolResultInfo> = emptyMap(),
    ) : StateAction

    // --- Full reset (bypasses transition validation) ---
    data class Reset(val model: Model, val permissionMode: PermissionMode) : StateAction
}

/**
 * 純粋関数: (State, Action) → State
 * テスト時はこの関数だけで状態遷移ロジックを検証可能。
 */
fun reduce(state: ChatUiState, action: StateAction): ChatUiState = when (action) {
    // --- Session lifecycle ---
    StateAction.StartConnecting -> state.copy(
        session = state.session.transitionTo(SessionState.Connecting),
    )
    is StateAction.SessionReady -> state.copy(
        session = state.session.transitionTo(SessionState.Ready, sessionId = action.sessionId),
    )
    StateAction.SessionAuthRequired -> state.copy(
        session = state.session.transitionTo(SessionState.AuthRequired),
    )
    is StateAction.SessionError -> state.copy(
        session = state.session.transitionTo(SessionState.Error, errorMessage = action.message),
    )
    StateAction.SessionDisconnected -> state.copy(
        session = state.session.transitionTo(SessionState.Disconnected),
        authOutputLines = persistentListOf(),
    )
    is StateAction.SessionIdUpdated -> state.copy(
        session = state.session.copy(sessionId = action.sessionId),
    )

    // --- Turn lifecycle ---
    is StateAction.TurnStarted -> state.copy(
        conversationTree = action.newTree,
        conversationCursor = ConversationCursor(activeLeafPath = action.newPath),
        attachedFiles = persistentListOf(),
        session = state.session.transitionTo(SessionState.Processing),
    )
    is StateAction.AssistantMessageReceived -> {
        val cursor = state.conversationCursor
        val targetPath = cursor.activeLeafPath
        val msg = action.assistantMsg
        val isSameStreaming = cursor.activeStreamingMessageId == msg.id

        val finalMsg = if (isSameStreaming) {
            // 旧メッセージから timing 情報を引き継ぐ
            val oldMsg = state.conversationTree.getActiveMessages().lastOrNull() as? ChatMessage.Assistant
            val mergedBlocks = mergeBlockTimings(oldMsg?.blocks, msg.blocks)
            msg.copy(
                timestamp = oldMsg?.timestamp ?: msg.timestamp,
                blocks = mergedBlocks,
            )
        } else {
            msg.copy(blocks = initBlockTimings(msg.blocks))
        }

        val newTree = if (isSameStreaming) {
            state.conversationTree.updateLastResponse(targetPath) { last ->
                if (last is ChatMessage.Assistant && last.id == finalMsg.id) finalMsg else last
            }
        } else {
            state.conversationTree.appendResponse(targetPath, finalMsg)
        }
        state.copy(
            conversationTree = newTree,
            conversationCursor = cursor.copy(activeStreamingMessageId = finalMsg.id),
        )
    }
    is StateAction.TurnCompleted -> state.copy(
        session = state.session.transitionTo(
            next = SessionState.WaitingForInput,
            errorMessage = if (action.isError) "Turn ended with error: ${action.errorMessage}" else null,
        ),
        conversationCursor = state.conversationCursor.copy(activeStreamingMessageId = null),
    )
    is StateAction.TurnError -> state.copy(
        session = state.session.transitionTo(action.toState, errorMessage = action.message),
    )
    is StateAction.Aborted -> state.copy(
        session = state.session.copy(state = SessionState.WaitingForInput),
        conversationTree = action.newTree,
        conversationCursor = state.conversationCursor.copy(activeStreamingMessageId = null),
    )

    // --- Edit / Navigate ---
    is StateAction.EditBranchCreated -> state.copy(
        conversationTree = action.newTree,
        conversationCursor = ConversationCursor(activeLeafPath = action.newPath),
    )
    is StateAction.EditSessionSynced -> state.copy(
        session = state.session.copy(sessionId = action.sessionId),
    )
    is StateAction.VersionNavigated -> state.copy(
        conversationTree = action.newTree,
        conversationCursor = ConversationCursor(activeLeafPath = action.newPath),
        session = SessionStatus(
            state = if (action.needsSessionSwitch) SessionState.Connecting else state.sessionState,
            sessionId = action.newSessionId,
        ),
    )
    is StateAction.BranchSwitchCompleted -> state.copy(
        session = state.session.transitionTo(SessionState.WaitingForInput, sessionId = action.sessionId),
    )
    is StateAction.BranchSwitchFailed -> state.copy(
        session = state.session.transitionTo(SessionState.Error, errorMessage = action.message),
    )

    // --- File management ---
    is StateAction.FileAttached -> {
        if (state.attachedFiles.any { it.id == action.file.id }) state
        else state.copy(attachedFiles = (state.attachedFiles + action.file).toImmutableList())
    }
    is StateAction.FileDetached -> state.copy(
        attachedFiles = state.attachedFiles.filter { it.id != action.file.id }.toImmutableList(),
    )

    // --- Config ---
    is StateAction.ModelChanged -> state.copy(model = action.model)
    is StateAction.PermissionModeChanged -> {
        val newBeforePlan = when {
            // Plan Mode に入る場合、現在のモードが Plan でなければ保存
            action.mode == PermissionMode.PLAN && state.permissionMode != PermissionMode.PLAN ->
                state.permissionMode
            // Plan Mode から出る場合、クリア
            action.mode != PermissionMode.PLAN && state.permissionMode == PermissionMode.PLAN ->
                null
            // それ以外はそのまま
            else -> state.permissionModeBeforePlan
        }
        state.copy(permissionMode = action.mode, permissionModeBeforePlan = newBeforePlan)
    }

    // --- Tool results ---
    is StateAction.ToolResultReceived -> state.copy(
        toolResults = (state.toolResults + action.results).toImmutableMap(),
    )

    // --- External sync ---
    is StateAction.SubAgentTasksUpdated -> state.copy(subAgentTasks = action.tasks.toImmutableMap())
    is StateAction.UsageUpdated -> state.copy(usage = action.usage)
    is StateAction.AuthOutputUpdated -> state.copy(authOutputLines = action.lines.toImmutableList())

    // --- History ---
    is StateAction.HistoryImported -> state.copy(
        conversationTree = action.tree,
        conversationCursor = action.cursor,
        toolResults = (state.toolResults + action.toolResults).toImmutableMap(),
    )

    // --- Reset ---
    is StateAction.Reset -> ChatUiState(model = action.model, permissionMode = action.permissionMode)
}

/**
 * 新規メッセージの全ブロックに現在時刻を startedAt としてセットする。
 */
private fun initBlockTimings(blocks: ImmutableList<UiContentBlock>): ImmutableList<UiContentBlock> {
    val now = System.currentTimeMillis()
    return blocks.map { block ->
        when (block) {
            is UiContentBlock.Thinking -> block.copy(startedAt = now)
            is UiContentBlock.ToolUse -> block.copy(startedAt = now)
            is UiContentBlock.Text -> block
        }
    }.toImmutableList()
}

/**
 * 同一ストリーミング中のブロック更新時、旧ブロックから startedAt を引き継ぐ。
 * 新規ブロックには現在時刻をセットする。
 */
private fun mergeBlockTimings(
    oldBlocks: ImmutableList<UiContentBlock>?,
    newBlocks: ImmutableList<UiContentBlock>,
): ImmutableList<UiContentBlock> {
    if (oldBlocks == null) return initBlockTimings(newBlocks)

    val now = System.currentTimeMillis()
    // ToolUse は toolUseId でマッチ、Thinking は同型の最後のブロックでマッチ
    val lastOldThinking = oldBlocks.filterIsInstance<UiContentBlock.Thinking>().lastOrNull()

    return newBlocks.map { block ->
        when (block) {
            is UiContentBlock.ToolUse -> {
                val oldBlock = oldBlocks.filterIsInstance<UiContentBlock.ToolUse>()
                    .firstOrNull { it.toolUseId == block.toolUseId }
                block.copy(startedAt = oldBlock?.startedAt ?: now)
            }
            is UiContentBlock.Thinking -> {
                block.copy(startedAt = lastOldThinking?.startedAt ?: now)
            }
            is UiContentBlock.Text -> block
        }
    }.toImmutableList()
}
