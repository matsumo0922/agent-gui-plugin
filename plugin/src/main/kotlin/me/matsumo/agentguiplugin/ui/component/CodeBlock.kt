package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CodeBlock(
    code: String,
    language: String? = null,
    modifier: Modifier = Modifier,
) {
    val highlighted = remember(code, language) {
        highlightCode(code, language?.lowercase())
    }

    var copyLabel by remember { mutableStateOf("Copy") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ChatTheme.Code.background,
                shape = RoundedCornerShape(ChatTheme.Radius.medium),
            ),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = ChatTheme.Code.headerBackground,
                    shape = RoundedCornerShape(
                        topStart = ChatTheme.Radius.medium,
                        topEnd = ChatTheme.Radius.medium,
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = (language ?: "code").uppercase(),
                color = ChatTheme.Code.languageLabel,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )

            OutlinedButton(
                onClick = {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(StringSelection(code), null)
                    copyLabel = "Copied!"
                    scope.launch {
                        delay(2000)
                        copyLabel = "Copy"
                    }
                },
            ) {
                Text(copyLabel, fontSize = 11.sp)
            }
        }

        // Code content with horizontal scroll
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(12.dp),
        ) {
            Text(
                text = highlighted,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

// ──────────────────────────────────────────
// Syntax highlighting engine
// ──────────────────────────────────────────

private fun highlightCode(code: String, language: String?): AnnotatedString {
    val rules = when (language) {
        "kotlin", "kt" -> kotlinRules
        "java" -> javaRules
        "python", "py" -> pythonRules
        "javascript", "js" -> jsRules
        "typescript", "ts" -> tsRules
        "bash", "shell", "sh", "zsh" -> bashRules
        "json" -> jsonRules
        "xml", "html", "svg" -> xmlRules
        else -> null
    }

    if (rules == null) {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = ChatTheme.Code.text)) {
                append(code)
            }
        }
    }

    return applyHighlighting(code, rules)
}

private data class HighlightRule(
    val pattern: Regex,
    val color: Color,
)

private fun applyHighlighting(code: String, rules: List<HighlightRule>): AnnotatedString {
    data class ColoredRange(val start: Int, val end: Int, val color: Color)

    val ranges = mutableListOf<ColoredRange>()

    for (rule in rules) {
        rule.pattern.findAll(code).forEach { match ->
            val range = if (match.groups.size > 1 && match.groups[1] != null) {
                val g = match.groups[1]!!
                ColoredRange(g.range.first, g.range.last + 1, rule.color)
            } else {
                ColoredRange(match.range.first, match.range.last + 1, rule.color)
            }
            // Only add if no existing range covers this start position
            if (ranges.none { it.start <= range.start && it.end > range.start }) {
                ranges.add(range)
            }
        }
    }

    ranges.sortBy { it.start }

    return buildAnnotatedString {
        var pos = 0
        for (r in ranges) {
            if (r.start > pos) {
                withStyle(SpanStyle(color = ChatTheme.Code.text)) {
                    append(code.substring(pos, r.start))
                }
            }
            if (r.start >= pos) {
                withStyle(SpanStyle(color = r.color)) {
                    append(code.substring(r.start, r.end))
                }
                pos = r.end
            }
        }
        if (pos < code.length) {
            withStyle(SpanStyle(color = ChatTheme.Code.text)) {
                append(code.substring(pos))
            }
        }
    }
}

// ──────────────────────────────────────────
// Language rules
// ──────────────────────────────────────────

private val kotlinRules: List<HighlightRule> by lazy {
    listOf(
        // Line comments
        HighlightRule(Regex("//.*"), ChatTheme.Code.comment),
        // Block comments
        HighlightRule(Regex("/\\*[\\s\\S]*?\\*/"), ChatTheme.Code.comment),
        // Strings
        HighlightRule(Regex("\"\"\"[\\s\\S]*?\"\"\""), ChatTheme.Code.string),
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        // Annotations
        HighlightRule(Regex("@\\w+"), ChatTheme.Code.property),
        // Numbers
        HighlightRule(Regex("\\b\\d+[.\\d]*[fFLl]?\\b"), ChatTheme.Code.number),
        // Keywords
        HighlightRule(
            Regex("\\b(fun|val|var|class|object|interface|sealed|data|enum|abstract|open|override|private|protected|public|internal|companion|import|package|return|if|else|when|for|while|do|try|catch|finally|throw|break|continue|in|is|as|by|suspend|inline|reified|crossinline|noinline|tailrec|operator|infix|typealias|annotation|const|lateinit|get|set|where|init|constructor|super|this|it|null|true|false)\\b"),
            ChatTheme.Code.keyword,
        ),
        // Types after colon or generic
        HighlightRule(Regex(":\\s*(\\w+)"), ChatTheme.Code.type),
        // Function calls
        HighlightRule(Regex("\\b(\\w+)\\s*\\("), ChatTheme.Code.function),
    )
}

private val javaRules: List<HighlightRule> by lazy {
    listOf(
        HighlightRule(Regex("//.*"), ChatTheme.Code.comment),
        HighlightRule(Regex("/\\*[\\s\\S]*?\\*/"), ChatTheme.Code.comment),
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("@\\w+"), ChatTheme.Code.property),
        HighlightRule(Regex("\\b\\d+[.\\d]*[fFLdD]?\\b"), ChatTheme.Code.number),
        HighlightRule(
            Regex("\\b(class|interface|enum|extends|implements|abstract|final|static|public|private|protected|void|return|if|else|for|while|do|try|catch|finally|throw|throws|new|import|package|synchronized|volatile|transient|native|this|super|switch|case|default|break|continue|instanceof|null|true|false|int|long|double|float|boolean|char|byte|short|var|record|sealed|permits)\\b"),
            ChatTheme.Code.keyword,
        ),
        HighlightRule(Regex("\\b(\\w+)\\s*\\("), ChatTheme.Code.function),
    )
}

private val pythonRules: List<HighlightRule> by lazy {
    listOf(
        // Comments
        HighlightRule(Regex("#.*"), ChatTheme.Code.comment),
        // Triple-quoted strings
        HighlightRule(Regex("\"\"\"[\\s\\S]*?\"\"\""), ChatTheme.Code.string),
        HighlightRule(Regex("'''[\\s\\S]*?'''"), ChatTheme.Code.string),
        // Strings
        HighlightRule(Regex("[fFrRbBuU]?\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("[fFrRbBuU]?'(?:[^'\\\\]|\\\\.)*'"), ChatTheme.Code.string),
        // Decorators
        HighlightRule(Regex("@\\w+"), ChatTheme.Code.property),
        // Numbers
        HighlightRule(Regex("\\b\\d+[.\\d]*[jJ]?\\b"), ChatTheme.Code.number),
        // Keywords
        HighlightRule(
            Regex("\\b(def|class|import|from|if|elif|else|return|for|while|with|as|try|except|finally|raise|yield|async|await|lambda|pass|break|continue|del|in|not|and|or|is|global|nonlocal|assert|None|True|False|self|cls)\\b"),
            ChatTheme.Code.keyword,
        ),
        HighlightRule(Regex("\\b(\\w+)\\s*\\("), ChatTheme.Code.function),
    )
}

private val jsRules: List<HighlightRule> by lazy {
    listOf(
        HighlightRule(Regex("//.*"), ChatTheme.Code.comment),
        HighlightRule(Regex("/\\*[\\s\\S]*?\\*/"), ChatTheme.Code.comment),
        HighlightRule(Regex("`(?:[^`\\\\]|\\\\.)*`"), ChatTheme.Code.string),
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("'(?:[^'\\\\]|\\\\.)*'"), ChatTheme.Code.string),
        HighlightRule(Regex("\\b\\d+[.\\d]*\\b"), ChatTheme.Code.number),
        HighlightRule(
            Regex("\\b(const|let|var|function|class|if|else|return|import|export|from|default|async|await|new|this|typeof|instanceof|for|while|do|try|catch|finally|throw|switch|case|break|continue|yield|of|in|delete|void|null|undefined|true|false|NaN)\\b"),
            ChatTheme.Code.keyword,
        ),
        HighlightRule(Regex("\\b(\\w+)\\s*\\("), ChatTheme.Code.function),
    )
}

private val tsRules: List<HighlightRule> by lazy {
    listOf(
        HighlightRule(Regex("//.*"), ChatTheme.Code.comment),
        HighlightRule(Regex("/\\*[\\s\\S]*?\\*/"), ChatTheme.Code.comment),
        HighlightRule(Regex("`(?:[^`\\\\]|\\\\.)*`"), ChatTheme.Code.string),
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("'(?:[^'\\\\]|\\\\.)*'"), ChatTheme.Code.string),
        HighlightRule(Regex("\\b\\d+[.\\d]*\\b"), ChatTheme.Code.number),
        HighlightRule(
            Regex("\\b(const|let|var|function|class|if|else|return|import|export|from|default|async|await|new|this|typeof|instanceof|for|while|do|try|catch|finally|throw|switch|case|break|continue|yield|of|in|delete|void|null|undefined|true|false|NaN|type|interface|enum|namespace|declare|abstract|implements|extends|as|keyof|readonly|private|protected|public|static|override|satisfies|any|string|number|boolean|unknown|never|object)\\b"),
            ChatTheme.Code.keyword,
        ),
        HighlightRule(Regex("\\b(\\w+)\\s*\\("), ChatTheme.Code.function),
    )
}

private val bashRules: List<HighlightRule> by lazy {
    listOf(
        HighlightRule(Regex("#.*"), ChatTheme.Code.comment),
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("'[^']*'"), ChatTheme.Code.string),
        // Variables
        HighlightRule(Regex("\\$\\{?\\w+\\}?"), ChatTheme.Code.property),
        HighlightRule(Regex("\\b\\d+\\b"), ChatTheme.Code.number),
        HighlightRule(
            Regex("\\b(if|then|else|elif|fi|for|do|done|while|until|case|esac|function|in|select|return|exit|local|export|source|alias|unalias|echo|printf|read|cd|mkdir|rmdir|rm|cp|mv|ls|cat|grep|sed|awk|find|xargs|chmod|chown|sudo|apt|yum|brew|npm|npx|node|git|gradle|docker|curl|wget|pip|python|java|set|unset|shift|trap|eval|exec|test|true|false)\\b"),
            ChatTheme.Code.keyword,
        ),
    )
}

private val jsonRules: List<HighlightRule> by lazy {
    listOf(
        // Keys
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\"\\s*:"), ChatTheme.Code.property),
        // String values
        HighlightRule(Regex(":\\s*(\"(?:[^\"\\\\]|\\\\.)*\")"), ChatTheme.Code.string),
        // Numbers
        HighlightRule(Regex(":\\s*(-?\\d+[.\\d]*)"), ChatTheme.Code.number),
        // Booleans and null
        HighlightRule(Regex("\\b(true|false|null)\\b"), ChatTheme.Code.keyword),
    )
}

private val xmlRules: List<HighlightRule> by lazy {
    listOf(
        // Comments
        HighlightRule(Regex("<!--[\\s\\S]*?-->"), ChatTheme.Code.comment),
        // Tags
        HighlightRule(Regex("</?\\w[\\w.-]*"), ChatTheme.Code.keyword),
        HighlightRule(Regex("/?>"), ChatTheme.Code.keyword),
        // Attribute names
        HighlightRule(Regex("\\b(\\w+)\\s*="), ChatTheme.Code.property),
        // Attribute values
        HighlightRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), ChatTheme.Code.string),
        HighlightRule(Regex("'[^']*'"), ChatTheme.Code.string),
    )
}
