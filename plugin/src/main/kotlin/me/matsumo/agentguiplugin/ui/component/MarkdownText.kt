package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val segments = remember(text) { parseMarkdownSegments(text) }

    val textPrimary = ChatTheme.Text.primary
    val textMuted = ChatTheme.Text.muted
    val borderColor = ChatTheme.Border.default
    val borderSubtle = ChatTheme.Border.subtle
    val blockquoteBorder = ChatTheme.Markdown.blockquoteBorder
    val blockquoteBackground = ChatTheme.Markdown.blockquoteBackground
    val tableBorder = ChatTheme.Markdown.tableBorder
    val tableHeaderBg = ChatTheme.Markdown.tableHeaderBackground
    val hrColor = ChatTheme.Markdown.horizontalRule
    val inlineBg = ChatTheme.Code.inlineBackground
    val inlineBorderColor = ChatTheme.Code.inlineBorder
    val codeFontSize = ChatTheme.Markdown.code.fontSize
    val linkColor = ChatTheme.Markdown.linkColor

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEach { segment ->
            when (segment) {
                is MdSegment.Heading -> {
                    HeadingBlock(segment, textPrimary, textMuted, borderColor, borderSubtle)
                }
                is MdSegment.Paragraph -> {
                    Text(
                        text = formatInline(segment.text, textPrimary, inlineBg, inlineBorderColor, codeFontSize, linkColor),
                        style = ChatTheme.Markdown.body.copy(color = textPrimary),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                is MdSegment.CodeBlock -> {
                    CodeBlock(
                        code = segment.code,
                        language = segment.language,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                is MdSegment.UnorderedList -> {
                    UnorderedListBlock(segment, textPrimary, inlineBg, inlineBorderColor, codeFontSize, linkColor)
                }
                is MdSegment.OrderedList -> {
                    OrderedListBlock(segment, textPrimary, inlineBg, inlineBorderColor, codeFontSize, linkColor)
                }
                is MdSegment.Blockquote -> {
                    BlockquoteBlock(segment, blockquoteBorder, blockquoteBackground, textPrimary, inlineBg, inlineBorderColor, codeFontSize, linkColor)
                }
                is MdSegment.Table -> {
                    TableBlock(segment, tableBorder, tableHeaderBg, textPrimary, inlineBg, inlineBorderColor, codeFontSize, linkColor)
                }
                is MdSegment.HorizontalRule -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .height(1.dp)
                            .background(hrColor),
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Segment data model
// ──────────────────────────────────────────

private sealed interface MdSegment {
    data class Heading(val level: Int, val text: String) : MdSegment
    data class Paragraph(val text: String) : MdSegment
    data class CodeBlock(val code: String, val language: String?) : MdSegment
    data class UnorderedList(val items: List<ListItem>) : MdSegment
    data class OrderedList(val items: List<ListItem>) : MdSegment
    data class Blockquote(val text: String) : MdSegment
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MdSegment
    data object HorizontalRule : MdSegment
}

private data class ListItem(val text: String, val indent: Int = 0)

// ──────────────────────────────────────────
// Block-level composables
// ──────────────────────────────────────────

@Composable
private fun HeadingBlock(
    heading: MdSegment.Heading,
    textPrimary: Color,
    textMuted: Color,
    borderColor: Color,
    borderSubtle: Color,
) {
    val (style, topPad, bottomPad) = when (heading.level) {
        1 -> Triple(ChatTheme.Markdown.h1, 32.dp, 24.dp)
        2 -> Triple(ChatTheme.Markdown.h2, 32.dp, 16.dp)
        3 -> Triple(ChatTheme.Markdown.h3, 24.dp, 12.dp)
        4 -> Triple(ChatTheme.Markdown.h4, 16.dp, 8.dp)
        5 -> Triple(ChatTheme.Markdown.h5, 16.dp, 8.dp)
        else -> Triple(ChatTheme.Markdown.h6, 16.dp, 8.dp)
    }
    val color = if (heading.level == 6) textMuted else textPrimary

    val drawBorder = heading.level == 1 || heading.level == 2
    val padBottom = if (drawBorder) {
        if (heading.level == 1) 12.dp else 8.dp
    } else {
        0.dp
    }
    val lineColor = if (heading.level == 1) borderColor else borderSubtle

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPad, bottom = bottomPad),
    ) {
        if (drawBorder) {
            Text(
                text = heading.text,
                style = style.copy(color = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = padBottom)
                    .drawBehind {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
            )
        } else {
            Text(
                text = heading.text,
                style = style.copy(color = color),
            )
        }
    }
}

@Composable
private fun UnorderedListBlock(
    list: MdSegment.UnorderedList,
    textPrimary: Color,
    inlineBg: Color,
    inlineBorder: Color,
    codeFontSize: TextUnit,
    linkColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = 16.dp),
    ) {
        list.items.forEach { item ->
            Row(
                modifier = Modifier.padding(
                    start = (item.indent * 16).dp,
                    bottom = 8.dp,
                ),
            ) {
                val marker = when (item.indent % 3) {
                    0 -> "\u2022"  // bullet
                    1 -> "\u25E6"  // circle
                    else -> "\u25AA" // square
                }
                Text(
                    text = marker,
                    style = ChatTheme.Markdown.body.copy(color = textPrimary),
                    modifier = Modifier.width(16.dp),
                )
                Text(
                    text = formatInline(item.text, textPrimary, inlineBg, inlineBorder, codeFontSize, linkColor),
                    style = ChatTheme.Markdown.body.copy(color = textPrimary),
                )
            }
        }
    }
}

@Composable
private fun OrderedListBlock(
    list: MdSegment.OrderedList,
    textPrimary: Color,
    inlineBg: Color,
    inlineBorder: Color,
    codeFontSize: TextUnit,
    linkColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = 16.dp),
    ) {
        list.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.padding(
                    start = (item.indent * 16).dp,
                    bottom = 8.dp,
                ),
            ) {
                Text(
                    text = "${index + 1}.",
                    style = ChatTheme.Markdown.body.copy(color = textPrimary),
                    modifier = Modifier.width(24.dp),
                )
                Text(
                    text = formatInline(item.text, textPrimary, inlineBg, inlineBorder, codeFontSize, linkColor),
                    style = ChatTheme.Markdown.body.copy(color = textPrimary),
                )
            }
        }
    }
}

@Composable
private fun BlockquoteBlock(
    bq: MdSegment.Blockquote,
    borderColor: Color,
    backgroundColor: Color,
    textPrimary: Color,
    inlineBg: Color,
    inlineBorder: Color,
    codeFontSize: TextUnit,
    linkColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            )
            .drawBehind {
                drawRect(
                    color = borderColor,
                    topLeft = Offset.Zero,
                    size = size.copy(width = 4.dp.toPx()),
                )
            }
            .padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Text(
            text = formatInline(bq.text, textPrimary, inlineBg, inlineBorder, codeFontSize, linkColor),
            style = ChatTheme.Markdown.body.copy(
                color = textPrimary,
                fontStyle = FontStyle.Italic,
            ),
        )
    }
}

@Composable
private fun TableBlock(
    table: MdSegment.Table,
    borderColor: Color,
    headerBackground: Color,
    textPrimary: Color,
    inlineBg: Color,
    inlineBorder: Color,
    codeFontSize: TextUnit,
    linkColor: Color,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .horizontalScroll(scrollState)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .background(headerBackground)
                    .drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
            ) {
                table.headers.forEachIndexed { index, header ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(borderColor),
                        )
                    }
                    Text(
                        text = formatInline(header.trim(), textPrimary, inlineBg, inlineBorder, codeFontSize, linkColor),
                        style = ChatTheme.Markdown.body.copy(
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            // Data rows
            table.rows.forEach { row ->
                Row(
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 0.5.dp.toPx(),
                        )
                    },
                ) {
                    row.forEachIndexed { index, cell ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(borderColor),
                            )
                        }
                        Text(
                            text = formatInline(cell.trim(), textPrimary, inlineBg, inlineBorder, codeFontSize, linkColor),
                            style = ChatTheme.Markdown.body.copy(color = textPrimary),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Markdown parser (line-by-line with state)
// ──────────────────────────────────────────

// Pre-compiled regex patterns for markdown parsing
private val horizontalRuleRegex = Regex("^(-{3,}|\\*{3,}|_{3,})$")
private val headingRegex = Regex("^(#{1,6})\\s+(.+)")
private val tableSeparatorRegex = Regex("^\\|[-:|\\s]+\\|$")
private val unorderedListRegex = Regex("^(\\s*)([-*+])\\s+(.+)")
private val orderedListRegex = Regex("^(\\s*)\\d+\\.\\s+(.+)")

private fun parseMarkdownSegments(text: String): List<MdSegment> {
    val lines = text.lines()
    val segments = mutableListOf<MdSegment>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block
        if (line.trimStart().startsWith("```")) {
            val language = line.trimStart().removePrefix("```").trim().ifEmpty { null }
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++ // skip closing ```
            segments.add(MdSegment.CodeBlock(codeLines.joinToString("\n").trimEnd(), language))
            continue
        }

        // Horizontal rule
        if (line.trim().matches(horizontalRuleRegex) && line.trim().length >= 3) {
            segments.add(MdSegment.HorizontalRule)
            i++
            continue
        }

        // Heading
        val headingMatch = headingRegex.matchEntire(line)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val content = headingMatch.groupValues[2]
            segments.add(MdSegment.Heading(level, content))
            i++
            continue
        }

        // Table (| header | header |)
        if (line.trimStart().startsWith("|") && i + 1 < lines.size) {
            val separatorLine = lines[i + 1]
            if (separatorLine.trim().matches(tableSeparatorRegex)) {
                val headers = line.trim().removeSurrounding("|").split("|").map { it.trim() }
                val rows = mutableListOf<List<String>>()
                var j = i + 2
                while (j < lines.size && lines[j].trimStart().startsWith("|")) {
                    rows.add(lines[j].trim().removeSurrounding("|").split("|").map { it.trim() })
                    j++
                }
                segments.add(MdSegment.Table(headers, rows))
                i = j
                continue
            }
        }

        // Blockquote
        if (line.trimStart().startsWith("> ") || line.trimStart() == ">") {
            val bqLines = mutableListOf<String>()
            while (i < lines.size && (lines[i].trimStart().startsWith("> ") || lines[i].trimStart() == ">")) {
                bqLines.add(lines[i].trimStart().removePrefix(">").removePrefix(" "))
                i++
            }
            segments.add(MdSegment.Blockquote(bqLines.joinToString("\n")))
            continue
        }

        // Unordered list
        if (unorderedListRegex.containsMatchIn(line)) {
            val items = mutableListOf<ListItem>()
            while (i < lines.size && unorderedListRegex.containsMatchIn(lines[i])) {
                val match = unorderedListRegex.matchEntire(lines[i])!!
                val indent = match.groupValues[1].length / 2
                items.add(ListItem(match.groupValues[3], indent))
                i++
            }
            segments.add(MdSegment.UnorderedList(items))
            continue
        }

        // Ordered list
        if (orderedListRegex.containsMatchIn(line)) {
            val items = mutableListOf<ListItem>()
            while (i < lines.size && orderedListRegex.containsMatchIn(lines[i])) {
                val match = orderedListRegex.matchEntire(lines[i])!!
                val indent = match.groupValues[1].length / 2
                items.add(ListItem(match.groupValues[2], indent))
                i++
            }
            segments.add(MdSegment.OrderedList(items))
            continue
        }

        // Empty line — skip
        if (line.isBlank()) {
            i++
            continue
        }

        // Plain paragraph (accumulate consecutive non-blank lines)
        val paraLines = mutableListOf<String>()
        while (i < lines.size && lines[i].isNotBlank() && !isBlockStart(lines[i])) {
            paraLines.add(lines[i])
            i++
        }
        if (paraLines.isNotEmpty()) {
            segments.add(MdSegment.Paragraph(paraLines.joinToString("\n")))
        }
    }

    return segments
}

private fun isBlockStart(line: String): Boolean {
    if (line.trimStart().startsWith("```")) return true
    if (line.trim().matches(horizontalRuleRegex)) return true
    if (headingRegex.containsMatchIn(line)) return true
    if (line.trimStart().startsWith("> ")) return true
    if (unorderedListRegex.containsMatchIn(line)) return true
    if (orderedListRegex.containsMatchIn(line)) return true
    if (line.trimStart().startsWith("|")) return true
    return false
}

// ──────────────────────────────────────────
// Inline formatting
// ──────────────────────────────────────────

private fun formatInline(
    text: String,
    textColor: Color,
    inlineBg: Color,
    inlineBorder: Color,
    codeFontSize: TextUnit,
    linkColor: Color,
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold + Italic: ***text***
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end >= 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }
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
                // Strikethrough: ~~text~~
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end >= 0) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic: *text* (single asterisk, not followed by another *)
                text[i] == '*' && (i + 1 >= text.length || text[i + 1] != '*') -> {
                    val end = text.indexOf('*', i + 1)
                    if (end >= 0 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code: `text`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end >= 0) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = codeFontSize,
                                background = inlineBg,
                            ),
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Link: [text](url)
                text[i] == '[' -> {
                    val closeBracket = text.indexOf(']', i + 1)
                    if (closeBracket >= 0 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                        val closeParen = text.indexOf(')', closeBracket + 2)
                        if (closeParen >= 0) {
                            val linkText = text.substring(i + 1, closeBracket)
                            withStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ) {
                                append(linkText)
                            }
                            i = closeParen + 1
                        } else {
                            append(text[i])
                            i++
                        }
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
