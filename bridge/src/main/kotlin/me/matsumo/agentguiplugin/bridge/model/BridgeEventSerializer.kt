package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

internal object BridgeEventSerializer : KSerializer<BridgeEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BridgeEvent")

    override fun serialize(encoder: Encoder, value: BridgeEvent) {
        error("BridgeEvent serialization is not supported")
    }

    override fun deserialize(decoder: Decoder): BridgeEvent {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("BridgeEvent can only be deserialized from JSON")
        val element = jsonDecoder.decodeJsonElement().jsonObject
        val type = element["type"]?.jsonPrimitive?.contentOrNull
            ?: return BridgeEvent.Unknown(element.toString())

        val json = jsonDecoder.json
        return when (type) {
            "ready" -> BridgeEvent.Ready
            "session_init" -> json.decodeFromJsonElement<BridgeEvent.SessionInit>(element)
            "assistant_message" -> json.decodeFromJsonElement<BridgeEvent.AssistantMessage>(element)
            "stream_message_start" -> json.decodeFromJsonElement<BridgeEvent.StreamMessageStart>(element)
            "stream_content_start" -> json.decodeFromJsonElement<BridgeEvent.StreamContentStart>(element)
            "stream_content_delta" -> json.decodeFromJsonElement<BridgeEvent.StreamContentDelta>(element)
            "stream_content_stop" -> json.decodeFromJsonElement<BridgeEvent.StreamContentStop>(element)
            "stream_message_stop" -> json.decodeFromJsonElement<BridgeEvent.StreamMessageStop>(element)
            "turn_result" -> json.decodeFromJsonElement<BridgeEvent.TurnResult>(element)
            "permission_request" -> json.decodeFromJsonElement<BridgeEvent.PermissionRequest>(element)
            "tool_progress" -> json.decodeFromJsonElement<BridgeEvent.ToolProgress>(element)
            "status" -> json.decodeFromJsonElement<BridgeEvent.Status>(element)
            "error" -> json.decodeFromJsonElement<BridgeEvent.Error>(element)
            else -> BridgeEvent.Unknown(element.toString())
        }
    }
}
