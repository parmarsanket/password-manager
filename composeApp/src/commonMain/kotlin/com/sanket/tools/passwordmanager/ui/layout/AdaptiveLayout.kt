package com.sanket.tools.passwordmanager.ui.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  Why width + height together?
//  A phone in landscape is ~700 dp wide (would trigger "Medium" by width alone)
//  but its height is only ~360 dp — there's almost no room to stack content.
//  A tablet portrait is also ~700 dp wide but its height is ~1000 dp.
//  Using BOTH dimensions lets us distinguish "rotated phone" from "real tablet".
//
//  This mirrors how Google's own reference apps (Now in Android, JetNews) work:
//  they use WindowWidthSizeClass + WindowHeightSizeClass together.
//  Here we replicate the same logic with BoxWithConstraints.maxWidth/maxHeight
//  — no extra dependency required for Android + Desktop JVM targets.
// ─────────────────────────────────────────────────────────────────────────────

/** Mirrors Material 3 WindowWidthSizeClass breakpoints. */
enum class AdaptiveWidthClass {
    /** < 600 dp  — phone portrait, very narrow window */
    Compact,
    /** 600–839 dp — tablet portrait, phone landscape (only if height ≥ 480 dp) */
    Medium,
    /** ≥ 840 dp  — tablet landscape, desktop, laptop */
    Expanded
}

/**
 * Describes the detected form-factor / posture of the current screen.
 *
 * Use this to drive two-pane vs single-pane decisions AND content density.
 */
enum class AdaptivePosture {
    /** Phone portrait — narrow, tall — single column */
    PhonePortrait,
    /**
     * Phone landscape — wide but very short (height < 480 dp).
     * Keep single-column; scrolling is tight.
     * widthClass = Compact or Medium, but NEVER two-pane.
     */
    PhoneLandscape,
    /** Tablet portrait — medium-wide and tall — richer single column */
    TabletPortrait,
    /** Tablet landscape / desktop — wide AND tall — two-pane sidebar */
    TabletLandscape,
}

@Immutable
data class AdaptiveLayoutSpec(
    val widthClass: AdaptiveWidthClass,
    val posture: AdaptivePosture,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val contentMaxWidth: Dp,
    val formMaxWidth: Dp,
    val listMinCellSize: Dp,
    val paneSpacing: Dp,
    /** Width of the left sidebar — meaningful only when useTwoPaneLayout = true. */
    val sidebarWidth: Dp,
    /**
     * Width of the narrow action strip shown in PhoneLandscape mode.
     * Zero on all other postures — only the PhoneLandscape arm sets this.
     */
    val compactSidebarWidth: Dp,
    /**
     * Standard vertical spacing between siblings in Column layouts.
     * Centralises the 18 dp / 12 dp / 20 dp values that were scattered inline.
     */
    val contentSpacing: Dp,
    /**
     * True only for TabletLandscape posture (≥ 840 dp wide AND ≥ 480 dp tall).
     * Deliberately false for PhoneLandscape so rotating a phone never shows two panes.
     */
    val useTwoPaneLayout: Boolean,
)

/**
 * Main entry-point. Call this from inside BoxWithConstraints so you have
 * both the available [maxWidth] and [maxHeight].
 *
 * Decision table:
 * ┌──────────────────────────┬────────────┬──────────────┬───────────────────┐
 * │ Scenario                 │ Width      │ Height       │ Posture           │
 * ├──────────────────────────┼────────────┼──────────────┼───────────────────┤
 * │ Phone portrait           │ < 600 dp   │ any          │ PhonePortrait     │
 * │ Phone landscape          │ ≥ 600 dp   │ < 480 dp     │ PhoneLandscape    │
 * │ Tablet portrait          │ 600–839 dp │ ≥ 480 dp     │ TabletPortrait    │
 * │ Tablet landscape/desktop │ ≥ 840 dp   │ ≥ 480 dp     │ TabletLandscape   │
 * └──────────────────────────┴────────────┴──────────────┴───────────────────┘
 */
fun adaptiveLayoutSpec(maxWidth: Dp, maxHeight: Dp): AdaptiveLayoutSpec {
    val isShortScreen = maxHeight < 480.dp   // phone landscape threshold

    return when {
        // ── Phone portrait ──────────────────────────────────────────────────
        maxWidth < 600.dp -> AdaptiveLayoutSpec(
            widthClass          = AdaptiveWidthClass.Compact,
            posture             = AdaptivePosture.PhonePortrait,
            horizontalPadding   = 16.dp,
            verticalPadding     = 16.dp,
            contentMaxWidth     = 560.dp,
            formMaxWidth        = 440.dp,
            listMinCellSize     = 260.dp,
            paneSpacing         = 16.dp,
            sidebarWidth        = 0.dp,
            compactSidebarWidth = 0.dp,
            contentSpacing      = 18.dp,
            useTwoPaneLayout    = false,
        )

        // ── Phone landscape — wide but VERY short ──────────────────────────
        // Width is ≥ 600 dp here, but height < 480 dp → it's a rotated phone
        // We keep a single-column compact-feeling layout to avoid content
        // being squeezed by a tall sidebar on a short screen.
        isShortScreen -> AdaptiveLayoutSpec(
            widthClass          = AdaptiveWidthClass.Medium,
            posture             = AdaptivePosture.PhoneLandscape,
            horizontalPadding   = 24.dp,
            verticalPadding     = 8.dp,    // less vertical breathing room
            contentMaxWidth     = 800.dp,
            formMaxWidth        = 560.dp,
            listMinCellSize     = 280.dp,
            paneSpacing         = 20.dp,
            sidebarWidth        = 0.dp,
            compactSidebarWidth = 220.dp,  // narrow action strip for landscape
            contentSpacing      = 12.dp,   // tighter on short screens
            useTwoPaneLayout    = false,
        )

        // ── Tablet portrait (600–839 dp wide, height ≥ 480 dp) ─────────────
        maxWidth < 840.dp -> AdaptiveLayoutSpec(
            widthClass          = AdaptiveWidthClass.Medium,
            posture             = AdaptivePosture.TabletPortrait,
            horizontalPadding   = 24.dp,
            verticalPadding     = 20.dp,
            contentMaxWidth     = 800.dp,
            formMaxWidth        = 560.dp,
            listMinCellSize     = 280.dp,
            paneSpacing         = 20.dp,
            sidebarWidth        = 0.dp,
            compactSidebarWidth = 0.dp,
            contentSpacing      = 18.dp,
            useTwoPaneLayout    = false,
        )

        // ── Tablet landscape / Desktop (≥ 840 dp wide, height ≥ 480 dp) ────
        else -> AdaptiveLayoutSpec(
            widthClass          = AdaptiveWidthClass.Expanded,
            posture             = AdaptivePosture.TabletLandscape,
            horizontalPadding   = 32.dp,
            verticalPadding     = 24.dp,
            contentMaxWidth     = 1200.dp,
            formMaxWidth        = 600.dp,
            listMinCellSize     = 320.dp,
            paneSpacing         = 24.dp,
            sidebarWidth        = 360.dp,
            compactSidebarWidth = 0.dp,
            contentSpacing      = 20.dp,
            useTwoPaneLayout    = true,
        )
    }
}
