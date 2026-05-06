package com.sanket.tools.passwordmanager.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass

@Composable
fun VaultEntryBadge(
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

fun vaultBadgeText(rawBadge: String, siteOrApp: String): String {
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

fun normalizedBadge(rawBadge: String): String {
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
