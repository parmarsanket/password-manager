package com.sanket.tools.passwordmanager.ui.util

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.SystemFlavorMap
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

actual class ClipboardManager {
    actual var onCleared: (() -> Unit)? = null
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledClear: java.util.concurrent.ScheduledFuture<*>? = null

    actual fun copyToClipboard(label: String, text: String, isSensitive: Boolean) {
        // Cancel any pending platform clear when a new copy occurs
        scheduledClear?.cancel(false)
        
        val selection = if (isSensitive) {
            SecureTransferable(text)
        } else {
            StringSelection(text)
        }
        try {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
        } catch (e: Exception) {
            // Fallback to basic string selection if custom flavors fail
            try {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
            } catch (inner: Exception) {
                // Ignore final fallback failure
            }
        }
    }

    /**
     * Specialized Transferable that includes Windows and Linux flags 
     * to prevent history, cloud sync, and monitor processing.
     */
    private class SecureTransferable(private val text: String) : Transferable {
        
        companion object {
            // Windows format names
            private const val FORMAT_EXCLUDE = "ExcludeClipboardContentFromMonitorProcessing"
            private const val FORMAT_HISTORY = "CanIncludeInClipboardHistory"
            private const val FORMAT_CLOUD = "CanUploadToCloudClipboard"
            
            // Linux format name (Respected by KDE Klipper, GNOME GPaste, CopyQ, etc.)
            private const val FORMAT_LINUX_HINT = "x-kde-passwordManagerHint"

            // DataFlavors
            private val flavorExclude = DataFlavor("application/x-password-manager-exclude", FORMAT_EXCLUDE)
            private val flavorHistory = DataFlavor("application/x-password-manager-history", FORMAT_HISTORY)
            private val flavorCloud = DataFlavor("application/x-password-manager-cloud", FORMAT_CLOUD)
            private val flavorLinuxHint = DataFlavor("application/x-password-manager-linux", FORMAT_LINUX_HINT)

            init {
                try {
                    val flavorMap = SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap
                    
                    // Windows: Exclude from monitors
                    flavorMap.addUnencodedNativeForFlavor(flavorExclude, FORMAT_EXCLUDE)
                    flavorMap.addFlavorForUnencodedNative(FORMAT_EXCLUDE, flavorExclude)
                    
                    // Windows: Exclude from Win+V history
                    flavorMap.addUnencodedNativeForFlavor(flavorHistory, FORMAT_HISTORY)
                    flavorMap.addFlavorForUnencodedNative(FORMAT_HISTORY, flavorHistory)
                    
                    // Windows: Exclude from Cloud Sync
                    flavorMap.addUnencodedNativeForFlavor(flavorCloud, FORMAT_CLOUD)
                    flavorMap.addFlavorForUnencodedNative(FORMAT_CLOUD, flavorCloud)
                    
                    // Linux: Exclude from clipboard managers
                    flavorMap.addUnencodedNativeForFlavor(flavorLinuxHint, FORMAT_LINUX_HINT)
                    flavorMap.addFlavorForUnencodedNative(FORMAT_LINUX_HINT, flavorLinuxHint)
                } catch (e: Exception) {
                    // Ignore mapping errors
                }
            }
        }

        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(
            DataFlavor.stringFlavor,
            flavorExclude,
            flavorHistory,
            flavorCloud,
            flavorLinuxHint
        )

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = 
            getTransferDataFlavors().any { it.equals(flavor) }

        override fun getTransferData(flavor: DataFlavor): Any {
            return when {
                flavor.equals(DataFlavor.stringFlavor) -> text
                flavor.equals(flavorExclude) -> {
                    // Windows monitor exclude format expects empty data (or a single zero byte)
                    ByteArrayInputStream(ByteArray(1) { 0 })
                }
                flavor.equals(flavorHistory) || flavor.equals(flavorCloud) -> {
                    // Windows expects a 4-byte integer (DWORD). 0 = False.
                    val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    buffer.putInt(0)
                    ByteArrayInputStream(buffer.array())
                }
                flavor.equals(flavorLinuxHint) -> {
                    // Linux expects the exact string "secret" to instruct the manager to drop it
                    ByteArrayInputStream("secret".toByteArray(Charsets.UTF_8))
                }
                else -> throw java.awt.datatransfer.UnsupportedFlavorException(flavor)
            }
        }
    }

    actual fun clearClipboard() {
        scheduledClear?.cancel(false)
        try {
            val selection = StringSelection("")
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(selection, null)
        } catch (e: Exception) {
            // Ignore
        }
        onCleared?.invoke()
    }

    actual fun scheduleClear(delayMillis: Long) {
        scheduledClear?.cancel(false)
        // Platform-level backup timer for clearing the clipboard
        scheduledClear = executor.schedule({
            clearClipboard()
        }, delayMillis, TimeUnit.MILLISECONDS)

        // Also keep the shutdown hook for safety
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                clearClipboard()
            } catch (e: Exception) {
                // Ignore
            }
        })
    }
}
