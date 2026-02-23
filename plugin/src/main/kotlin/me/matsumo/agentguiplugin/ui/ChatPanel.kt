package me.matsumo.agentguiplugin.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.ui.component.AnimatedNullableVisibility
import me.matsumo.agentguiplugin.ui.component.ChatInputArea
import me.matsumo.agentguiplugin.ui.component.ErrorBanner
import me.matsumo.agentguiplugin.ui.component.chat.ChatMessageList
import me.matsumo.agentguiplugin.ui.component.interaction.AskUserQuestionCard
import me.matsumo.agentguiplugin.ui.component.interaction.PermissionCard
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun ChatPanel(
    viewModel: ChatViewModel,
    project: Project,
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

        // Bottom interaction area
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            AnimatedNullableVisibility(
                value = uiState.pendingPermission,
                enter = fadeIn(tween(delayMillis = 300)) + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                PermissionCard(
                    modifier = Modifier.padding(bottom = 12.dp),
                    permission = it,
                    onAllow = { viewModel.respondPermission(true) },
                    onDeny = { msg -> viewModel.respondPermission(false, msg) },
                )
            }

            AnimatedNullableVisibility(
                value = uiState.pendingQuestion,
                enter = fadeIn(tween(delayMillis = 300)) + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                AskUserQuestionCard(
                    modifier = Modifier.padding(bottom = 12.dp),
                    question = it,
                    onSubmit = { answers -> viewModel.respondQuestion(answers) },
                    onCancel = { viewModel.respondPermission(allow = false) },
                )
            }

            ChatInputArea(
                project = project,
                sessionState = uiState.sessionState,
                attachedFiles = uiState.attachedFiles,
                onAttach = { file -> viewModel.attachFile(file) },
                onDetach = { file -> viewModel.detachFile(file) },
                onSend = viewModel::sendMessage,
                onAbort = viewModel::abortSession,
            )
        }
    }
}
