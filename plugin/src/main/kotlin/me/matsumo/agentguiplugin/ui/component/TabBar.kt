package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.model.ChatTab
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun TabBar(
    tabs: List<ChatTab>,
    activeTabId: String,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewChat: () -> Unit,
    onHistory: () -> Unit,
    onDeleteCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(ChatTheme.Background.secondary)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左側: タブリスト（横スクロール可能）
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(tabs, key = { it.id }) { tab ->
                TabItem(
                    tab = tab,
                    isActive = tab.id == activeTabId,
                    showClose = tabs.size > 1,
                    onClick = { onTabSelect(tab.id) },
                    onClose = { onTabClose(tab.id) },
                )
            }
        }

        // 右側: アクションボタン群
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 新規チャットボタン
            TabActionButton(onClick = onNewChat) {
                Icon(
                    key = AllIconsKeys.General.Add,
                    contentDescription = "New Chat",
                    modifier = Modifier.size(16.dp),
                )
            }

            // 履歴ボタン
            TabActionButton(onClick = onHistory) {
                Icon(
                    key = AllIconsKeys.Vcs.History,
                    contentDescription = "Session History",
                    modifier = Modifier.size(16.dp),
                )
            }

            // 削除ボタン
            TabActionButton(onClick = onDeleteCurrent) {
                Icon(
                    key = AllIconsKeys.General.Remove,
                    contentDescription = "Clear Current Chat",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: ChatTab,
    isActive: Boolean,
    showClose: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isActive -> ChatTheme.Background.primary
        isHovered -> ChatTheme.Background.muted
        else -> ChatTheme.Background.secondary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(backgroundColor)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .widthIn(max = 160.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = tab.title,
            modifier = Modifier.weight(1f, fill = false),
            color = if (isActive) ChatTheme.Text.primary else ChatTheme.Text.secondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showClose && (isActive || isHovered)) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    key = AllIconsKeys.Actions.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun TabActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (isHovered) Modifier.background(ChatTheme.Background.muted)
                else Modifier,
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
