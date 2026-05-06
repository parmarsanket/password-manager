package com.sanket.tools.passwordmanager.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass

@Composable
fun VaultFieldPreview(
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
