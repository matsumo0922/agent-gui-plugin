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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.component.Button
import me.matsumo.agentguiplugin.ui.component.LinkableText
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

private val warningColor = Color(0xFFF59E0B)
private val doneColor = Color(0xFF22C55E)

@Composable
fun AuthenticationCard(
    outputLines: List<String>,
    onSendInput: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }

    val sendInput = {
        if (inputText.isNotEmpty()) {
            onSendInput(inputText)
            inputText = ""
        }
    }

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
        val linkColor = JewelTheme.colorPalette.blue(9)
        val textStyle = JewelTheme.typography.medium.copy(color = JewelTheme.globalColors.text.normal)

        LaunchedEffect(outputLines.size) {
            if (outputLines.isNotEmpty()) {
                listState.animateScrollToItem(outputLines.size - 1)
            }
        }

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
                LinkableText(
                    text = line,
                    style = textStyle,
                    linkColor = linkColor,
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

        // Input + Done row
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val textColor = JewelTheme.globalColors.text.normal

            // Text field with inline send icon
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = JewelTheme.globalColors.borders.normal,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = JewelTheme.typography.medium.copy(color = textColor),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { sendInput() },
                    ),
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
            }

            // Done button
            Button(
                text = "Done",
                onClick = onDone,
                borderColor = doneColor.copy(alpha = 0.5f),
                backgroundColor = doneColor.copy(alpha = 0.15f),
                textColor = JewelTheme.globalColors.text.normal,
            )
        }
    }
}
