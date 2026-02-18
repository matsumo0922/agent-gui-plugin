package me.matsumo.agentguiplugin.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock

@Composable
fun AssistantMessageBlock(
    blocks: List<UiContentBlock>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    ToolUseBlock(
                        toolName = block.toolName,
                        inputJson = block.inputJson,
                        elapsed = block.elapsed,
                    )
                }
            }
        }
    }
}
