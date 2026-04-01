package com.sanket.tools.passwordmanager.ui.screen

import com.sanket.tools.passwordmanager.data.crypto.currentTimeMillis
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel

internal enum class BackupDialogMode {
    Export,
    Import
}

internal suspend fun runBackupAction(
    mode: BackupDialogMode,
    password: String,
    passworldViewModel: PassworldViewModel,
    backupFileGateway: BackupFileGateway
): String = when (mode) {
    BackupDialogMode.Export -> {
        val backupPackage = passworldViewModel.exportVault(password)
        val savedPath = backupFileGateway.exportPackage(
            pkg = backupPackage,
            suggestedFileName = defaultBackupFileName()
        )
        "Backup saved to $savedPath"
    }

    BackupDialogMode.Import -> {
        val backupPackage = backupFileGateway.importPackage()
        val importedCount = passworldViewModel.importVault(
            pkg = backupPackage,
            masterPassword = password
        )
        "$importedCount entries imported."
    }
}

internal fun defaultBackupFileName(): String =
    "passworld-backup-${currentTimeMillis()}.json"
