package me.matsumo.agentguiplugin.bridge.model

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable(with = BridgeEventSerializer::class)
sealed interface BridgeEvent {
    @Serializable data object Ready : BridgeEvent

    @Serializable data class SessionInit(
        val sessionId: String,
        val model: String,
        val claudeCodeVersion: String? = null,
        val tools: List<String> = emptyList(),
        val mcpServers: List<McpServerInfo> = emptyList(),
        val permissionMode: String? = null,
    ) : BridgeEvent

    @Serializable data class AssistantMessage(
        val sessionId: String,
        val parentToolUseId: String? = null,
        val content: List<ContentBlock> = emptyList(),
    ) : BridgeEvent

    @Serializable data class StreamMessageStart(val sessionId: String) : BridgeEvent
    @Serializable data class StreamContentStart(
        val sessionId: String,
        val index: Int,
        val blockType: String,
        val blockId: String? = null,
        val toolName: String? = null,
    ) : BridgeEvent
    @Serializable data class StreamContentDelta(
        val sessionId: String,
        val index: Int,
        val deltaType: String,
        val text: String,
    ) : BridgeEvent
    @Serializable data class StreamContentStop(val sessionId: String, val index: Int) : BridgeEvent
    @Serializable data class StreamMessageStop(val sessionId: String) : BridgeEvent

    @Serializable data class TurnResult(
        val sessionId: String,
        val subtype: String,
        val totalCostUsd: Double = 0.0,
        val numTurns: Int = 0,
        val isError: Boolean = false,
        val usage: TokenUsage? = null,
        val result: String? = null,
    ) : BridgeEvent

    @Serializable data class PermissionRequest(
        val requestId: String,
        val toolName: String,
        val toolInput: JsonObject = buildJsonObject {},
        val toolUseId: String? = null,
    ) : BridgeEvent

    @Serializable data class ToolProgress(
        val sessionId: String,
        val toolName: String,
        val toolUseId: String? = null,
        val elapsedSeconds: Double = 0.0,
    ) : BridgeEvent

    @Serializable data class Status(
        val sessionId: String,
        val status: String,
    ) : BridgeEvent

    @Serializable data class Error(
        val message: String,
        val fatal: Boolean = true,
    ) : BridgeEvent

    data class Unknown(val raw: String) : BridgeEvent
}
