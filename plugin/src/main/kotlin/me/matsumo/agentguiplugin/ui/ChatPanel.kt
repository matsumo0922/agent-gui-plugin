package me.matsumo.agentguiplugin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.matsumo.agentguiplugin.ui.chat.ChatMessageList
import me.matsumo.agentguiplugin.ui.component.ChatInputArea
import me.matsumo.agentguiplugin.ui.component.ErrorBanner
import me.matsumo.agentguiplugin.ui.interaction.AskUserQuestionCard
import me.matsumo.agentguiplugin.ui.interaction.PermissionCard
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun ChatPanel(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {

        // Error banner
        if (uiState.sessionState == SessionState.Error) {
            ErrorBanner(
                modifier = Modifier.fillMaxWidth(),
                message = uiState.errorMessage ?: "An error occurred",
                onReconnect = { viewModel.reconnect() },
            )
        }

        // Main content area
        ChatMessageList(
            modifier = Modifier.weight(1f),
            messages = uiState.messages,
            subAgentTasks = uiState.subAgentTasks,
        )

        // Divider
        Divider(
            modifier = Modifier.fillMaxWidth(),
            orientation = Orientation.Horizontal,
        )

        // Bottom interaction area: permission card, question card, or normal input
        val pendingPermission = uiState.pendingPermission
        val pendingQuestion = uiState.pendingQuestion
        when {
            pendingPermission != null -> {
                PermissionCard(
                    permission = pendingPermission,
                    onAllow = { viewModel.respondPermission(allow = true) },
                    onDeny = { msg -> viewModel.respondPermission(allow = false, denyMessage = msg) },
                )
            }
            pendingQuestion != null -> {
                AskUserQuestionCard(
                    question = pendingQuestion,
                    onSubmit = { answers -> viewModel.respondQuestion(answers) },
                    onCancel = { viewModel.respondPermission(allow = false) },
                )
            }
            else -> {
                ChatInputArea(
                    sessionState = uiState.sessionState,
                    onSend = viewModel::sendMessage,
                    onAbort = viewModel::abortSession,
                )
            }
        }
    }
}
