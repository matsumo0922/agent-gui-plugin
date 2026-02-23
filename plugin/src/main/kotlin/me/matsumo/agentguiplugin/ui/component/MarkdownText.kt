package me.matsumo.agentguiplugin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.intui.markdown.bridge.ProvideMarkdownStyling
import org.jetbrains.jewel.markdown.Markdown
import org.jetbrains.jewel.markdown.processing.MarkdownProcessor

@OptIn(ExperimentalJewelApi::class)
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val processor = remember { MarkdownProcessor() }

    ProvideMarkdownStyling {
        Markdown(
            markdown = text,
            modifier = modifier,
            processor = processor,
        )
    }
}
