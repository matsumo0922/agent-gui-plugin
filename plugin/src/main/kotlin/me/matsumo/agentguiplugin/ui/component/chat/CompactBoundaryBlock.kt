package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.theme.IdeaTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun CompactBoundaryBlock(
    modifier: Modifier = Modifier,
    preTokens: Int = 0,
    trigger: String = "manual",
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(IdeaTheme.colorScheme.outlineVariant),
            )

            Text(
                text = buildString {
                    append("Conversation compacted")
                    if (preTokens > 0) append(" (%,d tkn)".format(preTokens))
                    append(if (isExpanded) " \u25BE" else " \u25B8")
                },
                style = IdeaTheme.typography.bodySmall,
                color = IdeaTheme.colorScheme.onSurfaceDisabled,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(IdeaTheme.colorScheme.outlineVariant),
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(delayMillis = 200)),
            exit = fadeOut(tween(100)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IdeaTheme.colorScheme.surfaceContainer)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Trigger: $trigger",
                    style = IdeaTheme.typography.bodyMedium,
                    color = IdeaTheme.colorScheme.onSurfaceVariant,
                )

                if (preTokens > 0) {
                    Text(
                        text = "Pre-compact tokens: %,d".format(preTokens),
                        style = IdeaTheme.typography.bodyMedium,
                        color = IdeaTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
