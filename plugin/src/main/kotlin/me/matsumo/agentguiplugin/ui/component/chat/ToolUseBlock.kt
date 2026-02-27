package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

@Composable
fun ToolUseBlock(
    name: String,
    inputJson: JsonObject,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val inputMap = remember(inputJson) { inputJson.mapValues { it.value.toString() } }

    Column(
        modifier = modifier
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                key = AllIconsKeys.Actions.Find,
                tint = JewelTheme.globalColors.text.info,
                contentDescription = null,
            )

            Text(
                text = name,
                style = JewelTheme.typography.medium,
                color = JewelTheme.globalColors.text.info,
                fontWeight = FontWeight.SemiBold,
            )

            if (!isExpanded) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "(${inputMap.toList().joinToString { "${it.first}: ${it.second}" }})",
                    style = JewelTheme.typography.medium,
                    color = JewelTheme.globalColors.text.disabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = if (isExpanded) "\u25BE" else "\u25B8",
                style = JewelTheme.typography.regular,
                color = JewelTheme.globalColors.text.info,
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(delayMillis = 200)),
            exit = fadeOut(tween(100)),
        ) {
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(JewelTheme.colorPalette.gray(2))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    inputMap.forEach { (key, value) ->
                        Text(
                            text = buildAnnotatedString {
                                append("$key: ")

                                withStyle(
                                    JewelTheme.typography.medium.copy(
                                        color = JewelTheme.globalColors.text.normal,
                                    ).toSpanStyle()
                                ) {
                                    append(value.take(300))
                                }
                            },
                            style = JewelTheme.typography.medium,
                            color = JewelTheme.globalColors.text.info,
                        )
                    }
                }
            }
        }
    }
}
