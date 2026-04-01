package com.sanket.tools.passwordmanager.data.export

import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.ExportCrypto
import com.sanket.tools.passwordmanager.data.crypto.ExportPackage
import com.sanket.tools.passwordmanager.data.crypto.ExportVault
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.db.CredentialField
import com.sanket.tools.passwordmanager.data.db.PasswordEntry
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import kotlinx.serialization.json.Json

/**
 * Handles importing data from an ExportPackage.
 */
class ImportManager(
    private val repository: PasswordRepository,
    private val cryptoEngine: CryptoEngine,
    private val exportCrypto: ExportCrypto,
    private val session: PassworldSession
) {
    suspend fun importPackage(pkg: ExportPackage, masterPassword: String): Int {
        val key = session.passworldKey.value ?: throw Exception("Unauthorized")
        
        // 1. Decrypt JSON from package
        val plainJson = exportCrypto.decrypt(pkg, masterPassword)
        val vault = Json.decodeFromString<ExportVault>(plainJson)

        // 2. Insert into DB (re-encrypting with local key)
        vault.entries.forEach { exportEntry ->
            val entry = PasswordEntry(
                siteOrApp = exportEntry.siteOrApp,
                iconEmoji = exportEntry.iconEmoji,
                createdAt = exportEntry.createdAt,
                updatedAt = exportEntry.createdAt
            )

            val fields = exportEntry.fields.map { exportField ->
                CredentialField(
                    entryId = 0, // repo will fix
                    fieldLabel = exportField.label,
                    encryptedValue = cryptoEngine.encryptField(exportField.value, key),
                    isSecret = exportField.isSecret,
                    sortOrder = exportField.order
                )
            }

            repository.saveEntry(entry, fields)
        }

        return vault.entries.size
    }
}
