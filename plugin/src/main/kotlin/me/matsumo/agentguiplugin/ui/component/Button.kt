package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = Color.Transparent,
    textStyle: TextStyle = JewelTheme.typography.medium,
    textColor: Color = JewelTheme.globalColors.text.normal,
    shape: Shape = RoundedCornerShape(4.dp),
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable { onClick() }
            .padding(8.dp, 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.invoke()

            Text(
                text = text,
                style = textStyle,
                color = textColor,
            )
        }
    }
}
