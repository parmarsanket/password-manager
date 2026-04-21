package com.sanket.tools.passwordmanager.data.db


import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room Database
 *
 * Add all entities here
 */
@Database(
    entities = [
        PasswordEntry::class,
        CredentialField::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao
}