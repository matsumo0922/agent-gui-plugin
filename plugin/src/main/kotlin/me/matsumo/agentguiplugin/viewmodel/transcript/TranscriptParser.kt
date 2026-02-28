package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.ToolResultInfo
import me.matsumo.agentguiplugin.viewmodel.mapper.extractToolResults
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlockOrNull
import me.matsumo.claude.agent.internal.parseTranscriptLine
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.UserMessage
import java.util.*

/**
 * Parses JSONL lines from sub-agent transcript files into [ChatMessage] objects.
 *
 * Delegates JSON → [SDKMessage][me.matsumo.claude.agent.types.SDKMessage] parsing to the SDK's
 * [parseTranscriptLine], then converts SDK types to UI models.
 */
internal object TranscriptParser {

    /** parseLine の結果。メッセージまたはツール結果を返す。 */
    sealed interface ParsedLine {
        data class Msg(val message: ChatMessage) : ParsedLine
        data class ToolResults(val results: Map<String, ToolResultInfo>) : ParsedLine
    }

    /**
     * Parse a single JSONL line into a [ParsedLine], or `null` if unparseable.
     */
    fun parseLine(line: String): ParsedLine? {
        val message = parseTranscriptLine(line) ?: return null
        return when (message) {
            is AssistantMessage -> parseAssistant(message)?.let(ParsedLine::Msg)
            is UserMessage -> {
                // まず tool results を抽出
                val results = extractToolResults(message)
                if (results.isNotEmpty()) return ParsedLine.ToolResults(results)
                // tool results がなければ通常のユーザーメッセージ
                parseUser(message)?.let(ParsedLine::Msg)
            }
            else -> null
        }
    }

    private fun parseAssistant(msg: AssistantMessage): ChatMessage.Assistant? {
        val blocks = msg.content.mapNotNull { it.toUiBlockOrNull() }.toImmutableList()
        if (blocks.isEmpty()) return null
        return ChatMessage.Assistant(
            id = msg.uuid ?: UUID.randomUUID().toString(),
            blocks = blocks,
        )
    }

    private fun parseUser(msg: UserMessage): ChatMessage.User? {
        val text = extractUserText(msg.content) ?: return null
        return ChatMessage.User(
            id = msg.uuid ?: UUID.randomUUID().toString(),
            text = text,
        )
    }

    /** content: JsonElement? → String (string なら直接、array なら text block を結合) */
    private fun extractUserText(content: JsonElement?): String? {
        if (content == null) return null
        return when (content) {
            is JsonPrimitive -> content.contentOrNull?.takeIf { it.isNotBlank() }
            is JsonArray -> content.mapNotNull { elem ->
                val obj = elem.jsonObject
                if (obj["type"]?.jsonPrimitive?.contentOrNull == "text") {
                    obj["text"]?.jsonPrimitive?.contentOrNull
                } else null
            }.joinToString("\n").takeIf { it.isNotBlank() }
            else -> null
        }
    }
}
