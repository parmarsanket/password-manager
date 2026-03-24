package com.sanket.tools.passwordmanager.data.crypto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Handles encrypting and decrypting the vault backup file.
 *
 * Export Key derivation:
 *   PBKDF2(masterPassword + freshSalt, 200_000 iterations) → 256-bit AES key
 *
 * The salt is NOT secret — it is stored in plain text inside meta.json in the ZIP.
 * Without the correct master password the salt is useless.
 *
 * ZIP contents:
 *   ├── data.enc    ← AES-256-GCM encrypted JSON
 *   └── meta.json   ← version, exportedAt, salt (Base64), app version
 */
class ExportCrypto(private val cryptoEngine: CryptoEngine) {

    // ─────────────────────────────────────────────────────────────────────
    // EXPORT
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Encrypt a plain-text vault JSON string into a [ExportPackage].
     *
     * @param plainJson     The full decrypted vault JSON (from ExportManager)
     * @param masterPassword The user's master password — used ONLY to derive
     *                       the export key, then immediately discarded.
     * @return [ExportPackage] containing encrypted bytes + metadata
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(plainJson: String, masterPassword: String): ExportPackage {
        // Generate a fresh random salt just for this export
        val salt = cryptoEngine.generateSalt()

        // Derive the export key from master password + fresh salt
        val exportKey = cryptoEngine.deriveVaultKey(masterPassword, salt)

        // Encrypt the JSON using AES-256-GCM
        val encryptedData = cryptoEngine.encryptField(plainJson, exportKey)

        // Build the metadata (salt stored as Base64 — not secret)
        val meta = ExportMeta(
            version = EXPORT_VERSION,
            exportedAt = currentTimeMillis(),
            salt = Base64.encode(salt),
            appVersion = APP_VERSION
        )

        return ExportPackage(
            encryptedData = encryptedData,
            meta = Json.encodeToString(meta)
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // IMPORT
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Decrypt an [ExportPackage] back to plain-text JSON.
     *
     * @param pkg            The export package read from the ZIP file
     * @param masterPassword The user's master password — must be the same one
     *                       used when the backup was created
     * @return Decrypted vault JSON string, or throws if password is wrong
     * @throws Exception if the password is wrong (AES-GCM auth tag mismatch)
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(pkg: ExportPackage, masterPassword: String): String {
        // Parse metadata to get the salt that was used during export
        val meta = Json.decodeFromString<ExportMeta>(pkg.meta)
        val salt = Base64.decode(meta.salt)

        // Re-derive the exact same export key
        val exportKey = cryptoEngine.deriveVaultKey(masterPassword, salt)

        // Decrypt — throws if the password is wrong (auth tag mismatch)
        return cryptoEngine.decryptField(pkg.encryptedData, exportKey)
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────

    /** Parse metadata from a meta.json string */
    fun parseMeta(metaJson: String): ExportMeta =
        Json.decodeFromString(metaJson)

    companion object {
        const val EXPORT_VERSION = 2
        const val APP_VERSION    = "1.0.0"
        const val DATA_FILE_NAME = "data.enc"
        const val META_FILE_NAME = "meta.json"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DATA CLASSES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * In-memory representation of the ZIP contents.
 * Converted to actual ZIP bytes by ExportManager / ImportManager.
 */
data class ExportPackage(
    val encryptedData: String,  // AES-GCM encrypted vault JSON (Base64)
    val meta: String            // meta.json content
)

/**
 * meta.json — stored plain text inside the ZIP.
 * Contains the salt so any device can re-derive the export key.
 * Contains NO key, NO password, NO sensitive data.
 */
@Serializable
data class ExportMeta(
    val version: Int,
    val exportedAt: Long,
    val salt: String,       // Base64 encoded — NOT secret
    val appVersion: String
)

// ─────────────────────────────────────────────────────────────────────────────
// Export JSON schema — the plain text vault data before encryption
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class ExportVault(
    val version: Int = 2,
    val exportedAt: Long,
    val entries: List<ExportEntry>
)

@Serializable
data class ExportEntry(
    val siteOrApp: String,
    val iconEmoji: String,
    val createdAt: Long,
    val fields: List<ExportField>
)

@Serializable
data class ExportField(
    val label: String,
    val value: String,      // ⚠️ plain text — only in RAM during export/import
    val isSecret: Boolean,
    val order: Int
)

// expect/actual for current time (platform-specific)
expect fun currentTimeMillis(): Long
