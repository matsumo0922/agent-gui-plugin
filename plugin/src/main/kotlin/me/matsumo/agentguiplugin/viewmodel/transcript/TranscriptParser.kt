package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
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
 *   "message": { "content": [...] | "string" },
 *   "agentId": "...",
 *   "uuid": "...",
 *   "timestamp": "..."
 * }
 * ```
 *
 * - `"assistant"` lines → [ChatMessage.Assistant]
 * - `"user"` lines → [ChatMessage.User]
 *
 * Per the Anthropic API spec, `content` can be either a string (shorthand for
 * a single text block) or an array of content blocks.
 */
internal object TranscriptParser {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parse a single JSONL line into a [ChatMessage], or `null` if unparseable.
     */
    fun parseLine(line: String): ChatMessage? {
        if (line.isBlank()) return null

        return try {
            val obj = json.parseToJsonElement(line).jsonObject
            val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return null
            val messageObj = obj["message"]?.jsonObject ?: return null
            val uuid = obj["uuid"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
            val content = messageObj["content"] ?: return null

            // Normalise content: string → single-element text array (per API spec)
            val contentArray = when (content) {
                is JsonArray -> content
                is JsonPrimitive -> {
                    val text = content.contentOrNull
                    if (text.isNullOrBlank()) return null
                    return ChatMessage.User(id = uuid, text = text)
                }
                else -> return null
            }

            when (type) {
                "assistant" -> parseAssistantContent(uuid, contentArray)
                "user" -> parseUserArrayContent(uuid, contentArray)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAssistantContent(uuid: String, contentArray: JsonArray): ChatMessage.Assistant? {
        val blocks = contentArray.mapNotNull { element ->
            parseContentBlock(element.jsonObject)
        }
        if (blocks.isEmpty()) return null

        return ChatMessage.Assistant(id = uuid, blocks = blocks)
    }

    private fun parseUserArrayContent(uuid: String, contentArray: JsonArray): ChatMessage.User? {
        // Extract text from content blocks and join them
        val text = contentArray.mapNotNull { element ->
            val block = element.jsonObject
            if (block["type"]?.jsonPrimitive?.contentOrNull == "text") {
                block["text"]?.jsonPrimitive?.contentOrNull
            } else {
                null
            }
        }.joinToString("\n")

        if (text.isBlank()) return null
        return ChatMessage.User(id = uuid, text = text)
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
