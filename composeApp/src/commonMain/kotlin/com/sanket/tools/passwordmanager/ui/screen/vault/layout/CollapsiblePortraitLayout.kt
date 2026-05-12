package com.sanket.tools.passwordmanager.ui.screen.vault.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.component.VaultSearchField
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.screen.vault.hero.VaultHeroCard
import com.sanket.tools.passwordmanager.ui.screen.vault.list.VaultContentPane
import com.sanket.tools.passwordmanager.ui.screen.vault.scroll.HeroScrollState
import com.sanket.tools.passwordmanager.ui.screen.vault.scroll.collapseByProgress
import com.sanket.tools.passwordmanager.ui.screen.vault.scroll.rememberHeroCollapseUiState
import com.sanket.tools.passwordmanager.ui.screen.vault.scroll.rememberHeroScrollConnection
import com.sanket.tools.passwordmanager.ui.screen.vault.sidebar.VaultCollapsedMenu
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Phone portrait collapsible layout.
 *
 * Hero card collapses with parallax + scale + fade via [HeroScrollState].
 * A compact overflow ⋮ menu fades in once the hero is mostly collapsed.
 * The credential list sits below and receives leftover scroll via nested-scroll.
 *
 * All progress-derived floats use [derivedStateOf] so downstream reads
 * skip recomposition when the computed value hasn't actually changed.
 */
@Composable
internal fun CollapsiblePortraitLayout(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    items: List<CredentialItem>,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onEntrySelected: (Long, androidx.compose.ui.geometry.Rect?) -> Unit,
) {
    val density   = LocalDensity.current

    val maxCollapsePx = remember(density) {
        with(density) { (220.dp - 80.dp).toPx() }
    }

    val scope      = rememberCoroutineScope()
    val heroState  = remember { HeroScrollState(maxCollapsePx, scope) }
    val connection = rememberHeroScrollConnection(heroState)

    // ── Derived UI scalars — recomposition-safe ──────────────────────────

    val collapseUi = rememberHeroCollapseUiState(heroState, maxCollapsePx)
    val heroAreaScrollState = rememberScrollableState { it ->  0f }
    // Overflow menu state — hoisted so it survives alpha threshold crossings


    Column(modifier = Modifier.fillMaxSize().nestedScroll(connection)  .scrollable(
        state       = heroAreaScrollState,
        orientation = Orientation.Vertical,
    )) {

        // ── Hero + Search area ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(horizontal = layout.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero body — collapses with parallax + scale + fade
            Column(
                modifier = Modifier
                    .collapseByProgress(collapseUi.progress)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha        = collapseUi.heroAlpha
                        translationY = collapseUi.heroTranslationY
                        scaleX       = collapseUi.heroScale
                        scaleY       = collapseUi.heroScale
                    },
            ) {
                VaultHeroCard(
                    layout         = layout,
                    heroTitleStyle = heroTitleStyle,
                    totalEntries   = totalEntries,
                    totalSecrets   = totalSecrets,
                    onImport       = onImport,
                    onExport       = onExport,
                    onLogout       = onLogout,
                )
                Spacer(modifier = Modifier.height(layout.contentSpacing))
            }

            // Search + compact overflow menu row
            Row(verticalAlignment = Alignment.CenterVertically) {
                VaultSearchField(
                    value         = searchQuery,
                    onValueChange = onSearchChange,
                    modifier      = Modifier.weight(1f),
                )
                VaultCollapsedMenu(collapseUi.overflowAlpha,  onImport, onExport, onLogout)
            }
            Spacer(modifier = Modifier.height(layout.verticalPadding / 2))
        }
        // ── Credential list ─────────────────────────────────────────────
        VaultContentPane(
            modifier        = Modifier.weight(1f),
            layout          = layout,
            items           = items,
            onEntrySelected = onEntrySelected,
        )

    }
}
