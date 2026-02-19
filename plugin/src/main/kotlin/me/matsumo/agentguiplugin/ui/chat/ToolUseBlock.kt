package me.matsumo.agentguiplugin.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonObject
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ToolUseBlock(
    toolName: String,
    inputJson: JsonObject,
    elapsed: Double? = null,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(ChatTheme.Radius.medium)
    val toolNameColor = ChatTheme.ToolUse.toolNameColor
    val paramKeyColor = ChatTheme.ToolUse.paramKeyColor
    val paramValueColor = ChatTheme.ToolUse.paramValueColor
    val mutedColor = ChatTheme.Text.muted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = ChatTheme.ToolUse.background, shape = shape)
            .border(1.dp, ChatTheme.ToolUse.border, shape)
            .clickable { isExpanded = !isExpanded }
            .padding(12.dp)
            .animateContentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "\uD83D\uDD27", fontSize = 13.sp)

            Spacer(Modifier.width(6.dp))

            Text(
                text = toolName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = toolNameColor,
            )

            if (elapsed != null) {
                Text(
                    text = " ${String.format("%.1f", elapsed)}s",
                    fontSize = 12.sp,
                    color = mutedColor,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = if (isExpanded) "\u25BE" else "\u25B8",
                fontSize = 12.sp,
                color = mutedColor,
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                inputJson.entries.forEach { (key, value) ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "$key: ",
                            fontSize = 12.sp,
                            color = paramKeyColor,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = value.toString(),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = paramValueColor,
                        )
                    }
                }
            }
        } else {
            val summary = buildInputSummary(inputJson)
            if (summary.isNotEmpty()) {
                Text(
                    text = summary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = mutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun buildInputSummary(input: JsonObject): String =
    input.entries.take(3).joinToString(", ") { (key, value) ->
        "$key: ${value.toString().take(80)}"
    }
