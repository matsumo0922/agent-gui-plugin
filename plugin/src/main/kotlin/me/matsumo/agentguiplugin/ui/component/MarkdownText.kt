package me.matsumo.agentguiplugin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.markdown.bridge.ProvideMarkdownStyling
import org.jetbrains.jewel.intui.markdown.bridge.create
import org.jetbrains.jewel.intui.markdown.bridge.styling.create
import org.jetbrains.jewel.intui.markdown.bridge.styling.extensions.github.alerts.create
import org.jetbrains.jewel.intui.markdown.bridge.styling.extensions.github.tables.create
import org.jetbrains.jewel.markdown.Markdown
import org.jetbrains.jewel.markdown.extensions.autolink.AutolinkProcessorExtension
import org.jetbrains.jewel.markdown.extensions.github.alerts.AlertStyling
import org.jetbrains.jewel.markdown.extensions.github.alerts.GitHubAlertProcessorExtension
import org.jetbrains.jewel.markdown.extensions.github.alerts.GitHubAlertRendererExtension
import org.jetbrains.jewel.markdown.extensions.github.strikethrough.GitHubStrikethroughProcessorExtension
import org.jetbrains.jewel.markdown.extensions.github.strikethrough.GitHubStrikethroughRendererExtension
import org.jetbrains.jewel.markdown.extensions.github.tables.GfmTableStyling
import org.jetbrains.jewel.markdown.extensions.github.tables.GitHubTableProcessorExtension
import org.jetbrains.jewel.markdown.extensions.github.tables.GitHubTableRendererExtension
import org.jetbrains.jewel.markdown.processing.MarkdownProcessor
import org.jetbrains.jewel.markdown.rendering.MarkdownBlockRenderer
import org.jetbrains.jewel.markdown.rendering.MarkdownStyling

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalJewelApi::class)
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val isDark = JewelTheme.isDark

    val processor = remember {
        MarkdownProcessor(
            extensions = listOf(
                GitHubTableProcessorExtension,
                GitHubAlertProcessorExtension,
                GitHubStrikethroughProcessorExtension(),
                AutolinkProcessorExtension,
            ),
        )
    }

    val styling = remember(isDark) { MarkdownStyling.create() }
    val tableStyling = remember(isDark) { GfmTableStyling.create() }
    val alertStyling = remember(isDark) { AlertStyling.create() }

    val blockRenderer = remember(styling, tableStyling, alertStyling) {
        MarkdownBlockRenderer.create(
            styling = styling,
            rendererExtensions = listOf(
                GitHubTableRendererExtension(tableStyling, styling),
                GitHubAlertRendererExtension(alertStyling, styling),
                GitHubStrikethroughRendererExtension,
            ),
        )
    }

    ProvideMarkdownStyling(
        markdownStyling = styling,
        markdownProcessor = processor,
        markdownBlockRenderer = blockRenderer,
    ) {
        Markdown(
            modifier = modifier,
            markdown = text,
        )
    }
}
