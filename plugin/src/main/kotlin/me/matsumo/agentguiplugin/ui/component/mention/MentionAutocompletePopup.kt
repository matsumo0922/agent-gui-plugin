package me.matsumo.agentguiplugin.ui.component.mention

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.ui.component.SwingIcon
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

@Composable
fun MentionAutocompletePopup(
    suggestions: List<AttachedFile>,
    selectedIndex: Int,
    query: String,
    projectBasePath: String?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    var listSize by remember { mutableStateOf(0) }

    LaunchedEffect(selectedIndex) {
        if (suggestions.isNotEmpty()) {
            listState.scrollToItem(selectedIndex.coerceIn(suggestions.indices))
        }
    }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(
            x = 0,
            y = -listSize - density.run { 8.dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false),
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .heightIn(max = 300.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = JewelTheme.globalColors.borders.normal,
                    shape = RoundedCornerShape(8.dp),
                )
                .background(JewelTheme.colorPalette.gray(2))
                .padding(4.dp)
                .onSizeChanged {
                    listSize = it.height
                },
        ) {
            if (suggestions.isEmpty()) {
                Text(
                    text = "一致するファイルがありません",
                    modifier = Modifier.padding(16.dp),
                    style = JewelTheme.typography.medium,
                    color = JewelTheme.globalColors.text.disabled,
                )
            } else {
                LazyColumn(state = listState) {
                    itemsIndexed(
                        items = suggestions,
                        key = { _, file -> file.id },
                    ) { index, file ->
                        MentionFileItem(
                            file = file,
                            isSelected = index == selectedIndex,
                            query = query,
                            projectBasePath = projectBasePath,
                            onClick = { onSelect(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MentionFileItem(
    file: AttachedFile,
    isSelected: Boolean,
    query: String,
    projectBasePath: String?,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isSelected -> JewelTheme.colorPalette.blue(2)
        isHovered -> JewelTheme.colorPalette.blue(1)
        else -> Color.Transparent
    }

    val relativePath = remember(file.path, projectBasePath) {
        if (projectBasePath != null && file.path.startsWith(projectBasePath)) {
            file.path.removePrefix(projectBasePath).removePrefix("/")
        } else {
            file.path
        }
    }

    val highlightColor = JewelTheme.colorPalette.blue(7)
    val annotatedName = remember(file.name, query, highlightColor) {
        buildAnnotatedString {
            append(file.name)
            if (query.isNotEmpty()) {
                val lowerName = file.name.lowercase()
                val lowerQuery = query.lowercase()
                var searchStart = 0
                while (true) {
                    val matchIndex = lowerName.indexOf(lowerQuery, searchStart)
                    if (matchIndex == -1) break
                    addStyle(
                        SpanStyle(color = highlightColor),
                        matchIndex,
                        matchIndex + query.length,
                    )
                    searchStart = matchIndex + query.length
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(8.dp, 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SwingIcon(
            modifier = Modifier.size(16.dp),
            icon = file.icon,
            contentDescription = null,
        )

        Text(
            text = annotatedName,
            style = JewelTheme.typography.medium,
            softWrap = false,
        )

        Text(
            text = relativePath,
            style = JewelTheme.typography.small,
            color = JewelTheme.globalColors.text.disabled,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}
