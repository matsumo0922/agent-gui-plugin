package me.matsumo.agentguiplugin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.theme.colorPalette

/**
 * Chat UI theme — a thin façade over [JewelTheme].
 *
 * Colour tokens delegate to JewelTheme so the chat UI automatically
 * matches the IDE's light / dark appearance.  Properties that have no
 * JewelTheme counterpart (syntax-highlight colours, gradients, spacing,
 * radii) are kept as hard-coded constants.
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
        val primary: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.panelBackground

        val secondary: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.gray(2) else JewelTheme.colorPalette.gray(12)

        val muted: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.gray(2) else JewelTheme.colorPalette.gray(13)

        val mutedHalf: Color
            @Composable @ReadOnlyComposable
            get() = muted.copy(alpha = 0.5f)
    }

    // ──────────────────────────────────────────
    // Foreground / text colors
    // ──────────────────────────────────────────

    object Text {
        val primary: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.text.normal

        val secondary: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.text.info

        val muted: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.text.disabled

        val onPrimary: Color get() = Color.White
    }

    // ──────────────────────────────────────────
    // Border colors
    // ──────────────────────────────────────────

    object Border {
        val default: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.borders.normal

        val subtle: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.borders.disabled
    }

    // ──────────────────────────────────────────
    // User message colors
    // ──────────────────────────────────────────

    object UserMessage {
        val background: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.gray(1).copy(alpha = 0.5f)
            } else {
                JewelTheme.colorPalette.gray(13)
            }

        val border: Color
            @Composable @ReadOnlyComposable
            get() = Border.default
    }

    // ──────────────────────────────────────────
    // Thinking block colors
    // ──────────────────────────────────────────

    object Thinking {
        val background: Color
            @Composable @ReadOnlyComposable
            get() = Background.mutedHalf

        val border: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.borders.normal.copy(alpha = 0.4f)

        val iconDefault: Color
            @Composable @ReadOnlyComposable
            get() = Text.muted

        val iconHover: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.colorPalette.yellow(7)

        val text: Color
            @Composable @ReadOnlyComposable
            get() = Text.secondary
    }

    // ──────────────────────────────────────────
    // Tool use block colors (blue theme)
    // ──────────────────────────────────────────

    object ToolUse {
        val background: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.blue(1).copy(alpha = 0.2f)
            } else {
                JewelTheme.colorPalette.blue(12).copy(alpha = 0.5f)
            }

        val border: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.blue(4) else JewelTheme.colorPalette.blue(10)

        val iconColor: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.colorPalette.blue(7)

        val toolNameColor: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.blue(9) else JewelTheme.colorPalette.blue(3)

        val paramKeyColor: Color
            @Composable @ReadOnlyComposable
            get() = Text.secondary

        val paramValueColor: Color
            @Composable @ReadOnlyComposable
            get() = Text.primary
    }

    // ──────────────────────────────────────────
    // Code block colors
    // ──────────────────────────────────────────

    object CodeBlock {
        val background: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.gray(1) else JewelTheme.colorPalette.gray(13)

        val headerBackground: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.gray(2)
            } else {
                JewelTheme.colorPalette.gray(12)
            }

        val border: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.gray(3)
            } else {
                JewelTheme.colorPalette.gray(11)
            }
    }

    // ──────────────────────────────────────────
    // Input area colors
    // ──────────────────────────────────────────

    object Input {
        val background: Color
            @Composable @ReadOnlyComposable
            get() = Background.primary

        val border: Color
            @Composable @ReadOnlyComposable
            get() = Border.default

        val placeholder: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.text.disabled.copy(alpha = 0.5f)

        // Gradient colors — hard-coded (used in non-recomposable `remember {}`)
        val gradientStart: Color get() = Color(0xFF3B82F6)
        val gradientMid: Color get() = Color(0xFFA855F7)
        val gradientEnd: Color get() = Color(0xFFEC4899)

        val sendGradientStart: Color get() = Color(0xFF2563EB)
        val sendGradientMid: Color get() = Color(0xFF4F46E5)
        val sendGradientEnd: Color get() = Color(0xFF9333EA)
    }

    // ──────────────────────────────────────────
    // Status colors
    // ──────────────────────────────────────────

    object Status {
        val streaming: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.colorPalette.blue(7)

        val error: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.outlines.error

        val ready: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.colorPalette.green(7)

        val connecting: Color
            @Composable @ReadOnlyComposable
            get() = JewelTheme.globalColors.outlines.warning
    }

    // ──────────────────────────────────────────
    // Diff block colors
    // ──────────────────────────────────────────

    object Diff {
        val addedBackground: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.green(1).copy(alpha = 0.35f)
            } else {
                JewelTheme.colorPalette.green(12).copy(alpha = 0.45f)
            }

        val removedBackground: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) {
                JewelTheme.colorPalette.red(1).copy(alpha = 0.35f)
            } else {
                JewelTheme.colorPalette.red(12).copy(alpha = 0.45f)
            }

        val addedLabel: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.green(9) else JewelTheme.colorPalette.green(3)

        val removedLabel: Color
            @Composable @ReadOnlyComposable
            get() = if (JewelTheme.isDark) JewelTheme.colorPalette.red(9) else JewelTheme.colorPalette.red(3)
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
