package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.intellij.openapi.ide.CopyPasteManager
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.component.SwingIcon
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserMessageBubble(
    text: String,
    attachedFiles: List<AttachedFile>,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    var showEditor by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (!showEditor) {
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(JewelTheme.colorPalette.gray(1))
                    .border(
                        width = 1.dp,
                        color = JewelTheme.globalColors.borders.normal,
                        shape = shape
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MarkdownText(
                    text = text
                )

                if (attachedFiles.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        attachedFiles.forEach { file ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(JewelTheme.colorPalette.gray(2))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                SwingIcon(
                                    modifier = Modifier.size(14.dp),
                                    icon = file.icon,
                                    contentDescription = null,
                                )

                                Text(
                                    text = file.name,
                                    style = JewelTheme.typography.small,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            UserMessageFooter(
                text = text,
                onEdit = {
                    showEditor = true
                },
            )
        } else {
            UserMessageEditBubble(
                modifier = Modifier.fillMaxWidth(),
                initialText = text,
                shape = shape,
                onCancel = {
                    showEditor = false
                },
                onConfirm = { newText ->
                    onEdit(newText)
                    showEditor = false
                }
            )
        }
    }
}

@Composable
private fun UserMessageFooter(
    text: String,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconActionButton(
            key = AllIconsKeys.Actions.Edit,
            onClick = {
                onEdit(text)
            },
            contentDescription = "編集",
        )

        IconActionButton(
            key = AllIconsKeys.Actions.Copy,
            onClick = {
                CopyPasteManager.getInstance()
                    .setContents(StringSelection(text))
            },
            contentDescription = "出力をコピー",
        )
    }
}
