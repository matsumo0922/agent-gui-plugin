package me.matsumo.agentguiplugin.ui.component.interaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import me.matsumo.claude.agent.types.PermissionMode
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

private val allowColor = Color(0xFF22C55E)
private val denyColor = Color(0xFFEF4444)

@Composable
fun ExitPlanCard(
    permission: PendingPermission,
    project: Project,
    onAllow: (PermissionMode) -> Unit,
    onDeny: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMessageField by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // プラン内容を IntelliJ Editor で表示
    LaunchedEffect(permission) {
        val planContent = permission.toolInput["plan"]?.toString()
        if (!planContent.isNullOrBlank()) {
            ApplicationManager.getApplication().invokeLater {
                val virtualFile = LightVirtualFile("Plan.md", planContent)
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = allowColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
            )
            .background(allowColor.copy(alpha = 0.04f))
            .padding(12.dp),
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Plan Review",
                style = JewelTheme.typography.regular,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Claude wants to exit plan mode. Please review the plan in the editor and approve or deny.",
                style = JewelTheme.typography.medium,
                color = JewelTheme.globalColors.text.info,
            )
        }

        // Message field (deny reason)
        AnimatedVisibility(showMessageField) {
            MessageField(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                value = message,
                onValueChange = { message = it },
            )
        }

        // Buttons
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
                text = "Allow",
                onClick = { onAllow(PermissionMode.DEFAULT) },
                borderColor = allowColor.copy(alpha = 0.5f),
                backgroundColor = allowColor.copy(alpha = 0.15f),
                textColor = JewelTheme.globalColors.text.normal,
            )

            _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
                text = "Allow (Accept Edits)",
                onClick = { onAllow(PermissionMode.ACCEPT_EDITS) },
                borderColor = allowColor.copy(alpha = 0.5f),
                backgroundColor = allowColor.copy(alpha = 0.15f),
                textColor = JewelTheme.globalColors.text.normal,
            )

            Spacer(
                modifier = Modifier.weight(1f),
            )

            _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
                text = "Deny",
                onClick = { onDeny(message) },
                borderColor = denyColor.copy(alpha = 0.5f),
                backgroundColor = denyColor.copy(alpha = 0.15f),
                textColor = JewelTheme.globalColors.text.normal,
            )

            _root_ide_package_.me.matsumo.agentguiplugin.ui.component.Button(
                text = "Message",
                onClick = { showMessageField = true },
                borderColor = JewelTheme.globalColors.borders.normal,
                textColor = JewelTheme.globalColors.text.info,
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
                shape = RoundedCornerShape(4.dp),
            )
            .padding(8.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = JewelTheme.typography.medium.copy(
            color = textColor,
        ),
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            androidx.compose.foundation.layout.Box {
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
