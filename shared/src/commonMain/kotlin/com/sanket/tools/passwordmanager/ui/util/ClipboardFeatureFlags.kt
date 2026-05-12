package com.sanket.tools.passwordmanager.ui.util

/**
 * Feature flags for clipboard security behavior.
 *
 * Toggle these flags to enable/disable clipboard security features.
 * In the future, wire these to a Settings screen backed by DataStore persistence
 * so the user can control them from the UI.
 *
 * For now, just change `true` → `false` (or vice versa) to toggle a feature.
 */
object ClipboardFeatureFlags {

    /**
     * One-Time Paste: When enabled, the clipboard is cleared automatically
     * the moment the user returns to the app after pasting somewhere else.
     *
     * Flow:
     *   1. User copies a password  →  keyboard shows ***** suggestion
     *   2. User switches to Chrome/WhatsApp and pastes
     *   3. User returns to Password Manager
     *   4. Clipboard is immediately wiped — the password can never be pasted again
     *
     * When disabled (`false`), the clipboard will still auto-clear after [autoClearTimeoutMillis],
     * but it won't be wiped on app-foreground.
     *
     * Works on: Android, Windows, Linux (all platforms)
     */
    // TODO: Wire to Settings screen with DataStore persistence
    val oneTimePasteEnabled: Boolean = true

    /**
     * Auto-clear timeout in milliseconds.
     * The clipboard will be wiped after this duration regardless of whether
     * the user pasted or not.
     *
     * Set to `0L` to disable the timeout (NOT recommended for a password manager).
     *
     * Works on: Android, Windows, Linux (all platforms)
     */
    // TODO: Wire to Settings screen with DataStore persistence
    val autoClearTimeoutMillis: Long = 30_000L // 30 seconds

    /**
     * Mark clipboard content as sensitive.
     * On Android: Gboard/Samsung show ***** with 🔒 instead of plain text.
     * On Windows: Prevents Win+V clipboard history from recording it.
     * On Linux: Hints KDE Klipper / GNOME GPaste to drop it.
     *
     * There's almost no reason to disable this for a password manager.
     */
    // TODO: Wire to Settings screen with DataStore persistence
    val markAsSensitive: Boolean = true
}
