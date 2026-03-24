package com.sanket.tools.passwordmanager.data.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Combines:
 * PasswordEntry + List<CredentialField>
 *
 * Room will automatically JOIN data
 */
data class EntryWithFields(

    @Embedded
    val entry: PasswordEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val fields: List<CredentialField>
)
