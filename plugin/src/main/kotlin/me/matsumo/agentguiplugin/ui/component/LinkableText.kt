package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.intellij.ide.BrowserUtil

private val URL_REGEX = Regex("""https?://[^\s<>"')\]}>]+""")
private const val URL_TAG = "URL"

@Composable
fun LinkableText(
    text: String,
    style: TextStyle,
    linkColor: Color,
    modifier: Modifier = Modifier,
) {
    val annotated = remember(text, linkColor) {
        buildLinkedString(text, linkColor)
    }

    @Suppress("DEPRECATION")
    ClickableText(
        text = annotated,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(URL_TAG, offset, offset)
                .firstOrNull()
                ?.let { BrowserUtil.browse(it.item) }
        },
    )
}

private fun buildLinkedString(text: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in URL_REGEX.findAll(text)) {
            append(text.substring(lastIndex, match.range.first))
            pushStringAnnotation(tag = URL_TAG, annotation = match.value)
            pushStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
            append(match.value)
            pop()
            pop()
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
