package com.sanket.tools.passwordmanager.data.crypto

import java.io.File
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

/**
 * JVM Desktop actual: wraps the vault key in a PKCS12 keystore file
 * stored in the user's home directory.
 *
 * The keystore is protected with a password derived from the OS username,
 * so it's tied to the local user account.
 *
 * ⚠️ For production Desktop use, consider using Windows DPAPI, macOS Keychain,
 * or a user-input password to protect the keystore file.
 */
actual class KeystoreManager {

    private val keystoreFile = File(
        System.getProperty("user.home"),
        ".password_manager_keystore.p12"
    )

    // This password protects the PKCS12 file — derived from the OS user name.
    // It is NOT the vault key itself.
    private val keystorePassword: CharArray =
        "pm-ks-${System.getProperty("user.name")}-local".toCharArray()

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    actual fun saveVaultKey(vaultKey: ByteArray) {
        val ks = loadOrCreateKeystore()
        val secretEntry = KeyStore.SecretKeyEntry(SecretKeySpec(vaultKey, "AES"))
        ks.setEntry(
            VAULT_KEY_ALIAS,
            secretEntry,
            KeyStore.PasswordProtection(keystorePassword)
        )
        keystoreFile.outputStream().use { ks.store(it, keystorePassword) }
    }

    actual fun loadVaultKey(): ByteArray? {
        if (!keystoreFile.exists()) return null
        val ks = loadOrCreateKeystore()
        val entry = ks.getEntry(
            VAULT_KEY_ALIAS,
            KeyStore.PasswordProtection(keystorePassword)
        ) as? KeyStore.SecretKeyEntry
        return entry?.secretKey?.encoded
    }

    actual fun clearVaultKey() {
        if (keystoreFile.exists()) {
            keystoreFile.delete()
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────

    private fun loadOrCreateKeystore(): KeyStore {
        val ks = KeyStore.getInstance("PKCS12")
        if (keystoreFile.exists()) {
            keystoreFile.inputStream().use { ks.load(it, keystorePassword) }
        } else {
            ks.load(null, null) // initialise empty keystore
        }
        return ks
    }

    companion object {
        private const val VAULT_KEY_ALIAS = "vault_key"
    }
}
