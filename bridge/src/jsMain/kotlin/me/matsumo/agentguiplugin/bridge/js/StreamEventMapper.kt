package me.matsumo.agentguiplugin.bridge.js

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.ContentBlock
import me.matsumo.agentguiplugin.bridge.model.McpServerInfo
import me.matsumo.agentguiplugin.bridge.model.TokenUsage

internal fun mapSdkMessageToBridgeEvents(message: dynamic): List<BridgeEvent> {
    val type = message.type as? String ?: return emptyList()

    return when (type) {
        "system" -> handleSystemMessage(message)
        "stream_event" -> handleStreamEvent(message.session_id as? String ?: "", message.event)
        "assistant" -> handleAssistantMessage(message)
        "tool_progress" -> handleToolProgress(message)
        "result" -> handleResult(message)
        else -> emptyList()
    }
}

private fun handleSystemMessage(message: dynamic): List<BridgeEvent> {
    val subtype = message.subtype as? String ?: return emptyList()
    val sessionId = message.session_id as? String ?: ""

    return when (subtype) {
        "init" -> {
            val tools = dynamicArrayToStringList(message.tools)
            val mcpServers = dynamicArrayToMcpServers(message.mcp_servers)

            listOf(
                BridgeEvent.SessionInit(
                    sessionId = sessionId,
                    model = message.model as? String ?: "",
                    claudeCodeVersion = message.claude_code_version as? String,
                    tools = tools,
                    mcpServers = mcpServers,
                    permissionMode = message.permissionMode as? String,
                )
            )
        }
        "status" -> listOf(
            BridgeEvent.Status(
                sessionId = sessionId,
                status = message.status as? String ?: "",
            )
        )
        else -> emptyList()
    }
}

private fun handleStreamEvent(sessionId: String, event: dynamic): List<BridgeEvent> {
    val eventType = event.type as? String ?: return emptyList()

    return when (eventType) {
        "message_start" -> listOf(BridgeEvent.StreamMessageStart(sessionId))

        "content_block_start" -> {
            val block = event.content_block
            val blockType = block.type as? String ?: ""
            val blockId = if (blockType == "tool_use") block.id as? String else null
            val toolName = if (blockType == "tool_use") block.name as? String else null

            listOf(
                BridgeEvent.StreamContentStart(
                    sessionId = sessionId,
                    index = (event.index as? Number)?.toInt() ?: 0,
                    blockType = blockType,
                    blockId = blockId,
                    toolName = toolName,
                )
            )
        }

        "content_block_delta" -> {
            val delta = event.delta
            val deltaType = delta.type as? String ?: return emptyList()
            val text = when (deltaType) {
                "text_delta" -> delta.text as? String ?: ""
                "thinking_delta" -> delta.thinking as? String ?: ""
                "input_json_delta" -> delta.partial_json as? String ?: ""
                else -> return emptyList()
            }

            listOf(
                BridgeEvent.StreamContentDelta(
                    sessionId = sessionId,
                    index = (event.index as? Number)?.toInt() ?: 0,
                    deltaType = deltaType,
                    text = text,
                )
            )
        }

        "content_block_stop" -> listOf(
            BridgeEvent.StreamContentStop(
                sessionId = sessionId,
                index = (event.index as? Number)?.toInt() ?: 0,
            )
        )

        "message_stop" -> listOf(BridgeEvent.StreamMessageStop(sessionId))

        "message_delta" -> emptyList()

        else -> emptyList()
    }
}

private fun handleAssistantMessage(message: dynamic): List<BridgeEvent> {
    val sessionId = message.session_id as? String ?: ""
    val parentToolUseId = message.parent_tool_use_id as? String
    val content = dynamicArrayToContentBlocks(message.message?.content)

    return listOf(
        BridgeEvent.AssistantMessage(
            sessionId = sessionId,
            parentToolUseId = parentToolUseId,
            content = content,
        )
    )
}

private fun handleToolProgress(message: dynamic): List<BridgeEvent> {
    return listOf(
        BridgeEvent.ToolProgress(
            sessionId = message.session_id as? String ?: "",
            toolName = message.tool_name as? String ?: "",
            toolUseId = message.tool_use_id as? String,
            elapsedSeconds = (message.elapsed_time_seconds as? Number)?.toDouble() ?: 0.0,
        )
    )
}

private fun handleResult(message: dynamic): List<BridgeEvent> {
    val subtype = message.subtype as? String ?: ""
    val u = message.usage
    val usage = if (u != null && u != undefined) {
        TokenUsage(
            inputTokens = (u.input_tokens as? Number)?.toInt() ?: 0,
            outputTokens = (u.output_tokens as? Number)?.toInt() ?: 0,
            cacheCreationInputTokens = (u.cache_creation_input_tokens as? Number)?.toInt() ?: 0,
            cacheReadInputTokens = (u.cache_read_input_tokens as? Number)?.toInt() ?: 0,
        )
    } else {
        null
    }

    return listOf(
        BridgeEvent.TurnResult(
            sessionId = message.session_id as? String ?: "",
            subtype = subtype,
            totalCostUsd = (message.total_cost_usd as? Number)?.toDouble() ?: 0.0,
            numTurns = (message.num_turns as? Number)?.toInt() ?: 0,
            isError = message.is_error as? Boolean ?: false,
            usage = usage,
            result = if (subtype == "success") message.result as? String else null,
        )
    )
}

// --- Helper functions for dynamic → typed conversion ---

private fun dynamicArrayToStringList(arr: dynamic): List<String> {
    if (arr == null || arr == undefined) return emptyList()
    val result = mutableListOf<String>()
    val length = (arr.length as? Number)?.toInt() ?: return emptyList()
    for (i in 0 until length) {
        val item = arr[i] as? String
        if (item != null) result.add(item)
    }
    return result
}

private fun dynamicArrayToMcpServers(arr: dynamic): List<McpServerInfo> {
    if (arr == null || arr == undefined) return emptyList()
    val result = mutableListOf<McpServerInfo>()
    val length = (arr.length as? Number)?.toInt() ?: return emptyList()
    for (i in 0 until length) {
        val item = arr[i]
        result.add(
            McpServerInfo(
                name = item.name as? String ?: "",
                status = item.status as? String,
            )
        )
    }
    return result
}

private fun dynamicArrayToContentBlocks(arr: dynamic): List<ContentBlock> {
    if (arr == null || arr == undefined) return emptyList()
    val result = mutableListOf<ContentBlock>()
    val length = (arr.length as? Number)?.toInt() ?: return emptyList()
    for (i in 0 until length) {
        val item = arr[i]
        val blockType = item.type as? String
        val block = when (blockType) {
            "text" -> ContentBlock.Text(text = item.text as? String ?: "")
            "thinking" -> ContentBlock.Thinking(thinking = item.thinking as? String ?: "")
            "tool_use" -> ContentBlock.ToolUse(
                id = item.id as? String ?: "",
                name = item.name as? String ?: "",
                input = dynamicToJsonObject(item.input),
            )
            else -> ContentBlock.Unknown(dynamicToJsonObject(item))
        }
        result.add(block)
    }
    return result
}

internal fun dynamicToJsonObject(obj: dynamic): JsonObject {
    if (obj == null || obj == undefined) return buildJsonObject {}
    return try {
        val jsonStr = js("JSON.stringify(obj)") as String
        kotlinx.serialization.json.Json.parseToJsonElement(jsonStr) as? JsonObject ?: buildJsonObject {}
    } catch (_: Throwable) {
        buildJsonObject {}
    }
}
