package com.sanket.tools.passwordmanager.data.export

import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.ExportCrypto
import com.sanket.tools.passwordmanager.data.crypto.ExportEntry
import com.sanket.tools.passwordmanager.data.crypto.ExportField
import com.sanket.tools.passwordmanager.data.crypto.ExportPackage
import com.sanket.tools.passwordmanager.data.crypto.ExportVault
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.crypto.currentTimeMillis
import com.sanket.tools.passwordmanager.data.repository.PasswordRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles generating the ExportPackage from DB data.
 */
class ExportManager(
    private val repository: PasswordRepository,
    private val cryptoEngine: CryptoEngine,
    private val exportCrypto: ExportCrypto,
    private val session: PassworldSession
) {
    suspend fun createExportPackage(masterPassword: String): ExportPackage {
        val key = session.passworldKey.value ?: throw Exception("Unauthorized")
        val rawEntries = repository.getAllEntries().first()

        val exportEntries = rawEntries.map { relation ->
            ExportEntry(
                siteOrApp = relation.entry.siteOrApp,
                iconEmoji = relation.entry.iconEmoji,
                createdAt = relation.entry.createdAt,
                fields = relation.fields.map { field ->
                    ExportField(
                        label = field.fieldLabel,
                        value = cryptoEngine.decryptField(field.encryptedValue, key),
                        isSecret = field.isSecret,
                        order = field.sortOrder
                    )
                }
            )
        }

        val vault = ExportVault(
            exportedAt = currentTimeMillis(),
            entries = exportEntries
        )

        val plainJson = Json.encodeToString(vault)
        return exportCrypto.encrypt(plainJson, masterPassword)
    }
}
