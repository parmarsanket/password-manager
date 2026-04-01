package com.sanket.tools.passwordmanager.data.crypto

actual class BiometricManager {
    actual fun canAuthenticate(): Boolean {
        // Standard JVM (Desktop) doesn't have a unified Biometric API.
        // We return false to trigger the Master Password fallback.
        return false
    }

    actual suspend fun authenticate(title: String, subtitle: String): AuthResult {
        return AuthResult.NotAvailable
    }
}
