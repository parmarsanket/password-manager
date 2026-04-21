package com.sanket.tools.passwordmanager.data.crypto

/**
 * Common interface for platform biometric or device credential authentication.
 */
expect class BiometricManager {
    /**
     * Returns true if the platform should show a manual device-auth action.
     */
    fun shouldOfferAuthentication(): Boolean

    /**
     * Returns true if biometric/platform auth is available and enrolled.
     */
    fun canAuthenticate(): Boolean

    /**
     * User-facing label for the platform auth action.
     */
    fun authenticationLabel(): String

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
    object Canceled : AuthResult()
    data class Failure(val error: String) : AuthResult()
    object NotAvailable : AuthResult()
}
