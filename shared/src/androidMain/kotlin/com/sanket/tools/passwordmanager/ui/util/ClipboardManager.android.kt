package com.sanket.tools.passwordmanager.ui.util

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

actual class ClipboardManager(private val context: Context) : DefaultLifecycleObserver {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
    actual var onCleared: (() -> Unit)? = null

    private val screenLockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                clearClipboard()
            }
        }
    }

    init {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            
            // Register receiver for screen lock
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(screenLockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(screenLockReceiver, filter)
            }
        } catch (e: Exception) {
            // Log or handle initialization error
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to background. 
        // We DON'T clear here because the user needs to switch to another app to paste.
        // The SecureClipboardManager timer or "Clear on Resume" will handle it.
    }

    actual fun copyToClipboard(label: String, text: String, isSensitive: Boolean) {
        val clip = ClipData.newPlainText(label, text)
        
        if (isSensitive) {
            val extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
                // Backwards compatibility flags for keyboards
                putBoolean("android.content.extra.IS_SENSITIVE", true)
                putBoolean("com.google.android.content.extra.IS_SENSITIVE", true)
                putBoolean("com.samsung.android.content.clipdescription.extra.IS_SENSITIVE", true)
            }
            clip.description.extras = extras
        }
        
        clipboard.setPrimaryClip(clip)
    }

    actual fun clearClipboard() {
        // Industry-level Aggressive Wipe & History Flood:
        // Keyboards like Gboard and Samsung maintain their own history stacks.
        // Even if we clear the primary clip, the item remains in their history.
        // To forcefully erase it, we "flood" the clipboard with 20 unique invisible items.
        // This pushes the sensitive data off the bottom of the history stack, permanently deleting it.
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
        
        onCleared?.invoke()
    }

    actual fun scheduleClear(delayMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<ClipboardClearWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "clipboard_clear_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
