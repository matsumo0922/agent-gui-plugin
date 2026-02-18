package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed interface BridgeCommand {

    @Serializable
    @SerialName("start")
    data class Start(
        val prompt: String,
        val options: SessionOptions = SessionOptions(),
    ) : BridgeCommand

    @Serializable
    @SerialName("user_message")
    data class UserMessage(
        val text: String,
        val images: List<JsonObject>? = null,
        val documents: List<JsonObject>? = null,
    ) : BridgeCommand

    @Serializable
    @SerialName("permission_response")
    data class PermissionResponse(
        val requestId: String,
        val result: PermissionResult,
    ) : BridgeCommand

    @Serializable
    @SerialName("abort")
    data object Abort : BridgeCommand
}
