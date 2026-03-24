package com.sanket.tools.passwordmanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.passwordmanager.data.crypto.KeystoreManager
import com.sanket.tools.passwordmanager.data.db.DatabaseFactory
import com.sanket.tools.passwordmanager.di.initKoin
import org.koin.dsl.module

fun main() {
    // Start Koin before the window opens — no Context needed on JVM
    initKoin(module {
        single { DatabaseFactory() }
        single { KeystoreManager() }
    })

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Password Manager",
        ) {
            App()
        }
    }
}