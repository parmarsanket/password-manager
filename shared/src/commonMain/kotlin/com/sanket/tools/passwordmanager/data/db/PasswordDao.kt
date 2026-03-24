package com.sanket.tools.passwordmanager.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object
 * This is the ONLY place that talks to DB
 */
@Dao
interface PasswordDao {

    // -------------------------------
    // INSERT
    // -------------------------------

    /**
     * Insert entry and return generated ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PasswordEntry): Long


    /**
     * Insert multiple fields
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFields(fields: List<CredentialField>)


    // -------------------------------
    // UPDATE
    // -------------------------------

    @Update
    suspend fun updateEntry(entry: PasswordEntry)

    @Update
    suspend fun updateField(field: CredentialField)


    // -------------------------------
    // DELETE
    // -------------------------------

    /**
     * Delete entry → fields auto deleted (CASCADE)
     */
    @Delete
    suspend fun deleteEntry(entry: PasswordEntry)

    @Delete
    suspend fun deleteField(field: CredentialField)


    // -------------------------------
    // QUERIES
    // -------------------------------

    /**
     * Get all entries with their fields (reactive)
     */
    @Transaction
    @Query("SELECT * FROM password_entries ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<EntryWithFields>>


    /**
     * Get single entry with fields
     */
    @Transaction
    @Query("SELECT * FROM password_entries WHERE id = :entryId")
    fun getEntryById(entryId: Long): Flow<EntryWithFields?>


    /**
     * Get all fields for an entry (used during update to delete old ones)
     */
    @Query("SELECT * FROM credential_fields WHERE entryId = :entryId")
    suspend fun getFieldsForEntry(entryId: Long): List<CredentialField>


    /**
     * Delete all data (for testing/reset)
     */
    @Query("DELETE FROM password_entries")
    suspend fun clearAll()
}