package com.sanket.tools.passwordmanager.ui.screen.vault.hero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.component.VaultStatChip
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultHeroCard(
    layout: AdaptiveLayoutSpec,
    heroTitleStyle: TextStyle,
    totalEntries: Int,
    totalSecrets: Int,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit
) {
    val heroPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 14.dp    // tighter on phones
        AdaptiveWidthClass.Medium   -> 24.dp
        AdaptiveWidthClass.Expanded -> 28.dp
    }
    val innerSpacing = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 10.dp    // was 18 — saves vertical space
        AdaptiveWidthClass.Medium   -> 16.dp
        AdaptiveWidthClass.Expanded -> 18.dp
    }
    // On phones, suppress the description to keep the card compact.
    // The tagline is redundant when the card scrolls away anyway.
    val showDescription = layout.widthClass != AdaptiveWidthClass.Compact
    val descriptionStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> MaterialTheme.typography.bodySmall
        AdaptiveWidthClass.Medium   -> MaterialTheme.typography.bodyLarge
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.headlineSmall
    }
    val shieldPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 8.dp
        AdaptiveWidthClass.Medium   -> 12.dp
        AdaptiveWidthClass.Expanded -> 14.dp
    }

    Card(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f),
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
                .padding(heroPadding),
            verticalArrangement = Arrangement.spacedBy(innerSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Bug fix: weight(1f) prevents the title column from pushing
                // the shield icon off-screen on narrow Compact layouts
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text  = "Vault",
                        style = heroTitleStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    // Description hidden on phones — card is compact + scrolls away
                    if (showDescription) {
                        Text(
                            text  = "Your private sanctuary for passwords, cards, and everyday credentials.",
                            style = descriptionStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(shieldPadding)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VaultStatChip(icon = Icons.Default.Key, label = "$totalEntries entries")
                VaultStatChip(icon = Icons.Default.Lock, label = "$totalSecrets secrets")
            }

            VaultHeroActions(
                layout = layout,
                onImport = onImport,
                onExport = onExport,
                onLogout = onLogout
            )
        }
    }
}
