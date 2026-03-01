package me.matsumo.agentguiplugin.ui.component.interaction

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.ui.theme.IdeaTheme
import me.matsumo.agentguiplugin.ui.util.openDiffInIde
import me.matsumo.agentguiplugin.ui.util.openFilePreviewInIde
import me.matsumo.agentguiplugin.viewmodel.EditDiffInfo
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import me.matsumo.agentguiplugin.viewmodel.permission.ToolNames
import org.jetbrains.jewel.ui.component.Text

private val warningColor = Color(0xFFF59E0B)
private val allowColor = Color(0xFF22C55E)
private val denyColor = Color(0xFFEF4444)

@Composable
fun PermissionCard(
    permission: PendingPermission,
    project: Project,
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

        InputOrDiffSection(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            permission = permission,
            project = project,
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
            style = IdeaTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = buildAnnotatedString {
                append("Tool: ")

                withStyle(
                    IdeaTheme.typography.bodyMedium.copy(
                        color = IdeaTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    ).toSpanStyle()
                ) {
                    append(permission.toolName)
                }
            },
            style = IdeaTheme.typography.bodyMedium,
            color = IdeaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InputOrDiffSection(
    permission: PendingPermission,
    project: Project,
    modifier: Modifier = Modifier,
) {
    val filePath = permission.toolInput["file_path"]?.toString() ?: ""

    when (permission.toolName) {
        in ToolNames.EDIT_TOOL_NAMES -> {
            val oldString = permission.toolInput["old_string"]?.toString() ?: ""
            val newString = permission.toolInput["new_string"]?.toString() ?: ""
            val fileName = filePath.substringAfterLast('/')

            LaunchedEffect(permission) {
                val diffInfo = EditDiffInfo(
                    filePath = filePath,
                    oldString = oldString,
                    newString = newString,
                )
                openDiffInIde(project, diffInfo, fileName)
            }

            FilePathLabel(
                modifier = modifier,
                filePath = filePath,
            )
        }
        in ToolNames.WRITE_TOOL_NAMES -> {
            val content = permission.toolInput["content"]?.toString() ?: ""

            LaunchedEffect(permission) {
                openFilePreviewInIde(project, filePath, content)
            }

            FilePathLabel(
                modifier = modifier,
                filePath = filePath,
            )
        }
        else -> {
            InputParamSection(
                modifier = modifier,
                permission = permission,
            )
        }
    }
}

@Composable
private fun FilePathLabel(
    filePath: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(IdeaTheme.colorScheme.surfaceContainer)
            .padding(8.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append("file_path: ")

                withStyle(
                    IdeaTheme.typography.bodyMedium.copy(
                        color = IdeaTheme.colorScheme.onSurface,
                    ).toSpanStyle()
                ) {
                    append(filePath)
                }
            },
            style = IdeaTheme.typography.bodyMedium,
            color = IdeaTheme.colorScheme.onSurfaceVariant,
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
            .background(IdeaTheme.colorScheme.surfaceContainer)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        permission.toolInput.entries.take(10).forEach { (key, value) ->
            Text(
                text = buildAnnotatedString {
                    append("$key: ")

                    withStyle(
                        IdeaTheme.typography.bodyMedium.copy(
                            color = IdeaTheme.colorScheme.onSurface,
                        ).toSpanStyle()
                    ) {
                        append(value?.toString()?.take(300) ?: "null")
                    }
                },
                style = IdeaTheme.typography.bodyMedium,
                color = IdeaTheme.colorScheme.onSurfaceVariant,
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
    val textColor = IdeaTheme.colorScheme.onSurfaceVariant

    BasicTextField(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = IdeaTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = IdeaTheme.typography.bodyMedium.copy(
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
        _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
            text = "Allow",
            onClick = onAllow,
            borderColor = allowColor.copy(alpha = 0.5f),
            backgroundColor = allowColor.copy(alpha = 0.15f),
            textColor = IdeaTheme.colorScheme.onSurface,
        )

        _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
            text = "Deny",
            onClick = onDeny,
            borderColor = denyColor.copy(alpha = 0.5f),
            backgroundColor = denyColor.copy(alpha = 0.15f),
            textColor = IdeaTheme.colorScheme.onSurface,
        )

        Spacer(
            modifier = Modifier.weight(1f),
        )

        _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
            text = "Message",
            onClick = onMessage,
            borderColor = IdeaTheme.colorScheme.outline,
            textColor = IdeaTheme.colorScheme.onSurfaceVariant,
        )
    }
}
