package me.matsumo.agentguiplugin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun AgentGuiSettingsPanel(
    cliPath: String,
    onCliPathChange: (String) -> Unit,
) {
    val borderColor = JewelTheme.globalColors.borders.normal
    val textColor = JewelTheme.globalColors.text.normal
    val placeholderColor = JewelTheme.globalColors.text.disabled
    val bgColor = JewelTheme.globalColors.panelBackground

    Column(
        modifier = Modifier
            .width(600.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Claude CLI Path",
            style = JewelTheme.typography.medium,
            color = textColor,
        )

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Text field
            val shape = RoundedCornerShape(4.dp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, borderColor, shape)
                    .clip(shape)
                    .background(bgColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (cliPath.isEmpty()) {
                    Text(
                        text = "Auto detect (leave empty)",
                        style = JewelTheme.typography.medium,
                        color = placeholderColor,
                    )
                }

                BasicTextField(
                    value = cliPath,
                    onValueChange = onCliPathChange,
                    singleLine = true,
                    textStyle = JewelTheme.typography.medium.copy(color = textColor),
                    cursorBrush = SolidColor(textColor),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Browse button
            SettingsButton(
                text = "Browse...",
                onClick = {
                    val descriptor = FileChooserDescriptorFactory.singleFile().apply {
                        title = "Select Claude CLI"
                    }
                    FileChooser.chooseFile(descriptor, null, null) { file ->
                        onCliPathChange(file.path)
                    }
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // Help text
        Text(
            text = "Path to the Claude CLI binary. Leave empty to auto-detect from: " +
                "PATH, ~/.npm-global/bin/claude, /usr/local/bin/claude, ~/.local/bin/claude, etc.",
            style = JewelTheme.typography.small,
            color = placeholderColor,
        )
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = JewelTheme.globalColors.borders.normal
    val textColor = JewelTheme.globalColors.text.normal
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = JewelTheme.typography.medium,
            color = textColor,
        )
    }
}
