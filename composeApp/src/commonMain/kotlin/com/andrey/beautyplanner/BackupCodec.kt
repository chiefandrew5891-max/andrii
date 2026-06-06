package com.andrey.beautyplanner

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val backupJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

sealed class ParsedBackupFile {
    data class LegacyPlainPayload(val payloadJson: String) : ParsedBackupFile()
    data class PlainContainer(val container: BackupContainer) : ParsedBackupFile()
    data class EncryptedContainer(val container: BackupContainer) : ParsedBackupFile()
}

object BackupCodec {

    fun createPlainBackupFile(
        payloadJson: String,
        appointmentsCount: Int
    ): String {
        val container = BackupContainer(
            encrypted = false,
            payload = payloadJson,
            crypto = null,
            createdAtEpochMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            appointmentsCount = appointmentsCount
        )
        return backupJson.encodeToString(container)
    }
    fun createEncryptedBackupFile(
        payloadJson: String,
        password: String,
        appointmentsCount: Int
    ): String {
        val encrypted = BackupCrypto.encryptBackupPayload(
            plaintext = payloadJson,
            password = password
        )
        val container = BackupContainer(
            encrypted = true,
            payload = null,
            crypto = encrypted.metadata,
            createdAtEpochMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            appointmentsCount = appointmentsCount
        )
        return backupJson.encodeToString(container)
    }

    fun parseBackupFile(text: String): ParsedBackupFile? {
        if (text.isBlank()) return null

        val asContainer = runCatching {
            backupJson.decodeFromString<BackupContainer>(text)
        }.getOrNull()

        if (asContainer != null && asContainer.type == BACKUP_TYPE) {
            return if (asContainer.encrypted) {
                if (asContainer.crypto == null) null
                else ParsedBackupFile.EncryptedContainer(asContainer)
            } else {
                if (asContainer.payload.isNullOrBlank()) null
                else ParsedBackupFile.PlainContainer(asContainer)
            }
        }

        val legacy = DataManager.importBackupPayload(text)
        return if (legacy.isNotEmpty()) {
            ParsedBackupFile.LegacyPlainPayload(text)
        } else {
            null
        }
    }

    fun extractPlainPayload(parsed: ParsedBackupFile): String? {
        return when (parsed) {
            is ParsedBackupFile.LegacyPlainPayload -> parsed.payloadJson
            is ParsedBackupFile.PlainContainer -> parsed.container.payload
            is ParsedBackupFile.EncryptedContainer -> null
        }
    }

    fun decryptPayload(
        container: BackupContainer,
        password: String
    ): String? {
        val crypto = container.crypto ?: return null
        return runCatching {
            BackupCrypto.decryptBackupPayload(
                metadata = crypto,
                password = password
            ).plaintext
        }.getOrNull()
    }
}