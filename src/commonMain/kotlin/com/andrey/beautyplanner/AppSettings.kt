package com.andrey.beautyplanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.andrey.beautyplanner.notifications.NotificationSound
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val settingsJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

@Serializable
private data class SettingsSnapshot(
    val isDarkMode: Boolean = false,
    val selectedLanguage: String = "Русский",
    val fontSizeMode: String = "Средний",

    val notificationsEnabled: Boolean = true,
    val notificationSound: String = NotificationSound.DEFAULT.name,

    val reminderDaysBefore: Int = 0,
    val reminderHoursBefore: Int = 1,

    val servicePhone: String = ""
)

object AppSettings {

    private val storage: SettingsStorage by lazy { createSettingsStorage() }

    var isDarkMode by mutableStateOf(false)
    var selectedLanguage by mutableStateOf("Русский")
    var fontSizeMode by mutableStateOf("Средний")

    // --- notifications settings ---
    var notificationsEnabled by mutableStateOf(true)
    var notificationSound by mutableStateOf(NotificationSound.DEFAULT)

    // flexible reminder (days + hours)
    var reminderDaysBefore by mutableStateOf(0)   // 0..3
    var reminderHoursBefore by mutableStateOf(1)  // 0..12

    // Support phone
    var servicePhone by mutableStateOf("")

    fun reminderMinutesComputed(): List<Int> {
        val total = reminderDaysBefore * 24 * 60 + reminderHoursBefore * 60
        return if (total > 0) listOf(total) else emptyList()
    }

    val languageCodes = mapOf(
        "Italiano" to "it",
        "Русский" to "ru",
        "English" to "en",
        "Українська" to "uk"
    )

    fun getFontScale(): Float = when (fontSizeMode) {
        "Мелкий" -> 0.85f
        "Крупный" -> 1.25f
        else -> 1.0f
    }

    /**
     * Call once on app startup.
     * Android: after AndroidAppContext.context is set.
     */
    fun load() {
        val raw = runCatching { storage.read() }.getOrNull() ?: return
        if (raw.isBlank()) return

        val snapshot = runCatching { settingsJson.decodeFromString<SettingsSnapshot>(raw) }
            .getOrNull() ?: return

        isDarkMode = snapshot.isDarkMode
        selectedLanguage = snapshot.selectedLanguage
        fontSizeMode = snapshot.fontSizeMode

        notificationsEnabled = snapshot.notificationsEnabled
        notificationSound = runCatching { NotificationSound.valueOf(snapshot.notificationSound) }
            .getOrNull() ?: NotificationSound.DEFAULT

        reminderDaysBefore = snapshot.reminderDaysBefore
        reminderHoursBefore = snapshot.reminderHoursBefore

        servicePhone = snapshot.servicePhone

        // sync current language in Locales
        val code = languageCodes[selectedLanguage] ?: "en"
        Locales.currentLanguage = code
    }

    fun persist() {
        val snapshot = SettingsSnapshot(
            isDarkMode = isDarkMode,
            selectedLanguage = selectedLanguage,
            fontSizeMode = fontSizeMode,

            notificationsEnabled = notificationsEnabled,
            notificationSound = notificationSound.name,

            reminderDaysBefore = reminderDaysBefore,
            reminderHoursBefore = reminderHoursBefore,

            servicePhone = servicePhone
        )

        runCatching {
            storage.write(settingsJson.encodeToString(snapshot))
        }
    }
}