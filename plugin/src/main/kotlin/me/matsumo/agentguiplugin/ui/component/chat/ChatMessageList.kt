package me.matsumo.agentguiplugin.ui.component.chat

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    subAgentTasks: Map<String, SubAgentTask>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var lastMessages by remember { mutableStateOf<List<ChatMessage>?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && lastMessages != null) {
            listState.animateScrollToItem(messages.lastIndex)
        }

        lastMessages = messages
    }

    if (messages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Start a conversation...",
                style = JewelTheme.typography.regular,
                color = JewelTheme.globalColors.text.info
            )
        }
    } else {
        SelectionContainer(modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = messages,
                    key = { it.id },
                ) { message ->
                    when (message) {
                        is ChatMessage.User -> {
                            UserMessageBubble(
                                modifier = Modifier.fillMaxWidth(),
                                text = message.text,
                                attachedFiles = message.attachedFiles,
                            )
                        }

                        is ChatMessage.Assistant -> {
                            AssistantMessageBlock(
                                modifier = Modifier.fillMaxWidth(),
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
