package com.sanket.tools.passwordmanager.data.export

import android.app.Activity
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred

class BackupFileActivityBridge {
    private var pendingCreateDocument: CompletableDeferred<Uri?>? = null
    private var pendingOpenDocument: CompletableDeferred<Uri?>? = null

    suspend fun awaitCreateDocument(activity: Activity, suggestedFileName: String): Uri? {
        ensureNoPendingOperation()
        pendingCreateDocument = CompletableDeferred()

        activity.startActivityForResult(
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, suggestedFileName)
            },
            REQUEST_CREATE_DOCUMENT
        )

        return pendingCreateDocument?.await()
    }

    suspend fun awaitOpenDocument(activity: Activity): Uri? {
        ensureNoPendingOperation()
        pendingOpenDocument = CompletableDeferred()

        activity.startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("application/json", "text/plain", "application/octet-stream")
                )
            },
            REQUEST_OPEN_DOCUMENT
        )

        return pendingOpenDocument?.await()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CREATE_DOCUMENT -> {
                pendingCreateDocument?.complete(if (resultCode == Activity.RESULT_OK) data?.data else null)
                pendingCreateDocument = null
            }

            REQUEST_OPEN_DOCUMENT -> {
                pendingOpenDocument?.complete(if (resultCode == Activity.RESULT_OK) data?.data else null)
                pendingOpenDocument = null
            }
        }
    }

    private fun ensureNoPendingOperation() {
        if (pendingCreateDocument != null || pendingOpenDocument != null) {
            throw IllegalStateException("Another backup operation is already running.")
        }
    }

    companion object {
        private const val REQUEST_CREATE_DOCUMENT = 8101
        private const val REQUEST_OPEN_DOCUMENT = 8102
    }
}
