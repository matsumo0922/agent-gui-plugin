package me.matsumo.agentguiplugin.ui.component.chat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import me.matsumo.agentguiplugin.ui.component.Button
import me.matsumo.agentguiplugin.ui.theme.IdeaTheme

@Composable
fun UserMessageEditBubble(
    initialText: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(TextFieldValue(initialText)) }

    Column(
        modifier = modifier
            .clip(shape)
            .background(IdeaTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = IdeaTheme.colorScheme.outline,
                shape = shape,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { event ->
                    when (event.type) {
                        KeyEventType.KeyDown if event.key == Key.Enter && event.isShiftPressed -> {
                            if (value.text.isNotBlank()) onConfirm(value.text.trim())
                            true
                        }
                        KeyEventType.KeyDown if event.key == Key.Escape -> {
                            onCancel()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                },
            value = value,
            onValueChange = { value = it },
            textStyle = IdeaTheme.typography.bodyMedium,
            cursorBrush = SolidColor(IdeaTheme.typography.bodyMedium.color),
            minLines = 2,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.End,
            ),
        ) {
            Button(
                text = "Cancel",
                onClick = onCancel,
                borderColor = IdeaTheme.colorScheme.outline,
            )

            Button(
                text = "Send",
                onClick = { if (value.text.isNotBlank()) onConfirm(value.text.trim()) },
                borderColor = IdeaTheme.colorScheme.outline,
                backgroundColor = IdeaTheme.colorScheme.primary,
                enabled = value.text.isNotBlank(),
            )
        }
    }
}
