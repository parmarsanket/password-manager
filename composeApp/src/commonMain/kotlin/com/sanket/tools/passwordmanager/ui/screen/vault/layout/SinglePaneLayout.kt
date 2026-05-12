package com.sanket.tools.passwordmanager.ui.screen.vault.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec

/**
 * Single-column layout.
 * - Phone portrait (centered=false) → CollapsiblePhonePortraitLayout:
 *   hero scrolls away, collapsed TopAppBar animates in with search + actions menu.
 * - Tablet portrait (centered=true)  → centered, width-constrained Column (unchanged).
 */
@Composable
internal fun SinglePaneLayout(
    layout: AdaptiveLayoutSpec,
    centered: Boolean,
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

        CollapsiblePortraitLayout(
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            searchQuery    = searchQuery,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            items          = items,
            onSearchChange = onSearchChange,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
            onEntrySelected = onEntrySelected,
        )
}
