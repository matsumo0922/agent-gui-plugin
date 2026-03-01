package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.ui.theme.IdeaTheme
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

val LocalSubAgentDepth = compositionLocalOf { 0 }

private const val MAX_DEPTH = 4

@Composable
fun SubAgentTaskCard(
    task: SubAgentTask,
    subAgentTasks: Map<String, SubAgentTask>,
    project: Project,
    modifier: Modifier = Modifier,
) {
    val depth = LocalSubAgentDepth.current
    val canExpand = depth < MAX_DEPTH && task.messages.isNotEmpty()

    var isPopupVisible by remember { mutableStateOf(false) }
    val messageCount = task.messages.size

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (canExpand) Modifier.clickable { isPopupVisible = !isPopupVisible } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            key = AllIconsKeys.Actions.Find,
            tint = if (canExpand) IdeaTheme.colorScheme.onSurfaceVariant else IdeaTheme.colorScheme.onSurfaceDisabled,
            contentDescription = null,
        )

        Text(
            text = "SubAgent",
            style = IdeaTheme.typography.bodyMedium,
            color = if (canExpand) IdeaTheme.colorScheme.onSurfaceVariant else IdeaTheme.colorScheme.onSurfaceDisabled,
            fontWeight = FontWeight.SemiBold,
        )

        if (messageCount > 0) {
            Text(
                text = "($messageCount messages)",
                style = IdeaTheme.typography.bodyMedium,
                color = IdeaTheme.colorScheme.onSurfaceDisabled,
            )
        }

        val elapsedText = remember(task.startedAt, task.completedAt) {
            val start = task.startedAt ?: return@remember null
            val end = task.completedAt ?: return@remember null
            val ms = end - start
            if (ms < 1000) "${ms}ms" else "%.1fs".format(ms / 1000.0)
        }

        if (elapsedText != null) {
            Text(
                text = "($elapsedText)",
                style = IdeaTheme.typography.bodyMedium,
                color = IdeaTheme.colorScheme.onSurfaceDisabled,
            )
        }

        if (canExpand) {
            Text(
                text = if (isPopupVisible) "\u25BE" else "\u25B8",
                style = IdeaTheme.typography.bodyLarge,
                color = IdeaTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (isPopupVisible && canExpand) {
        Popup(
            onDismissRequest = { isPopupVisible = false },
            properties = PopupProperties(focusable = true),
            alignment = Alignment.TopStart,
        ) {
            val shape = RoundedCornerShape(8.dp)

            CompositionLocalProvider(LocalSubAgentDepth provides depth + 1) {
                ChatMessageList(
                    modifier = Modifier
                        .size(400.dp, 500.dp)
                        .shadow(elevation = 8.dp, shape = shape)
                        .clip(shape)
                        .border(
                            width = 1.dp,
                            color = IdeaTheme.colorScheme.outline,
                            shape = shape,
                        )
                        .background(IdeaTheme.colorScheme.background)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                isPopupVisible = false
                                true
                            } else {
                                false
                            }
                        },
                    messages = task.messages,
                    subAgentTasks = subAgentTasks,
                    toolResults = task.toolResults,
                    editInfoMap = emptyMap(),
                    canInteract = false,
                    project = project,
                    onEdit = { _, _ -> },
                    onNavigateVersion = { _, _ -> },
                )
            }
        }
    }
}
