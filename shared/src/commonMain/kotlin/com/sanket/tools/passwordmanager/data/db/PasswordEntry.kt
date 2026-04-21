package com.sanket.tools.passwordmanager.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

/**
 * This represents ONE vault item (like GitHub, Bank, etc.)
 * Only NON-sensitive data is stored here (plain text)
 */
@Entity(tableName = "password_entries")
data class PasswordEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Example: "GitHub", "HDFC Bank"
    val siteOrApp: String,

    // Optional emoji for UI
    val iconEmoji: String = "🔐",

    // Metadata (not sensitive)
    val createdAt: Long =  Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)