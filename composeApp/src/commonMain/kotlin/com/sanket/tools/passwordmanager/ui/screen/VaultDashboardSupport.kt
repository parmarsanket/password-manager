package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.runtime.saveable.Saver
import com.sanket.tools.passwordmanager.data.crypto.currentTimeMillis
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Dialog state — single sealed interface replaces three separate nullable vars:
//    selectedEntryId + showEditor + backupDialogMode
// ─────────────────────────────────────────────────────────────────────────────

sealed interface VaultDialog {
    /** FAB pressed — editor pre-loaded for a brand-new entry. */
    data object AddEntry : VaultDialog

    /** Edit pressed inside detail view — editor loading an existing entry. */
    data class EditEntry(val entryId: Long) : VaultDialog

    /** Row tapped — show read-only detail for this entry. */
    data class ViewEntry(val entryId: Long) : VaultDialog

    /** User chose Export from the hero card actions. */
    data object ExportBackup : VaultDialog

    /** User chose Import from the hero card actions. */
    data object ImportBackup : VaultDialog
}

// ─────────────────────────────────────────────────────────────────────────────
//  rememberSaveable Saver — encodes VaultDialog as a plain list so it survives
//  configuration changes and process-death restoration.
// ─────────────────────────────────────────────────────────────────────────────

private const val TAG_NONE   = 0
private const val TAG_ADD    = 1
private const val TAG_EDIT   = 2
private const val TAG_VIEW   = 3
private const val TAG_EXPORT = 4
private const val TAG_IMPORT = 5

internal val VaultDialogSaver: Saver<VaultDialog?, List<Any?>> = Saver(
    save = { dialog ->
        when (dialog) {
            null                     -> listOf(TAG_NONE)
            VaultDialog.AddEntry     -> listOf(TAG_ADD)
            is VaultDialog.EditEntry -> listOf(TAG_EDIT, dialog.entryId)
            is VaultDialog.ViewEntry -> listOf(TAG_VIEW, dialog.entryId)
            VaultDialog.ExportBackup -> listOf(TAG_EXPORT)
            VaultDialog.ImportBackup -> listOf(TAG_IMPORT)
        }
    },
    restore = { list ->
        @Suppress("UNCHECKED_CAST")
        when (list[0] as Int) {
            TAG_ADD    -> VaultDialog.AddEntry
            TAG_EDIT   -> VaultDialog.EditEntry(list[1] as Long)
            TAG_VIEW   -> VaultDialog.ViewEntry(list[1] as Long)
            TAG_EXPORT -> VaultDialog.ExportBackup
            TAG_IMPORT -> VaultDialog.ImportBackup
            else       -> null
        }
    }
)

// ─────────────────────────────────────────────────────────────────────────────
//  Keep BackupDialogMode as a thin bridge for BackupPasswordDialog.
//  It can be inlined into VaultDialog in a future clean-up pass.
// ─────────────────────────────────────────────────────────────────────────────
internal enum class BackupDialogMode { Export, Import }

// ─────────────────────────────────────────────────────────────────────────────
//  Backup business logic — accepts VaultDialog directly so callers don't need
//  to convert back to the legacy enum.
// ─────────────────────────────────────────────────────────────────────────────
internal suspend fun runBackupAction(
    dialog: VaultDialog,
    password: String,
    passworldViewModel: PassworldViewModel,
    backupFileGateway: BackupFileGateway,
): String = when (dialog) {
    VaultDialog.ExportBackup -> {
        val pkg       = passworldViewModel.exportVault(password)
        val savedPath = backupFileGateway.exportPackage(
            pkg               = pkg,
            suggestedFileName = defaultBackupFileName(),
        )
        "Backup saved to $savedPath"
    }
    VaultDialog.ImportBackup -> {
        val pkg           = backupFileGateway.importPackage()
        val importedCount = passworldViewModel.importVault(
            pkg            = pkg,
            masterPassword = password,
        )
        "$importedCount entries imported."
    }
    // All other VaultDialog subtypes are not backup-related —
    // callers must guard before calling this function.
    else -> error("runBackupAction: called with non-backup dialog $dialog")
}

internal fun defaultBackupFileName(): String =
    "passworld-backup-${currentTimeMillis()}.json"
