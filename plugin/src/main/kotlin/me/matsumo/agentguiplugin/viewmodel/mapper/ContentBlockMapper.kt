package me.matsumo.agentguiplugin.viewmodel.mapper

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.matsumo.agentguiplugin.viewmodel.EditDiffInfo
import me.matsumo.agentguiplugin.viewmodel.ToolResultInfo
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import me.matsumo.agentguiplugin.viewmodel.permission.ToolNames
import me.matsumo.claude.agent.types.ContentBlock
import me.matsumo.claude.agent.types.TextBlock
import me.matsumo.claude.agent.types.ThinkingBlock
import me.matsumo.claude.agent.types.ToolResultBlock
import me.matsumo.claude.agent.types.ToolUseBlock
import me.matsumo.claude.agent.types.UserMessage

/** ToolResult のテキストを保存時にこの長さで切り詰める。表示側では再 truncate しない。 */
internal const val TOOL_RESULT_MAX_LENGTH = 1000

private fun ToolUseBlock.toEditDiffInfoOrNull(): EditDiffInfo? {
    val filePath = input["file_path"]?.jsonPrimitive?.content ?: return null
    return when (name) {
        in ToolNames.EDIT_TOOL_NAMES -> {
            val oldString = input["old_string"]?.jsonPrimitive?.content ?: return null
            val newString = input["new_string"]?.jsonPrimitive?.content ?: return null
            EditDiffInfo(filePath = filePath, oldString = oldString, newString = newString)
        }
        in ToolNames.WRITE_TOOL_NAMES -> {
            val content = input["content"]?.jsonPrimitive?.content ?: return null
            EditDiffInfo(filePath = filePath, oldString = "", newString = content)
        }
        else -> null
    }
}

internal fun ContentBlock.toUiBlock(): UiContentBlock = when (this) {
    is TextBlock -> UiContentBlock.Text(text)
    is ThinkingBlock -> UiContentBlock.Thinking(thinking)
    is ToolUseBlock -> UiContentBlock.ToolUse(
        toolName = name,
        inputJson = input,
        toolUseId = id,
        diffInfo = toEditDiffInfoOrNull(),
    )
    is ToolResultBlock -> UiContentBlock.Text("[Tool result]")
}

internal fun ContentBlock.toUiBlockOrNull(): UiContentBlock? = when (this) {
    is TextBlock -> if (text.isBlank()) null else UiContentBlock.Text(text)
    is ThinkingBlock -> if (thinking.isBlank()) null else UiContentBlock.Thinking(thinking)
    is ToolUseBlock -> UiContentBlock.ToolUse(
        toolName = name,
        inputJson = input,
        toolUseId = id,
        diffInfo = toEditDiffInfoOrNull(),
    )
    is ToolResultBlock -> null
}

/**
 * UserMessage からツール結果を抽出し、toolUseId → ToolResultInfo のマップを返す。
 *
 * CLI は結果を2つの経路で送る:
 * 1. `message.content` — tool_result ブロックの JsonArray
 * 2. `tool_use_result` — トップレベルの JsonObject
 *
 * 両方を探索して統合する。
 */
internal fun extractToolResults(message: UserMessage): Map<String, ToolResultInfo> {
    val results = mutableMapOf<String, ToolResultInfo>()

    // 経路1: message.content 内の tool_result ブロック
    val content = message.content
    if (content is JsonArray) {
        for (element in content) {
            val obj = runCatching { element.jsonObject }.getOrNull() ?: continue
            if (obj["type"]?.jsonPrimitive?.content != "tool_result") continue

            val toolUseId = obj["tool_use_id"]?.jsonPrimitive?.content ?: continue
            val isError = obj["is_error"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            val resultContent = obj["content"] ?: continue
            val text = extractResultText(resultContent)
            if (text != null) {
                results[toolUseId] = ToolResultInfo(content = text.take(TOOL_RESULT_MAX_LENGTH), isError = isError)
            }
        }
    }

    // 経路2: トップレベルの tool_use_result フィールド
    val toolUseResult = message.toolUseResult
    if (toolUseResult != null) {
        val toolUseId = toolUseResult["tool_use_id"]?.jsonPrimitive?.content
        if (toolUseId != null && toolUseId !in results) {
            val isError = toolUseResult["is_error"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            val resultContent = toolUseResult["content"]
            val text = if (resultContent != null) extractResultText(resultContent) else null
            if (text != null) {
                results[toolUseId] = ToolResultInfo(content = text.take(TOOL_RESULT_MAX_LENGTH), isError = isError)
            }
        }
    }

    return results
}

private fun extractResultText(element: kotlinx.serialization.json.JsonElement): String? {
    return when (element) {
        is JsonPrimitive -> element.content.takeIf { it.isNotBlank() }
        is JsonArray -> {
            element.mapNotNull { part ->
                runCatching {
                    val obj = part.jsonObject
                    when (obj["type"]?.jsonPrimitive?.content) {
                        "text" -> obj["text"]?.jsonPrimitive?.content
                        else -> null
                    }
                }.getOrNull()
            }.joinToString("\n").takeIf { it.isNotBlank() }
        }
        else -> element.toString().takeIf { it.isNotBlank() }
    }
}
