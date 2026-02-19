package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.Serializable

@Serializable
data class McpServerInfo(
    val name: String,
    val status: String? = null,
)
