package com.sanket.tools.passwordmanager.ui.screen.vault.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.screen.vault.list.VaultContentPane
import com.sanket.tools.passwordmanager.ui.screen.vault.sidebar.VaultSidebar

/**
 * Two-pane layout used for both TabletLandscape (sidebarModifier = full width)
 * and PhoneLandscape (sidebarModifier = narrow compactSidebarWidth).
 * The only difference between the two callers is [sidebarModifier].
 */
@Composable
internal fun TwoPaneLayout(
    layout: AdaptiveLayoutSpec,
    sidebarModifier: Modifier,
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
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = layout.horizontalPadding,
                vertical   = layout.verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing),
    ) {
        VaultSidebar(
            modifier       = sidebarModifier.fillMaxHeight(),
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            searchQuery    = searchQuery,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            onSearchChange = onSearchChange,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
        )
        VaultContentPane(
            modifier        = Modifier.weight(1f),
            layout          = layout,
            items           = items,
            onEntrySelected = onEntrySelected,
        )
    }
}
