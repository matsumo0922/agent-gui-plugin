package me.matsumo.agentguiplugin.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    subAgentTasks: Map<String, SubAgentTask>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages change (including in-place streaming updates)
    val lastAssistant = messages.filterIsInstance<ChatMessage.Assistant>().lastOrNull()
    val scrollKey = lastAssistant?.let {
        Pair(it.blocks.size, it.blocks.lastOrNull()?.contentSignature())
    }
    val subAgentMessageCount = subAgentTasks.values.sumOf { it.messages.size }
    LaunchedEffect(messages.size, scrollKey, subAgentMessageCount) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    if (messages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = EMPTY_PLACEHOLDER,
                fontSize = EMPTY_PLACEHOLDER_FONT_SIZE,
                color = ChatTheme.Text.muted,
            )
        }
    } else {
        SelectionContainer(modifier = modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = ChatTheme.Spacing.messageListPadding,
                    vertical = ChatTheme.Spacing.messageListPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(ChatTheme.Spacing.messageGap),
            ) {
                items(
                    items = messages,
                    key = { it.id },
                ) { message ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = when (message) {
                            is ChatMessage.User -> Alignment.CenterEnd
                            is ChatMessage.Assistant -> Alignment.CenterStart
                        },
                    ) {
                        when (message) {
                            is ChatMessage.User -> UserMessageBubble(text = message.text)
                            is ChatMessage.Assistant -> AssistantMessageBlock(
                                blocks = message.blocks,
                                subAgentTasks = subAgentTasks,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun UiContentBlock.contentSignature(): Int = when (this) {
    is UiContentBlock.Text -> text.length
    is UiContentBlock.Thinking -> text.length
    is UiContentBlock.ToolUse -> inputJson.size
}

private const val EMPTY_PLACEHOLDER = "Start a conversation..."
private val EMPTY_PLACEHOLDER_FONT_SIZE = 14.sp
