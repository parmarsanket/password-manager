package com.sanket.tools.passwordmanager.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sanket.tools.passwordmanager.util.DesktopPathUtils
import java.io.File

/**
 * JVM Desktop actual: stores the DB in a dedicated app data folder.
 * No Context needed on JVM.
 */
actual class DatabaseFactory {
    actual fun create(): AppDatabase {
        val dbFile = File(DesktopPathUtils.getDataDirectory(), "password_manager.db")
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
