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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.intellij.util.IconUtil
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
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
            .clip(RoundedCornerShape(ChatTheme.Radius.small))
            .background(ChatTheme.Background.muted)
            .border(1.dp, ChatTheme.Border.subtle, RoundedCornerShape(ChatTheme.Radius.small))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SwingIcon(
            icon = file.icon,
            size = 14.dp,
            contentDescription = null,
        )

        Text(
            text = file.name,
            style = JewelTheme.typography.small,
            maxLines = 1,
        )

        IconActionButton(
            key = AllIconsKeys.Actions.Close,
            onClick = onRemove,
            contentDescription = "Remove ${file.name}",
        )
    }
}

@Composable
fun SwingIcon(
    icon: Icon?,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val imageBitmap: ImageBitmap? = remember(icon) {
        icon?.let {
            IconUtil.toBufferedImage(it).toComposeImageBitmap()
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier.size(size),
        )
    }
}
