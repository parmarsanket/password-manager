package com.sanket.tools.passwordmanager.ui.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual class ClipboardManager {
    actual fun copyToClipboard(label: String, text: String) {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }
}
