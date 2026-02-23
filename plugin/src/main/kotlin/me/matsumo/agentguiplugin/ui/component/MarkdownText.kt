package me.matsumo.agentguiplugin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.markdown.Markdown
import org.jetbrains.jewel.markdown.extensions.LocalMarkdownBlockRenderer
import org.jetbrains.jewel.markdown.extensions.LocalMarkdownProcessor
import org.jetbrains.jewel.markdown.extensions.LocalMarkdownStyling

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalJewelApi::class)
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Markdown(
        modifier = modifier,
        markdown = text,
        markdownStyling = LocalMarkdownStyling.current,
        processor = LocalMarkdownProcessor.current,
        blockRenderer = LocalMarkdownBlockRenderer.current,
    )
}
