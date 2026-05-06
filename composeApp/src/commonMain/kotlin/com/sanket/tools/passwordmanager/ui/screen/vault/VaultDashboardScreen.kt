package com.sanket.tools.passwordmanager.ui.screen.vault

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.domain.model.CredentialItem
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.screen.vault.layout.SinglePaneLayout
import com.sanket.tools.passwordmanager.ui.screen.vault.layout.TwoPaneLayout
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditUiState
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.CategoryTemplate
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// ─────────────────────────────────────────────────────────────────────────────
//  State holder — the ONLY composable allowed to touch ViewModels / Koin.
//  It collects flows, owns mutable state, and wires lambdas.
//  It passes plain data + callbacks down to VaultDashboardContent.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDashboardScreen(
    passworldViewModel: PassworldViewModel,
    addEditViewModel: AddEditViewModel,
    onLogout: () -> Unit,
) {
    val items       by passworldViewModel.items.collectAsState()
    val searchQuery by passworldViewModel.searchQuery.collectAsState()
    val editorState by addEditViewModel.uiState.collectAsState()

    val backupFileGateway: BackupFileGateway = koinInject()
    val clipboardManager:  ClipboardManager  = koinInject()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    // ── One nullable sealed interface replaces three separate state vars:
    //    selectedEntryId + showEditor + backupDialogMode
    var activeDialog by rememberSaveable(stateSaver = VaultDialogSaver) {
        mutableStateOf<VaultDialog?>(null)
    }

    // NOT rememberSaveable — an in-flight coroutine cannot resume after process death,
    // so pretending "busy" is true on re-launch would just lock the dialog forever.
    var backupBusy by remember { mutableStateOf(false) }

    // ── derivedStateOf avoids re-running these on every recomposition ────────
    val selectedItem by remember(items, activeDialog) {
        derivedStateOf {
            val id = (activeDialog as? VaultDialog.ViewEntry)?.entryId
            items.find { it.entryId == id }
        }
    }
    val totalSecrets by remember(items) {
        derivedStateOf { items.sumOf { c -> c.fields.count { it.isSecret } } }
    }

    VaultDashboardContent(
        items             = items,
        searchQuery       = searchQuery,
        totalSecrets      = totalSecrets,
        editorState       = editorState,
        activeDialog      = activeDialog,
        selectedItem      = selectedItem,
        backupBusy        = backupBusy,
        clipboardManager  = clipboardManager,
        snackbarHostState = snackbarHostState,
        onSearchChange    = passworldViewModel::onSearch,
        onAddEntry = {
            addEditViewModel.prepareNewEntry()
            activeDialog = VaultDialog.AddEntry
        },
        onEntrySelected = { activeDialog = VaultDialog.ViewEntry(it) },
        onEditEntry = { entryId ->
            addEditViewModel.loadEntry(entryId)
            activeDialog = VaultDialog.EditEntry(entryId)
        },
        onDeleteEntry = { entryId ->
            passworldViewModel.deleteEntry(entryId)
            activeDialog = null
            scope.launch { snackbarHostState.showSnackbar("Entry deleted.") }
        },
        onEditorSiteNameChange   = addEditViewModel::onSiteNameChange,
        onEditorEmojiChange      = addEditViewModel::onEmojiChange,
        onEditorAddField         = addEditViewModel::addField,
        onEditorRemoveField      = addEditViewModel::removeField,
        onEditorFieldChange      = addEditViewModel::onFieldChange,
        onEditorTemplateSelected = addEditViewModel::applyTemplate,
        onEditorSave = {
            val isEdit = editorState.isEditMode
            addEditViewModel.save {
                activeDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (isEdit) "Entry updated." else "Entry added."
                    )
                }
            }
        },
        onEditorDismiss = {
            addEditViewModel.clearEditor()
            activeDialog = null
        },
        onExport        = { activeDialog = VaultDialog.ExportBackup },
        onImport        = { activeDialog = VaultDialog.ImportBackup },
        onLogout        = { passworldViewModel.logout(); onLogout() },
        onDialogDismiss = { if (!backupBusy) activeDialog = null },
        onBackupConfirm = { password ->
            val dialog = activeDialog ?: return@VaultDashboardContent
            scope.launch {
                backupBusy = true
                try {
                    val message = runBackupAction(
                        dialog             = dialog,
                        password           = password,
                        passworldViewModel = passworldViewModel,
                        backupFileGateway  = backupFileGateway,
                    )
                    activeDialog = null
                    snackbarHostState.showSnackbar(message)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(e.message ?: "Backup action failed.")
                } finally {
                    backupBusy = false
                }
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pure UI — stateless, no ViewModel, no koinInject, no collectAsState.
//  Receives all data and lambdas from the state holder above.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VaultDashboardContent(
    // ── data ─────────────────────────────────────────────────────────────────
    items: List<CredentialItem>,
    searchQuery: String,
    totalSecrets: Int,
    editorState: AddEditUiState,
    activeDialog: VaultDialog?,
    selectedItem: CredentialItem?,
    backupBusy: Boolean,
    clipboardManager: ClipboardManager,
    snackbarHostState: SnackbarHostState,
    // ── actions ───────────────────────────────────────────────────────────────
    onSearchChange: (String) -> Unit,
    onAddEntry: () -> Unit,
    onEntrySelected: (Long) -> Unit,
    onEditEntry: (Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onEditorSiteNameChange: (String) -> Unit,
    onEditorEmojiChange: (String) -> Unit,
    onEditorAddField: () -> Unit,
    onEditorRemoveField: (Int) -> Unit,
    onEditorFieldChange: (Int, String?, String?, Boolean?) -> Unit,
    onEditorTemplateSelected: (CategoryTemplate) -> Unit,
    onEditorSave: () -> Unit,
    onEditorDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onLogout: () -> Unit,
    onDialogDismiss: () -> Unit,
    onBackupConfirm: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        // ── Layout decision ─────────────────────────────────────────────────
        // Both maxWidth AND maxHeight are used so phone-landscape (wide + short)
        // is never mistaken for a tablet (wide + tall).
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val layout = adaptiveLayoutSpec(maxWidth, maxHeight)

            // vaultHeroTitleStyle is @Composable; Compose skips it automatically
            // when widthClass hasn't changed, so no manual remember wrapper needed.
            val heroTitleStyle = vaultHeroTitleStyle(layout.widthClass)

            when {
                // ── Tablet landscape / Desktop — full two-pane sidebar ────────
                layout.useTwoPaneLayout -> TwoPaneLayout(
                    layout          = layout,
                    sidebarModifier = Modifier,   // sidebar manages its own animated width
                    heroTitleStyle  = heroTitleStyle,
                    searchQuery     = searchQuery,
                    totalEntries    = items.size,
                    totalSecrets    = totalSecrets,
                    items           = items,
                    onSearchChange  = onSearchChange,
                    onImport        = onImport,
                    onExport        = onExport,
                    onLogout        = onLogout,
                    onEntrySelected = onEntrySelected,
                )

                // ── Phone landscape — wide but very short, narrow action strip ─
                layout.posture == AdaptivePosture.PhoneLandscape -> TwoPaneLayout(
                    layout          = layout,
                    // compactSidebarWidth replaces the old inline 220.dp magic number
                    sidebarModifier = Modifier.width(layout.compactSidebarWidth),
                    heroTitleStyle  = heroTitleStyle,
                    searchQuery     = searchQuery,
                    totalEntries    = items.size,
                    totalSecrets    = totalSecrets,
                    items           = items,
                    onSearchChange  = onSearchChange,
                    onImport        = onImport,
                    onExport        = onExport,
                    onLogout        = onLogout,
                    onEntrySelected = onEntrySelected,
                )

                // ── Tablet portrait — centered constrained single column ───────
                layout.widthClass == AdaptiveWidthClass.Medium -> SinglePaneLayout(
                    layout          = layout,
                    centered        = true,
                    heroTitleStyle  = heroTitleStyle,
                    searchQuery     = searchQuery,
                    totalEntries    = items.size,
                    totalSecrets    = totalSecrets,
                    items           = items,
                    onSearchChange  = onSearchChange,
                    onImport        = onImport,
                    onExport        = onExport,
                    onLogout        = onLogout,
                    onEntrySelected = onEntrySelected,
                )

                // ── Phone portrait — compact single column ────────────────────
                else -> SinglePaneLayout(
                    layout          = layout,
                    centered        = false,
                    heroTitleStyle  = heroTitleStyle,
                    searchQuery     = searchQuery,
                    totalEntries    = items.size,
                    totalSecrets    = totalSecrets,
                    items           = items,
                    onSearchChange  = onSearchChange,
                    onImport        = onImport,
                    onExport        = onExport,
                    onLogout        = onLogout,
                    onEntrySelected = onEntrySelected,
                )
            }
        }
    }

    // ── Dialogs — rendered OUTSIDE Scaffold so they go truly full-screen ──────
    // Exhaustive when on the sealed interface: compiler catches missing branches.
    when (val dialog = activeDialog) {
        is VaultDialog.ViewEntry -> selectedItem?.let { item ->
            VaultDetailDialog(
                item             = item,
                clipboardManager = clipboardManager,
                onDismiss        = onDialogDismiss,
                onEdit           = onEditEntry,
                onDelete         = onDeleteEntry,
            )
        }

        is VaultDialog.AddEntry,
        is VaultDialog.EditEntry -> VaultEditorDialog(
            state                = editorState,
            onDismiss            = onEditorDismiss,
            onSiteNameChange     = onEditorSiteNameChange,
            onEmojiChange        = onEditorEmojiChange,
            onAddField           = onEditorAddField,
            onRemoveField        = onEditorRemoveField,
            onFieldChange        = onEditorFieldChange,
            onTemplateSelected   = onEditorTemplateSelected,
            onSave               = onEditorSave,
        )

        VaultDialog.ExportBackup -> BackupPasswordDialog(
            mode      = BackupDialogMode.Export,
            isBusy    = backupBusy,
            onDismiss = onDialogDismiss,
            onConfirm = onBackupConfirm,
        )

        VaultDialog.ImportBackup -> BackupPasswordDialog(
            mode      = BackupDialogMode.Import,
            isBusy    = backupBusy,
            onDismiss = onDialogDismiss,
            onConfirm = onBackupConfirm,
        )

        null -> Unit
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hero title style scales with width class.
 *
 * @Composable so it can read MaterialTheme.typography.
 * Callers should memoize with `remember(layout.widthClass) { vaultHeroTitleStyle(...) }`
 * inside a BoxWithConstraints to avoid recomputing on every resize pixel.
 *
 * Typography: SemiBold from the M3 type scale (was Serif Italic).
 * If you want a brand font, set it once in the Typography theme object.
 */
@Composable
internal fun vaultHeroTitleStyle(widthClass: AdaptiveWidthClass): TextStyle =
    when (widthClass) {
        AdaptiveWidthClass.Compact  -> MaterialTheme.typography.displaySmall
        AdaptiveWidthClass.Medium   -> MaterialTheme.typography.displayMedium
        AdaptiveWidthClass.Expanded -> MaterialTheme.typography.displayLarge
    }.copy(fontWeight = FontWeight.SemiBold)
