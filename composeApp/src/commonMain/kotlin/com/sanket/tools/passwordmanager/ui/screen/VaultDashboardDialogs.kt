package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.domain.model.DecryptedField
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditUiState
import com.sanket.tools.passwordmanager.ui.viewmodel.CategoryTemplate

@Composable
internal fun VaultDetailDialog(
    item: CredentialItem,
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            val layout = adaptiveLayoutSpec(maxWidth)
            val dialogWidth = when (layout.widthClass) {
                AdaptiveWidthClass.Compact -> 560.dp
                AdaptiveWidthClass.Medium -> 700.dp
                AdaptiveWidthClass.Expanded -> 780.dp
            }
            val titleStyle = when (layout.widthClass) {
                AdaptiveWidthClass.Compact -> MaterialTheme.typography.headlineSmall
                AdaptiveWidthClass.Medium -> MaterialTheme.typography.headlineMedium
                AdaptiveWidthClass.Expanded -> MaterialTheme.typography.displaySmall
            }.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dialogWidth),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.siteOrApp,
                                style = titleStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.fields.size} saved field${if (item.fields.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        VaultEntryBadge(
                            text = vaultBadgeText(item.iconEmoji, item.siteOrApp),
                            layout = layout
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(onClick = { onEdit(item.entryId) }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }

                        FilledTonalButton(onClick = { onDelete(item.entryId) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item.fields.forEach { field ->
                            VaultDetailFieldCard(
                                field = field,
                                clipboardManager = clipboardManager
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun VaultDetailFieldCard(
    field: DecryptedField,
    clipboardManager: ClipboardManager
) {
    var revealed by rememberSaveable(field.fieldId) { mutableStateOf(!field.isSecret) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (revealed) field.value else "\u2022".repeat(12),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (field.isSecret) {
                IconButton(onClick = { revealed = !revealed }) {
                    Icon(
                        imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }

            IconButton(
                onClick = { clipboardManager.copyToClipboard(field.label, field.value) }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VaultEditorDialog(
    state: AddEditUiState,
    onDismiss: () -> Unit,
    onSiteNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onAddField: () -> Unit,
    onRemoveField: (Int) -> Unit,
    onFieldChange: (Int, String?, String?, Boolean?) -> Unit,
    onTemplateSelected: (CategoryTemplate) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            val layout = adaptiveLayoutSpec(maxWidth)
            val dialogWidth = when (layout.widthClass) {
                AdaptiveWidthClass.Compact -> 560.dp
                AdaptiveWidthClass.Medium -> 720.dp
                AdaptiveWidthClass.Expanded -> 820.dp
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dialogWidth),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = if (state.isEditMode) "Edit entry" else "New entry",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Keep everything in one vault screen. Update the fields here and save.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (layout.widthClass == AdaptiveWidthClass.Compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.emoji,
                                onValueChange = onEmojiChange,
                                label = { Text("Badge") },
                                modifier = Modifier.width(96.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp)
                            )
                            OutlinedTextField(
                                value = state.siteName,
                                onValueChange = onSiteNameChange,
                                label = { Text("Site or app") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.emoji,
                                onValueChange = onEmojiChange,
                                label = { Text("Badge") },
                                modifier = Modifier.width(96.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp)
                            )
                            OutlinedTextField(
                                value = state.siteName,
                                onValueChange = onSiteNameChange,
                                label = { Text("Site or app") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }

                    if (!state.isEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Quick templates",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TemplateChip("Website") {
                                    onTemplateSelected(CategoryTemplate.WEBSITE)
                                }
                                TemplateChip("Bank") {
                                    onTemplateSelected(CategoryTemplate.BANK)
                                }
                                TemplateChip("SIM") {
                                    onTemplateSelected(CategoryTemplate.SIM)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.fields.forEachIndexed { index, field ->
                            FieldItem(
                                label = field.label,
                                value = field.value,
                                isSecret = field.isSecret,
                                onLabelChange = { onFieldChange(index, it, null, null) },
                                onValueChange = { onFieldChange(index, null, it, null) },
                                onToggleSecret = {
                                    onFieldChange(index, null, null, !field.isSecret)
                                },
                                onRemove = { onRemoveField(index) }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onAddField,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add custom field")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSave,
                            enabled = state.siteName.isNotBlank()
                        ) {
                            Text(if (state.isEditMode) "Save changes" else "Save entry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BackupPasswordDialog(
    mode: BackupDialogMode,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by rememberSaveable(mode) { mutableStateOf("") }
    var passwordVisible by rememberSaveable(mode) { mutableStateOf(false) }

    val title = when (mode) {
        BackupDialogMode.Export -> "Export encrypted backup"
        BackupDialogMode.Import -> "Import encrypted backup"
    }
    val description = when (mode) {
        BackupDialogMode.Export ->
            "Create a backup file you can move between mobile and laptop. Use a backup password you will remember."
        BackupDialogMode.Import ->
            "Choose the backup file from your other device, then enter the same backup password used during export."
    }

    AlertDialog(
        onDismissRequest = {
            if (!isBusy) {
                onDismiss()
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Backup password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = null
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )

                if (isBusy) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (mode == BackupDialogMode.Export) {
                                "Saving backup..."
                            } else {
                                "Importing backup..."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank() && !isBusy
            ) {
                Text(if (mode == BackupDialogMode.Export) "Export" else "Import")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBusy
            ) {
                Text("Cancel")
            }
        }
    )
}
