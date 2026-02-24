@file:Suppress("UnstableApiUsage")

package me.matsumo.agentguiplugin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock.FencedCodeBlock
import org.jetbrains.jewel.markdown.extensions.MarkdownRendererExtension
import org.jetbrains.jewel.markdown.rendering.DefaultMarkdownBlockRenderer
import org.jetbrains.jewel.markdown.rendering.InlineMarkdownRenderer
import org.jetbrains.jewel.markdown.rendering.MarkdownStyling

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalJewelApi::class)
class CustomCodeBlockRenderer(
    rootStyling: MarkdownStyling,
    rendererExtensions: List<MarkdownRendererExtension>,
    inlineRenderer: InlineMarkdownRenderer,
    private val codeBlockBackground: Color,
    private val headerBackground: Color,
    private val borderColor: Color,
    private val labelColor: Color,
    private val borderWidth: Dp = 1.dp,
) : DefaultMarkdownBlockRenderer(rootStyling, rendererExtensions, inlineRenderer) {

    @Composable
    override fun RenderFencedCodeBlock(
        block: FencedCodeBlock,
        styling: MarkdownStyling.Code.Fenced,
        enabled: Boolean,
        modifier: Modifier,
    ) {
        val language = block.mimeType?.displayName().orEmpty()

        CodeBlock(
            content = block.content,
            language = language,
            modifier = modifier,
            diffLines = null,
            onOpenDiff = null,
            styling = styling,
        )
    }
}
