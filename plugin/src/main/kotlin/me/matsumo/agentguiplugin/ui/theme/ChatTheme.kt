package me.matsumo.agentguiplugin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme

/**
 * Chat UI theme matching claude-code-viewer's design system.
 * Adapts to IDE light/dark theme via Jewel.
 */
object ChatTheme {

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = JewelTheme.isDark

    // ──────────────────────────────────────────
    // Background colors
    // ──────────────────────────────────────────

    object Background {
        val primary: Color @Composable get() = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
        val secondary: Color @Composable get() = if (isDark) Color(0xFF262626) else Color(0xFFF5F5F5)
        val muted: Color @Composable get() = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF7F7F8)
        val mutedHalf: Color @Composable get() = if (isDark) Color(0x802A2A2A) else Color(0x80F7F7F8)
    }

    // ──────────────────────────────────────────
    // Foreground / text colors
    // ──────────────────────────────────────────

    object Text {
        val primary: Color @Composable get() = if (isDark) Color(0xFFFAFAFA) else Color(0xFF171717)
        val secondary: Color @Composable get() = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)
        val muted: Color @Composable get() = if (isDark) Color(0xFF888888) else Color(0xFF9CA3AF)
        val onPrimary: Color get() = Color.White
    }

    // ──────────────────────────────────────────
    // Border colors
    // ──────────────────────────────────────────

    object Border {
        val default: Color @Composable get() = if (isDark) Color(0xFF3A3A3A) else Color(0xFFE5E7EB)
        val subtle: Color @Composable get() = if (isDark) Color(0x66404040) else Color(0x66E5E7EB)
    }

    // ──────────────────────────────────────────
    // User message colors (slate tones)
    // ──────────────────────────────────────────

    object UserMessage {
        val background: Color @Composable get() = if (isDark) Color(0x800F172A) else Color(0xFFF8FAFC)
        val border: Color @Composable get() = Border.default
    }

    // ──────────────────────────────────────────
    // Thinking block colors (muted + yellow accent)
    // ──────────────────────────────────────────

    object Thinking {
        val background: Color @Composable get() = Background.mutedHalf
        val border: Color @Composable get() = if (isDark) Color(0x664A4A4A) else Color(0x66D1D5DB)
        val iconDefault: Color @Composable get() = Text.muted
        val iconHover: Color get() = Color(0xFFCA8A04) // yellow-600
        val text: Color @Composable get() = Text.secondary
    }

    // ──────────────────────────────────────────
    // Tool use block colors (blue theme)
    // ──────────────────────────────────────────

    object ToolUse {
        val background: Color @Composable get() = if (isDark) Color(0x33172554) else Color(0x80EFF6FF)
        val border: Color @Composable get() = if (isDark) Color(0xFF1E3A5F) else Color(0xFFBFDBFE)
        val iconColor: Color @Composable get() = if (isDark) Color(0xFF60A5FA) else Color(0xFF3B82F6)
        val toolNameColor: Color @Composable get() = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF)
        val paramKeyColor: Color @Composable get() = Text.secondary
        val paramValueColor: Color @Composable get() = Text.primary
    }

    // ──────────────────────────────────────────
    // Code block colors (oneDark-inspired)
    // ──────────────────────────────────────────

    object Code {
        val background: Color get() = Color(0xFF1E1E1E)
        val headerBackground: Color get() = Color(0xFF2D2D2D)
        val text: Color get() = Color(0xFFD4D4D4)
        val languageLabel: Color get() = Color(0xFFAAAAAA)

        // Syntax highlighting (oneDark palette)
        val keyword: Color get() = Color(0xFFC678DD)    // purple
        val string: Color get() = Color(0xFF98C379)     // green
        val comment: Color get() = Color(0xFF5C6370)    // gray
        val number: Color get() = Color(0xFFD19A66)     // orange
        val function: Color get() = Color(0xFF61AFEF)   // blue
        val type: Color get() = Color(0xFFE5C07B)       // yellow
        val operator: Color get() = Color(0xFF56B6C2)   // cyan
        val property: Color get() = Color(0xFFE06C75)   // red

        // Inline code
        val inlineBackground: Color @Composable get() = if (isDark) Color(0xFF3A3A3A) else Color(0x80E5E7EB)
        val inlineBorder: Color @Composable get() = Border.default
    }

    // ──────────────────────────────────────────
    // Input area colors
    // ──────────────────────────────────────────

    object Input {
        val background: Color @Composable get() = Background.primary
        val border: Color @Composable get() = Border.default
        val placeholder: Color @Composable get() = if (isDark) Color(0x80888888) else Color(0x809CA3AF)

        // Gradient colors for border effect
        val gradientStart: Color get() = Color(0xFF3B82F6)  // blue-500
        val gradientMid: Color get() = Color(0xFFA855F7)    // purple-500
        val gradientEnd: Color get() = Color(0xFFEC4899)     // pink-500

        // Send button gradient
        val sendGradientStart: Color get() = Color(0xFF2563EB) // blue-600
        val sendGradientMid: Color get() = Color(0xFF4F46E5)   // indigo-600
        val sendGradientEnd: Color get() = Color(0xFF9333EA)   // purple-600
    }

    // ──────────────────────────────────────────
    // Status colors
    // ──────────────────────────────────────────

    object Status {
        val streaming: Color get() = Color(0xFF3B82F6)   // blue-500
        val error: Color get() = Color(0xFFEF4444)       // red-500
        val ready: Color get() = Color(0xFF22C55E)       // green-500
        val connecting: Color get() = Color(0xFFF59E0B)  // amber-500
    }

    // ──────────────────────────────────────────
    // Markdown heading styles
    // ──────────────────────────────────────────

    object Markdown {
        val h1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        val h2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        val h3 = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        val h4 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
        val h5 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        val h6 = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)

        val body = TextStyle(fontSize = 14.sp, lineHeight = 22.sp)
        val code = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)

        val blockquoteBorder: Color @Composable get() = if (isDark) Color(0x4DFAFAFA) else Color(0x4D205BE0)
        val blockquoteBackground: Color @Composable get() = if (isDark) Color(0x4D2A2A2A) else Color(0x4DF7F7F8)
        val linkColor: Color get() = Color(0xFF3B82F6)
        val tableBorder: Color @Composable get() = Border.default
        val tableHeaderBackground: Color @Composable get() = Background.mutedHalf
        val horizontalRule: Color @Composable get() = Border.default
    }

    // ──────────────────────────────────────────
    // Spacing constants
    // ──────────────────────────────────────────

    object Spacing {
        val messageListPadding: Dp = 16.dp
        val messageGap: Dp = 16.dp
        val messageMaxWidth: Dp = 768.dp
        val blockGap: Dp = 8.dp
        val inputPaddingHorizontal: Dp = 20.dp
        val inputPaddingVertical: Dp = 16.dp
        val headerHeight: Dp = 40.dp
    }

    // ──────────────────────────────────────────
    // Corner radius
    // ──────────────────────────────────────────

    object Radius {
        val small: Dp = 4.dp
        val medium: Dp = 8.dp
        val large: Dp = 12.dp
        val full: Dp = 9999.dp
    }
}
