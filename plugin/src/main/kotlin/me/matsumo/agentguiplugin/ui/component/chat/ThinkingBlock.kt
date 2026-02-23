package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ThinkingBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val background = ChatTheme.Thinking.background
    val borderColor = ChatTheme.Thinking.border
    val iconColor = ChatTheme.Thinking.iconDefault
    val textColor = ChatTheme.Thinking.text
    val mutedColor = ChatTheme.Text.muted
    val cornerRadius = ChatTheme.Radius.medium
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = background,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                )
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                            0f,
                        ),
                    ),
                )
            }
            .clickable { isExpanded = !isExpanded }
            .padding(12.dp)
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\uD83D\uDCA1",
                fontSize = 13.sp,
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = "Thinking",
                fontSize = 13.sp,
                color = iconColor,
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = if (isExpanded) "\u25BE" else "\u25B8",
                fontSize = 12.sp,
                color = mutedColor,
            )
        }

        if (isExpanded) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Text(
                text = text.lineSequence().firstOrNull() ?: "",
                fontSize = 13.sp,
                color = mutedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
