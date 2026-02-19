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

/**
 * BridgeEvent の JSON シリアライザ。
 * sealed interface を `{"type": "xxx", ...}` 形式で読み書きする。
 */
internal object BridgeEventSerializer : KSerializer<BridgeEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BridgeEvent")

    override fun serialize(encoder: Encoder, value: BridgeEvent) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("BridgeEvent can only be serialized to JSON")

        // Unknown はそのまま raw 文字列として出力
        if (value is BridgeEvent.Unknown) {
            jsonEncoder.encodeJsonElement(JsonPrimitive(value.raw))
            return
        }

        val json = jsonEncoder.json
        val (type, fields) = when (value) {
            is BridgeEvent.Ready -> BridgeEventTypes.READY to json.encodeToJsonElement(BridgeEvent.Ready.serializer(), value).jsonObject
            is BridgeEvent.SessionInit -> BridgeEventTypes.SESSION_INIT to json.encodeToJsonElement(BridgeEvent.SessionInit.serializer(), value).jsonObject
            is BridgeEvent.AssistantMessage -> BridgeEventTypes.ASSISTANT_MESSAGE to json.encodeToJsonElement(BridgeEvent.AssistantMessage.serializer(), value).jsonObject
            is BridgeEvent.StreamMessageStart -> BridgeEventTypes.STREAM_MESSAGE_START to json.encodeToJsonElement(BridgeEvent.StreamMessageStart.serializer(), value).jsonObject
            is BridgeEvent.StreamContentStart -> BridgeEventTypes.STREAM_CONTENT_START to json.encodeToJsonElement(BridgeEvent.StreamContentStart.serializer(), value).jsonObject
            is BridgeEvent.StreamContentDelta -> BridgeEventTypes.STREAM_CONTENT_DELTA to json.encodeToJsonElement(BridgeEvent.StreamContentDelta.serializer(), value).jsonObject
            is BridgeEvent.StreamContentStop -> BridgeEventTypes.STREAM_CONTENT_STOP to json.encodeToJsonElement(BridgeEvent.StreamContentStop.serializer(), value).jsonObject
            is BridgeEvent.StreamMessageStop -> BridgeEventTypes.STREAM_MESSAGE_STOP to json.encodeToJsonElement(BridgeEvent.StreamMessageStop.serializer(), value).jsonObject
            is BridgeEvent.TurnResult -> BridgeEventTypes.TURN_RESULT to json.encodeToJsonElement(BridgeEvent.TurnResult.serializer(), value).jsonObject
            is BridgeEvent.PermissionRequest -> BridgeEventTypes.PERMISSION_REQUEST to json.encodeToJsonElement(BridgeEvent.PermissionRequest.serializer(), value).jsonObject
            is BridgeEvent.ToolProgress -> BridgeEventTypes.TOOL_PROGRESS to json.encodeToJsonElement(BridgeEvent.ToolProgress.serializer(), value).jsonObject
            is BridgeEvent.Status -> BridgeEventTypes.STATUS to json.encodeToJsonElement(BridgeEvent.Status.serializer(), value).jsonObject
            is BridgeEvent.Error -> BridgeEventTypes.ERROR to json.encodeToJsonElement(BridgeEvent.Error.serializer(), value).jsonObject
            is BridgeEvent.Unknown -> error("unreachable") // 上で早期 return 済み
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
            BridgeEventTypes.READY -> BridgeEvent.Ready
            BridgeEventTypes.SESSION_INIT -> json.decodeFromJsonElement<BridgeEvent.SessionInit>(element)
            BridgeEventTypes.ASSISTANT_MESSAGE -> json.decodeFromJsonElement<BridgeEvent.AssistantMessage>(element)
            BridgeEventTypes.STREAM_MESSAGE_START -> json.decodeFromJsonElement<BridgeEvent.StreamMessageStart>(element)
            BridgeEventTypes.STREAM_CONTENT_START -> json.decodeFromJsonElement<BridgeEvent.StreamContentStart>(element)
            BridgeEventTypes.STREAM_CONTENT_DELTA -> json.decodeFromJsonElement<BridgeEvent.StreamContentDelta>(element)
            BridgeEventTypes.STREAM_CONTENT_STOP -> json.decodeFromJsonElement<BridgeEvent.StreamContentStop>(element)
            BridgeEventTypes.STREAM_MESSAGE_STOP -> json.decodeFromJsonElement<BridgeEvent.StreamMessageStop>(element)
            BridgeEventTypes.TURN_RESULT -> json.decodeFromJsonElement<BridgeEvent.TurnResult>(element)
            BridgeEventTypes.PERMISSION_REQUEST -> json.decodeFromJsonElement<BridgeEvent.PermissionRequest>(element)
            BridgeEventTypes.TOOL_PROGRESS -> json.decodeFromJsonElement<BridgeEvent.ToolProgress>(element)
            BridgeEventTypes.STATUS -> json.decodeFromJsonElement<BridgeEvent.Status>(element)
            BridgeEventTypes.ERROR -> json.decodeFromJsonElement<BridgeEvent.Error>(element)
            else -> BridgeEvent.Unknown(element.toString())
        }
    }
}
