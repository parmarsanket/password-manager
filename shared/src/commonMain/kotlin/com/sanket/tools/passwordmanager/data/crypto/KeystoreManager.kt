package com.sanket.tools.passwordmanager.data.crypto

/**
 * Common contract for storing and retrieving the Vault Key
 * in the platform's secure storage.
 *
 * Android → Android Keystore (hardware-backed AES wrapping key)
 * JVM     → Java PKCS12 KeyStore file in user home
 * iOS     → iOS Keychain (SecItemAdd / SecItemCopyMatching)
 *
 * The Vault Key NEVER hits disk unprotected.
 */
expect class KeystoreManager {

    /**
     * Encrypt and save the vault key in the platform secure store.
     * Overwrites any previously saved key.
     */
    fun saveVaultKey(vaultKey: ByteArray)

    /**
     * Load and decrypt the vault key from the platform secure store.
     * Returns null if no key has been saved yet (first launch).
     */
    fun loadVaultKey(): ByteArray?

    /**
     * Wipe the stored vault key — called on logout / app reset.
     */
    fun clearVaultKey()
}
