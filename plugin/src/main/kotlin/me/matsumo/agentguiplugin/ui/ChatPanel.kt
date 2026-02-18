package me.matsumo.agentguiplugin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.chat.ChatMessageList
import me.matsumo.agentguiplugin.ui.input.ChatInputArea
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ChatPanel(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (uiState.sessionState) {
                    SessionState.Disconnected -> "Disconnected"
                    SessionState.Connecting -> "Connecting..."
                    SessionState.Ready -> "Ready"
                    SessionState.Streaming -> "Claude is thinking..."
                    SessionState.WaitingForInput -> "Ready"
                    SessionState.Error -> "Error"
                },
                fontSize = 12.sp,
                color = when (uiState.sessionState) {
                    SessionState.Error -> Color.Red
                    SessionState.Streaming -> Color(0xFF2563EB)
                    else -> Color.Gray
                },
                modifier = Modifier.weight(1f),
            )

            if (uiState.model != null) {
                Text(
                    text = uiState.model ?: "",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            if (uiState.totalCostUsd > 0) {
                Text(
                    text = "$${String.format("%.4f", uiState.totalCostUsd)}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                )
            }
        }

        // Error / Reconnect
        if (uiState.sessionState == SessionState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "An error occurred",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    DefaultButton(onClick = { viewModel.reconnect() }) {
                        Text("Reconnect")
                    }
                }
            }
        }

        // Message list
        ChatMessageList(
            messages = uiState.messages,
            modifier = Modifier.weight(1f),
        )

        // Input area
        ChatInputArea(
            inputText = uiState.inputText,
            sessionState = uiState.sessionState,
            onInputChanged = viewModel::updateInputText,
            onSend = viewModel::sendMessage,
            onAbort = viewModel::abortSession,
        )
    }
}
