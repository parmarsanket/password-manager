package com.sanket.tools.passwordmanager.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Platform-specific factory for DataStore.
 * Android needs Context.
 * JVM needs a path in user home.
 */
expect class PassworldPrefsFactory {
    fun create(): PassworldPrefs
}
