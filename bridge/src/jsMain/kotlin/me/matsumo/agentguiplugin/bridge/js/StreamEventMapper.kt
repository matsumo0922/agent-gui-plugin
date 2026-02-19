package me.matsumo.agentguiplugin.bridge.js

import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.ContentBlock
import me.matsumo.agentguiplugin.bridge.model.ContentBlockTypes
import me.matsumo.agentguiplugin.bridge.model.McpServerInfo
import me.matsumo.agentguiplugin.bridge.model.TokenUsage

/**
 * SDK の query() が返すメッセージを BridgeEvent に変換する。
 * メッセージ type ごとにハンドラへ振り分ける。
 */
internal fun mapSdkMessageToBridgeEvents(message: dynamic): List<BridgeEvent> {
    val type = dynamicStringOrNull(message.type) ?: return emptyList()

    return when (type) {
        SdkMessageTypes.SYSTEM -> handleSystemMessage(message)
        SdkMessageTypes.STREAM_EVENT -> handleStreamEvent(
            sessionId = dynamicString(message.session_id),
            event = message.event,
        )
        SdkMessageTypes.ASSISTANT -> handleAssistantMessage(message)
        SdkMessageTypes.TOOL_PROGRESS -> handleToolProgress(message)
        SdkMessageTypes.RESULT -> handleResult(message)
        else -> emptyList()
    }
}

// --- system メッセージ ---

private fun handleSystemMessage(message: dynamic): List<BridgeEvent> {
    val subtype = dynamicStringOrNull(message.subtype) ?: return emptyList()
    val sessionId = dynamicString(message.session_id)

    return when (subtype) {
        SdkSystemSubtypes.INIT -> listOf(
            BridgeEvent.SessionInit(
                sessionId = sessionId,
                model = dynamicString(message.model),
                claudeCodeVersion = dynamicStringOrNull(message.claude_code_version),
                tools = dynamicToList(message.tools) { dynamicString(it) },
                mcpServers = dynamicToList(message.mcp_servers) { item ->
                    McpServerInfo(
                        name = dynamicString(item.name),
                        status = dynamicStringOrNull(item.status),
                    )
                },
                permissionMode = dynamicStringOrNull(message.permissionMode),
            )
        )
        SdkSystemSubtypes.STATUS -> listOf(
            BridgeEvent.Status(
                sessionId = sessionId,
                status = dynamicString(message.status),
            )
        )
        else -> emptyList()
    }
}

// --- ストリームイベント ---

private fun handleStreamEvent(sessionId: String, event: dynamic): List<BridgeEvent> {
    val eventType = dynamicStringOrNull(event.type) ?: return emptyList()

    return when (eventType) {
        SdkStreamEventTypes.MESSAGE_START -> listOf(
            BridgeEvent.StreamMessageStart(sessionId),
        )

        SdkStreamEventTypes.CONTENT_BLOCK_START -> {
            val block = event.content_block
            val blockType = dynamicString(block.type)
            val isToolUse = blockType == ContentBlockTypes.TOOL_USE

            listOf(
                BridgeEvent.StreamContentStart(
                    sessionId = sessionId,
                    index = dynamicInt(event.index),
                    blockType = blockType,
                    blockId = if (isToolUse) dynamicStringOrNull(block.id) else null,
                    toolName = if (isToolUse) dynamicStringOrNull(block.name) else null,
                )
            )
        }

        SdkStreamEventTypes.CONTENT_BLOCK_DELTA -> {
            val delta = event.delta
            val deltaType = dynamicStringOrNull(delta.type) ?: return emptyList()
            val text = when (deltaType) {
                SdkDeltaTypes.TEXT_DELTA -> dynamicString(delta.text)
                SdkDeltaTypes.THINKING_DELTA -> dynamicString(delta.thinking)
                SdkDeltaTypes.INPUT_JSON_DELTA -> dynamicString(delta.partial_json)
                else -> return emptyList()
            }

            listOf(
                BridgeEvent.StreamContentDelta(
                    sessionId = sessionId,
                    index = dynamicInt(event.index),
                    deltaType = deltaType,
                    text = text,
                )
            )
        }

        SdkStreamEventTypes.CONTENT_BLOCK_STOP -> listOf(
            BridgeEvent.StreamContentStop(
                sessionId = sessionId,
                index = dynamicInt(event.index),
            )
        )

        SdkStreamEventTypes.MESSAGE_STOP -> listOf(
            BridgeEvent.StreamMessageStop(sessionId),
        )

        SdkStreamEventTypes.MESSAGE_DELTA -> emptyList()

        else -> emptyList()
    }
}

// --- アシスタントメッセージ ---

private fun handleAssistantMessage(message: dynamic): List<BridgeEvent> {
    val content = dynamicToList(message.message?.content) { item ->
        when (dynamicStringOrNull(item.type)) {
            ContentBlockTypes.TEXT -> ContentBlock.Text(text = dynamicString(item.text))
            ContentBlockTypes.THINKING -> ContentBlock.Thinking(thinking = dynamicString(item.thinking))
            ContentBlockTypes.TOOL_USE -> ContentBlock.ToolUse(
                id = dynamicString(item.id),
                name = dynamicString(item.name),
                input = dynamicToJsonObject(item.input),
            )
            else -> ContentBlock.Unknown(dynamicToJsonObject(item))
        }
    }

    return listOf(
        BridgeEvent.AssistantMessage(
            sessionId = dynamicString(message.session_id),
            parentToolUseId = dynamicStringOrNull(message.parent_tool_use_id),
            content = content,
        )
    )
}

// --- ツール進捗 ---

private fun handleToolProgress(message: dynamic): List<BridgeEvent> = listOf(
    BridgeEvent.ToolProgress(
        sessionId = dynamicString(message.session_id),
        toolName = dynamicString(message.tool_name),
        toolUseId = dynamicStringOrNull(message.tool_use_id),
        elapsedSeconds = dynamicDouble(message.elapsed_time_seconds),
    )
)

// --- ターン結果 ---

private fun handleResult(message: dynamic): List<BridgeEvent> {
    val subtype = dynamicString(message.subtype)
    val u = message.usage
    val usage = if (!isNullOrUndefined(u)) {
        TokenUsage(
            inputTokens = dynamicInt(u.input_tokens),
            outputTokens = dynamicInt(u.output_tokens),
            cacheCreationInputTokens = dynamicInt(u.cache_creation_input_tokens),
            cacheReadInputTokens = dynamicInt(u.cache_read_input_tokens),
        )
    } else {
        null
    }

    return listOf(
        BridgeEvent.TurnResult(
            sessionId = dynamicString(message.session_id),
            subtype = subtype,
            totalCostUsd = dynamicDouble(message.total_cost_usd),
            numTurns = dynamicInt(message.num_turns),
            isError = dynamicBool(message.is_error),
            usage = usage,
            result = if (subtype == "success") dynamicStringOrNull(message.result) else null,
        )
    )
}
