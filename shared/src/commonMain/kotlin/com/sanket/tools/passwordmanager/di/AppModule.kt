package com.sanket.tools.passwordmanager.di

import com.sanket.tools.passwordmanager.data.crypto.BiometricManager
import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.ExportCrypto
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.db.AppDatabase
import com.sanket.tools.passwordmanager.data.db.DatabaseFactory
import com.sanket.tools.passwordmanager.data.export.ExportManager
import com.sanket.tools.passwordmanager.data.export.ImportManager
import com.sanket.tools.passwordmanager.data.prefs.PassworldPrefsFactory
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import com.sanket.tools.passwordmanager.ui.util.ClipboardManager
import com.sanket.tools.passwordmanager.ui.util.SecureClipboardManager
import com.sanket.tools.passwordmanager.ui.util.SecureClipboardManagerImpl
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

// ─────────────────────────────────────────────────────────────────────────────
// DATABASE MODULE
// ─────────────────────────────────────────────────────────────────────────────
val databaseModule = module {
    single<AppDatabase> { get<DatabaseFactory>().create() }
    single { get<AppDatabase>().passwordDao() }
}

// ─────────────────────────────────────────────────────────────────────────────
// CRYPTO MODULE
// ─────────────────────────────────────────────────────────────────────────────
val cryptoModule = module {
    single { CryptoEngine() }
    single { PassworldSession() }
    single { ExportCrypto(get()) }
    single<SecureClipboardManager> { SecureClipboardManagerImpl(get()) }
    // BiometricManager and ClipboardManager are provided via platformModule
}

// ─────────────────────────────────────────────────────────────────────────────
// REPOSITORY MODULE
// ─────────────────────────────────────────────────────────────────────────────
val repositoryModule = module {
    single { get<PassworldPrefsFactory>().create() }
    single { PasswordRepository(get()) }
    single { ExportManager(get(), get(), get(), get()) }
    single { ImportManager(get(), get(), get(), get()) }
}

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL MODULE
// ─────────────────────────────────────────────────────────────────────────────
val viewModelModule = module {
    factory { PassworldViewModel(get(), get(), get(), get(), get()) }
    factory { UnlockViewModel(get(), get(), get(), get(), get()) }
    factory { AddEditViewModel(get(), get(), get()) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Startup helper
// ─────────────────────────────────────────────────────────────────────────────
fun initKoin(platformModule: Module) {
    if (GlobalContext.getOrNull() != null) {
        return
    }

    startKoin {
        modules(
            platformModule,
            databaseModule,
            cryptoModule,
            repositoryModule,
            viewModelModule
        )
    }
}
