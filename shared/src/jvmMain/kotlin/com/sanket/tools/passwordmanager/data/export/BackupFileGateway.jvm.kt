package com.sanket.tools.passwordmanager.data.export

import com.sanket.tools.passwordmanager.data.crypto.ExportPackage
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import kotlinx.serialization.json.Json

actual class BackupFileGateway {
    private val backupJson = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    actual suspend fun exportPackage(pkg: ExportPackage, suggestedFileName: String): String {
        val file = chooseSaveFile(suggestedFileName)
            ?: throw IllegalStateException("Export cancelled.")

        file.writeText(backupJson.encodeToString(ExportPackage.serializer(), pkg))
        return file.absolutePath
    }

    actual suspend fun importPackage(): ExportPackage {
        val file = chooseOpenFile()
            ?: throw IllegalStateException("Import cancelled.")

        return backupJson.decodeFromString(ExportPackage.serializer(), file.readText())
    }

    private fun chooseSaveFile(suggestedFileName: String): File? {
        val dialog = createDialog(FileDialog.SAVE)
        dialog.file = suggestedFileName
        dialog.isVisible = true

        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName)
    }

    private fun chooseOpenFile(): File? {
        val dialog = createDialog(FileDialog.LOAD)
        dialog.isVisible = true

        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName)
    }

    private fun createDialog(mode: Int): FileDialog {
        val owner = if (GraphicsEnvironment.isHeadless()) {
            null
        } else {
            Frame().apply {
                addWindowListener(
                    object : WindowAdapter() {
                        override fun windowClosing(event: WindowEvent?) {
                            dispose()
                        }
                    }
                )
            }
        }

        return FileDialog(owner, "Passworld Backup", mode).apply {
            isMultipleMode = false
        }
    }
}
