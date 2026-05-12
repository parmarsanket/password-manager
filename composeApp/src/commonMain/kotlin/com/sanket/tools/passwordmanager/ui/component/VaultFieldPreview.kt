package com.sanket.tools.passwordmanager.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass

@Composable
fun VaultFieldPreview(
    layout: AdaptiveLayoutSpec,
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
