package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import java.util.*

/**
 * Parses JSONL lines from sub-agent transcript files into [ChatMessage] objects.
 *
 * Each JSONL line has the format:
 * ```json
 * {
 *   "type": "assistant" | "user",
 *   "message": { "content": [...] },
 *   "agentId": "...",
 *   "uuid": "...",
 *   "timestamp": "..."
 * }
 * ```
 *
 * Only `"assistant"` type lines are converted to [ChatMessage.Assistant].
 */
internal object TranscriptParser {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parse a single JSONL line into a [ChatMessage.Assistant], or `null`
     * if the line is not an assistant message or is unparseable.
     */
    fun parseLine(line: String): ChatMessage.Assistant? {
        if (line.isBlank()) return null

        return try {
            val obj = json.parseToJsonElement(line).jsonObject
            val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return null

            if (type != "assistant") return null

            val messageObj = obj["message"]?.jsonObject ?: return null
            val contentArray = messageObj["content"]?.jsonArray ?: return null

            val blocks = contentArray.mapNotNull { element ->
                parseContentBlock(element.jsonObject)
            }

            if (blocks.isEmpty()) return null

            ChatMessage.Assistant(
                id = obj["uuid"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString(),
                blocks = blocks,
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse all lines from a JSONL transcript into a list of [ChatMessage.Assistant].
     */
    fun parseAll(lines: List<String>): List<ChatMessage.Assistant> {
        return lines.mapNotNull { parseLine(it) }
    }

    private fun parseContentBlock(block: JsonObject): UiContentBlock? {
        return when (val blockType = block["type"]?.jsonPrimitive?.contentOrNull) {
            "text" -> {
                val text = block["text"]?.jsonPrimitive?.contentOrNull ?: return null
                if (text.isBlank()) return null
                UiContentBlock.Text(text)
            }

            "thinking" -> {
                val thinking = block["thinking"]?.jsonPrimitive?.contentOrNull ?: return null
                if (thinking.isBlank()) return null
                UiContentBlock.Thinking(thinking)
            }

            "tool_use" -> {
                val id = block["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val name = block["name"]?.jsonPrimitive?.contentOrNull ?: ""
                val input = block["input"]?.jsonObject ?: JsonObject(emptyMap())
                UiContentBlock.ToolUse(toolName = name, inputJson = input, toolUseId = id)
            }

            else -> null
        }
    }
}
