package com.andrey.beautyplanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.andrey.beautyplanner.notifications.NotificationSound

object AppSettings {
    var isDarkMode by mutableStateOf(false)
    var selectedLanguage by mutableStateOf("Русский")
    var fontSizeMode by mutableStateOf("Средний")

    // --- notifications settings ---
    var notificationsEnabled by mutableStateOf(true)
    var notificationSound by mutableStateOf(NotificationSound.DEFAULT)

    // NEW: flexible reminder (days + hours)
    var reminderDaysBefore by mutableStateOf(0)   // 0..3
    var reminderHoursBefore by mutableStateOf(1)  // 0..12

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
}