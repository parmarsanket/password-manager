package com.sanket.tools.passwordmanager.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * This represents EACH field inside an entry
 * Example: Email, Password, MPIN, etc.
 *
 * IMPORTANT:
 * - encryptedValue is ALWAYS encrypted
 * - NEVER store plain text here
 */
@Entity(
    tableName = "credential_fields",

    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE // delete fields when entry deleted
        )
    ],

    indices = [Index(value = ["entryId"])] // improves query performance
)
data class CredentialField(

    @PrimaryKey(autoGenerate = true)
    val fieldId: Long = 0,

    // Foreign key -> PasswordEntry.id
    val entryId: Long,

    // Example: "Email", "Password", "TPIN"
    val fieldLabel: String,

    // 🔐 AES-GCM encrypted value (Base64)
    val encryptedValue: String,

    // true = hidden (●●●●), false = visible
    val isSecret: Boolean = true,

    // Used to maintain order in UI
    val sortOrder: Int = 0
)