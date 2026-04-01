package com.sanket.tools.passwordmanager.data.crypto

/**
 * Common interface for Biometric (Android) and OS Password (JVM).
 */
expect class BiometricManager {
    /**
     * Returns true if biometric/platform auth is available and enrolled.
     */
    fun canAuthenticate(): Boolean

    /**
     * Triggers the biometric prompt or OS password dialog.
     * @param title Title for the prompt
     * @param subtitle Subtitle for the prompt
     * @return Result of the authentication
     */
    suspend fun authenticate(title: String, subtitle: String): AuthResult
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val error: String) : AuthResult()
    object NotAvailable : AuthResult()
}
