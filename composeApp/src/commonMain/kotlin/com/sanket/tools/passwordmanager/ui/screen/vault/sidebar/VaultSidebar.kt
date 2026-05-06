package com.sanket.tools.passwordmanager.ui.screen.vault.sidebar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture

/**
 * Sidebar used in two-pane layouts.
 *
 * - **PhoneLandscape**: always shows the compact [VaultPhoneLandscapeSidebar]
 *   (screen is too short for a rail toggle to be useful).
 * - **Tablet / Desktop**: toggles between a slim [VaultNavigationRail] (icon-only)
 *   and the full expanded sidebar ([VaultHeroCard] + [VaultSearchField]).
 *   Width animates with a spring, content cross-fades.
 */
@Composable
internal fun VaultSidebar(
    modifier: Modifier,
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val targetWidth = if (isExpanded) layout.sidebarWidth else RailWidth
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "sidebar-width",
    )

    if (layout.posture == AdaptivePosture.PhoneLandscape) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(tween(90)))
                    .using(SizeTransform(clip = false))
            },
            label = "sidebar-content",
        ) { expanded ->
            if (expanded) {

                VaultPhoneLandscapeSidebar(
                    modifier = modifier,
                    searchQuery = searchQuery,
                    totalEntries = totalEntries,
                    totalSecrets = totalSecrets,
                    onSearchChange = onSearchChange,
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                    onCollapse = { isExpanded = false }
                )
            } else {
                Surface(
                    modifier = Modifier,
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 1.dp,
                ) {
                    VaultNavigationRail(
                        onExpand = { isExpanded = true },
                        onImport = onImport,
                        onExport = onExport,
                        onLogout = onLogout,
                    )
                }

            }
        }

    } else {
        // Toggle state — starts expanded


        // Animate width with a spring for a premium feel
        Surface(
            modifier = Modifier.width(animatedWidth),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {

        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(tween(90)))
                    .using(SizeTransform(clip = false))
            },
            label = "sidebar-content",
        ) { expanded ->
            if (expanded) {
                // ── Full sidebar ─────────────────────────────────
                VaultExpandedSidebar(
                    layout = layout,
                    heroTitleStyle = heroTitleStyle,
                    searchQuery = searchQuery,
                    totalEntries = totalEntries,
                    totalSecrets = totalSecrets,
                    onSearchChange = onSearchChange,
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                    onCollapse = { isExpanded = false },
                )
            } else {
                // ── Icon-only rail ───────────────────────────────
                VaultNavigationRail(
                    onExpand = { isExpanded = true },
                    onImport = onImport,
                    onExport = onExport,
                    onLogout = onLogout,
                )
            }
        }
    }
}
}
