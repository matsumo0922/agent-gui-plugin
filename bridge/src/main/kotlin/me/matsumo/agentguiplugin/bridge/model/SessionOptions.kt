package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SessionOptions(
    val cwd: String? = null,
    val resume: String? = null,
    val model: String? = null,
    val systemPrompt: String? = null,
    val permissionMode: String? = null,
    val disallowedTools: List<String>? = null,
    val maxTurns: Int? = null,
    val maxThinkingTokens: Int? = null,
    val maxBudgetUsd: Double? = null,
    val settingSources: List<String>? = null,
    val env: JsonObject? = null,
)
