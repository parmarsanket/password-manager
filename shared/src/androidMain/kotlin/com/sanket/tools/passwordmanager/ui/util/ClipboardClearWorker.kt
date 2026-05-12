package com.sanket.tools.passwordmanager.ui.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ClipboardClearWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // ── Industry-level Aggressive Wipe & History Flood ──────────────────
        // Mirrors the same logic from ClipboardManager.clearClipboard().
        // Keyboards like Gboard and Samsung maintain their own history stacks.
        // Even if we clear the primary clip, the item remains in their history.
        // We "flood" the clipboard with 20 unique invisible items to push
        // the sensitive data off the bottom of the history stack permanently.
        try {
            for (i in 1..20) {
                // Use varying numbers of zero-width spaces so the keyboard treats them as unique
                val invisibleText = "\u200B".repeat(i)
                val blankClip = ClipData.newPlainText("cleared_$i", invisibleText).apply {
                    val extras = PersistableBundle().apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                        }
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                        putBoolean("com.google.android.content.extra.IS_SENSITIVE", true)
                        putBoolean("com.samsung.android.content.clipdescription.extra.IS_SENSITIVE", true)
                    }
                    description.extras = extras
                }
                clipboard.setPrimaryClip(blankClip)

                // Small sleep to ensure the clipboard service processes each IPC call distinctly
                Thread.sleep(5)
            }
        } catch (e: Exception) {
            // Ignore if flooding fails
        }

        // Now perform the official system clear for the primary slot
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                clipboard.clearPrimaryClip()
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }

        return Result.success()
    }
}
