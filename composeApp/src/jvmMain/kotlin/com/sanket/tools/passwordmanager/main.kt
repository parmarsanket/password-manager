package com.sanket.tools.passwordmanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.passwordmanager.data.crypto.BiometricManager
import com.sanket.tools.passwordmanager.data.crypto.KeystoreManager
import com.sanket.tools.passwordmanager.data.db.DatabaseFactory
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.data.prefs.PassworldPrefsFactory
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import com.sanket.tools.passwordmanager.di.initKoin
import org.koin.dsl.module

fun main() {
    // Start Koin before the window opens
    initKoin(module {
        single { DatabaseFactory() }
        single { KeystoreManager() }
        single { PassworldPrefsFactory() }
        single { ClipboardManager() }
        single { BiometricManager() }
        single { BackupFileGateway() }
    })

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Passworld Manager",
        ) {
            App()
        }
    }
}
