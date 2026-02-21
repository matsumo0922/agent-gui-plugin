package me.matsumo.agentguiplugin.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.chat.ChatMessageList
import me.matsumo.agentguiplugin.ui.input.ChatInputArea
import me.matsumo.agentguiplugin.ui.interaction.AskUserQuestionCard
import me.matsumo.agentguiplugin.ui.interaction.PermissionCard
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ChatPanel(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        ChatHeader(
            sessionState = uiState.sessionState,
            model = uiState.model,
            totalCostUsd = uiState.totalCostUsd,
        )

        // Error banner
        if (uiState.sessionState == SessionState.Error) {
            ErrorBanner(
                message = uiState.errorMessage ?: "An error occurred",
                onReconnect = { viewModel.reconnect() },
            )
        }

        // Main content area
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.messages.isEmpty() && uiState.isStreaming) {
                // Processing animation when streaming but no messages yet
                ProcessingAnimation()
            } else {
                ChatMessageList(
                    messages = uiState.messages,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Divider
        val dividerColor = ChatTheme.Border.subtle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dividerColor),
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

@Composable
private fun ChatHeader(
    sessionState: SessionState,
    model: String?,
    totalCostUsd: Double,
    modifier: Modifier = Modifier,
) {
    val bgPrimary = ChatTheme.Background.primary
    val headerBackground = remember(bgPrimary) { bgPrimary.copy(alpha = 0.95f) }
    val borderColor = ChatTheme.Border.subtle

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ChatTheme.Spacing.headerHeight)
            .drawBehind {
                // Bottom border
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .background(headerBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status badge
        StatusBadge(sessionState = sessionState)

        Spacer(modifier = Modifier.width(8.dp))

        // Session title
        Text(
            text = "Claude Code",
            fontSize = 13.sp,
            color = ChatTheme.Text.primary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Model badge
        if (model != null) {
            Text(
                text = model,
                fontSize = 11.sp,
                color = ChatTheme.Text.muted,
                modifier = Modifier
                    .background(
                        ChatTheme.Background.muted,
                        RoundedCornerShape(ChatTheme.Radius.small),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Cost badge
        if (totalCostUsd > 0) {
            Text(
                text = "$${String.format("%.4f", totalCostUsd)}",
                fontSize = 11.sp,
                color = ChatTheme.Text.muted,
                modifier = Modifier
                    .background(
                        ChatTheme.Background.muted,
                        RoundedCornerShape(ChatTheme.Radius.small),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun StatusBadge(
    sessionState: SessionState,
    modifier: Modifier = Modifier,
) {
    val (statusText, badgeBg, badgeText, isAnimated) = when (sessionState) {
        SessionState.Streaming -> StatusBadgeStyle(
            "Running",
            ChatTheme.Status.ready.copy(alpha = 0.1f),
            ChatTheme.Status.ready,
            true,
        )
        SessionState.Ready, SessionState.WaitingForInput -> StatusBadgeStyle(
            "Ready",
            ChatTheme.Status.ready.copy(alpha = 0.1f),
            ChatTheme.Status.ready,
            false,
        )
        SessionState.Connecting -> StatusBadgeStyle(
            "Connecting",
            ChatTheme.Status.connecting.copy(alpha = 0.1f),
            ChatTheme.Status.connecting,
            true,
        )
        SessionState.Disconnected -> StatusBadgeStyle(
            "Disconnected",
            ChatTheme.Background.muted,
            ChatTheme.Text.muted,
            false,
        )
        SessionState.Error -> StatusBadgeStyle(
            "Error",
            ChatTheme.Status.error.copy(alpha = 0.1f),
            ChatTheme.Status.error,
            false,
        )
    }

    Row(
        modifier = modifier
            .background(badgeBg, RoundedCornerShape(ChatTheme.Radius.small))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (isAnimated) {
            val infiniteTransition = rememberInfiniteTransition()
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(pulseAlpha)
                    .background(badgeText, CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(badgeText, CircleShape),
            )
        }

        Text(
            text = statusText,
            fontSize = 10.sp,
            color = badgeText,
        )
    }
}

private data class StatusBadgeStyle(
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    val isAnimated: Boolean,
)

@Composable
private fun ErrorBanner(
    message: String,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorBg = ChatTheme.Status.error.copy(alpha = 0.1f)
    val errorBorder = ChatTheme.Status.error

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Left border accent
                drawRect(
                    color = errorBorder,
                    topLeft = Offset.Zero,
                    size = Size(4.dp.toPx(), size.height),
                )
            }
            .background(errorBg)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = ChatTheme.Status.error,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Reconnect button (outline style)
        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    ChatTheme.Status.error,
                    RoundedCornerShape(ChatTheme.Radius.medium),
                )
                .clickable(onClick = onReconnect)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Reconnect",
                fontSize = 12.sp,
                color = ChatTheme.Status.error,
            )
        }
    }
}

@Composable
private fun ProcessingAnimation(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "\u25CF \u25CF \u25CF",
                fontSize = 20.sp,
                color = ChatTheme.Status.streaming,
                modifier = Modifier.alpha(alpha),
            )
            Text(
                text = "Processing...",
                fontSize = 14.sp,
                color = ChatTheme.Text.muted,
                modifier = Modifier.alpha(alpha),
            )
        }
    }
}
