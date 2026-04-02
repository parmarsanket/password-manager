package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass

// ─────────────────────────────────────────────────────────────────────────────
//  Layout helpers — eliminate duplicated Row+Sidebar+ContentPane blocks
// ─────────────────────────────────────────────────────────────────────────────

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
    onEntrySelected: (Long) -> Unit,
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

/**
 * Single-column layout used for tablet portrait (centered = true)
 * and phone portrait (centered = false).
 * Uses [layout.contentSpacing] instead of the old inline 18.dp.
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
    onEntrySelected: (Long) -> Unit,
) {
    val outerModifier = Modifier
        .fillMaxSize()
        .padding(
            horizontal = layout.horizontalPadding,
            vertical   = layout.verticalPadding,
        )

    @Composable
    fun Content(modifier: Modifier) {
        Column(
            modifier            = modifier,
            verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
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
            VaultSearchField(
                value         = searchQuery,
                onValueChange = onSearchChange,
                modifier      = Modifier.fillMaxWidth(),
            )
            VaultContentPane(
                modifier        = Modifier.weight(1f),
                layout          = layout,
                items           = items,
                onEntrySelected = onEntrySelected,
            )
        }
    }

    if (centered) {
        Box(modifier = outerModifier, contentAlignment = Alignment.TopCenter) {
            Content(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = layout.contentMaxWidth),
            )
        }
    } else {
        Content(modifier = outerModifier.fillMaxHeight())
    }
}

// ─────────────────────────────────────────────────────────────────────────────

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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
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
        VaultSearchField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun VaultSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        placeholder = { Text("Search your sanctuary") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )
}

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
        AdaptiveWidthClass.Compact -> 18.dp
        AdaptiveWidthClass.Medium -> 24.dp
        AdaptiveWidthClass.Expanded -> 28.dp
    }
    val descriptionStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.bodyMedium
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.bodyLarge
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.headlineSmall
    }
    val shieldPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 10.dp
        AdaptiveWidthClass.Medium -> 12.dp
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                        text = "Vault",
                        style = heroTitleStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your private sanctuary for passwords, cards, and everyday credentials.",
                        style = descriptionStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultHeroActions(
    layout: AdaptiveLayoutSpec,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onLogout: () -> Unit
) {
    when (layout.widthClass) {
        // Compact – stack Import/Export side-by-side, Lock Vault full width below
        AdaptiveWidthClass.Compact -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onImport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import")
                    }
                    FilledTonalButton(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export")
                    }
                }
                FilledTonalButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Vault")
                }
            }
        }

        // Medium – all three actions in a wrapping row, full label text
        AdaptiveWidthClass.Medium -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(onClick = onImport, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
                FilledTonalButton(onClick = onExport, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
                FilledTonalButton(onClick = onLogout, shape = RoundedCornerShape(22.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Vault")
                }
            }
        }

        // Expanded – compact labels (sidebar is narrow, every dp matters)
        AdaptiveWidthClass.Expanded -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(onClick = onImport) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
                FilledTonalButton(onClick = onExport) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
                FilledTonalButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock")
                }
            }
        }
    }
}

@Composable
internal fun VaultStatChip(
    icon: ImageVector,
    label: String,
) {
    // M3 SuggestionChip gives correct ripple, theming and accessibility for free
    SuggestionChip(
        onClick = {},
        label   = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        icon    = {
            Icon(
                imageVector     = icon,
                contentDescription = null,
                tint            = MaterialTheme.colorScheme.primary,
                modifier        = Modifier.size(18.dp),
            )
        },
    )
}

@Composable
internal fun VaultContentPane(
    modifier: Modifier,
    layout: AdaptiveLayoutSpec,
    items: List<CredentialItem>,
    onEntrySelected: (Long) -> Unit
) {
    val cardSpacing = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 14.dp
        AdaptiveWidthClass.Medium -> 16.dp
        AdaptiveWidthClass.Expanded -> 18.dp
    }

    Box(modifier = modifier) {
        when {
            // Empty vault – show placeholder regardless of width class
            items.isEmpty() -> EmptyState(modifier = Modifier.fillMaxSize())

            // Compact – single-column list
            layout.widthClass == AdaptiveWidthClass.Compact -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing)
                ) {
                    items(items, key = { it.entryId }) { item ->
                        VaultCredentialCard(
                            layout = layout,
                            item = item,
                            cardIndex = item.entryId.toInt(),
                            onClick = { onEntrySelected(item.entryId) }
                        )
                    }
                }
            }

            // Medium & Expanded – adaptive grid
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(layout.listMinCellSize),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing)
                ) {
                    gridItems(items, key = { it.entryId }) { item ->
                        VaultCredentialCard(
                            layout = layout,
                            item = item,
                            cardIndex = item.entryId.toInt(),
                            onClick = { onEntrySelected(item.entryId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun VaultCredentialCard(
    layout: AdaptiveLayoutSpec,
    item: CredentialItem,
    cardIndex: Int,
    onClick: () -> Unit
) {
    val cardPadding = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 18.dp
        AdaptiveWidthClass.Medium -> 20.dp
        AdaptiveWidthClass.Expanded -> 24.dp
    }
    val cardShape = when (layout.widthClass) {
        AdaptiveWidthClass.Compact  -> 24.dp
        AdaptiveWidthClass.Medium   -> 28.dp
        AdaptiveWidthClass.Expanded -> 32.dp
    }
    val titleStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.titleLarge
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.headlineSmall
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.headlineMedium
    }.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium
    )
    val previewFields = item.fields.take(2)
    // Bug fix: use .mod(size) on hashCode() so very large Long entryIds
    // don't overflow Int and produce a negative mod result
    val cardColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.68f),
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.68f),
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
    )
    val colorIndex = item.entryId.hashCode().and(0x7FFF_FFFF).mod(cardColors.size)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardShape),
        colors = CardDefaults.cardColors(
            containerColor = cardColors[colorIndex]
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VaultEntryBadge(
                    text = vaultBadgeText(item.iconEmoji, item.siteOrApp),
                    layout = layout
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.siteOrApp,
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (previewFields.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.82f)
                    ) {
                        Text(
                            text = "No fields saved yet",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    previewFields.forEach { field ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.82f)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                VaultFieldPreview(
                                    layout = layout,
                                    label = field.label,
                                    value = if (field.isSecret) "\u2022".repeat(10) else field.value
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tap or click to view, copy, edit, or delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun VaultEntryBadge(
    text: String,
    layout: AdaptiveLayoutSpec
) {
    val badgeSize = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> 52.dp
        AdaptiveWidthClass.Medium -> 56.dp
        AdaptiveWidthClass.Expanded -> 60.dp
    }
    // Bug fix: check size-class THEN char length so emojis and
    // short labels render at the right scale per screen class.
    val badgeTextStyle = when {
        layout.widthClass == AdaptiveWidthClass.Expanded ->
            if (text.length >= 3) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.headlineMedium
        layout.widthClass == AdaptiveWidthClass.Medium ->
            if (text.length >= 3) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.headlineSmall
        else -> // Compact
            if (text.length >= 3) MaterialTheme.typography.labelLarge
            else MaterialTheme.typography.headlineSmall
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f)
    ) {
        Box(
            modifier = Modifier.size(badgeSize),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = badgeTextStyle,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun vaultBadgeText(rawBadge: String, siteOrApp: String): String {
    val normalized = normalizedBadge(rawBadge)
    if (normalized.isNotBlank()) {
        return normalized
    }

    val fallback = siteOrApp
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()

    return fallback ?: "\uD83D\uDD10"
}

internal fun normalizedBadge(rawBadge: String): String {
    val trimmed = rawBadge.trim()
    if (trimmed.isBlank()) return ""
    // Use Unicode code-point count so multi-code-unit emojis (e.g. flag emojis)
    // are counted as 1 character, not 2+ UTF-16 code units.
    val codePointCount = trimmed.codePointCount(0, trimmed.length)
    if (codePointCount > 4) return ""
    if (trimmed.any { it.isWhitespace() }) return ""
    if (trimmed.contains('@') || trimmed.contains('.') || trimmed.contains('/') || trimmed.contains('\\')) {
        return ""
    }
    return trimmed
}

@Composable
internal fun VaultFieldPreview(
    layout: AdaptiveLayoutSpec,
    label: String,
    value: String
) {
    val valueStyle = when (layout.widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.bodyMedium
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.bodyLarge
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.titleMedium
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
