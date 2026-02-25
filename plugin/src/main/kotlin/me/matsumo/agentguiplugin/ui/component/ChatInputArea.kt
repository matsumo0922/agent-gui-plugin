package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.ui.component.interaction.FileAttachPopup
import me.matsumo.agentguiplugin.util.PluginIcons
import me.matsumo.agentguiplugin.viewmodel.SessionState
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography
import javax.swing.Icon

@Composable
fun ChatInputArea(
    project: Project,
    sessionState: SessionState,
    attachedFiles: List<AttachedFile>,
    currentModel: Model,
    currentPermissionMode: PermissionMode,
    contextUsage: Float,
    totalInputTokens: Long,
    onAttach: (AttachedFile) -> Unit,
    onDetach: (AttachedFile) -> Unit,
    onSend: (String) -> Unit,
    onAbort: () -> Unit,
    onModelChange: (Model) -> Unit,
    onModeChange: (PermissionMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(TextFieldValue()) }

    fun send() {
        onSend(value.text.trim())
        value = TextFieldValue()
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = JewelTheme.globalColors.borders.disabled,
                shape = RoundedCornerShape(8.dp),
            )
    ) {
        TopSection(
            modifier = Modifier.fillMaxWidth(),
            project = project,
            attachedFiles = attachedFiles,
            onAttach = onAttach,
            onDetach = onDetach,
            contextUsage = contextUsage,
            totalInputTokens = totalInputTokens,
        )

        InputSection(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChanged = { newValue -> value = newValue },
            onSend = ::send,
        )

        BottomSection(
            modifier = Modifier.fillMaxWidth(),
            sessionState = sessionState,
            isInputEmpty = value.text.isEmpty(),
            currentModel = currentModel,
            currentPermissionMode = currentPermissionMode,
            onSend = ::send,
            onAbort = onAbort,
            onModelChange = onModelChange,
            onModeChange = onModeChange,
        )
    }
}

@Composable
private fun TopSection(
    project: Project,
    attachedFiles: List<AttachedFile>,
    onAttach: (AttachedFile) -> Unit,
    onDetach: (AttachedFile) -> Unit,
    contextUsage: Float,
    totalInputTokens: Long,
    modifier: Modifier = Modifier,
) {
    var showPopup by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .background(JewelTheme.colorPalette.gray(2))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            IconActionButton(
                key = AllIconsKeys.Actions.Attach,
                onClick = { showPopup = !showPopup },
                contentDescription = null,
            )

            if (showPopup) {
                FileAttachPopup(
                    project = project,
                    onFileSelected = { file ->
                        onAttach(file)
                        showPopup = false
                    },
                    onDismiss = { showPopup = false },
                )
            }
        }

        LazyRow(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(attachedFiles, key = { it.id }) { file ->
                AttachedFileChip(
                    file = file,
                    onRemove = { onDetach(file) },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ContextUsageIndicator(
                modifier = Modifier.size(14.dp),
                usage = contextUsage,
            )

            Text(
                text = "%,d tkn".format(totalInputTokens) + " (${(contextUsage * 100).toInt()}%)",
                style = JewelTheme.typography.small,
                color = JewelTheme.globalColors.text.info,
            )
        }
    }
}

@Composable
private fun ContextUsageIndicator(usage: Float, modifier: Modifier = Modifier) {
    val trackColor = JewelTheme.globalColors.borders.disabled
    val fillColor = when {
        usage < 0.7f -> JewelTheme.colorPalette.green(7)
        usage < 0.9f -> JewelTheme.colorPalette.yellow(7)
        else -> JewelTheme.colorPalette.red(7)
    }

    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val arcSize = size.minDimension - stroke
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSizeObj = Size(arcSize, arcSize)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSizeObj,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        if (usage > 0f) {
            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = 360f * usage,
                useCenter = false,
                topLeft = topLeft,
                size = arcSizeObj,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun InputSection(
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        modifier = modifier
            .background(JewelTheme.globalColors.panelBackground)
            .padding(8.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && event.isShiftPressed) {
                    onSend.invoke()
                    true
                } else {
                    false
                }
            },
        value = value,
        onValueChange = onValueChanged,
        textStyle = JewelTheme.typography.medium,
        cursorBrush = SolidColor(JewelTheme.typography.medium.color),
        minLines = 2,
        decorationBox = { innerTextField ->
            if (value.text.isEmpty()) {
                Text(
                    text = "Send a message (Shift+Enter)",
                    color = JewelTheme.globalColors.text.info,
                )
            }

            innerTextField.invoke()
        }
    )
}

@Composable
private fun BottomSection(
    sessionState: SessionState,
    isInputEmpty: Boolean,
    currentModel: Model,
    currentPermissionMode: PermissionMode,
    onSend: () -> Unit,
    onAbort: () -> Unit,
    onModelChange: (Model) -> Unit,
    onModeChange: (PermissionMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSend = (sessionState == SessionState.Ready || sessionState == SessionState.WaitingForInput) && !isInputEmpty
    val isProcessing = sessionState == SessionState.Processing

    Row(
        modifier = modifier
            .background(JewelTheme.globalColors.panelBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectorPopupButton(
            selectedText = currentModel.displayName,
            items = Model.entries,
            itemText = { it.displayName },
            itemDescription = { it.description },
            itemIcon = { PluginIcons.CLAUDE },
            onItemSelected = onModelChange,
        )

        SelectorPopupButton(
            selectedText = currentPermissionMode.displayName,
            items = PermissionMode.entries,
            itemText = { it.displayName },
            itemDescription = { it.description },
            itemIcon = { mode ->
                when (mode) {
                    PermissionMode.DEFAULT -> AllIcons.Actions.Lightning
                    PermissionMode.ACCEPT_EDITS -> AllIcons.Actions.Edit
                    PermissionMode.PLAN -> AllIcons.Actions.ListFiles
                    PermissionMode.BYPASS_PERMISSIONS -> AllIcons.General.Error
                }
            },
            onItemSelected = onModeChange,
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        IconActionButton(
            key = if (isProcessing) AllIconsKeys.Run.Stop else AllIconsKeys.Debugger.ThreadRunning,
            onClick = if (isProcessing) onAbort else onSend,
            contentDescription = null,
            enabled = isProcessing || canSend,
        )
    }
}

@Composable
private fun <T> SelectorPopupButton(
    selectedText: String,
    items: List<T>,
    itemText: (T) -> String,
    itemDescription: (T) -> String,
    itemIcon: (T) -> Icon,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        PopupButton(
            text = selectedText,
            onClick = { showPopup = !showPopup },
        )

        if (showPopup) {
            Popup(
                onDismissRequest = { showPopup = false },
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JewelTheme.colorPalette.gray(2))
                        .border(
                            width = 1.dp,
                            color = JewelTheme.globalColors.borders.disabled,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                ) {
                    items.forEach { item ->
                        PopupMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            text = itemText(item),
                            description = itemDescription(item),
                            icon = itemIcon(item),
                            onClick = {
                                onItemSelected(item)
                                showPopup = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .background(if (isHovered) JewelTheme.colorPalette.blue(1) else JewelTheme.colorPalette.gray(2))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = text,
        style = JewelTheme.typography.small,
        color = JewelTheme.globalColors.text.info,
    )
}

@Composable
private fun PopupMenuItem(
    text: String,
    description: String,
    icon: Icon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .hoverable(interactionSource)
            .clickable { onClick() }
            .background(if (isHovered) JewelTheme.colorPalette.blue(1) else JewelTheme.colorPalette.gray(2))
            .padding(8.dp, 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SwingIcon(
            modifier = Modifier.size(16.dp),
            icon = icon,
            contentDescription = null,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = text,
                style = JewelTheme.typography.small,
            )

            Text(
                text = description,
                style = JewelTheme.typography.small,
                color = JewelTheme.globalColors.text.info,
            )
        }
    }
}
