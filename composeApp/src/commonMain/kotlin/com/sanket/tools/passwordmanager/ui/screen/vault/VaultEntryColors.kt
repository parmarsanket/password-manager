package com.sanket.tools.passwordmanager.ui.screen.vault

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal data class VaultEntryColorScheme(
    val container: Color,
    val onContainer: Color,
    val supportingText: Color,
    val accent: Color,
    val onAccent: Color,
    val badgeContainer: Color,
    val badgeContent: Color,
    val fieldContainer: Color,
    val destructiveContainer: Color,
    val onDestructiveContainer: Color
)

@Composable
internal fun vaultEntryColorScheme(entryId: Long): VaultEntryColorScheme {
    val colors = MaterialTheme.colorScheme
    val variants = listOf(
        VaultEntryColorScheme(
            container = colors.primaryContainer.copy(alpha = 0.72f),
            onContainer = colors.onSurface,
            supportingText = colors.onSurfaceVariant,
            accent = colors.primary,
            onAccent = colors.onPrimary,
            badgeContainer = colors.surfaceContainerLowest.copy(alpha = 0.92f),
            badgeContent = colors.primary,
            fieldContainer = colors.surfaceContainerLowest.copy(alpha = 0.82f),
            destructiveContainer = colors.errorContainer,
            onDestructiveContainer = colors.onErrorContainer
        ),
        VaultEntryColorScheme(
            container = colors.secondaryContainer.copy(alpha = 0.68f),
            onContainer = colors.onSurface,
            supportingText = colors.onSurfaceVariant,
            accent = colors.secondary,
            onAccent = colors.onSecondary,
            badgeContainer = colors.surfaceContainerLowest.copy(alpha = 0.92f),
            badgeContent = colors.primary,
            fieldContainer = colors.surfaceContainerLowest.copy(alpha = 0.82f),
            destructiveContainer = colors.errorContainer,
            onDestructiveContainer = colors.onErrorContainer
        ),
        VaultEntryColorScheme(
            container = colors.tertiaryContainer.copy(alpha = 0.68f),
            onContainer = colors.onSurface,
            supportingText = colors.onSurfaceVariant,
            accent = colors.tertiary,
            onAccent = colors.onTertiary,
            badgeContainer = colors.surfaceContainerLowest.copy(alpha = 0.92f),
            badgeContent = colors.primary,
            fieldContainer = colors.surfaceContainerLowest.copy(alpha = 0.82f),
            destructiveContainer = colors.errorContainer,
            onDestructiveContainer = colors.onErrorContainer
        ),
        VaultEntryColorScheme(
            container = colors.surfaceContainerHigh.copy(alpha = 0.94f),
            onContainer = colors.onSurface,
            supportingText = colors.onSurfaceVariant,
            accent = colors.primary,
            onAccent = colors.onPrimary,
            badgeContainer = colors.surfaceContainerLowest.copy(alpha = 0.92f),
            badgeContent = colors.primary,
            fieldContainer = colors.surfaceContainerLowest.copy(alpha = 0.82f),
            destructiveContainer = colors.errorContainer,
            onDestructiveContainer = colors.onErrorContainer
        )
    )

    return variants[entryId.hashCode().and(0x7FFF_FFFF).mod(variants.size)]
}
