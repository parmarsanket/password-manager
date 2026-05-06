package com.sanket.tools.passwordmanager.ui.screen.vault.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.component.VaultSearchField

/**
 * Compact sidebar for PhoneLandscape (~220 dp wide, ~360 dp tall).
 * Replaces the full VaultHeroCard (which is too tall for landscape) with:
 * - App title + shield icon (one line)
 * - Entry / secret counts (compact chips)
 * - Search field
 * - Import / Export / Lock as small full-width buttons
 * All wrapped in a verticalScroll so nothing is cut off.
 */
@Composable
internal fun VaultPhoneLandscapeSidebar(
    modifier: Modifier,
    searchQuery: String,
    totalEntries: Int,
    totalSecrets: Int,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit,
    onCollapse:() -> Unit

) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){


            // Compact title row
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp).size(18.dp),
                        )
                    }
                    Text(
                        text = "Vault",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                // Compact stats
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "$totalEntries",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = { Icon(Icons.Default.Key, null, Modifier.size(14.dp)) },
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "$totalSecrets",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = { Icon(Icons.Default.Lock, null, Modifier.size(14.dp)) },
                    )
                }
            }
            IconButton(onClick = onCollapse )
            {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = "Collapse sidebar",
//                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // Search
        VaultSearchField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(4.dp))

        // ── Action buttons — compact, always visible ───────────────────────
        FilledTonalButton(
            onClick  = onImport,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Download, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Import", style = MaterialTheme.typography.labelMedium)
        }
        FilledTonalButton(
            onClick  = onExport,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.FileUpload, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Export", style = MaterialTheme.typography.labelMedium)
        }
        FilledTonalButton(
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Lock", style = MaterialTheme.typography.labelMedium)
        }
    }
}
