package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun ThinkingBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

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
            Text(
                text = "Thinking",
                style = JewelTheme.typography.regular,
                color = JewelTheme.globalColors.text.info,
            )

            Text(
                text = if (isExpanded) "\u25BE" else "\u25B8",
                style = JewelTheme.typography.regular,
                color = JewelTheme.globalColors.text.info,
            )
        }

        if (isExpanded) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Divider(
                    modifier = Modifier.fillMaxHeight(),
                    orientation = Orientation.Vertical,
                    thickness = 2.dp,
                )

                SelectionContainer(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text,
                        style = JewelTheme.typography.medium,
                        color = JewelTheme.globalColors.text.info,
                    )
                }
            }
        }
    }
}
