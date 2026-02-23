@file:Suppress("UnstableApiUsage")

package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.intellij.openapi.ide.CopyPasteManager
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.code.highlighting.LocalCodeHighlighter
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock.FencedCodeBlock
import org.jetbrains.jewel.markdown.extensions.MarkdownRendererExtension
import org.jetbrains.jewel.markdown.rendering.DefaultMarkdownBlockRenderer
import org.jetbrains.jewel.markdown.rendering.InlineMarkdownRenderer
import org.jetbrains.jewel.markdown.rendering.MarkdownStyling
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.awt.datatransfer.StringSelection

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
        val shape = RoundedCornerShape(8.dp)

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(codeBlockBackground, shape)
                .border(borderWidth, borderColor, shape),
        ) {
            // Header row: language label (left) + copy button (right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackground)
                    .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = language.ifEmpty { "code" },
                    style = styling.infoTextStyle,
                    color = labelColor,
                )

                IconActionButton(
                    key = AllIconsKeys.Actions.Copy,
                    onClick = {
                        CopyPasteManager.getInstance()
                            .setContents(StringSelection(block.content))
                    },
                    contentDescription = "Copy code",
                )
            }

            // Syntax-highlighted code content
            HighlightedCodeContent(
                block = block,
                styling = styling,
            )
        }
    }
}

@OptIn(ExperimentalJewelApi::class)
@Composable
private fun HighlightedCodeContent(
    block: FencedCodeBlock,
    styling: MarkdownStyling.Code.Fenced,
) {
    val highlighter = LocalCodeHighlighter.current
    val highlightedText by highlighter
        .highlight(block.content, block.mimeType)
        .collectAsState(initial = AnnotatedString(block.content))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(styling.padding),
    ) {
        Text(
            text = highlightedText,
            style = styling.editorTextStyle,
            softWrap = false,
        )
    }
}
