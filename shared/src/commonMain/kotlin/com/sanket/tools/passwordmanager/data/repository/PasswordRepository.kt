package com.sanket.tools.passwordmanager.data.repository

import com.sanket.tools.passwordmanager.data.db.CredentialField
import com.sanket.tools.passwordmanager.data.db.EntryWithFields
import com.sanket.tools.passwordmanager.data.db.PasswordDao
import com.sanket.tools.passwordmanager.data.db.PasswordEntry
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all vault CRUD operations.
 *
 * The Repository sits between the DAO (raw DB) and the ViewModels (UI).
 * It does NOT do encryption — that is the job of CryptoEngine.
 *
 * Usage in ViewModel:
 *    val repo: PasswordRepository by inject()
 */
class PasswordRepository(private val dao: PasswordDao) {

    // ─────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────

    /** All entries with their encrypted fields — reactive Flow */
    fun getAllEntries(): Flow<List<EntryWithFields>> = dao.getAllEntries()

    /** Single entry by ID — reactive Flow */
    fun getEntryById(id: Long): Flow<EntryWithFields?> = dao.getEntryById(id)

    // ─────────────────────────────────────────────────────────────────────
    // WRITE
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Save a new entry + its encrypted fields atomically.
     *
     * @param entry     The PasswordEntry header (site name, emoji, timestamps)
     * @param fields    The encrypted CredentialFields ready for storage
     */
    suspend fun saveEntry(entry: PasswordEntry, fields: List<CredentialField>) {
        val newId = dao.insertEntry(entry)
        val fieldsWithId = fields.map { it.copy(entryId = newId) }
        dao.insertFields(fieldsWithId)
    }

    /**
     * Update an existing entry header + replace all its fields.
     * Deleting old fields is handled automatically by Room CASCADE.
     */
    suspend fun updateEntry(entry: PasswordEntry, fields: List<CredentialField>) {
        dao.updateEntry(entry)
        // Delete old fields first (CASCADE doesn't fire on UPDATE)
        val existing = dao.getFieldsForEntry(entry.id)
        existing.forEach { dao.deleteField(it) }
        // Insert fresh fields
        val fieldsWithId = fields.map { it.copy(entryId = entry.id) }
        dao.insertFields(fieldsWithId)
    }

    /** Delete an entry — all its fields are auto-deleted via CASCADE */
    suspend fun deleteEntry(entry: PasswordEntry) = dao.deleteEntry(entry)

    // ─────────────────────────────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────────────────────────────

    /** Wipe entire vault — used during reset / import */
    suspend fun clearAll() = dao.clearAll()
}
