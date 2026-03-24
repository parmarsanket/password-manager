package com.sanket.tools.passwordmanager.domain.model

/**
 * Decrypted, in-memory domain model.
 *
 * This is what the UI works with — NEVER persisted to DB in this form.
 * Created by decrypting EncryptedField.encryptedValue with the Vault Key.
 */
data class CredentialItem(
    val entryId: Long,
    val siteOrApp: String,
    val iconEmoji: String,
    val createdAt: Long,
    val updatedAt: Long,

    /** Each field is fully decrypted here */
    val fields: List<DecryptedField>
)

data class DecryptedField(
    val fieldId: Long,
    val label: String,       // e.g. "Email", "TPIN"
    val value: String,       // ⚠️ plain text — only lives in RAM
    val isSecret: Boolean,
    val sortOrder: Int
)
