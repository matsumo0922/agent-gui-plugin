package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SubAgentTaskCard(
    task: SubAgentTask,
    subAgentTasks: Map<String, SubAgentTask>,
    toolName: String? = null,
    modifier: Modifier = Modifier,
    depth: Int = 0,
) {
    if (depth > 4) return // 無限再帰ガード

    var isExpanded by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    val accentColor = ChatTheme.ToolUse.toolNameColor
    val mutedColor = ChatTheme.Text.muted
    val messageCount = task.messages.size
    val label = toolName ?: task.spawnedByToolName ?: DEFAULT_TASK_LABEL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = ChatTheme.ToolUse.background, shape = shape)
            .border(1.dp, ChatTheme.ToolUse.border, shape)
            .drawBehind {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = ACCENT_BORDER_WIDTH.toPx(),
                )
            }
            .clickable { isExpanded = !isExpanded }
            .padding(CARD_PADDING)
            .animateContentSize(),
    ) {
        // ヘッダー行
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = AGENT_ICON, fontSize = HEADER_FONT_SIZE)

            Spacer(Modifier.width(ICON_SPACING))

            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = HEADER_FONT_SIZE,
                color = accentColor,
            )

            if (messageCount > 0) {
                Text(
                    text = " ($messageCount messages)",
                    fontSize = DETAIL_FONT_SIZE,
                    color = mutedColor,
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = if (isExpanded) ARROW_DOWN else ARROW_RIGHT,
                fontSize = DETAIL_FONT_SIZE,
                color = mutedColor,
            )
        }

        // 展開コンテンツ
        if (isExpanded && task.messages.isNotEmpty()) {
            Box(modifier = Modifier.padding(top = EXPANDED_TOP_PADDING)) {
                Column {
                    task.messages.forEach { message ->
                        when (message) {
                            is ChatMessage.Assistant -> {
                                // サブエージェント内のブロックを直接描画
                                message.blocks.forEach { block ->
                                    when (block) {
                                        is UiContentBlock.Text -> {
                                            MarkdownText(
                                                text = block.text,
                                                modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                                            )
                                        }
                                        is UiContentBlock.Thinking -> {
                                            ThinkingBlock(
                                                text = block.text,
                                                modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                                            )
                                        }
                                        is UiContentBlock.ToolUse -> {
                                            val nestedTask = block.toolUseId?.let { subAgentTasks[it] }
                                            if (nestedTask != null) {
                                                SubAgentTaskCard(
                                                    task = nestedTask,
                                                    toolName = block.toolName,
                                                    subAgentTasks = subAgentTasks,
                                                    depth = depth + 1,
                                                    modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                                                )
                                            } else {
                                                ToolUseBlock(
                                                    toolName = block.toolName,
                                                    inputJson = block.inputJson,
                                                    elapsed = block.elapsed,
                                                    modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            is ChatMessage.User -> {
                                // ツール結果は表示しない
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val AGENT_ICON = "\uD83E\uDD16"
private const val ARROW_DOWN = "\u25BE"
private const val ARROW_RIGHT = "\u25B8"
private const val DEFAULT_TASK_LABEL = "Sub-agent Task"

private val HEADER_FONT_SIZE = 13.sp
private val DETAIL_FONT_SIZE = 12.sp
private val CARD_CORNER_RADIUS = 8.dp
private val CARD_PADDING = 12.dp
private val ACCENT_BORDER_WIDTH = 3.dp
private val ICON_SPACING = 6.dp
private val EXPANDED_TOP_PADDING = 8.dp
private val INNER_VERTICAL_PADDING = 4.dp
