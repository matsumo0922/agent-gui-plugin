package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object BridgeEventSerializer : KSerializer<BridgeEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BridgeEvent")

    override fun serialize(encoder: Encoder, value: BridgeEvent) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("BridgeEvent can only be serialized to JSON")
        val json = jsonEncoder.json

        val (type, fields) = when (value) {
            is BridgeEvent.Ready -> "ready" to json.encodeToJsonElement(BridgeEvent.Ready.serializer(), value).jsonObject
            is BridgeEvent.SessionInit -> "session_init" to json.encodeToJsonElement(BridgeEvent.SessionInit.serializer(), value).jsonObject
            is BridgeEvent.AssistantMessage -> "assistant_message" to json.encodeToJsonElement(BridgeEvent.AssistantMessage.serializer(), value).jsonObject
            is BridgeEvent.StreamMessageStart -> "stream_message_start" to json.encodeToJsonElement(BridgeEvent.StreamMessageStart.serializer(), value).jsonObject
            is BridgeEvent.StreamContentStart -> "stream_content_start" to json.encodeToJsonElement(BridgeEvent.StreamContentStart.serializer(), value).jsonObject
            is BridgeEvent.StreamContentDelta -> "stream_content_delta" to json.encodeToJsonElement(BridgeEvent.StreamContentDelta.serializer(), value).jsonObject
            is BridgeEvent.StreamContentStop -> "stream_content_stop" to json.encodeToJsonElement(BridgeEvent.StreamContentStop.serializer(), value).jsonObject
            is BridgeEvent.StreamMessageStop -> "stream_message_stop" to json.encodeToJsonElement(BridgeEvent.StreamMessageStop.serializer(), value).jsonObject
            is BridgeEvent.TurnResult -> "turn_result" to json.encodeToJsonElement(BridgeEvent.TurnResult.serializer(), value).jsonObject
            is BridgeEvent.PermissionRequest -> "permission_request" to json.encodeToJsonElement(BridgeEvent.PermissionRequest.serializer(), value).jsonObject
            is BridgeEvent.ToolProgress -> "tool_progress" to json.encodeToJsonElement(BridgeEvent.ToolProgress.serializer(), value).jsonObject
            is BridgeEvent.Status -> "status" to json.encodeToJsonElement(BridgeEvent.Status.serializer(), value).jsonObject
            is BridgeEvent.Error -> "error" to json.encodeToJsonElement(BridgeEvent.Error.serializer(), value).jsonObject
            is BridgeEvent.Unknown -> {
                jsonEncoder.encodeJsonElement(JsonPrimitive(value.raw))
                return
            }
        }

        val obj = buildJsonObject {
            put("type", type)
            fields.forEach { (key, v) -> put(key, v) }
        }
        jsonEncoder.encodeJsonElement(obj)
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
