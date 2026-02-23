package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.intellij.util.IconUtil
import me.matsumo.agentguiplugin.model.AttachedFile
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography
import javax.swing.Icon

@Composable
fun AttachedFileChip(
    file: AttachedFile,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(JewelTheme.colorPalette.gray(1))
            .border(
                width = 1.dp,
                color = JewelTheme.globalColors.borders.disabled,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SwingIcon(
            modifier = Modifier.size(14.dp),
            icon = file.icon,
            contentDescription = null,
        )

        Text(
            text = file.name,
            style = JewelTheme.typography.small,
            maxLines = 1,
        )

        IconActionButton(
            modifier = Modifier.size(16.dp),
            key = AllIconsKeys.Actions.Close,
            onClick = onRemove,
            contentDescription = "Remove ${file.name}",
        )
    }
}

@Composable
fun SwingIcon(
    icon: Icon?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val imageBitmap: ImageBitmap? = remember(icon) {
        icon?.takeIf { it.iconWidth > 0 && it.iconHeight > 0 }?.let {
            IconUtil.toBufferedImage(it).toComposeImageBitmap()
        }
    }

    if (imageBitmap != null) {
        Image(
            modifier = modifier,
            bitmap = imageBitmap,
            contentDescription = contentDescription,
        )
    }
}
