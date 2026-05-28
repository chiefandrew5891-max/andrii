package com.andrey.beautyplanner

expect object BackupCrypto {
    fun encryptBackupPayload(
        plaintext: String,
        password: String
    ): BackupEncryptionResult

    fun decryptBackupPayload(
        metadata: BackupCryptoMetadata,
        password: String
    ): BackupDecryptionResult
}