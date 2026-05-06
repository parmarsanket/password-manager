package com.sanket.tools.passwordmanager.util

import java.io.File

object DesktopPathUtils {
    /**
     * Returns a robust data directory for the application.
     * On Windows, it uses %LOCALAPPDATA%\PassworldManager.
     * On other platforms, it uses user.home/.passworldmanager.
     */
    fun getDataDirectory(): File {
        val osName = System.getProperty("os.name").lowercase()
        val baseDir = if (osName.contains("win")) {
            val localAppData = System.getenv("LOCALAPPDATA")
            if (localAppData != null) {
                File(localAppData)
            } else {
                File(System.getProperty("user.home"), "AppData/Local")
            }
        } else {
            File(System.getProperty("user.home"))
        }
        
        val appDir = if (osName.contains("win")) {
            File(baseDir, "PassworldManager")
        } else {
            File(baseDir, ".passworldmanager")
        }
        
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir
    }
}
