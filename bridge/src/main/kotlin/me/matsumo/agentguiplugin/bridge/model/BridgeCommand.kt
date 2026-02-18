package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
sealed interface BridgeCommand {
    val type: String

    @Serializable
    @SerialName("start")
    data class Start(
        override val type: String = "start",
        val prompt: String,
        val options: SessionOptions = SessionOptions(),
    ) : BridgeCommand

    @Serializable
    @SerialName("user_message")
    data class UserMessage(
        override val type: String = "user_message",
        val text: String,
        val images: List<JsonObject>? = null,
        val documents: List<JsonObject>? = null,
    ) : BridgeCommand

    @Serializable
    @SerialName("permission_response")
    data class PermissionResponse(
        override val type: String = "permission_response",
        val requestId: String,
        val result: PermissionResult,
    ) : BridgeCommand

    @Serializable
    @SerialName("abort")
    data class Abort(
        override val type: String = "abort",
    ) : BridgeCommand
}
