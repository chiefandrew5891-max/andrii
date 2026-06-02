package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales

@Composable
fun AppearanceSettingsScreen() {
    val languages = AppSettings.languageCodes.keys.toList()
    val themeOptions = listOf(Locales.t("theme_light"), Locales.t("theme_dark"))
    val fontOptions = listOf(
        Locales.t("font_small"),
        Locales.t("font_medium"),
        Locales.t("font_large")
    )

    val fontScale = AppSettings.getFontScale()
    val onSurface = MaterialTheme.colors.onSurface
    val onBg = MaterialTheme.colors.onBackground

    var selectedLanguageDraft by remember { mutableStateOf(AppSettings.selectedLanguage) }
    var selectedThemeDraft by remember {
        mutableStateOf(
            if (AppSettings.isDarkMode) {
                Locales.t("theme_dark")
            } else {
                Locales.t("theme_light")
            }
        )
    }
    var selectedFontDraft by remember {
        mutableStateOf(
            when (AppSettings.fontSizeMode) {
                "Мелкий" -> Locales.t("font_small")
                "Крупный" -> Locales.t("font_large")
                else -> Locales.t("font_medium")
            }
        )
    }
    var userNameDraft by remember { mutableStateOf(AppSettings.ownerName) }

    val currentThemeValue =
        if (AppSettings.isDarkMode) Locales.t("theme_dark") else Locales.t("theme_light")

    val currentFontValue = when (AppSettings.fontSizeMode) {
        "Мелкий" -> Locales.t("font_small")
        "Крупный" -> Locales.t("font_large")
        else -> Locales.t("font_medium")
    }

    val hasChanges =
        selectedLanguageDraft != AppSettings.selectedLanguage ||
                selectedThemeDraft != currentThemeValue ||
                selectedFontDraft != currentFontValue ||
                userNameDraft.trim() != AppSettings.ownerName.trim()

    CenteredNarrowContentContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = Locales.t("appearance_settings"),
                fontSize = (22 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )

            Text(
                text = Locales.t("appearance_settings_hint"),
                fontSize = (14 * fontScale).sp,
                color = onBg.copy(alpha = 0.7f)
            )

            androidx.compose.material.Divider()

            SettingsDropdown(
                label = Locales.t("language_label"),
                selected = selectedLanguageDraft,
                items = languages,
                onSelect = { newValue ->
                    selectedLanguageDraft = newValue
                }
            )

            SettingsDropdown(
                label = Locales.t("theme_label"),
                selected = selectedThemeDraft,
                items = themeOptions,
                onSelect = { newValue ->
                    selectedThemeDraft = newValue
                }
            )

            SettingsDropdown(
                label = Locales.t("font_size_label"),
                selected = selectedFontDraft,
                items = fontOptions,
                onSelect = { newValue ->
                    selectedFontDraft = newValue
                }
            )

            androidx.compose.material.Divider()

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Locales.t("user_name_label"),
                    fontSize = (16 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface.copy(alpha = 0.85f)
                )

                OutlinedTextField(
                    value = userNameDraft,
                    onValueChange = { userNameDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(
                            Locales.t("user_name_hint"),
                            color = onSurface.copy(alpha = 0.65f)
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = (16 * fontScale).sp,
                        color = onSurface
                    )
                )
            }

            Spacer(Modifier.padding(top = 4.dp))

            PrimaryActionButton(
                text = Locales.t("save"),
                onClick = {
                    AppSettings.selectedLanguage = selectedLanguageDraft

                    val code = AppSettings.languageCodes[selectedLanguageDraft] ?: "en"
                    Locales.currentLanguage = code

                    AppSettings.isDarkMode = (selectedThemeDraft == Locales.t("theme_dark"))

                    AppSettings.fontSizeMode = when (selectedFontDraft) {
                        Locales.t("font_small") -> "Мелкий"
                        Locales.t("font_large") -> "Крупный"
                        else -> "Средний"
                    }

                    AppSettings.ownerName = userNameDraft.trim()
                    AppSettings.persist()
                },
                enabled = hasChanges
            )
        }
    }
}