package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun ErrorBanner(
    message: String,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorColor = JewelTheme.globalColors.text.error

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(errorColor.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            style = JewelTheme.typography.regular,
            color = errorColor,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = errorColor,
                    shape = CircleShape,
                )
                .clickable { onReconnect.invoke() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Reconnect",
                style = JewelTheme.typography.medium,
                color = errorColor,
            )
        }
    }
}
