package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val segments = remember(text) { parseMarkdown(text) }

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.TextSegment -> {
                    Text(
                        text = formatInlineMarkdown(segment.text),
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                is MarkdownSegment.CodeBlockSegment -> {
                    CodeBlock(
                        code = segment.code,
                        language = segment.language,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private sealed interface MarkdownSegment {
    data class TextSegment(val text: String) : MarkdownSegment
    data class CodeBlockSegment(val code: String, val language: String?) : MarkdownSegment
}

private fun parseMarkdown(text: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val codeBlockRegex = Regex("```(\\w*)?\\n([\\s\\S]*?)```")

    var lastIndex = 0
    codeBlockRegex.findAll(text).forEach { match ->
        val beforeText = text.substring(lastIndex, match.range.first).trim()
        if (beforeText.isNotEmpty()) {
            segments.add(MarkdownSegment.TextSegment(beforeText))
        }

        val language = match.groupValues[1].ifEmpty { null }
        val code = match.groupValues[2].trimEnd()
        segments.add(MarkdownSegment.CodeBlockSegment(code, language))

        lastIndex = match.range.last + 1
    }

    val remaining = text.substring(lastIndex).trim()
    if (remaining.isNotEmpty()) {
        segments.add(MarkdownSegment.TextSegment(remaining))
    }

    return segments
}

private fun formatInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end >= 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code: `text`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end >= 0) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
