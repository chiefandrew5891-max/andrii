package com.andrey.beautyplanner

import kotlinx.serialization.Serializable

@Serializable
data class BackupContainer(
    val type: String = BACKUP_TYPE,
    val version: Int = BACKUP_VERSION,
    val encrypted: Boolean,
    val payload: String? = null,
    val crypto: BackupCryptoMetadata? = null
)

@Serializable
data class BackupCryptoMetadata(
    val kdf: String,
    val iterations: Int,
    val saltBase64: String,
    val ivBase64: String,
    val ciphertextBase64: String
)

data class BackupEncryptionResult(
    val metadata: BackupCryptoMetadata
)

data class BackupDecryptionResult(
    val plaintext: String
)

const val BACKUP_TYPE = "beautyplanner-backup"
const val BACKUP_VERSION = 2