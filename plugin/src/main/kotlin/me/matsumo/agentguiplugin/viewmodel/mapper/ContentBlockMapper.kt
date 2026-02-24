package me.matsumo.agentguiplugin.viewmodel.mapper

import kotlinx.serialization.json.jsonPrimitive
import me.matsumo.agentguiplugin.viewmodel.EditDiffInfo
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import me.matsumo.claude.agent.types.ContentBlock
import me.matsumo.claude.agent.types.TextBlock
import me.matsumo.claude.agent.types.ThinkingBlock
import me.matsumo.claude.agent.types.ToolResultBlock
import me.matsumo.claude.agent.types.ToolUseBlock

private val EDIT_TOOL_NAMES = setOf("Edit", "str_replace_based_edit_tool", "StrReplaceBasedEditTool")

private fun ToolUseBlock.toEditDiffInfoOrNull(): EditDiffInfo? {
    if (name !in EDIT_TOOL_NAMES) return null
    val filePath = input["file_path"]?.jsonPrimitive?.content ?: return null
    val oldString = input["old_string"]?.jsonPrimitive?.content ?: return null
    val newString = input["new_string"]?.jsonPrimitive?.content ?: return null
    return EditDiffInfo(filePath = filePath, oldString = oldString, newString = newString)
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
