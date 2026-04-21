package com.sanket.tools.passwordmanager.data.prefs

import com.sanket.tools.passwordmanager.util.DesktopPathUtils
import java.io.File
import java.util.Properties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * A robust, simple properties-based storage for Desktop.
 * This avoids the complex file-locking issues that cause DataStore to hang in MSI builds.
 */
class SimpleDesktopPrefs(private val file: File) : DataStore<Preferences> {
    private val properties = Properties()
    private val _data = MutableStateFlow<Preferences>(androidx.datastore.preferences.core.emptyPreferences())
    
    override val data: Flow<Preferences> = _data.asStateFlow()

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            try {
                file.inputStream().use { properties.load(it) }
                updateState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateState() {
        val builder = androidx.datastore.preferences.core.emptyPreferences().toMutablePreferences()
        properties.forEach { key, value ->
            val k = key.toString()
            val v = value.toString()
            // We only use these specific keys in PassworldPrefs
            when (k) {
                "master_salt", "master_hash" -> builder[stringPreferencesKey(k)] = v
                "biometric_enabled" -> builder[booleanPreferencesKey(k)] = v.toBoolean()
            }
        }
        _data.value = builder
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val current = _data.value
        val next = transform(current)
        
        // Sync back to properties
        next.asMap().forEach { (key, value) ->
            properties.setProperty(key.name, value.toString())
        }
        
        try {
            file.outputStream().use { properties.store(it, "Passworld Manager Settings") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        _data.value = next
        return next
    }
}

actual class PassworldPrefsFactory {
    actual fun create(): PassworldPrefs {
        val prefsFile = File(DesktopPathUtils.getDataDirectory(), "settings.properties")
        val simpleStore = SimpleDesktopPrefs(prefsFile)
        return PassworldPrefs(simpleStore)
    }
}
