package com.andrey.beautyplanner

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ОБЪЕКТ НАСТРОЕК
object AppSettings {
    var isDarkMode by mutableStateOf(false)
    var selectedLanguage by mutableStateOf("Русский")
    var fontSizeMode by mutableStateOf("Средний")

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPage(onExport: () -> Unit, onImport: () -> Unit) {
    val context = LocalContext.current
    val languages = AppSettings.languageCodes.keys.toList()

    val themeOptions = listOf(Locales.t("theme_light"), Locales.t("theme_dark"))
    val fontOptions = listOf(Locales.t("font_small"), Locales.t("font_medium"), Locales.t("font_large"))

    val fontScale = AppSettings.getFontScale()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = Locales.t("nav_settings"),
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold
        )

        // Селектор Языка
        SettingsDropdown(
            label = Locales.t("language_label"),
            selected = AppSettings.selectedLanguage,
            items = languages,
            onSelect = { newValue ->
                // ПРОВЕРКА: Если язык тот же самый, ничего не делаем (стоп цикл)
                if (AppSettings.selectedLanguage != newValue) {
                    AppSettings.selectedLanguage = newValue
                    val code = AppSettings.languageCodes[newValue] ?: "en"
                    // Обновляем Locales вручную перед рекреацией
                    Locales.currentLanguage = code
                    setLocale(context as Activity, code)
                }
            }
        )

        // Селектор Темы
        SettingsDropdown(
            label = Locales.t("theme_label"),
            selected = if (AppSettings.isDarkMode) Locales.t("theme_dark") else Locales.t("theme_light"),
            items = themeOptions,
            onSelect = { newValue ->
                AppSettings.isDarkMode = (newValue == Locales.t("theme_dark"))
            }
        )

        // Селектор Шрифта
        SettingsDropdown(
            label = Locales.t("font_size_label"),
            selected = when (AppSettings.fontSizeMode) {
                "Мелкий" -> Locales.t("font_small")
                "Крупный" -> Locales.t("font_large")
                else -> Locales.t("font_medium")
            },
            items = fontOptions,
            onSelect = { newValue ->
                AppSettings.fontSizeMode = when (newValue) {
                    Locales.t("font_small") -> "Мелкий"
                    Locales.t("font_large") -> "Крупный"
                    else -> "Средний"
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Divider()

        // Раздел Резервного копирования
        Column {
            Text(
                text = Locales.t("backup_section"),
                fontSize = (14 * fontScale).sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(0.45f).height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Text(text = Locales.t("export_db"), fontSize = (14 * fontScale).sp)
                }

                Spacer(modifier = Modifier.weight(0.1f))

                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier.weight(0.45f).height(38.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Locales.t("import_db"), fontSize = (14 * fontScale).sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { /* Логика */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = Locales.t("privacy_policy"), fontSize = (16 * fontScale).sp)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsDropdown(
    label: String,
    selected: String,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val fontScale = AppSettings.getFontScale()

    Column {
        Text(text = label, fontSize = (14 * fontScale).sp, color = Color.Gray)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontSize = (16 * fontScale).sp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(onClick = {
                        onSelect(item)
                        expanded = false
                    }) {
                        Text(text = item, fontSize = (16 * fontScale).sp)
                    }
                }
            }
        }
    }
}

fun setLocale(activity: Activity, langCode: String) {
    val locale = Locale(langCode)
    Locale.setDefault(locale)
    val config = Configuration(activity.resources.configuration)
    config.setLocale(locale)
    activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    // РЕКРЕАЦИЯ только если нужно
    activity.recreate()
}