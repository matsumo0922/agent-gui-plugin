package me.matsumo.agentguiplugin.viewmodel.mapper

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class StreamEventType(val wire: String) {
    MessageStart("message_start"),
    MessageStop("message_stop"),
    ContentBlockStart("content_block_start"),
    ContentBlockDelta("content_block_delta"),
    ContentBlockStop("content_block_stop");

    companion object {
        fun fromWire(value: String?): StreamEventType? =
            entries.firstOrNull { it.wire == value }
    }
}

enum class BlockType(val wire: String) {
    Text("text"),
    Thinking("thinking"),
    ToolUse("tool_use");

    companion object {
        fun fromWire(value: String?): BlockType =
            entries.firstOrNull { it.wire == value } ?: Text
    }
}

enum class DeltaType(val wire: String, val contentKey: String, val blockType: BlockType) {
    TextDelta("text_delta", "text", BlockType.Text),
    ThinkingDelta("thinking_delta", "thinking", BlockType.Thinking),
    InputJsonDelta("input_json_delta", "partial_json", BlockType.ToolUse);

    companion object {
        fun fromWire(value: String?): DeltaType? =
            entries.firstOrNull { it.wire == value }
    }
}

sealed interface ParsedStreamEvent {
    data class MessageStart(val messageId: String) : ParsedStreamEvent
    data object MessageStop : ParsedStreamEvent
    data class ContentBlockStart(
        val index: Int,
        val blockType: BlockType,
        val toolName: String?,
        val toolUseId: String?,
    ) : ParsedStreamEvent

    data class ContentBlockDelta(
        val index: Int,
        val deltaType: DeltaType,
        val text: String,
    ) : ParsedStreamEvent

    data class ContentBlockStop(val index: Int) : ParsedStreamEvent
}

fun parseStreamEvent(event: JsonObject): ParsedStreamEvent? {
    val type = StreamEventType.fromWire(
        event["type"]?.jsonPrimitive?.contentOrNull,
    ) ?: return null

    return when (type) {
        StreamEventType.MessageStart -> {
            val messageId = event["message"]
                ?.jsonObject?.get("id")
                ?.jsonPrimitive?.contentOrNull.orEmpty()
            ParsedStreamEvent.MessageStart(messageId)
        }

        StreamEventType.MessageStop -> ParsedStreamEvent.MessageStop

        StreamEventType.ContentBlockStart -> {
            val index = event.intField("index") ?: return null
            val contentBlock = event["content_block"]?.jsonObject
            val blockType = BlockType.fromWire(
                contentBlock?.get("type")?.jsonPrimitive?.contentOrNull,
            )
            val toolName = contentBlock?.get("name")?.jsonPrimitive?.contentOrNull
            val toolUseId = contentBlock?.get("id")?.jsonPrimitive?.contentOrNull
            ParsedStreamEvent.ContentBlockStart(index, blockType, toolName, toolUseId)
        }

        StreamEventType.ContentBlockDelta -> {
            val index = event.intField("index") ?: return null
            val delta = event["delta"]?.jsonObject ?: return null
            val deltaType = DeltaType.fromWire(
                delta["type"]?.jsonPrimitive?.contentOrNull,
            ) ?: return null
            val text = delta[deltaType.contentKey]?.jsonPrimitive?.contentOrNull.orEmpty()
            ParsedStreamEvent.ContentBlockDelta(index, deltaType, text)
        }

        StreamEventType.ContentBlockStop -> {
            val index = event.intField("index") ?: return null
            ParsedStreamEvent.ContentBlockStop(index)
        }
    }
}

private fun JsonObject.intField(key: String): Int? =
    this[key]?.jsonPrimitive?.intOrNull
