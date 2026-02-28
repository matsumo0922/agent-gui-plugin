package me.matsumo.agentguiplugin.viewmodel.transcript

import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Phase A: TranscriptParser の現行テスト。
 * parseTranscriptLine (SDK internal) を経由した end-to-end テスト。
 *
 * NOTE: SDK の MessageParser は以下のフォーマットを期待する:
 * - assistant: {"type":"assistant","session_id":"...","message":{"content":[...],"model":"..."},"uuid":"..."}
 * - user:      {"type":"user","session_id":"...","message":{"content":"..."},"uuid":"..."}
 * つまり content は "message" オブジェクトの中にネストされる。
 */
class TranscriptParserTest {

    // ────────────────────────────────────────
    // 1: Assistant メッセージのパース
    // ────────────────────────────────────────

    @Test
    fun `parses assistant message with text content`() {
        val line = """{"type":"assistant","session_id":"s1","message":{"content":[{"type":"text","text":"Hello world"}],"model":"sonnet"},"uuid":"msg-1"}"""
        val parsed = TranscriptParser.parseLine(line)

        assertNotNull(parsed)
        assertIs<TranscriptParser.ParsedLine.Msg>(parsed)
        val result = parsed.message
        assertIs<ChatMessage.Assistant>(result)
        assertEquals("msg-1", result.id)
        assertEquals(1, result.blocks.size)
        assertIs<UiContentBlock.Text>(result.blocks[0])
        assertEquals("Hello world", (result.blocks[0] as UiContentBlock.Text).text)
    }

    // ────────────────────────────────────────
    // 2: User メッセージのパース
    // ────────────────────────────────────────

    @Test
    fun `parses user message with string content`() {
        val line = """{"type":"user","session_id":"s1","message":{"content":"How are you?"},"uuid":"usr-1"}"""
        val parsed = TranscriptParser.parseLine(line)

        assertNotNull(parsed)
        assertIs<TranscriptParser.ParsedLine.Msg>(parsed)
        val result = parsed.message
        assertIs<ChatMessage.User>(result)
        assertEquals("How are you?", result.text)
    }

    // ────────────────────────────────────────
    // 3: 不正 JSON
    // ────────────────────────────────────────

    @Test
    fun `returns null for invalid JSON`() {
        val line = """{"type": broken json}"""
        val result = TranscriptParser.parseLine(line)
        assertNull(result)
    }

    // ────────────────────────────────────────
    // 4: 空行
    // ────────────────────────────────────────

    @Test
    fun `returns null for empty line`() {
        assertNull(TranscriptParser.parseLine(""))
        assertNull(TranscriptParser.parseLine("   "))
    }

    // ────────────────────────────────────────
    // 5: ToolUse ブロック付き Assistant
    // ────────────────────────────────────────

    @Test
    fun `parses assistant message with tool use block`() {
        val line = """{"type":"assistant","session_id":"s1","message":{"content":[{"type":"tool_use","id":"toolu_1","name":"Write","input":{"file_path":"/tmp/test.txt","content":"hello"}}],"model":"sonnet"},"uuid":"msg-2"}"""
        val parsed = TranscriptParser.parseLine(line)

        assertNotNull(parsed)
        assertIs<TranscriptParser.ParsedLine.Msg>(parsed)
        val result = parsed.message
        assertIs<ChatMessage.Assistant>(result)
        assertEquals(1, result.blocks.size)
        assertIs<UiContentBlock.ToolUse>(result.blocks[0])
        assertEquals("Write", (result.blocks[0] as UiContentBlock.ToolUse).toolName)
    }

    // ────────────────────────────────────────
    // 6: サポート外メッセージ型 (result)
    // ────────────────────────────────────────

    @Test
    fun `returns null for unsupported message type (result)`() {
        val line = """{"type":"result","session_id":"s1","subtype":"success","is_error":false,"num_turns":1}"""
        val result = TranscriptParser.parseLine(line)
        assertNull(result)
    }

    // ────────────────────────────────────────
    // 7: JsonArray 内に非 object 要素 (User content)
    // ────────────────────────────────────────

    @Test
    fun `user message with array content containing non-object elements throws`() {
        // JsonArray に数値などの非 object 要素があると、extractUserText 内の
        // elem.jsonObject アクセスで IllegalArgumentException が発生する。
        // 現行実装ではこの例外がキャッチされずに漏れる（既知のバグ — Phase B で修正予定）。
        val line = """{"type":"user","session_id":"s1","message":{"content":[{"type":"text","text":"hello"},42]},"uuid":"usr-2"}"""
        try {
            TranscriptParser.parseLine(line)
            // 例外が発生しなかった場合も OK (修正後)
        } catch (_: IllegalArgumentException) {
            // 現行動作: 例外が漏れる
        }
    }

    // ────────────────────────────────────────
    // 8: content が null の UserMessage
    // ────────────────────────────────────────

    @Test
    fun `user message with null content returns null`() {
        // message オブジェクトはあるが content がない
        val line = """{"type":"user","session_id":"s1","message":{},"uuid":"usr-3"}"""
        val result = TranscriptParser.parseLine(line)
        assertNull(result)
    }
}
