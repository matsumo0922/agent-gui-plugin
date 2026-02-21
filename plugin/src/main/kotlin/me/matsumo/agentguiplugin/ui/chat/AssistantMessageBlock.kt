package me.matsumo.agentguiplugin.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock

@Composable
fun AssistantMessageBlock(
    blocks: List<UiContentBlock>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = ChatTheme.Spacing.messageMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ChatTheme.Spacing.blockGap),
    ) {
        blocks.forEach { block ->
            when (block) {
                is UiContentBlock.Text -> {
                    MarkdownText(
                        text = block.text,
                    )
                }
                is UiContentBlock.Thinking -> {
                    ThinkingBlock(
                        text = block.text,
                    )
                }
                is UiContentBlock.ToolUse -> {
                    if (block.subAgentTask != null) {
                        SubAgentTaskCard(
                            task = block.subAgentTask,
                            toolName = block.toolName,
                            isToolStreaming = block.isStreaming,
                        )
                    } else {
                        ToolUseBlock(
                            toolName = block.toolName,
                            inputJson = block.inputJson,
                            elapsed = block.elapsed,
                            isStreaming = block.isStreaming,
                        )
                    }
                }
            }
        }
    }
}
