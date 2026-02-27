package me.matsumo.agentguiplugin.testutil

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.TextBlock
import java.util.*

/**
 * テスト用メッセージビルダー。
 * ConversationTreeTest の既存パターンに合わせつつ、SDK 型のヘルパーも提供。
 */
object TestFixtures {

    fun userMsg(
        text: String,
        editGroupId: String = "eg-${text.hashCode()}",
        attachedFiles: ImmutableList<AttachedFile> = persistentListOf(),
    ) = ChatMessage.User(
        id = UUID.randomUUID().toString(),
        editGroupId = editGroupId,
        text = text,
        attachedFiles = attachedFiles,
    )

    fun assistantMsg(
        text: String,
        id: String = UUID.randomUUID().toString(),
    ) = ChatMessage.Assistant(
        id = id,
        blocks = persistentListOf(UiContentBlock.Text(text)),
        timestamp = 0L,
    )

    fun interruptedMsg() = ChatMessage.Interrupted(
        id = UUID.randomUUID().toString(),
        timestamp = 0L,
    )

    /** SDK AssistantMessage を生成 */
    fun sdkAssistantMessage(
        text: String,
        sessionId: String = "test-session",
        uuid: String = UUID.randomUUID().toString(),
        parentToolUseId: String? = null,
    ) = AssistantMessage(
        sessionId = sessionId,
        content = listOf(TextBlock(text)),
        uuid = uuid,
        parentToolUseId = parentToolUseId,
    )

    /** SDK SystemMessage を生成 */
    fun sdkSystemMessage(
        sessionId: String = "test-session",
        subtype: String = "init",
    ) = SystemMessage(
        sessionId = sessionId,
        subtype = subtype,
    )

    /** SDK ResultMessage を生成 */
    fun sdkResultMessage(
        sessionId: String = "test-session",
        totalCostUsd: Double? = 0.01,
        isError: Boolean = false,
    ) = ResultMessage(
        sessionId = sessionId,
        subtype = if (isError) "error" else "success",
        totalCostUsd = totalCostUsd,
        isError = isError,
    )
}
