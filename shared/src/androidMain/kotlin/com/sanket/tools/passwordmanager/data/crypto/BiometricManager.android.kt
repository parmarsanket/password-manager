package com.sanket.tools.passwordmanager.data.crypto

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BiometricManager(
    private val context: Context,
    private val activityProvider: ActivityProvider
) {

    actual fun canAuthenticate(): Boolean {
        val manager = AndroidBiometricManager.from(context)
        val result = manager.canAuthenticate(
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or 
            AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    actual suspend fun authenticate(title: String, subtitle: String): AuthResult {
        if (!canAuthenticate()) return AuthResult.NotAvailable

        val activity = activityProvider.currentActivity()
            ?: return AuthResult.Failure("Biometric prompt is unavailable right now.")

        return suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(AuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            continuation.resume(AuthResult.Failure(errString.toString()))
                        }
                    }

                    override fun onAuthenticationFailed() {
                        // Keep the prompt open so the user can retry.
                    }
                }
            )

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or
                        AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
            }

            prompt.authenticate(info)
        }
    }
}
