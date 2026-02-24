@file:Suppress("UnstableApiUsage")

package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
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

    data class Context(override val text: String) : DiffLine
    data class Added(override val text: String) : DiffLine
    data class Removed(override val text: String) : DiffLine
}

/**
 * ファイル全体の内容・置換前後テキストから、コンテキスト付きの diff 行リストを生成する。
 *
 * - [fileContent] 内に [oldString] が見つからない場合は `-`/`+` 行のみを返す
 * - 複数箇所一致の場合は最初の一致位置を使用する
 */
fun computeDiffLines(
    fileContent: String,
    oldString: String,
    newString: String,
    contextLines: Int = DIFF_CONTEXT_LINES,
): List<DiffLine> {
    val fileLines = fileContent.lines()
    val oldLines = oldString.trimEnd('\n').lines()
    val newLines = newString.trimEnd('\n').lines()

    val startIdx = findOldStringStartLine(fileLines, oldLines)

    if (startIdx < 0) {
        return buildList {
            oldLines.forEach { add(DiffLine.Removed(it)) }
            newLines.forEach { add(DiffLine.Added(it)) }
        }
    }

    val contextBefore = fileLines.subList(maxOf(0, startIdx - contextLines), startIdx)
    val contextAfter = fileLines.subList(
        startIdx + oldLines.size,
        minOf(fileLines.size, startIdx + oldLines.size + contextLines),
    )

    return buildList {
        contextBefore.forEach { add(DiffLine.Context(it)) }
        oldLines.forEach { add(DiffLine.Removed(it)) }
        newLines.forEach { add(DiffLine.Added(it)) }
        contextAfter.forEach { add(DiffLine.Context(it)) }
    }
}

/** [fileLines] 内で [oldLines] が連続して一致し始める先頭行インデックスを返す。見つからない場合は -1。 */
private fun findOldStringStartLine(fileLines: List<String>, oldLines: List<String>): Int {
    if (oldLines.isEmpty()) return -1
    outer@ for (i in 0..fileLines.size - oldLines.size) {
        for (j in oldLines.indices) {
            if (fileLines[i + j] != oldLines[j]) continue@outer
        }
        return i
    }
    return -1
}

// ─────────────────────────────────────────────────────────────────────────────
// Composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 汎用コードブロック Composable。
 *
 * - [diffLines] が null の場合: 通常のシンタックスハイライト表示
 * - [diffLines] が non-null の場合: diff 表示（+/-/コンテキスト行）
 * - [onOpenDiff] が non-null の場合: ヘッダーに「開く」ボタンを表示
 */
@OptIn(ExperimentalJewelApi::class)
@Composable
fun CodeBlock(
    content: String,
    language: String = "",
    modifier: Modifier = Modifier,
    diffLines: List<DiffLine>? = null,
    onOpenDiff: (() -> Unit)? = null,
    styling: MarkdownStyling.Code.Fenced? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val codeBlockBg = ChatTheme.CodeBlock.background
    val headerBg = ChatTheme.CodeBlock.headerBackground
    val borderColor = ChatTheme.CodeBlock.border

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(codeBlockBg, shape)
            .border(1.dp, borderColor, shape),
    ) {
        // ── ヘッダー行 ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = language.ifEmpty { "code" },
                color = ChatTheme.Text.secondary,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenDiff != null) {
                    IconActionButton(
                        key = AllIconsKeys.Welcome.Open,
                        onClick = onOpenDiff,
                        contentDescription = "開く",
                    )
                    Spacer(Modifier.width(4.dp))
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

        // ── コンテンツ ───────────────────────────────────────────────────────
        if (diffLines != null) {
            DiffContent(diffLines = diffLines)
        } else if (styling != null) {
            // FencedCodeBlock 向け: シンタックスハイライト表示
            HighlightedCodeBlockContent(
                content = content,
                styling = styling,
            )
        } else {
            // フォールバック: プレーンテキスト
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

// ─────────────────────────────────────────────────────────────────────────────
// Diff 表示
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiffContent(diffLines: List<DiffLine>) {
    val addedBg = ChatTheme.Diff.addedBackground
    val removedBg = ChatTheme.Diff.removedBackground
    val addedLabel = ChatTheme.Diff.addedLabel
    val removedLabel = ChatTheme.Diff.removedLabel
    val contextColor = ChatTheme.Text.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        diffLines.forEach { line ->
            val (prefix, bg, labelColor) = when (line) {
                is DiffLine.Added -> Triple("+", addedBg, addedLabel)
                is DiffLine.Removed -> Triple("-", removedBg, removedLabel)
                is DiffLine.Context -> Triple(" ", Color.Transparent, contextColor)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 12.dp, vertical = 1.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = prefix,
                    color = labelColor,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(14.dp),
                )
                Text(
                    text = line.text,
                    fontFamily = FontFamily.Monospace,
                    color = if (line is DiffLine.Context) contextColor else labelColor,
                    softWrap = false,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// シンタックスハイライト表示（FencedCodeBlock 向け）
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalJewelApi::class)
@Composable
internal fun HighlightedCodeBlockContent(
    content: String,
    styling: MarkdownStyling.Code.Fenced,
    block: FencedCodeBlock? = null,
) {
    val highlighter = LocalCodeHighlighter.current
    val mimeType = block?.mimeType

    val highlightedText = if (mimeType != null) {
        androidx.compose.runtime.produceState(
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
