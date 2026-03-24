package com.sanket.tools.passwordmanager.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

/**
 * JVM Desktop actual: stores the DB in the user's home folder.
 * No Context needed on JVM.
 */
actual class DatabaseFactory {
    actual fun create(): AppDatabase {
        val dbFile = File(System.getProperty("user.home"), "password_manager.db")
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
