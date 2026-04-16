package com.sanket.tools.passwordmanager.data.crypto

/**
 * Common crypto contract for:
 *  - AES-256-GCM field encryption / decryption
 *  - PBKDF2-HMAC-SHA256 vault key derivation
 *  - Master password hashing for verification
 *  - Secure random salt generation
 *
 * Android / JVM → javax.crypto
 * iOS           → CommonCrypto (platform.CoreCrypto)
 */
expect class CryptoEngine() {

    /**
     * Encrypt a plain text string using AES-256-GCM.
     *
     * Returns Base64( IV[12] ‖ Ciphertext ‖ AuthTag[16] )
     * A fresh random IV is generated for every call — NEVER reused.
     *
     * @param plainText  The sensitive value to encrypt (e.g. "abc123!")
     * @param vaultKey   The 256-bit AES vault key (32 bytes)
     */
    fun encryptField(plainText: String, vaultKey: ByteArray): String

    /**
     * Decrypt a Base64(IV ‖ Ciphertext ‖ AuthTag) string back to plain text.
     *
     * @throws Exception if the key or ciphertext is wrong (auth tag mismatch)
     */
    fun decryptField(encryptedValue: String, vaultKey: ByteArray): String

    /**
     * Derive a 256-bit AES vault key from a master password + salt.
     *
     * Algorithm: PBKDF2-HMAC-SHA256 with 200,000 iterations
     *
     * @param password  The user's master password
     * @param salt      16-byte random salt (stored in DataStore)
     * @return 32-byte AES key
     */
    fun deriveVaultKey(password: String, salt: ByteArray): ByteArray

    /**
     * Generate a cryptographically random 16-byte salt.
     * Call once on first launch — store the result in DataStore.
     */
    fun generateSalt(): ByteArray

    /**
     * Hash a master password for local verification.
     * Uses PBKDF2 with a separate "hash" iteration count (100,000).
     *
     * @return hex-encoded hash string suitable for storage
     */
    fun hashPassword(password: String, salt: ByteArray): String

    /**
     * Builds the stored verifier hash from a derived vault key.
     *
     * This lets setup/login use a single PBKDF2 pass while still keeping
     * a stable verifier in preferences.
     */
    fun createPasswordVerifier(vaultKey: ByteArray): String

    /**
     * Verify a master password against a stored hash.
     */
    fun verifyPassword(password: String, salt: ByteArray, storedHash: String): Boolean
}
