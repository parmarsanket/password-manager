package com.sanket.tools.passwordmanager.ui.component

import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TemplateChip(label: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label) }
    )
}
