package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(with = ContentBlockSerializer::class)
sealed interface ContentBlock {
    @Serializable data class Text(val text: String) : ContentBlock
    @Serializable data class Thinking(val thinking: String) : ContentBlock
    @Serializable data class ToolUse(
        val id: String,
        val name: String,
        val input: JsonObject = buildJsonObject {},
    ) : ContentBlock
    data class Unknown(val raw: JsonObject) : ContentBlock
}

internal object ContentBlockSerializer : KSerializer<ContentBlock> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ContentBlock")
    override fun serialize(encoder: Encoder, value: ContentBlock) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("JSON only")
        val json = jsonEncoder.json
        val (type, fields) = when (value) {
            is ContentBlock.Text -> "text" to json.encodeToJsonElement(ContentBlock.Text.serializer(), value).jsonObject
            is ContentBlock.Thinking -> "thinking" to json.encodeToJsonElement(ContentBlock.Thinking.serializer(), value).jsonObject
            is ContentBlock.ToolUse -> "tool_use" to json.encodeToJsonElement(ContentBlock.ToolUse.serializer(), value).jsonObject
            is ContentBlock.Unknown -> {
                jsonEncoder.encodeJsonElement(value.raw)
                return
            }
        }
        val obj = buildJsonObject {
            put("type", type)
            fields.forEach { (key, v) -> put(key, v) }
        }
        jsonEncoder.encodeJsonElement(obj)
    }
    override fun deserialize(decoder: Decoder): ContentBlock {
        val jsonDecoder = decoder as? JsonDecoder ?: error("JSON only")
        val element = jsonDecoder.decodeJsonElement().jsonObject
        val type = element["type"]?.jsonPrimitive?.contentOrNull
        val json = jsonDecoder.json
        return when (type) {
            "text" -> json.decodeFromJsonElement<ContentBlock.Text>(element)
            "thinking" -> json.decodeFromJsonElement<ContentBlock.Thinking>(element)
            "tool_use" -> json.decodeFromJsonElement<ContentBlock.ToolUse>(element)
            else -> ContentBlock.Unknown(element)
        }
    }
}
