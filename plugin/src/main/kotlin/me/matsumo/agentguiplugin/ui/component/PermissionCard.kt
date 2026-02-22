package me.matsumo.agentguiplugin.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

private val warningColor = Color(0xFFF59E0B)
private val allowColor = Color(0xFF22C55E)
private val denyColor = Color(0xFFEF4444)

@Composable
fun PermissionCard(
    permission: PendingPermission,
    onAllow: () -> Unit,
    onDeny: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMessageField by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = warningColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
            )
            .background(warningColor.copy(alpha = 0.04f))
            .padding(12.dp),
    ) {
        HeaderSection(
            modifier = Modifier.fillMaxWidth(),
            permission = permission,
        )

        InputParamSection(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            permission = permission,
        )

        AnimatedVisibility(showMessageField) {
            MessageField(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                value = message,
                onValueChange = { message = it },
            )
        }

        ButtonSection(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            onAllow = onAllow,
            onDeny = { onDeny(message) },
            onMessage = {
                showMessageField = true
            },
        )
    }
}

@Composable
private fun HeaderSection(
    permission: PendingPermission,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Tool Permission Request",
            style = JewelTheme.typography.regular,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = buildAnnotatedString {
                append("Tool: ")

                withStyle(
                    JewelTheme.typography.medium.copy(
                        color = JewelTheme.colorPalette.blue(9),
                        fontWeight = FontWeight.SemiBold,
                    ).toSpanStyle()
                ) {
                    append(permission.toolName)
                }
            },
            style = JewelTheme.typography.medium,
            color = JewelTheme.globalColors.text.info,
        )
    }
}

@Composable
private fun InputParamSection(
    permission: PendingPermission,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(JewelTheme.colorPalette.gray(2))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        permission.toolInput.entries.take(10).forEach { (key, value) ->
            Text(
                text = buildAnnotatedString {
                    append("$key: ")

                    withStyle(
                        JewelTheme.typography.medium.copy(
                            color = JewelTheme.globalColors.text.normal,
                        ).toSpanStyle()
                    ) {
                        append(value?.toString()?.take(300) ?: "null")
                    }
                },
                style = JewelTheme.typography.medium,
                color = JewelTheme.globalColors.text.info,
            )
        }
    }
}

@Composable
private fun MessageField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = JewelTheme.globalColors.text.info

    BasicTextField(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = JewelTheme.globalColors.borders.normal,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = JewelTheme.typography.medium.copy(
            color = textColor,
        ),
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = "Reason for denial (optional)...",
                        fontSize = 12.sp,
                        color = textColor,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun ButtonSection(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    onMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            text = "Allow",
            onClick = onAllow,
            borderColor = allowColor.copy(alpha = 0.5f),
            backgroundColor = allowColor.copy(alpha = 0.15f),
            textColor = JewelTheme.globalColors.text.normal,
        )

        Button(
            text = "Deny",
            onClick = onDeny,
            borderColor = denyColor.copy(alpha = 0.5f),
            backgroundColor = denyColor.copy(alpha = 0.15f),
            textColor = JewelTheme.globalColors.text.normal,
        )

        Spacer(
            modifier = Modifier.weight(1f),
        )

        Button(
            text = "Message",
            onClick = onMessage,
            borderColor = JewelTheme.globalColors.borders.normal,
            textColor = JewelTheme.globalColors.text.info,
        )
    }
}
