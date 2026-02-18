package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PermissionResult(
    val behavior: String,
    val message: String? = null,
    val updatedInput: JsonObject? = null,
)
