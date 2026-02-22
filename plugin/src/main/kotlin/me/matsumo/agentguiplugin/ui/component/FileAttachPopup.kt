package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.model.toAttachedFile
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.util.FilePickerUtil
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

@Composable
fun FileAttachPopup(
    project: Project,
    onFileSelected: (AttachedFile) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
        alignment = Alignment.TopStart,
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .heightIn(max = 420.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(ChatTheme.Radius.medium))
                .clip(RoundedCornerShape(ChatTheme.Radius.medium))
                .border(1.dp, ChatTheme.Border.default, RoundedCornerShape(ChatTheme.Radius.medium))
                .background(ChatTheme.Background.primary)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                },
        ) {
            PopupSearchBar(
                query = searchQuery,
                onQueryChanged = { searchQuery = it },
                onClear = { searchQuery = "" },
            )

            Divider(
                modifier = Modifier.fillMaxWidth(),
                orientation = Orientation.Horizontal,
            )

            if (searchQuery.isEmpty()) {
                DefaultPopupContent(
                    project = project,
                    onFileSelected = onFileSelected,
                    onDismiss = onDismiss,
                )
            } else {
                SearchResultsContent(
                    project = project,
                    query = searchQuery,
                    onFileSelected = onFileSelected,
                )
            }
        }
    }
}

@Composable
private fun PopupSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            key = AllIconsKeys.Actions.Search,
            contentDescription = null,
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = JewelTheme.typography.medium,
            cursorBrush = SolidColor(JewelTheme.typography.medium.color),
            singleLine = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "検索",
                        color = ChatTheme.Text.muted,
                        style = JewelTheme.typography.medium,
                    )
                }
                inner()
            },
        )

        if (query.isNotEmpty()) {
            IconActionButton(
                key = AllIconsKeys.Actions.Close,
                onClick = onClear,
                contentDescription = "Clear search",
            )
        }
    }
}

@Composable
private fun DefaultPopupContent(
    project: Project,
    onFileSelected: (AttachedFile) -> Unit,
    onDismiss: () -> Unit,
) {
    val recentFiles = remember(project) {
        FilePickerUtil.getRecentFiles(project)
    }

    LazyColumn {
        item {
            PopupMenuItem(
                text = "ファイル",
                iconKey = AllIconsKeys.Nodes.Folder,
                showChevron = true,
                onClick = {
                    FilePickerUtil.chooseFilesFromOS(project, imageOnly = false) { files ->
                        files.firstOrNull()?.toAttachedFile()?.let(onFileSelected)
                    }
                    onDismiss()
                },
            )
        }

        item {
            PopupMenuItem(
                text = "画像...",
                iconKey = AllIconsKeys.FileTypes.Image,
                showChevron = false,
                onClick = {
                    FilePickerUtil.chooseFilesFromOS(project, imageOnly = true) { files ->
                        files.firstOrNull()?.toAttachedFile()?.let(onFileSelected)
                    }
                    onDismiss()
                },
            )
        }

        item {
            Text(
                text = "最近使用したファイル",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = JewelTheme.typography.small,
                color = ChatTheme.Text.muted,
            )
        }

        items(recentFiles, key = { it.id }) { file ->
            PopupFileItem(
                file = file,
                projectBasePath = project.basePath,
                onClick = { onFileSelected(file) },
            )
        }
    }
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
private fun SearchResultsContent(
    project: Project,
    query: String,
    onFileSelected: (AttachedFile) -> Unit,
) {
    var results by remember { mutableStateOf<List<AttachedFile>>(emptyList()) }
    var cachedFilenames by remember { mutableStateOf<List<String>?>(null) }

    LaunchedEffect(project) {
        cachedFilenames = withContext(Dispatchers.Default) {
            FilePickerUtil.loadAllProjectFilenames(project)
        }
    }

    LaunchedEffect(cachedFilenames) {
        val names = cachedFilenames ?: return@LaunchedEffect
        snapshotFlow { query }
            .debounce(200)
            .mapLatest { q ->
                if (q.isBlank()) {
                    emptyList()
                } else {
                    val matched = names.filter { it.contains(q, ignoreCase = true) }.take(50)
                    withContext(Dispatchers.Default) {
                        FilePickerUtil.resolveFiles(project, matched)
                    }
                }
            }
            .collect { results = it }
    }

    LazyColumn {
        if (results.isEmpty()) {
            item {
                Text(
                    text = "一致するファイルがありません",
                    modifier = Modifier.padding(16.dp),
                    color = ChatTheme.Text.muted,
                    style = JewelTheme.typography.medium,
                )
            }
        }

        items(results, key = { it.id }) { file ->
            PopupFileItem(
                file = file,
                projectBasePath = project.basePath,
                onClick = { onFileSelected(file) },
            )
        }
    }
}

@Composable
private fun PopupMenuItem(
    text: String,
    iconKey: Any,
    showChevron: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        @Suppress("UNCHECKED_CAST")
        Icon(
            key = iconKey as org.jetbrains.jewel.ui.icon.IconKey,
            contentDescription = null,
        )

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = JewelTheme.typography.medium,
        )

        if (showChevron) {
            Icon(
                key = AllIconsKeys.General.ArrowRight,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun PopupFileItem(
    file: AttachedFile,
    projectBasePath: String?,
    onClick: () -> Unit,
) {
    val relativePath = remember(file.path, projectBasePath) {
        if (projectBasePath != null && file.path.startsWith(projectBasePath)) {
            file.path.removePrefix(projectBasePath).removePrefix("/")
        } else {
            file.path
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SwingIcon(
            icon = file.icon,
            size = 16.dp,
            contentDescription = null,
        )

        Text(
            text = file.name,
            style = JewelTheme.typography.medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = relativePath,
            style = JewelTheme.typography.small,
            color = ChatTheme.Text.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
