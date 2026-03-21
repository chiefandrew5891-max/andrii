package com.andrey.beautyplanner

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
}

object DataManager {
    // Путь к файлу будет инициализироваться при первом доступе
    private var storageFile: File? = null

    fun initialize(filesDir: File) {
        storageFile = File(filesDir, "appointments_db.json")
    }

    fun saveToDatabase(data: List<Appointment>) {
        try {
            val jsonString = jsonConfig.encodeToString(data)
            storageFile?.writeText(jsonString)
        } catch (e: Exception) {
            println("Save error: ${e.message}")
        }
    }

    fun loadFromDatabase(): List<Appointment> {
        return try {
            val file = storageFile
            if (file != null && file.exists()) {
                val jsonString = file.readText()
                jsonConfig.decodeFromString<List<Appointment>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Load error: ${e.message}")
            emptyList()
        }
    }

    fun exportBackup(data: List<Appointment>): String {
        return try {
            jsonConfig.encodeToString(data)
        } catch (e: Exception) { "" }
    }

    fun importBackup(json: String): List<Appointment> {
        if (json.isBlank()) return emptyList()
        return try {
            jsonConfig.decodeFromString<List<Appointment>>(json)
        } catch (e: Exception) { emptyList() }
    }
}