package com.sanket.tools.passwordmanager.data.export

import com.sanket.tools.passwordmanager.data.crypto.ExportPackage

expect class BackupFileGateway {
    suspend fun exportPackage(pkg: ExportPackage, suggestedFileName: String): String
    suspend fun importPackage(): ExportPackage
}
