package me.matsumo.agentguiplugin.ui.component.chat

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
import java.util.*

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
            .padding(BLOCK_PADDING)
            .animateContentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = TOOL_ICON, fontSize = TOOL_NAME_FONT_SIZE)

            Spacer(Modifier.width(ICON_SPACING))

            Text(
                text = toolName,
                fontWeight = FontWeight.SemiBold,
                fontSize = TOOL_NAME_FONT_SIZE,
                color = toolNameColor,
            )

            if (elapsed != null) {
                Text(
                    text = " ${String.format(Locale.US, ELAPSED_FORMAT, elapsed)}s",
                    fontSize = DETAIL_FONT_SIZE,
                    color = mutedColor,
                    modifier = Modifier.padding(start = LABEL_START_PADDING),
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = if (isExpanded) ARROW_DOWN else ARROW_RIGHT,
                fontSize = DETAIL_FONT_SIZE,
                color = mutedColor,
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .padding(top = EXPANDED_TOP_PADDING)
                    .heightIn(max = EXPANDED_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState()),
            ) {
                inputJson.entries.forEach { (key, value) ->
                    Row(modifier = Modifier.padding(vertical = PARAM_VERTICAL_PADDING)) {
                        Text(
                            text = "$key: ",
                            fontSize = DETAIL_FONT_SIZE,
                            color = paramKeyColor,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = value.toString(),
                            fontSize = DETAIL_FONT_SIZE,
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
                    fontSize = DETAIL_FONT_SIZE,
                    fontFamily = FontFamily.Monospace,
                    color = mutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = SUMMARY_TOP_PADDING),
                )
            }
        }
    }
}

private fun buildInputSummary(input: JsonObject): String =
    input.entries.take(SUMMARY_MAX_KEYS).joinToString(", ") { (key, value) ->
        "$key: ${value.toString().take(SUMMARY_MAX_VALUE_LENGTH)}"
    }

private const val TOOL_ICON = "\uD83D\uDD27"
private const val ARROW_DOWN = "\u25BE"
private const val ARROW_RIGHT = "\u25B8"
private const val ELAPSED_FORMAT = "%.1f"

private val TOOL_NAME_FONT_SIZE = 13.sp
private val DETAIL_FONT_SIZE = 12.sp
private val BLOCK_PADDING = 12.dp
private val ICON_SPACING = 6.dp
private val LABEL_START_PADDING = 8.dp
private val EXPANDED_TOP_PADDING = 8.dp
private val EXPANDED_MAX_HEIGHT = 200.dp
private val PARAM_VERTICAL_PADDING = 2.dp
private val SUMMARY_TOP_PADDING = 4.dp

private const val SUMMARY_MAX_KEYS = 3
private const val SUMMARY_MAX_VALUE_LENGTH = 80
