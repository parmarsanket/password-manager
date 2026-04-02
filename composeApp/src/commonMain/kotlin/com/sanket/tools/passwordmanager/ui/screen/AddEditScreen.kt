package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.CategoryTemplate

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: AddEditViewModel,
    entryId: Long? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Entry" else "New Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save(onBack) }) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            val layout = adaptiveLayoutSpec(maxWidth, maxHeight)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = layout.contentMaxWidth)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(
                    start = layout.horizontalPadding,
                    top = layout.verticalPadding,
                    end = layout.horizontalPadding,
                    bottom = layout.verticalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (layout.widthClass == AdaptiveWidthClass.Compact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.emoji,
                                onValueChange = { viewModel.onEmojiChange(it) },
                                label = { Text("Icon") },
                                modifier = Modifier.width(96.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.siteName,
                                onValueChange = { viewModel.onSiteNameChange(it) },
                                label = { Text("Site or App Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = uiState.emoji,
                                onValueChange = { viewModel.onEmojiChange(it) },
                                label = { Text("Icon") },
                                modifier = Modifier.width(96.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedTextField(
                                value = uiState.siteName,
                                onValueChange = { viewModel.onSiteNameChange(it) },
                                label = { Text("Site or App Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                if (!uiState.isEditMode) {
                    item {
                        Text("Quick Templates", style = MaterialTheme.typography.labelMedium)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TemplateChip("Website", { viewModel.applyTemplate(CategoryTemplate.WEBSITE) })
                            TemplateChip("Bank", { viewModel.applyTemplate(CategoryTemplate.BANK) })
                            TemplateChip("SIM", { viewModel.applyTemplate(CategoryTemplate.SIM) })
                        }
                    }
                }

                item { HorizontalDivider() }

                itemsIndexed(uiState.fields) { index, field ->
                    FieldItem(
                        label = field.label,
                        value = field.value,
                        isSecret = field.isSecret,
                        onLabelChange = { viewModel.onFieldChange(index, label = it) },
                        onValueChange = { viewModel.onFieldChange(index, value = it) },
                        onToggleSecret = { viewModel.onFieldChange(index, isSecret = !field.isSecret) },
                        onRemove = { viewModel.removeField(index) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { viewModel.addField() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Field")
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateChip(label: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
fun FieldItem(
    label: String,
    value: String,
    isSecret: Boolean,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onToggleSecret: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = label,
                    onValueChange = onLabelChange,
                    placeholder = { Text("Field Label (e.g. Email)") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.labelLarge
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = onToggleSecret) {
                        Icon(
                            imageVector = if (isSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}
