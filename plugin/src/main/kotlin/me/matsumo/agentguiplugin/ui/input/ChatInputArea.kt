package me.matsumo.agentguiplugin.ui.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextArea

@Composable
fun ChatInputArea(
    inputText: String,
    sessionState: SessionState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onAbort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSend = sessionState == SessionState.Ready ||
        sessionState == SessionState.WaitingForInput

    val isStreaming = sessionState == SessionState.Streaming

    var textFieldValue by remember { mutableStateOf(TextFieldValue(inputText)) }

    // Sync when inputText changes externally (e.g., cleared after send)
    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        TextArea(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onInputChanged(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.Enter &&
                        event.isShiftPressed
                    ) {
                        if (canSend) {
                            onSend()
                        }
                        true
                    } else {
                        false
                    }
                },
            placeholder = {
                Text(
                    when (sessionState) {
                        SessionState.Disconnected -> "Connecting..."
                        SessionState.Connecting -> "Connecting..."
                        SessionState.Ready -> "Send a message (Shift+Enter)"
                        SessionState.Streaming -> "Claude is responding..."
                        SessionState.WaitingForInput -> "Send a message (Shift+Enter)"
                        SessionState.Error -> "Error occurred. Try reconnecting."
                    },
                )
            },
            enabled = canSend,
        )

        if (isStreaming) {
            DefaultButton(
                onClick = onAbort,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Stop")
            }
        } else {
            DefaultButton(
                onClick = onSend,
                enabled = canSend && inputText.isNotBlank(),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Send")
            }
        }
    }
}
