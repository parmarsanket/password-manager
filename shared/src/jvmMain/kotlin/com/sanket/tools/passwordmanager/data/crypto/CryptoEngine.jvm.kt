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
 * JVM Desktop actual: identical logic to Android (both use javax.crypto).
 * No Android-specific APIs used here.
 */
@OptIn(ExperimentalEncodingApi::class)
actual class CryptoEngine actual constructor() {

    private val secureRandom = SecureRandom()

    actual fun encryptField(plainText: String, vaultKey: ByteArray): String {
        val iv = ByteArray(IV_SIZE).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(vaultKey, "AES"),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )
        val cipherAndTag = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encode(iv + cipherAndTag)
    }

    actual fun decryptField(encryptedValue: String, vaultKey: ByteArray): String {
        val combined = Base64.decode(encryptedValue)
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

    actual fun deriveVaultKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val derived = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return derived
    }

    actual fun generateSalt(): ByteArray =
        ByteArray(SALT_SIZE).also { secureRandom.nextBytes(it) }

    actual fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return hash.joinToString("") { "%02x".format(it) }
    }

    actual fun createPasswordVerifier(vaultKey: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(vaultKey)
            .joinToString("") { "%02x".format(it) }
    }

    actual fun verifyPassword(password: String, salt: ByteArray, storedHash: String): Boolean =
        MessageDigest.isEqual(
            hashPassword(password, salt).encodeToByteArray(),
            storedHash.encodeToByteArray()
        )

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PBKDF2_ALGORITHM       = "PBKDF2WithHmacSHA256"
        private const val IV_SIZE                = 12
        private const val TAG_LENGTH_BITS        = 128
        private const val SALT_SIZE              = 16
        private const val KEY_LENGTH_BITS        = 256
        private const val PBKDF2_ITERATIONS      = 200_000
        private const val HASH_ITERATIONS        = 100_000
    }
}
