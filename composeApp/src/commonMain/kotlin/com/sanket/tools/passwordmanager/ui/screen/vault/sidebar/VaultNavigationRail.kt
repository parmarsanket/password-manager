package com.sanket.tools.passwordmanager.ui.screen.vault.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Width of the collapsed icon-only rail. */
internal val RailWidth = 72.dp

/**
 * Icon-only NavigationRail — shows toggle, shield badge, import, export, lock.
 *
 * Custom implementation (not M3 NavigationRail) because we only need action
 * icons, not destination-based navigation with selection state.
 */
@Composable
internal fun VaultNavigationRail(
    onExpand: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState())
            .width(RailWidth)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Expand toggle
        IconButton(onClick = onExpand) {
            Icon(
                imageVector        = Icons.Default.Menu,
                contentDescription = "Expand sidebar",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Shield brand icon
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Icon(
                imageVector        = Icons.Default.Shield,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.padding(10.dp).size(22.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        // Action icons — bottom-anchored for easy thumb reach
        RailActionButton(
            icon    = Icons.Default.Download,
            label   = "Import",
            onClick = onImport,
        )
        RailActionButton(
            icon    = Icons.Default.FileUpload,
            label   = "Export",
            onClick = onExport,
        )
        RailActionButton(
            icon    = Icons.AutoMirrored.Filled.ExitToApp,
            label   = "Lock",
            onClick = onLogout,
        )
    }
}

/**
 * Single icon button in the navigation rail with a small label below.
 */
@Composable
private fun RailActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp),
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
