package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

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
    override fun serialize(encoder: Encoder, value: ContentBlock) = error("Not supported")
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
