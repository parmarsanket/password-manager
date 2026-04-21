package com.sanket.tools.passwordmanager.data.prefs

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "passworld_prefs")

actual class PassworldPrefsFactory(private val context: Context) {
    actual fun create(): PassworldPrefs {
        return PassworldPrefs(context.dataStore)
    }
}
