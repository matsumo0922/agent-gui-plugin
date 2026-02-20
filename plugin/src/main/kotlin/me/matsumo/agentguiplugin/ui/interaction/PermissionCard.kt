package me.matsumo.agentguiplugin.ui.interaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import org.jetbrains.jewel.ui.component.Text

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
    var showDenyMessage by remember(permission) { mutableStateOf(false) }
    var denyMessage by remember(permission) { mutableStateOf("") }

    val cardShape = RoundedCornerShape(ChatTheme.Radius.large)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, warningColor.copy(alpha = 0.4f), cardShape)
            .background(warningColor.copy(alpha = 0.04f), cardShape),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "⚠", fontSize = 13.sp, color = warningColor)
            Text(
                text = "Tool Permission Request",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChatTheme.Text.primary,
            )
        }

        // Tool name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Tool:", fontSize = 11.sp, color = ChatTheme.Text.muted)
            Text(
                text = permission.toolName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ChatTheme.ToolUse.toolNameColor,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Input parameters
        if (permission.toolInput.isNotEmpty()) {
            val paramKeyColor = ChatTheme.ToolUse.paramKeyColor
            val paramValColor = ChatTheme.ToolUse.paramValueColor
            val codeBackground = ChatTheme.Background.muted

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
                    .background(codeBackground, RoundedCornerShape(ChatTheme.Radius.medium))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                permission.toolInput.entries.take(10).forEach { (key, value) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "$key:",
                            fontSize = 11.sp,
                            color = paramKeyColor,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = value?.toString()?.take(300) ?: "null",
                            fontSize = 11.sp,
                            color = paramValColor,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }

        // Expandable deny message input
        AnimatedVisibility(visible = showDenyMessage) {
            val textColor = ChatTheme.Text.primary
            val placeholderColor = ChatTheme.Input.placeholder
            val borderColor = ChatTheme.Border.default

            BasicTextField(
                value = denyMessage,
                onValueChange = { denyMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(ChatTheme.Radius.medium))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = textColor),
                cursorBrush = SolidColor(textColor),
                decorationBox = { innerTextField ->
                    Box {
                        if (denyMessage.isEmpty()) {
                            Text(
                                text = "Reason for denial (optional)...",
                                fontSize = 12.sp,
                                color = placeholderColor,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Allow button
            Box(
                modifier = Modifier
                    .background(allowColor.copy(alpha = 0.12f), RoundedCornerShape(ChatTheme.Radius.medium))
                    .border(1.dp, allowColor.copy(alpha = 0.5f), RoundedCornerShape(ChatTheme.Radius.medium))
                    .clickable(onClick = onAllow)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Allow",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = allowColor,
                )
            }

            // Deny button
            Box(
                modifier = Modifier
                    .background(denyColor.copy(alpha = 0.08f), RoundedCornerShape(ChatTheme.Radius.medium))
                    .border(1.dp, denyColor.copy(alpha = 0.4f), RoundedCornerShape(ChatTheme.Radius.medium))
                    .clickable {
                        val msg = if (showDenyMessage && denyMessage.isNotBlank()) {
                            denyMessage
                        } else {
                            "Denied by user"
                        }
                        onDeny(msg)
                    }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Deny",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = denyColor,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Toggle deny-with-message
            val toggleBorder = ChatTheme.Border.default
            Box(
                modifier = Modifier
                    .border(1.dp, toggleBorder, RoundedCornerShape(ChatTheme.Radius.medium))
                    .clickable { showDenyMessage = !showDenyMessage }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (showDenyMessage) "Hide message" else "+ Add message",
                    fontSize = 11.sp,
                    color = ChatTheme.Text.muted,
                )
            }
        }
    }
}
