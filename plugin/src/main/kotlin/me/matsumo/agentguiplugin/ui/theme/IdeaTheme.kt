package me.matsumo.agentguiplugin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.theme.colorPalette

/**
 * Material3 ColorScheme-style interface for IDE theme colors.
 *
 * All tokens are **role-based** (surface, onSurface, primary, etc.)
 * and must NOT contain component-specific or interaction-state names.
 */
interface IdeaColorScheme {

    // ── Surface ──────────────────────────────────

    /** Main panel / window background. */
    val background: Color

    /** Raised surface (cards, code blocks). */
    val surface: Color

    /** Higher-emphasis container (headers, secondary panels). */
    val surfaceContainer: Color

    // ── On Surface (content on top of surfaces) ──

    /** Primary text / icons on surface. */
    val onSurface: Color

    /** Secondary text / icons on surface. */
    val onSurfaceVariant: Color

    /** Disabled / placeholder text on surface. */
    val onSurfaceDisabled: Color

    // ── Outline ──────────────────────────────────

    /** Default border / divider. */
    val outline: Color

    /** Subtle border / divider. */
    val outlineVariant: Color

    // ── Primary (accent / brand) ────────────────

    /** Primary accent color (buttons, links, active indicators). */
    val primary: Color

    /** Opaque container tone derived from primary. Call sites apply alpha as needed. */
    val primaryContainer: Color

    /** Content color on primaryContainer (accent text, link text). */
    val onPrimaryContainer: Color

    // ── Semantic ─────────────────────────────────

    val error: Color
    val warning: Color
    val success: Color

    // TODO: Diff colors (addedBackground, removedBackground, addedLabel, removedLabel)
    // TODO: Gradient colors for input decoration
}

// ─────────────────────────────────────────────────
// Light implementation
// ─────────────────────────────────────────────────

internal data class LightIdeaColorScheme(
    override val background: Color,
    override val onSurface: Color,
    override val onSurfaceVariant: Color,
    override val onSurfaceDisabled: Color,
    override val outline: Color,
    override val outlineVariant: Color,
    override val error: Color,
    override val warning: Color,
    private val gray12: Color,
    private val gray13: Color,
    private val blue3: Color,
    private val blue7: Color,
    private val blue12: Color,
    private val green7: Color,
) : IdeaColorScheme {
    override val surface: Color get() = gray13
    override val surfaceContainer: Color get() = gray12
    override val primary: Color get() = blue7
    override val primaryContainer: Color get() = blue12
    override val onPrimaryContainer: Color get() = blue3
    override val success: Color get() = green7
}

// ─────────────────────────────────────────────────
// Dark implementation
// ─────────────────────────────────────────────────

internal data class DarkIdeaColorScheme(
    override val background: Color,
    override val onSurface: Color,
    override val onSurfaceVariant: Color,
    override val onSurfaceDisabled: Color,
    override val outline: Color,
    override val outlineVariant: Color,
    override val error: Color,
    override val warning: Color,
    private val gray1: Color,
    private val gray2: Color,
    private val blue1: Color,
    private val blue7: Color,
    private val blue9: Color,
    private val green7: Color,
) : IdeaColorScheme {
    override val surface: Color get() = gray1
    override val surfaceContainer: Color get() = gray2
    override val primary: Color get() = blue7
    override val primaryContainer: Color get() = blue1
    override val onPrimaryContainer: Color get() = blue9
    override val success: Color get() = green7
}

// ─────────────────────────────────────────────────
// CompositionLocal + IdeaTheme entry point
// ─────────────────────────────────────────────────

private val LocalIdeaColorScheme = staticCompositionLocalOf<IdeaColorScheme> {
    error("IdeaTheme not provided. Wrap your content with IdeaTheme { ... }")
}

object IdeaTheme {

    val colorScheme: IdeaColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalIdeaColorScheme.current

    object spacing {
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 20.dp
    }

    object shapes {
        val small: Dp = 4.dp
        val medium: Dp = 8.dp
        val large: Dp = 12.dp
        val full: Dp = 9999.dp
    }
}

/**
 * Provides [IdeaTheme] by reading the current [JewelTheme] and
 * selecting the appropriate light/dark color scheme.
 *
 * Usage:
 * ```
 * IdeaTheme {
 *     // IdeaTheme.colorScheme is now available
 *     Text("Hello", color = IdeaTheme.colorScheme.onSurface)
 * }
 * ```
 */
@Suppress("DEPRECATION")
@Composable
fun IdeaTheme(content: @Composable () -> Unit) {
    val scheme = if (JewelTheme.isDark) {
        DarkIdeaColorScheme(
            background = JewelTheme.globalColors.panelBackground,
            onSurface = JewelTheme.globalColors.text.normal,
            onSurfaceVariant = JewelTheme.globalColors.text.info,
            onSurfaceDisabled = JewelTheme.globalColors.text.disabled,
            outline = JewelTheme.globalColors.borders.normal,
            outlineVariant = JewelTheme.globalColors.borders.disabled,
            error = JewelTheme.globalColors.text.error,
            warning = JewelTheme.globalColors.outlines.warning,
            gray1 = JewelTheme.colorPalette.gray(1),
            gray2 = JewelTheme.colorPalette.gray(2),
            blue1 = JewelTheme.colorPalette.blue(1),
            blue7 = JewelTheme.colorPalette.blue(7),
            blue9 = JewelTheme.colorPalette.blue(9),
            green7 = JewelTheme.colorPalette.green(7),
        )
    } else {
        LightIdeaColorScheme(
            background = JewelTheme.globalColors.panelBackground,
            onSurface = JewelTheme.globalColors.text.normal,
            onSurfaceVariant = JewelTheme.globalColors.text.info,
            onSurfaceDisabled = JewelTheme.globalColors.text.disabled,
            outline = JewelTheme.globalColors.borders.normal,
            outlineVariant = JewelTheme.globalColors.borders.disabled,
            error = JewelTheme.globalColors.text.error,
            warning = JewelTheme.globalColors.outlines.warning,
            gray12 = JewelTheme.colorPalette.gray(12),
            gray13 = JewelTheme.colorPalette.gray(13),
            blue3 = JewelTheme.colorPalette.blue(3),
            blue7 = JewelTheme.colorPalette.blue(7),
            blue12 = JewelTheme.colorPalette.blue(12),
            green7 = JewelTheme.colorPalette.green(7),
        )
    }

    CompositionLocalProvider(
        LocalIdeaColorScheme provides scheme,
        content = content,
    )
}
