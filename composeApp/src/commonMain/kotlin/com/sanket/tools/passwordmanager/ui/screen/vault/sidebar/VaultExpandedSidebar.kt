package com.sanket.tools.passwordmanager.ui.screen.vault.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.component.VaultSearchField
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.screen.vault.hero.VaultHeroCard

/**
 * Full expanded sidebar content — hero card, search field, and a collapse toggle.
 */
@Composable
internal fun VaultExpandedSidebar(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onCollapse: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(state = rememberScrollState())
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
    ) {
        // Collapse toggle at the top
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.MenuOpen,
                contentDescription = "Collapse sidebar",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        VaultHeroCard(
            layout         = layout,
            heroTitleStyle = heroTitleStyle,
            totalEntries   = totalEntries,
            totalSecrets   = totalSecrets,
            onImport       = onImport,
            onExport       = onExport,
            onLogout       = onLogout,
        )
        VaultSearchField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth(),
        )
    }
}
