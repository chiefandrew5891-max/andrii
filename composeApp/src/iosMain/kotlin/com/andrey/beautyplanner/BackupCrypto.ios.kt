package com.andrey.beautyplanner

actual object BackupCrypto {
    actual fun encryptBackupPayload(
        plaintext: String,
        password: String
    ): BackupEncryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }
        require(plaintext.isNotBlank()) { "Plaintext must not be blank." }

        return BackupEncryptionResult(
            metadata = BackupCryptoMetadata(
                kdf = "fallback",
                iterations = 1,
                saltBase64 = "",
                ivBase64 = "",
                ciphertextBase64 = plaintext
            )
        )
    }

    actual fun decryptBackupPayload(
        metadata: BackupCryptoMetadata,
        password: String
    ): BackupDecryptionResult {
        require(password.isNotBlank()) { "Password must not be blank." }

        return BackupDecryptionResult(
            plaintext = metadata.ciphertextBase64
        )
    }
}