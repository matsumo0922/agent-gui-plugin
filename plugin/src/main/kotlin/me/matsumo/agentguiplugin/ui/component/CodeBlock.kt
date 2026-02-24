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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.intellij.openapi.ide.CopyPasteManager
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.code.highlighting.LocalCodeHighlighter
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock.FencedCodeBlock
import org.jetbrains.jewel.markdown.rendering.MarkdownStyling
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.awt.datatransfer.StringSelection

/** diff 表示時に前後に表示するコンテキスト行数 */
const val DIFF_CONTEXT_LINES = 3

/** diff の1行を表す sealed interface */
sealed interface DiffLine {
    val text: String
    val oldLineNumber: Int?
    val newLineNumber: Int?

    data class Context(override val text: String, override val oldLineNumber: Int, override val newLineNumber: Int) : DiffLine
    data class Added(override val text: String, override val newLineNumber: Int) : DiffLine {
        override val oldLineNumber: Int? = null
    }

    data class Removed(override val text: String, override val oldLineNumber: Int) : DiffLine {
        override val newLineNumber: Int? = null
    }
}

/**
 * old_string / new_string の行単位 diff を LCS アルゴリズムで計算し、コンテキスト付きの diff 行リストを返す。
 *
 * - 隣接する delta のコンテキスト範囲が重なる場合は同一 hunk として結合する
 * - 各 [DiffLine] には old/new ファイルの1ベース行番号を付与する
 */
fun computeDiffLines(
    oldString: String,
    newString: String,
    contextLines: Int = DIFF_CONTEXT_LINES,
): List<DiffLine> {
    val oldLines = oldString.trimEnd('\n').lines()
    val newLines = newString.trimEnd('\n').lines()

    val patch = DiffUtils.diff(oldLines, newLines)
    if (patch.deltas.isEmpty()) return emptyList()

    val allDeltas: List<AbstractDelta<String>> = patch.deltas

    // 隣接 delta のコンテキスト窓が重なる場合は同一 hunk に結合する
    // hunk = コンテキストを含めた連続して表示すべき delta のグループ
    val hunks = mutableListOf<List<AbstractDelta<String>>>()
    var currentHunk = mutableListOf<AbstractDelta<String>>(allDeltas[0])

    for (i in 1 until allDeltas.size) {
        val prev = allDeltas[i - 1]
        val curr = allDeltas[i]
        // prev の後コンテキスト窓末尾 vs curr の前コンテキスト窓先頭
        val prevContextEnd = prev.source.position + prev.source.lines.size + contextLines
        val currContextStart = curr.source.position - contextLines
        if (currContextStart <= prevContextEnd) {
            // 窓が重なる → 同一 hunk に結合
            currentHunk.add(curr)
        } else {
            hunks.add(currentHunk.toList())
            currentHunk = mutableListOf(curr)
        }
    }
    hunks.add(currentHunk.toList())

    val result = mutableListOf<DiffLine>()

    for (hunkDeltas in hunks) {
        val firstDelta = hunkDeltas.first()
        val lastDelta = hunkDeltas.last()

        // hunk の開始位置（コンテキスト分だけ手前から、ファイル先頭を下回らない）
        var oldPos = maxOf(0, firstDelta.source.position - contextLines)
        var newPos = maxOf(0, firstDelta.target.position - contextLines)

        for (delta in hunkDeltas) {
            // この delta の前にあるコンテキスト行
            while (oldPos < delta.source.position) {
                result.add(DiffLine.Context(oldLines[oldPos], oldLineNumber = oldPos + 1, newLineNumber = newPos + 1))
                oldPos++
                newPos++
            }

            // 削除行（source 側）
            for (line in delta.source.lines) {
                result.add(DiffLine.Removed(line, oldLineNumber = oldPos + 1))
                oldPos++
            }

            // 追加行（target 側）
            for (line in delta.target.lines) {
                result.add(DiffLine.Added(line, newLineNumber = newPos + 1))
                newPos++
            }
        }

        // hunk 末尾のコンテキスト行（最後の delta の後、ファイル末尾を超えない）
        val hunkEndOld = minOf(oldLines.size, lastDelta.source.position + lastDelta.source.lines.size + contextLines)
        while (oldPos < hunkEndOld) {
            result.add(DiffLine.Context(oldLines[oldPos], oldLineNumber = oldPos + 1, newLineNumber = newPos + 1))
            oldPos++
            newPos++
        }
    }

    return result
}

/**
 * 汎用コードブロック Composable。
 *
 * - [diffLines] が null の場合: 通常のシンタックスハイライト表示
 * - [diffLines] が non-null の場合: diff 表示（+/-/コンテキスト行）
 * - [showLineNumbers] が true の場合: 左側に行番号列を表示（diff 表示時は old/new 各列）
 * - [onOpenDiff] が non-null の場合: ヘッダーに「開く」ボタンを表示
 */
@OptIn(ExperimentalJewelApi::class)
@Composable
fun CodeBlock(
    content: String,
    language: String = "",
    modifier: Modifier = Modifier,
    diffLines: List<DiffLine>? = null,
    showLineNumbers: Boolean = false,
    onOpenDiff: (() -> Unit)? = null,
    styling: MarkdownStyling.Code.Fenced? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val codeBlockBg = ChatTheme.CodeBlock.background
    val headerBg = ChatTheme.CodeBlock.headerBackground
    val borderColor = ChatTheme.CodeBlock.border

    Column(
        modifier = modifier
            .clip(shape)
            .background(codeBlockBg, shape)
            .border(1.dp, borderColor, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(1f),
                text = language.ifEmpty { "code" },
                color = ChatTheme.Text.secondary,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (onOpenDiff != null) {
                    IconActionButton(
                        key = AllIconsKeys.General.OpenInToolWindow,
                        onClick = onOpenDiff,
                        contentDescription = "開く",
                    )
                }

                IconActionButton(
                    key = AllIconsKeys.Actions.Copy,
                    onClick = {
                        CopyPasteManager.getInstance()
                            .setContents(StringSelection(content))
                    },
                    contentDescription = "コードをコピー",
                )
            }
        }

        if (diffLines != null) {
            DiffContent(
                modifier = Modifier.fillMaxWidth(),
                diffLines = diffLines,
                showLineNumbers = showLineNumbers,
            )
        } else if (styling != null) {
            HighlightedCodeBlockContent(
                modifier = Modifier.fillMaxWidth(),
                content = content,
                styling = styling,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                Text(
                    text = content,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun DiffContent(
    diffLines: List<DiffLine>,
    showLineNumbers: Boolean,
    modifier: Modifier = Modifier,
) {
    val addedBg = ChatTheme.Diff.addedBackground
    val removedBg = ChatTheme.Diff.removedBackground
    val addedLabel = ChatTheme.Diff.addedLabel
    val removedLabel = ChatTheme.Diff.removedLabel
    val contextColor = ChatTheme.Text.primary
    val lineNumColor = ChatTheme.Text.secondary

    Column(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        diffLines.forEach { line ->
            val (prefix, bg, lineColor) = when (line) {
                is DiffLine.Added -> Triple("+", addedBg, addedLabel)
                is DiffLine.Removed -> Triple("-", removedBg, removedLabel)
                is DiffLine.Context -> Triple(" ", Color.Transparent, contextColor)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(8.dp, 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showLineNumbers) {
                    Text(
                        modifier = Modifier.width(24.dp),
                        text = (line.oldLineNumber ?: line.newLineNumber)?.toString().orEmpty(),
                        color = lineNumColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                    )
                }

                Text(
                    text = prefix,
                    color = lineColor,
                    fontFamily = FontFamily.Monospace,
                )

                Text(
                    text = line.text,
                    fontFamily = FontFamily.Monospace,
                    color = lineColor,
                    softWrap = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalJewelApi::class)
@Composable
internal fun HighlightedCodeBlockContent(
    content: String,
    styling: MarkdownStyling.Code.Fenced,
    modifier: Modifier = Modifier,
    block: FencedCodeBlock? = null,
) {
    val highlighter = LocalCodeHighlighter.current
    val mimeType = block?.mimeType

    val highlightedText = if (mimeType != null) {
        produceState(
            initialValue = AnnotatedString(content),
            key1 = content,
            key2 = mimeType,
        ) {
            highlighter.highlight(content, mimeType).collect { value = it }
        }.value
    } else {
        AnnotatedString(content)
    }

    Box(
        modifier = modifier
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
