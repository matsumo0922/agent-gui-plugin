package me.matsumo.agentguiplugin.viewmodel.mapper

import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import me.matsumo.claude.agent.types.ContentBlock
import me.matsumo.claude.agent.types.TextBlock
import me.matsumo.claude.agent.types.ThinkingBlock
import me.matsumo.claude.agent.types.ToolResultBlock
import me.matsumo.claude.agent.types.ToolUseBlock

internal fun ContentBlock.toUiBlock(): UiContentBlock = when (this) {
    is TextBlock -> UiContentBlock.Text(text)
    is ThinkingBlock -> UiContentBlock.Thinking(thinking)
    is ToolUseBlock -> UiContentBlock.ToolUse(
        toolName = name,
        inputJson = input,
        toolUseId = id,
    )
    is ToolResultBlock -> UiContentBlock.Text("[Tool result]")
}

internal fun ContentBlock.toUiBlockOrNull(): UiContentBlock? = when (this) {
    is TextBlock -> if (text.isBlank()) null else UiContentBlock.Text(text)
    is ThinkingBlock -> if (thinking.isBlank()) null else UiContentBlock.Thinking(thinking)
    is ToolUseBlock -> UiContentBlock.ToolUse(toolName = name, inputJson = input, toolUseId = id)
    is ToolResultBlock -> null
}
