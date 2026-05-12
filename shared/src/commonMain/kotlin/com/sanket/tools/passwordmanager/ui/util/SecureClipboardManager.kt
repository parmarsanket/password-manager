package com.sanket.tools.passwordmanager.ui.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Security state of the clipboard.
 */
data class ClipboardSecurityState(
    val isSensitive: Boolean = false,
    val remainingSeconds: Int = 0,
    val isOneTimePaste: Boolean = false,
    val label: String? = null
)

/**
 * Enterprise-grade secure clipboard manager.
 */
interface SecureClipboardManager {
    /**
     * Copies text to clipboard with security features.
     * @param label The label for the clipboard content.
     * @param text The sensitive text to copy.
     * @param isSensitive Whether the content should be treated as sensitive (e.g., hidden from previews).
     * @param autoClearMillis Time in milliseconds after which the clipboard is cleared. 0 to disable.
     * @param oneTimePaste If true, the clipboard will be cleared after the first paste (if platform supports detection) or timeout.
     */
    suspend fun copySecure(
        label: String,
        text: String,
        isSensitive: Boolean = true,
        autoClearMillis: Long = 30_000,
        oneTimePaste: Boolean = false
    )

    /**
     * Clears the clipboard immediately.
     */
    suspend fun clearClipboard()

    /**
     * Called when the app returns to the foreground.
     * Used to clear "one-time paste" content.
     */
    suspend fun onAppForegrounded()

    /**
     * Observes the current clipboard security state.
     */
    val securityState: StateFlow<ClipboardSecurityState>
}
