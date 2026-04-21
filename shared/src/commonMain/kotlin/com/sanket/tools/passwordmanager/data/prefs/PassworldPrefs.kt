package com.sanket.tools.passwordmanager.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Persistent storage for Passworld Manager metadata.
 * Stores:
 *  - Master Salt (Base64)
 *  - Master Password Hash (Hex)
 *  - Biometric enabled flag
 */
class PassworldPrefs(private val dataStore: DataStore<Preferences>) {

    private val KEY_SALT = stringPreferencesKey("master_salt")
    private val KEY_HASH = stringPreferencesKey("master_hash")
    private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun saveMasterSecrets(salt: ByteArray, hash: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SALT] = Base64.encode(salt)
            prefs[KEY_HASH] = hash
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getSalt(): ByteArray? {
        val base64 = dataStore.data.map { it[KEY_SALT] }.first()
        return base64?.let { Base64.decode(it) }
    }

    suspend fun getPasswordHash(): String? {
        return dataStore.data.map { it[KEY_HASH] }.first()
    }

    fun isSetupComplete(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SALT] != null && prefs[KEY_HASH] != null
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
