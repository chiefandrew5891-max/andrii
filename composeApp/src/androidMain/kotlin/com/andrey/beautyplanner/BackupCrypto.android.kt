package com.andrey.beautyplanner

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object BackupCrypto {

    private const val KDF_ALG_SHA256 = "PBKDF2WithHmacSHA256"
    private const val KDF_ALG_SHA1 = "PBKDF2WithHmacSHA1"
    private const val AES_MODE = "AES/GCM/NoPadding"

    private const val ITERATIONS = 120_000
    private const val KEY_SIZE_BITS = 256
    private const val SALT_SIZE_BYTES = 16
    private const val IV_SIZE_BYTES = 12
    private const val GCM_TAG_BITS = 128

    actual fun encryptBackupPayload(
        plaintext: String,
        password: String
    ): BackupEncryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }
        require(plaintext.isNotBlank()) { "Plaintext must not be blank." }

        val salt = randomBytes(SALT_SIZE_BYTES)
        val iv = randomBytes(IV_SIZE_BYTES)

        val key = deriveAesKey(password, salt)

        val cipher = Cipher.getInstance(AES_MODE)
        val gcmSpec = GCMParameterSpec(GCM_TAG_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return BackupEncryptionResult(
            metadata = BackupCryptoMetadata(
                kdf = "pbkdf2",
                iterations = ITERATIONS,
                saltBase64 = salt.toBase64(),
                ivBase64 = iv.toBase64(),
                ciphertextBase64 = ciphertext.toBase64()
            )
        )
    }

    actual fun decryptBackupPayload(
        metadata: BackupCryptoMetadata,
        password: String
    ): BackupDecryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }

        val salt = metadata.saltBase64.fromBase64()
        val iv = metadata.ivBase64.fromBase64()
        val ciphertext = metadata.ciphertextBase64.fromBase64()

        val key = deriveAesKey(
            password = password,
            salt = salt,
            iterations = metadata.iterations
        )

        val cipher = Cipher.getInstance(AES_MODE)
        val gcmSpec = GCMParameterSpec(GCM_TAG_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val plaintextBytes = cipher.doFinal(ciphertext)

        return BackupDecryptionResult(
            plaintext = plaintextBytes.toString(Charsets.UTF_8)
        )
    }

    private fun deriveAesKey(
        password: String,
        salt: ByteArray,
        iterations: Int = ITERATIONS
    ): SecretKeySpec {
        val keyBytes = deriveKeyBytes(password, salt, iterations)
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun deriveKeyBytes(
        password: String,
        salt: ByteArray,
        iterations: Int
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_SIZE_BITS)

        try {
            return SecretKeyFactory
                .getInstance(KDF_ALG_SHA256)
                .generateSecret(spec)
                .encoded
        } catch (_: Exception) {
            return SecretKeyFactory
                .getInstance(KDF_ALG_SHA1)
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun randomBytes(size: Int): ByteArray {
        val out = ByteArray(size)
        SecureRandom().nextBytes(out)
        return out
    }

    private fun ByteArray.toBase64(): String =
        Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        Base64.decode(this, Base64.NO_WRAP)
}