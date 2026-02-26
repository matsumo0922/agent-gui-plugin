package me.matsumo.agentguiplugin.ui.component.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.component.Button
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

private val warningColor = Color(0xFFF59E0B)

@Composable
fun AuthenticationCard(
    outputLines: List<String>,
    onSendInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = warningColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
            )
            .background(warningColor.copy(alpha = 0.04f))
            .padding(12.dp),
    ) {
        // Header
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "CLI Authentication Required",
                style = JewelTheme.typography.regular,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "The Claude CLI requires authentication before starting a session. Please complete the authentication below.",
                style = JewelTheme.typography.medium,
                color = JewelTheme.globalColors.text.info,
            )
        }

        // Output area
        val listState = rememberLazyListState()

        LaunchedEffect(outputLines.size) {
            if (outputLines.isNotEmpty()) {
                listState.animateScrollToItem(outputLines.size - 1)
            }
        }

        SelectionContainer {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(JewelTheme.globalColors.panelBackground)
                    .border(
                        width = 1.dp,
                        color = JewelTheme.globalColors.borders.normal,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(8.dp),
                state = listState,
            ) {
                items(outputLines) { line ->
                    Text(
                        text = line,
                        style = JewelTheme.typography.medium,
                    )
                }

                if (outputLines.isEmpty()) {
                    item {
                        Text(
                            text = "Waiting for CLI output...",
                            style = JewelTheme.typography.medium,
                            color = JewelTheme.globalColors.text.info,
                        )
                    }
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val textColor = JewelTheme.globalColors.text.normal

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = JewelTheme.globalColors.borders.normal,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(8.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && event.isShiftPressed) {
                            onSendInput(inputText)
                            inputText = ""
                            true
                        } else {
                            false
                        }
                    },
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = JewelTheme.typography.medium.copy(color = textColor),
                cursorBrush = SolidColor(textColor),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type response and press Send...",
                                fontSize = 12.sp,
                                color = JewelTheme.globalColors.text.info,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Button(
                text = "Send",
                onClick = {
                    if (inputText.isNotEmpty()) {
                        onSendInput(inputText)
                        inputText = ""
                    }
                },
                borderColor = warningColor.copy(alpha = 0.5f),
                backgroundColor = warningColor.copy(alpha = 0.15f),
                textColor = JewelTheme.globalColors.text.normal,
                enabled = inputText.isNotEmpty(),
            )
        }
    }
}
