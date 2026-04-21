package com.sanket.tools.passwordmanager

import android.app.Application
import com.sanket.tools.passwordmanager.data.crypto.ActivityProvider
import com.sanket.tools.passwordmanager.data.crypto.BiometricManager
import com.sanket.tools.passwordmanager.data.crypto.KeystoreManager
import com.sanket.tools.passwordmanager.data.db.DatabaseFactory
import com.sanket.tools.passwordmanager.data.export.BackupFileActivityBridge
import com.sanket.tools.passwordmanager.data.export.BackupFileGateway
import com.sanket.tools.passwordmanager.data.prefs.PassworldPrefsFactory
import com.sanket.tools.passwordmanager.di.initKoin
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import org.koin.dsl.module

class PasswordManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(
            module {
                single { ActivityProvider() }
                single { BackupFileActivityBridge() }
                single { DatabaseFactory(applicationContext) }
                single { KeystoreManager(applicationContext) }
                single { PassworldPrefsFactory(applicationContext) }
                single { ClipboardManager(applicationContext) }
                single { BiometricManager(applicationContext, get()) }
                single { BackupFileGateway(applicationContext, get(), get()) }
            }
        )
    }
}
