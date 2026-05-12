package com.sanket.tools.passwordmanager.ui.screen.vault.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.component.EmptyState
import com.sanket.tools.passwordmanager.ui.component.VaultEntryBadge
import com.sanket.tools.passwordmanager.ui.component.VaultFieldPreview
import com.sanket.tools.passwordmanager.ui.component.vaultBadgeText
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.screen.vault.vaultEntryColorScheme

@Composable
internal fun VaultContentPane(
    modifier: Modifier,
    layout: AdaptiveLayoutSpec,
    items: List<CredentialItem>,
    onEntrySelected: (Long, androidx.compose.ui.geometry.Rect?) -> Unit
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
                    state          = rememberLazyListState(),
                    contentPadding = PaddingValues(
                        start  = layout.horizontalPadding,
                        end    = layout.horizontalPadding,
                        top    = 0.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(layout.contentSpacing),
                ) {

                        items(items, key = { it.entryId }, contentType = { "credential" }) { item ->
                            VaultCredentialCard(
                                layout    = layout,
                                item      = item,
                                onClick   = { bounds -> onEntrySelected(item.entryId, bounds) },
                            )
                        }
                    }

            }

            // Medium & Expanded – adaptive grid
            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(layout.listMinCellSize),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                    verticalItemSpacing = cardSpacing
                ) {
                    items(
                        items = items,
                        key = { it.entryId }
                    ) { item ->
                        VaultCredentialCard(
                            layout = layout,
                            item = item,
                            onClick = { bounds -> onEntrySelected(item.entryId, bounds) }
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
    onClick: (androidx.compose.ui.geometry.Rect) -> Unit
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
        fontWeight = FontWeight.SemiBold
    )
    val previewFields = item.fields.take(2)
    val entryColors = vaultEntryColorScheme(item.entryId)

    val boundsRef = androidx.compose.runtime.remember { arrayOf(androidx.compose.ui.geometry.Rect.Zero) }

    val cardModifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates -> boundsRef[0] = coordinates.boundsInWindow() }

    Card(
        onClick = { onClick(boundsRef[0]) },
        modifier = cardModifier,
        shape = RoundedCornerShape(cardShape),
        colors = CardDefaults.cardColors(
            containerColor = entryColors.container,
            contentColor = entryColors.onContainer
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
                Box {
                    VaultEntryBadge(
                        text = vaultBadgeText(item.iconEmoji, item.siteOrApp),
                        layout = layout,
                        containerColor = entryColors.badgeContainer,
                        contentColor = entryColors.badgeContent
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.siteOrApp,
                        style = titleStyle,
                        color = entryColors.onContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = entryColors.supportingText
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (previewFields.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = entryColors.fieldContainer,
                        contentColor = entryColors.onContainer
                    ) {
                        Text(
                            text = "No fields saved yet",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = entryColors.supportingText
                        )
                    }
                } else {
                    previewFields.forEach { field ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = entryColors.fieldContainer,
                            contentColor = entryColors.onContainer
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                VaultFieldPreview(
                                    layout = layout,
                                    label = field.label,
                                    value = if (field.isSecret) "\u2022".repeat(10) else field.value,
                                    labelColor = entryColors.supportingText,
                                    valueColor = entryColors.onContainer
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tap or click to view, copy, edit, or delete",
                style = MaterialTheme.typography.labelMedium,
                color = entryColors.supportingText
            )
        }
    }
}
