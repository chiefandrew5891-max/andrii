
@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)
package com.andrey.beautyplanner

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual object BackupCrypto {

    private const val ITERATIONS = 120_000
    private const val SALT_SIZE_BYTES = 16

    actual fun encryptBackupPayload(
        plaintext: String,
        password: String
    ): BackupEncryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }
        require(plaintext.isNotBlank()) { "Plaintext must not be blank." }

        val bridge = BackupCryptoBridgeConnector.encrypt
            ?: error("iOS backup crypto bridge is not connected.")

        val saltBase64 = randomSaltBase64(SALT_SIZE_BYTES)

        val result = runBlocking {
            val deferred = CompletableDeferred<Map<String, String>>()
            bridge.invoke(
                plaintext,
                password,
                saltBase64,
                ITERATIONS,
                deferred
            )
            deferred.await()
        }

        val ivBase64 = result["ivBase64"].orEmpty()
        val ciphertextBase64 = result["ciphertextBase64"].orEmpty()

        if (ivBase64.isBlank() || ciphertextBase64.isBlank()) {
            error("iOS backup encryption returned incomplete payload.")
        }

        return BackupEncryptionResult(
            metadata = BackupCryptoMetadata(
                kdf = "pbkdf2",
                iterations = ITERATIONS,
                saltBase64 = saltBase64,
                ivBase64 = ivBase64,
                ciphertextBase64 = ciphertextBase64
            )
        )
    }

    actual fun decryptBackupPayload(
        metadata: BackupCryptoMetadata,
        password: String
    ): BackupDecryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }

        if (metadata.kdf == "fallback") {
            return BackupDecryptionResult(
                plaintext = metadata.ciphertextBase64
            )
        }

        val bridge = BackupCryptoBridgeConnector.decrypt
            ?: error("iOS backup crypto bridge is not connected.")

        val plaintext = runBlocking {
            val deferred = CompletableDeferred<String>()
            bridge.invoke(
                metadata.ciphertextBase64,
                password,
                metadata.saltBase64,
                metadata.ivBase64,
                metadata.iterations,
                deferred
            )
            deferred.await()
        }

        if (plaintext.isBlank()) {
            error("iOS backup decryption failed.")
        }

        return BackupDecryptionResult(
            plaintext = plaintext
        )
    }

    private fun randomSaltBase64(size: Int): String {
        val bytes = ByteArray(size)

        val status = bytes.usePinned { pinned ->
            SecRandomCopyBytes(
                kSecRandomDefault,
                size.toULong(),
                pinned.addressOf(0)
            )
        }

        if (status != 0) {
            error("Unable to generate random salt.")
        }

        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = size.toULong()
            )
        }

        return data.base64EncodedStringWithOptions(0u)
    }
}