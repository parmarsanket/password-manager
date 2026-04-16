package com.sanket.tools.passwordmanager.data.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Android actual: uses javax.crypto for all crypto operations.
 *
 * AES-256-GCM  → Cipher("AES/GCM/NoPadding")
 * PBKDF2       → SecretKeyFactory("PBKDF2WithHmacSHA256")
 * Random       → SecureRandom
 */
@OptIn(ExperimentalEncodingApi::class)
actual class CryptoEngine actual constructor() {

    private val secureRandom = SecureRandom()

    // ─────────────────────────────────────────────────────────────────────
    // FIELD ENCRYPTION / DECRYPTION
    // ─────────────────────────────────────────────────────────────────────

    actual fun encryptField(plainText: String, vaultKey: ByteArray): String {
        // Fresh random 12-byte IV for every single field
        val iv = ByteArray(IV_SIZE).also { secureRandom.nextBytes(it) }

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(vaultKey, "AES"),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )

        // Java's GCM doFinal returns: ciphertext ‖ authTag (16 bytes appended automatically)
        val cipherAndTag = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Pack: IV[12] ‖ Ciphertext+Tag → Base64
        val combined = iv + cipherAndTag
        return Base64.encode(combined)
    }

    actual fun decryptField(encryptedValue: String, vaultKey: ByteArray): String {
        val combined = Base64.decode(encryptedValue)

        // Unpack: first 12 bytes = IV, rest = ciphertext+tag
        val iv = combined.copyOfRange(0, IV_SIZE)
        val cipherAndTag = combined.copyOfRange(IV_SIZE, combined.size)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(vaultKey, "AES"),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )

        return cipher.doFinal(cipherAndTag).toString(Charsets.UTF_8)
    }

    // ─────────────────────────────────────────────────────────────────────
    // KEY DERIVATION
    // ─────────────────────────────────────────────────────────────────────

    actual fun deriveVaultKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val derived = factory.generateSecret(spec).encoded
        spec.clearPassword() // wipe password from memory
        return derived
    }

    actual fun generateSalt(): ByteArray =
        ByteArray(SALT_SIZE).also { secureRandom.nextBytes(it) }

    // ─────────────────────────────────────────────────────────────────────
    // PASSWORD HASHING (for verification only — separate from vault key)
    // ─────────────────────────────────────────────────────────────────────

    actual fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            HASH_ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return hash.toHex()
    }

    actual fun createPasswordVerifier(vaultKey: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(vaultKey)
            .toHex()
    }

    actual fun verifyPassword(password: String, salt: ByteArray, storedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return MessageDigest.isEqual(
            computedHash.encodeToByteArray(),
            storedHash.encodeToByteArray()
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // COMPANION
    // ─────────────────────────────────────────────────────────────────────

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PBKDF2_ALGORITHM       = "PBKDF2WithHmacSHA256"
        private const val IV_SIZE                = 12      // 96-bit IV (GCM standard)
        private const val TAG_LENGTH_BITS        = 128     // 16-byte auth tag
        private const val SALT_SIZE              = 16      // 128-bit salt
        private const val KEY_LENGTH_BITS        = 256     // AES-256
        private const val PBKDF2_ITERATIONS      = 200_000 // Per PRD spec
        private const val HASH_ITERATIONS        = 100_000 // For verification hash
    }
}

// Extension to convert ByteArray to hex string
internal fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
