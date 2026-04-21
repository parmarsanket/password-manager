package com.sanket.tools.passwordmanager.ui.util

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context

actual class ClipboardManager(private val context: Context) {
    actual fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
