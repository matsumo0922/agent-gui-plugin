package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.matsumo.agentguiplugin.service.SessionHistoryService
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionHistoryPopup(
    project: Project,
    onSessionSelect: (SessionHistoryService.SessionSummary, List<ChatMessage>) -> Unit,
    onDismiss: () -> Unit,
) {
    val historyService = remember { project.service<SessionHistoryService>() }
    var sessions by remember { mutableStateOf<List<SessionHistoryService.SessionSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingSessionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sessions = historyService.listSessions()
        isLoading = false
    }

    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .heightIn(max = 480.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(ChatTheme.Background.primary),
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Session History",
                        fontSize = 14.sp,
                        color = ChatTheme.Text.primary,
                    )
                    if (isLoading) {
                        CircularProgressIndicator()
                    }
                }

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    orientation = Orientation.Horizontal,
                )

                if (!isLoading && sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No session history found",
                            color = ChatTheme.Text.muted,
                            fontSize = 13.sp,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(sessions, key = { it.sessionId }) { summary ->
                            SessionRow(
                                summary = summary,
                                isLoading = loadingSessionId == summary.sessionId,
                                onClick = {
                                    loadingSessionId = summary.sessionId
                                    // メッセージの読み込みは LaunchedEffect で行う
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // セッション選択時のメッセージ読み込み
    loadingSessionId?.let { sessionId ->
        val summary = sessions.find { it.sessionId == sessionId }
        if (summary != null) {
            LaunchedEffect(sessionId) {
                val messages = historyService.readSessionMessages(sessionId)
                onSessionSelect(summary, messages)
                loadingSessionId = null
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())

@Composable
private fun SessionRow(
    summary: SessionHistoryService.SessionSummary,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isHovered) Modifier.background(ChatTheme.Background.muted)
                else Modifier,
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // 日時 + モデル
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = summary.startTime?.let { dateFormatter.format(it) } ?: "Unknown",
                fontSize = 11.sp,
                color = ChatTheme.Text.muted,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                summary.model?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = ChatTheme.Text.muted,
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
        }

        // プロンプト
        summary.firstPrompt?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = ChatTheme.Text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // メタデータ
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val totalMessages = summary.userMessageCount + summary.assistantMessageCount
            Text(
                text = "$totalMessages messages",
                fontSize = 11.sp,
                color = ChatTheme.Text.muted,
            )
            summary.durationMinutes?.let {
                Text(
                    text = "$it min",
                    fontSize = 11.sp,
                    color = ChatTheme.Text.muted,
                )
            }
            summary.totalCostUsd?.let {
                Text(
                    text = "$${String.format("%.2f", it)}",
                    fontSize = 11.sp,
                    color = ChatTheme.Text.muted,
                )
            }
        }
    }
}
