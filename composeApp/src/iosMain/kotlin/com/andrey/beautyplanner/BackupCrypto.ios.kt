package com.andrey.beautyplanner

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.Foundation.NSError

actual object BackupCrypto {

    actual fun encryptBackupPayload(
        plaintext: String,
        password: String
    ): BackupEncryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }
        require(plaintext.isNotBlank()) { "Plaintext must not be blank." }

        val saltBytes = randomBytes(16)
        val saltBase64 = saltBytes.encodeBase64()

        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            errorPtr.value = null

            val result = BackupCryptoBridge.encrypt(
                plaintext = plaintext,
                password = password,
                saltBase64 = saltBase64,
                iterations = 120_000,
                error = errorPtr.ptr
            )

            val error = errorPtr.value
            require(result != null && error == null) {
                error?.localizedDescription ?: "iOS encryption failed."
            }

            val ivBase64 = result.objectForKey("ivBase64") as? String
                ?: error("Missing ivBase64 from iOS encryption result.")
            val ciphertextBase64 = result.objectForKey("ciphertextBase64") as? String
                ?: error("Missing ciphertextBase64 from iOS encryption result.")

            return BackupEncryptionResult(
                metadata = BackupCryptoMetadata(
                    kdf = "pbkdf2",
                    iterations = 120_000,
                    saltBase64 = saltBase64,
                    ivBase64 = ivBase64,
                    ciphertextBase64 = ciphertextBase64
                )
            )
        }
    }

    actual fun decryptBackupPayload(
        metadata: BackupCryptoMetadata,
        password: String
    ): BackupDecryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }

        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            errorPtr.value = null

            val result = BackupCryptoBridge.decrypt(
                ciphertextBase64 = metadata.ciphertextBase64,
                password = password,
                saltBase64 = metadata.saltBase64,
                ivBase64 = metadata.ivBase64,
                iterations = metadata.iterations,
                error = errorPtr.ptr
            )

            val error = errorPtr.value
            require(result != null && error == null) {
                error?.localizedDescription ?: "iOS decryption failed."
            }

            return BackupDecryptionResult(
                plaintext = result.toString()
            )
        }
    }

    private fun randomBytes(size: Int): ByteArray {
        val array = ByteArray(size)
        kotlin.random.Random.Default.nextBytes(array)
        return array
    }

    private fun ByteArray.encodeBase64(): String {
        return platform.Foundation.NSData.create(bytes = this.refTo(0), length = this.size.toULong())
            .base64EncodedStringWithOptions(0u)
    }
}