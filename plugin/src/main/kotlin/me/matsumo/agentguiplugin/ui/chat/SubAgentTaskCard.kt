package me.matsumo.agentguiplugin.ui.chat

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
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SubAgentTaskCard(
    task: ChatMessage.SubAgentTask,
    modifier: Modifier = Modifier,
    depth: Int = 0,
) {
    if (depth > 4) return // 無限再帰ガード

    var isExpanded by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    val accentColor = ChatTheme.ToolUse.toolNameColor
    val mutedColor = ChatTheme.Text.muted
    val messageCount = task.messages.size
    val toolLabel = task.spawnedByToolName ?: DEFAULT_TASK_LABEL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = ChatTheme.ToolUse.background, shape = shape)
            .border(1.dp, ChatTheme.ToolUse.border, shape)
            .drawBehind {
                // 左アクセントボーダー
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
                text = toolLabel,
                fontWeight = FontWeight.SemiBold,
                fontSize = HEADER_FONT_SIZE,
                color = accentColor,
            )

            Text(
                text = " ($messageCount messages)",
                fontSize = DETAIL_FONT_SIZE,
                color = mutedColor,
            )

            if (task.isComplete) {
                Text(
                    text = COMPLETE_LABEL,
                    fontSize = DETAIL_FONT_SIZE,
                    color = mutedColor,
                    modifier = Modifier.padding(start = STATUS_START_PADDING),
                )
            } else {
                Text(
                    text = RUNNING_LABEL,
                    fontSize = DETAIL_FONT_SIZE,
                    color = mutedColor,
                    modifier = Modifier.padding(start = STATUS_START_PADDING),
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
                            is ChatMessage.Assistant -> AssistantMessageBlock(
                                blocks = message.blocks,
                                modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                            )
                            is ChatMessage.SubAgentTask -> SubAgentTaskCard(
                                task = message,
                                depth = depth + 1,
                                modifier = Modifier.padding(vertical = INNER_VERTICAL_PADDING),
                            )
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
private const val RUNNING_LABEL = "· Running..."
private const val COMPLETE_LABEL = "· Complete"
private const val DEFAULT_TASK_LABEL = "Sub-agent Task"

private val HEADER_FONT_SIZE = 13.sp
private val DETAIL_FONT_SIZE = 12.sp
private val CARD_CORNER_RADIUS = 8.dp
private val CARD_PADDING = 12.dp
private val ACCENT_BORDER_WIDTH = 3.dp
private val ICON_SPACING = 6.dp
private val STATUS_START_PADDING = 8.dp
private val EXPANDED_TOP_PADDING = 8.dp
private val INNER_VERTICAL_PADDING = 4.dp
