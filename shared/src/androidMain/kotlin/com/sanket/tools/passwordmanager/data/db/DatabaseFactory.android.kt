package com.sanket.tools.passwordmanager.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Android actual: uses a Context to get the DB file path
 * and builds the Room database with the BundledSQLiteDriver.
 *
 * ⚠️ This class is injected by Koin in MainActivity via:
 *    single { DatabaseFactory(androidContext()) }
 */
actual class DatabaseFactory(private val context: Context) {
    actual fun create(): AppDatabase {
        val dbFile = context.getDatabasePath("password_manager.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
