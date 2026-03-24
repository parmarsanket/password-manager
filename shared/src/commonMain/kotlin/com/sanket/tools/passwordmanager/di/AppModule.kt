package com.sanket.tools.passwordmanager.di

import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.ExportCrypto
import com.sanket.tools.passwordmanager.data.db.AppDatabase
import com.sanket.tools.passwordmanager.data.db.DatabaseFactory
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

// ─────────────────────────────────────────────────────────────────────────────
// DATABASE MODULE
// ─────────────────────────────────────────────────────────────────────────────
val databaseModule = module {
    // DatabaseFactory is provided by each platform's entry-point module
    single<AppDatabase> { get<DatabaseFactory>().create() }
    single { get<AppDatabase>().passwordDao() }
}

// ─────────────────────────────────────────────────────────────────────────────
// CRYPTO MODULE
// CryptoEngine and KeystoreManager are expect/actual — no args on JVM/iOS.
// On Android, KeystoreManager needs Context → provided via platformModule.
// ─────────────────────────────────────────────────────────────────────────────
val cryptoModule = module {
    single { CryptoEngine() }
    single { ExportCrypto(get()) }
    // KeystoreManager is constructed via platformModule (needs Context on Android)
}

// ─────────────────────────────────────────────────────────────────────────────
// REPOSITORY MODULE
// ─────────────────────────────────────────────────────────────────────────────
val repositoryModule = module {
    single { PasswordRepository(get()) }
}

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL MODULE
// ─────────────────────────────────────────────────────────────────────────────
val viewModelModule = module {
    // We'll define ViewModels here as we create them
}

// ─────────────────────────────────────────────────────────────────────────────
// Startup helper — called from each platform entry point
//
// platformModule must provide:
//   single { DatabaseFactory(...) }    ← all platforms
//   single { KeystoreManager(...) }    ← all platforms (Android needs Context)
// ─────────────────────────────────────────────────────────────────────────────
fun initKoin(platformModule: Module) {
    startKoin {
        modules(
            platformModule,    // platform-specific: DatabaseFactory + KeystoreManager
            databaseModule,
            cryptoModule,
            repositoryModule,
            viewModelModule
        )
    }
}
