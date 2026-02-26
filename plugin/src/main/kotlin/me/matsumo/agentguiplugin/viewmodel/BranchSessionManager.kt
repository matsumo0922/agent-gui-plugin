package me.matsumo.agentguiplugin.viewmodel

import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.resumeSession
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import me.matsumo.claude.agent.types.SessionOptionsBuilder
import java.util.concurrent.ConcurrentHashMap

/**
 * 各ブランチのセッションを管理するコンポーネント。
 * 編集によるブランチセッション作成・既存セッションの再接続・全セッションの一括クリーンアップを担当。
 */
class BranchSessionManager(
    private val applyCommonConfig: SessionOptionsBuilder.(Model, PermissionMode) -> Unit,
) {
    // branchSessionId -> ClaudeSDKClient
    private val activeSessions = ConcurrentHashMap<String, ClaudeSDKClient>()

    /**
     * 編集による新しいブランチのセッションを作成。
     * messagesBeforeEdit: 編集対象メッセージより前の全メッセージ (active path)
     * originalAttachedFiles: 編集元メッセージの添付ファイル
     */
    suspend fun createEditBranchSession(
        messagesBeforeEdit: List<ChatMessage>,
        originalAttachedFiles: List<AttachedFile>,
        model: Model,
        permissionMode: PermissionMode,
    ): ClaudeSDKClient {
        val contextPrompt = buildContextSystemPrompt(messagesBeforeEdit, originalAttachedFiles)
        val client = createSession {
            applyCommonConfig(model, permissionMode)
            systemPrompt = contextPrompt
        }
        client.connect()
        val sessionId = client.sessionId
        if (sessionId != null) {
            activeSessions[sessionId] = client
        }
        return client
    }

    /**
     * 既存ブランチのセッションを取得（会話継続用）。
     * セッションが閉じている場合は resumeSession で再接続。
     */
    suspend fun getOrResumeSession(
        branchSessionId: String,
        model: Model,
        permissionMode: PermissionMode,
    ): ClaudeSDKClient {
        activeSessions[branchSessionId]?.let { return it }
        val client = resumeSession(branchSessionId) {
            applyCommonConfig(model, permissionMode)
            forkSession = false // 同じセッションを継続
        }
        client.connect()
        activeSessions[branchSessionId] = client
        return client
    }

    fun removeSession(sessionId: String) {
        activeSessions.remove(sessionId)?.close()
    }

    fun closeAll() {
        activeSessions.values.forEach { it.close() }
        activeSessions.clear()
    }
}

/**
 * 編集ブランチのコンテキスト復元用システムプロンプトを構築。
 * テキスト・ツール使用履歴・添付ファイル情報を含む。
 */
fun buildContextSystemPrompt(
    messagesBeforeEdit: List<ChatMessage>,
    originalAttachedFiles: List<AttachedFile>,
): String {
    val sb = StringBuilder()
    sb.appendLine(
        """
        You are continuing a branched conversation after the user edited an earlier message.

        Known limitations:
        - Tool execution history/results may be incomplete.
        - Workspace file state may have diverged from the original branch.
        - If any missing context is required, ask a short clarifying question or re-run tools.

        Conversation history up to branch point:
        """.trimIndent(),
    )
    sb.appendLine()

    for (msg in messagesBeforeEdit) {
        when (msg) {
            is ChatMessage.User -> {
                sb.appendLine("[User]: ${msg.text}")
                if (msg.attachedFiles.isNotEmpty()) {
                    sb.appendLine("  (Attached files: ${msg.attachedFiles.joinToString { it.name }})")
                }
            }
            is ChatMessage.Assistant -> {
                val textParts = msg.blocks.filterIsInstance<UiContentBlock.Text>()
                    .joinToString("\n") { it.text }
                if (textParts.isNotBlank()) {
                    sb.appendLine("[Assistant]: $textParts")
                }

                // ツール使用の要約を含める
                val toolUses = msg.blocks.filterIsInstance<UiContentBlock.ToolUse>()
                for (tool in toolUses) {
                    sb.appendLine("  [Tool used: ${tool.toolName}]")
                    tool.inputJson["file_path"]?.let { path ->
                        sb.appendLine("    target: $path")
                    }
                    tool.inputJson["command"]?.let { cmd ->
                        sb.appendLine("    command: $cmd")
                    }
                }
            }
            is ChatMessage.Interrupted -> {
                sb.appendLine("[System: Response was interrupted]")
            }
        }
    }

    if (originalAttachedFiles.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("Previously attached files: ${originalAttachedFiles.joinToString { it.name }}")
    }

    sb.appendLine()
    sb.appendLine("Continue the conversation from here. The user will send their next message.")
    return sb.toString()
}
