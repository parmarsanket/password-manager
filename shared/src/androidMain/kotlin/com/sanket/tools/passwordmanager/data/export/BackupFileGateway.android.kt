package com.sanket.tools.passwordmanager.data.export

import android.content.Context
import com.sanket.tools.passwordmanager.data.crypto.ActivityProvider
import com.sanket.tools.passwordmanager.data.crypto.ExportPackage
import kotlinx.serialization.json.Json

actual class BackupFileGateway(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val activityBridge: BackupFileActivityBridge
) {
    private val backupJson = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    actual suspend fun exportPackage(pkg: ExportPackage, suggestedFileName: String): String {
        val activity = activityProvider.currentActivity()
            ?: throw IllegalStateException("No active screen is available for export.")
        val uri = activityBridge.awaitCreateDocument(activity, suggestedFileName)
            ?: throw IllegalStateException("Export cancelled.")

        context.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
            requireNotNull(writer) { "Unable to write the selected backup file." }
            writer.write(backupJson.encodeToString(ExportPackage.serializer(), pkg))
        }

        return uri.toString()
    }

    actual suspend fun importPackage(): ExportPackage {
        val activity = activityProvider.currentActivity()
            ?: throw IllegalStateException("No active screen is available for import.")
        val uri = activityBridge.awaitOpenDocument(activity)
            ?: throw IllegalStateException("Import cancelled.")

        val backupText = context.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
            requireNotNull(reader) { "Unable to read the selected backup file." }
            reader.readText()
        }

        return backupJson.decodeFromString(ExportPackage.serializer(), backupText)
    }
}
