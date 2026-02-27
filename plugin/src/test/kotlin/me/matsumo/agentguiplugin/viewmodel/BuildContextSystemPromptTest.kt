package me.matsumo.agentguiplugin.viewmodel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.matsumo.agentguiplugin.model.AttachedFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Phase A: buildContextSystemPrompt の純粋関数テスト。
 * 外部依存ゼロ、完全にテスト可能。
 */
class BuildContextSystemPromptTest {

    // ────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────

    private fun userMsg(
        text: String,
        attachedFiles: List<AttachedFile> = emptyList(),
    ) = ChatMessage.User(
        id = "u-${text.hashCode()}",
        editGroupId = "eg-${text.hashCode()}",
        text = text,
        attachedFiles = attachedFiles,
    )

    private fun assistantMsg(
        text: String,
        toolUses: List<UiContentBlock.ToolUse> = emptyList(),
    ) = ChatMessage.Assistant(
        id = "a-${text.hashCode()}",
        blocks = if (toolUses.isEmpty()) listOf(UiContentBlock.Text(text))
                 else listOf(UiContentBlock.Text(text)) + toolUses,
        timestamp = 0L,
    )

    private fun interruptedMsg() = ChatMessage.Interrupted(
        id = "int-1",
        timestamp = 0L,
    )

    // ────────────────────────────────────────
    // 1: User メッセージのみ
    // ────────────────────────────────────────

    @Test
    fun `user messages are formatted correctly`() {
        val messages = listOf(userMsg("Hello"))
        val result = buildContextSystemPrompt(messages, emptyList())

        assertContains(result, "[User]: Hello")
    }

    // ────────────────────────────────────────
    // 2: Assistant メッセージ (テキスト)
    // ────────────────────────────────────────

    @Test
    fun `assistant text messages are formatted correctly`() {
        val messages = listOf(
            userMsg("Hi"),
            assistantMsg("Hello there!"),
        )
        val result = buildContextSystemPrompt(messages, emptyList())

        assertContains(result, "[User]: Hi")
        assertContains(result, "[Assistant]: Hello there!")
    }

    // ────────────────────────────────────────
    // 3: Assistant メッセージ (ツール使用)
    // ────────────────────────────────────────

    @Test
    fun `assistant tool use messages include tool summary`() {
        val toolUse = UiContentBlock.ToolUse(
            toolName = "Write",
            inputJson = JsonObject(
                mapOf(
                    "file_path" to JsonPrimitive("/tmp/test.txt"),
                    "command" to JsonPrimitive("echo hello"),
                ),
            ),
        )
        val messages = listOf(
            userMsg("Write a file"),
            assistantMsg("Sure!", toolUses = listOf(toolUse)),
        )
        val result = buildContextSystemPrompt(messages, emptyList())

        assertContains(result, "[Tool used: Write]")
        assertContains(result, "target: \"/tmp/test.txt\"")
        assertContains(result, "command: \"echo hello\"")
    }

    // ────────────────────────────────────────
    // 4: 添付ファイル付き User
    // ────────────────────────────────────────

    @Test
    fun `user message with attached files shows file names`() {
        val file = AttachedFile(
            id = "f1",
            name = "screenshot.png",
            path = "/tmp/screenshot.png",
            icon = null,
            isImage = true,
        )
        val messages = listOf(userMsg("Look at this", attachedFiles = listOf(file)))
        val result = buildContextSystemPrompt(messages, emptyList())

        assertContains(result, "[User]: Look at this")
        assertContains(result, "Attached files: screenshot.png")
    }

    // ────────────────────────────────────────
    // 5: Interrupted メッセージ
    // ────────────────────────────────────────

    @Test
    fun `interrupted message is formatted`() {
        val messages = listOf(
            userMsg("Do something"),
            interruptedMsg(),
        )
        val result = buildContextSystemPrompt(messages, emptyList())

        assertContains(result, "[System: Response was interrupted]")
    }

    // ────────────────────────────────────────
    // 6: 空リスト
    // ────────────────────────────────────────

    @Test
    fun `empty messages list produces header and footer only`() {
        val result = buildContextSystemPrompt(emptyList(), emptyList())

        assertContains(result, "Conversation history up to branch point:")
        assertContains(result, "Continue the conversation from here.")
        assertFalse(result.contains("[User]"))
        assertFalse(result.contains("[Assistant]"))
    }

    // ────────────────────────────────────────
    // 7: originalAttachedFiles 付き
    // ────────────────────────────────────────

    @Test
    fun `original attached files section is included`() {
        val file = AttachedFile(
            id = "f2",
            name = "data.csv",
            path = "/tmp/data.csv",
            icon = null,
        )
        val result = buildContextSystemPrompt(emptyList(), listOf(file))

        assertContains(result, "Previously attached files: data.csv")
    }
}
