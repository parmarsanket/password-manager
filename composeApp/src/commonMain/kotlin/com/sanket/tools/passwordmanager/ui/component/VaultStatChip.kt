package com.sanket.tools.passwordmanager.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun VaultStatChip(
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
