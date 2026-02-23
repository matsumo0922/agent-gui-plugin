package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.theme.ChatTheme

@Composable
fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ChatTheme.Radius.medium)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = ChatTheme.Spacing.messageMaxWidth)
                .clip(shape)
                .background(color = ChatTheme.UserMessage.background, shape = shape)
                .border(1.dp, ChatTheme.UserMessage.border, shape)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            MarkdownText(text = text)
        }
    }
}
