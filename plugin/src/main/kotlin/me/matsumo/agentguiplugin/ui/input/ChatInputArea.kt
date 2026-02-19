package me.matsumo.agentguiplugin.ui.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.SessionState
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

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val gradientAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
    )

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                ChatTheme.Input.gradientStart,
                ChatTheme.Input.gradientMid,
                ChatTheme.Input.gradientEnd,
            ),
        )
    }

    val inputBackground = ChatTheme.Input.background
    val inputBorder = ChatTheme.Input.border
    val containerShape = RoundedCornerShape(ChatTheme.Radius.large)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (gradientAlpha > 0f) {
                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(-1.dp.toPx(), -1.dp.toPx()),
                            size = Size(
                                size.width + 2.dp.toPx(),
                                size.height + 2.dp.toPx(),
                            ),
                            cornerRadius = CornerRadius(
                                ChatTheme.Radius.large.toPx() + 1.dp.toPx(),
                            ),
                            alpha = gradientAlpha,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                }
                .background(inputBackground, containerShape)
                .border(1.dp, inputBorder, containerShape),
        ) {
            // TextArea
            TextArea(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onInputChanged(it.text)
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 200.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown &&
                            event.key == Key.Enter &&
                            event.isShiftPressed
                        ) {
                            if (canSend && inputText.isNotBlank()) {
                                onSend()
                            }
                            true
                        } else {
                            false
                        }
                    },
                placeholder = {
                    Text(
                        text = when (sessionState) {
                            SessionState.Disconnected -> "Connecting..."
                            SessionState.Connecting -> "Connecting..."
                            SessionState.Ready -> "Send a message (Shift+Enter)"
                            SessionState.Streaming -> "Claude is responding..."
                            SessionState.WaitingForInput -> "Send a message (Shift+Enter)"
                            SessionState.Error -> "Error occurred. Try reconnecting."
                        },
                        color = ChatTheme.Input.placeholder,
                    )
                },
                enabled = canSend,
            )

            // Bottom controls row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: character count badge
                if (inputText.isNotEmpty()) {
                    Text(
                        text = "${inputText.length} chars",
                        fontSize = 10.sp,
                        color = ChatTheme.Text.muted,
                        modifier = Modifier
                            .background(
                                ChatTheme.Background.muted,
                                RoundedCornerShape(ChatTheme.Radius.small),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                } else {
                    Spacer(Modifier)
                }

                // Right side: Send/Stop button
                if (isStreaming) {
                    StopButton(onClick = onAbort)
                } else {
                    SendButton(
                        onClick = onSend,
                        enabled = canSend && inputText.isNotBlank(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val enabledBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                ChatTheme.Input.sendGradientStart,
                ChatTheme.Input.sendGradientMid,
                ChatTheme.Input.sendGradientEnd,
            ),
        )
    }
    val disabledBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                ChatTheme.Input.sendGradientStart.copy(alpha = 0.4f),
                ChatTheme.Input.sendGradientEnd.copy(alpha = 0.4f),
            ),
        )
    }
    val gradientBrush = if (enabled) enabledBrush else disabledBrush

    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .background(gradientBrush, RoundedCornerShape(ChatTheme.Radius.medium))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Send",
            color = ChatTheme.Text.onPrimary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun StopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .background(
                ChatTheme.Background.secondary,
                RoundedCornerShape(ChatTheme.Radius.medium),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u23F9 Stop",
            color = ChatTheme.Text.primary,
            fontSize = 13.sp,
        )
    }
}
