package me.matsumo.agentguiplugin.ui.component.mention

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MentionVisualTransformation(
    private val confirmedMentions: Set<String>,
    private val activeMentionRange: IntRange?,
    private val mentionColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        val style = SpanStyle(color = mentionColor)
        val rawText = text.text

        // Highlight confirmed mentions
        for (mention in confirmedMentions) {
            val pattern = "@$mention"
            var startIndex = 0

            while (true) {
                val index = rawText.indexOf(pattern, startIndex)
                if (index == -1) break

                // Validate: must be at start or preceded by whitespace
                val validStart = index == 0 || rawText[index - 1].isWhitespace()
                if (validStart) {
                    spanStyles.add(
                        AnnotatedString.Range(style, index, index + pattern.length)
                    )
                }

                startIndex = index + pattern.length
            }
        }

        // Highlight active mention range (typing)
        if (activeMentionRange != null) {
            val start = activeMentionRange.first.coerceIn(0, rawText.length)
            val end = (activeMentionRange.last + 1).coerceIn(0, rawText.length)
            if (start < end) {
                spanStyles.add(AnnotatedString.Range(style, start, end))
            }
        }

        return TransformedText(
            AnnotatedString(rawText, spanStyles + text.spanStyles, text.paragraphStyles),
            OffsetMapping.Identity,
        )
    }
}
