package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VaultDashboardScreen(
    passworldViewModel: PassworldViewModel,
    addEditViewModel: AddEditViewModel,
    onLogout: () -> Unit
) {
    val items by passworldViewModel.items.collectAsState()
    val searchQuery by passworldViewModel.searchQuery.collectAsState()
    val editorState by addEditViewModel.uiState.collectAsState()
    val backupFileGateway: BackupFileGateway = koinInject()
    val clipboardManager: ClipboardManager = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedEntryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var backupDialogMode by rememberSaveable { mutableStateOf<BackupDialogMode?>(null) }
    var backupBusy by rememberSaveable { mutableStateOf(false) }

    val selectedItem by remember(items, selectedEntryId) {
        derivedStateOf { items.find { it.entryId == selectedEntryId } }
    }
    val totalSecrets by remember(items) {
        derivedStateOf { items.sumOf { credential -> credential.fields.count { it.isSecret } } }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addEditViewModel.prepareNewEntry()
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val layout = adaptiveLayoutSpec(maxWidth)
            val heroTitleStyle = vaultHeroTitleStyle(layout.widthClass)

            if (layout.widthClass == AdaptiveWidthClass.Expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = layout.horizontalPadding,
                            vertical = layout.verticalPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing)
                ) {
                    VaultSidebar(
                        modifier = Modifier
                            .width(336.dp)
                            .fillMaxHeight(),
                        layout = layout,
                        heroTitleStyle = heroTitleStyle,
                        searchQuery = searchQuery,
                        totalEntries = items.size,
                        totalSecrets = totalSecrets,
                        onSearchChange = passworldViewModel::onSearch,
                        onImport = { backupDialogMode = BackupDialogMode.Import },
                        onExport = { backupDialogMode = BackupDialogMode.Export },
                        onLogout = onLogout
                    )

                    VaultContentPane(
                        modifier = Modifier.weight(1f),
                        layout = layout,
                        items = items,
                        onEntrySelected = { selectedEntryId = it }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = layout.horizontalPadding,
                            vertical = layout.verticalPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    VaultHeroCard(
                        layout = layout,
                        heroTitleStyle = heroTitleStyle,
                        totalEntries = items.size,
                        totalSecrets = totalSecrets,
                        onImport = { backupDialogMode = BackupDialogMode.Import },
                        onExport = { backupDialogMode = BackupDialogMode.Export },
                        onLogout = onLogout
                    )

                    VaultSearchField(
                        value = searchQuery,
                        onValueChange = passworldViewModel::onSearch,
                        modifier = Modifier.fillMaxWidth()
                    )

                    VaultContentPane(
                        modifier = Modifier.weight(1f),
                        layout = layout,
                        items = items,
                        onEntrySelected = { selectedEntryId = it }
                    )
                }
            }
        }
    }

    selectedItem?.let { item ->
        VaultDetailDialog(
            item = item,
            clipboardManager = clipboardManager,
            onDismiss = { selectedEntryId = null },
            onEdit = { entryId ->
                selectedEntryId = null
                addEditViewModel.loadEntry(entryId)
                showEditor = true
            },
            onDelete = { entryId ->
                passworldViewModel.deleteEntry(entryId)
                selectedEntryId = null
                scope.launch {
                    snackbarHostState.showSnackbar("Entry deleted.")
                }
            }
        )
    }

    if (showEditor) {
        VaultEditorDialog(
            state = editorState,
            onDismiss = {
                addEditViewModel.clearEditor()
                showEditor = false
            },
            onSiteNameChange = addEditViewModel::onSiteNameChange,
            onEmojiChange = addEditViewModel::onEmojiChange,
            onAddField = addEditViewModel::addField,
            onRemoveField = addEditViewModel::removeField,
            onFieldChange = addEditViewModel::onFieldChange,
            onTemplateSelected = addEditViewModel::applyTemplate,
            onSave = {
                addEditViewModel.save {
                    showEditor = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (editorState.isEditMode) "Entry updated." else "Entry added."
                        )
                    }
                }
            }
        )
    }

    backupDialogMode?.let { mode ->
        BackupPasswordDialog(
            mode = mode,
            isBusy = backupBusy,
            onDismiss = {
                if (!backupBusy) {
                    backupDialogMode = null
                }
            },
            onConfirm = { password ->
                scope.launch {
                    backupBusy = true
                    try {
                        val message = runBackupAction(
                            mode = mode,
                            password = password,
                            passworldViewModel = passworldViewModel,
                            backupFileGateway = backupFileGateway
                        )
                        backupDialogMode = null
                        snackbarHostState.showSnackbar(message)
                    } catch (error: Exception) {
                        snackbarHostState.showSnackbar(error.message ?: "Backup action failed.")
                    } finally {
                        backupBusy = false
                    }
                }
            }
        )
    }
}

@Composable
private fun vaultHeroTitleStyle(widthClass: AdaptiveWidthClass): TextStyle =
    when (widthClass) {
        AdaptiveWidthClass.Compact -> MaterialTheme.typography.displaySmall
        AdaptiveWidthClass.Medium -> MaterialTheme.typography.displayMedium
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.displayLarge
    }.copy(
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium
    )
