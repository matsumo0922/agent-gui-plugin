package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock

@Composable
fun AssistantMessageBlock(
    blocks: List<UiContentBlock>,
    subAgentTasks: Map<String, SubAgentTask>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
                        text = block.text,
                    )
                }
                is UiContentBlock.ToolUse -> {
                    ToolUseBlock(
                        modifier = Modifier.fillMaxWidth(),
                        name = block.toolName,
                        inputJson = block.inputJson,
                    )

                    val task = block.toolUseId?.let { subAgentTasks[it] }
                    if (task != null) {
                        SubAgentTaskCard(
                            task = task,
                            subAgentTasks = subAgentTasks,
                        )
                    }
                }
            }
        }
    }
}
