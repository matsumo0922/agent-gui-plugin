package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

@Composable
fun ChatInputArea(
    project: Project,
    sessionState: SessionState,
    attachedFiles: List<AttachedFile>,
    onAttach: (AttachedFile) -> Unit,
    onDetach: (AttachedFile) -> Unit,
    onSend: (String) -> Unit,
    onAbort: () -> Unit,
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
            contextUses = 0.62f,
        )

        InputSection(
            modifier = Modifier.fillMaxWidth(),
            sessionState = sessionState,
            value = value,
            onValueChanged = { newValue -> value = newValue },
            onSend = ::send,
        )

        BottomSection(
            modifier = Modifier.fillMaxWidth(),
            sessionState = sessionState,
            isInputEmpty = value.text.isEmpty(),
            onSend = ::send,
            onAbort = onAbort,
        )
    }
}

@Composable
private fun TopSection(
    project: Project,
    attachedFiles: List<AttachedFile>,
    onAttach: (AttachedFile) -> Unit,
    onDetach: (AttachedFile) -> Unit,
    contextUses: Float,
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
            Text(
                text = "${(contextUses * 100).toInt()}%",
                style = JewelTheme.typography.small,
                color = JewelTheme.globalColors.text.info
            )
        }
    }
}

@Composable
private fun InputSection(
    sessionState: SessionState,
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
    onSend: () -> Unit,
    onAbort: () -> Unit,
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
        // TODO: モデル変更の DropDown

        // TODO: モード切り替え（Auto, Accept Edits, Plan Mode, Bypass Permissions）の DropDown

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
