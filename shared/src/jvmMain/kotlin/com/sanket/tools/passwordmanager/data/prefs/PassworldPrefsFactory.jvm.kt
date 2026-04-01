package com.sanket.tools.passwordmanager.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File

actual class PassworldPrefsFactory {
    actual fun create(): PassworldPrefs {
        val dataStore = PreferenceDataStoreFactory.create {
            File(System.getProperty("user.home"), "passworld_prefs.preferences_pb")
        }
        return PassworldPrefs(dataStore)
    }
}
