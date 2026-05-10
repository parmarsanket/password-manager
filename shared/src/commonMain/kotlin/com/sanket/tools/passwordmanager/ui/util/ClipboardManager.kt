package com.sanket.tools.passwordmanager.ui.util

/**
 * Multiplatform Clipboard helper.
 */
expect class ClipboardManager {
    fun copyToClipboard(label: String, text: String, isSensitive: Boolean = false)
    fun clearClipboard()
    fun scheduleClear(delayMillis: Long)
    var onCleared: (() -> Unit)?
}
