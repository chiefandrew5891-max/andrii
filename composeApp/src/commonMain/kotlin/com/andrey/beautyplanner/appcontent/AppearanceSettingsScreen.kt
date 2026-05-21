package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    var nameEditMode by remember { mutableStateOf(false) }
    var userNameDraft by remember { mutableStateOf(AppSettings.ownerName) }

    val fieldColors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = onSurface,
        cursorColor = MaterialTheme.colors.primary,
        focusedBorderColor = MaterialTheme.colors.primary,
        unfocusedBorderColor = onSurface.copy(alpha = 0.25f),
        focusedLabelColor = MaterialTheme.colors.primary,
        unfocusedLabelColor = onSurface.copy(alpha = 0.65f),
        trailingIconColor = onSurface.copy(alpha = 0.75f)
    )

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

        Divider()

        SettingsDropdown(
            label = Locales.t("language_label"),
            selected = AppSettings.selectedLanguage,
            items = languages,
            onSelect = { newValue ->
                if (AppSettings.selectedLanguage != newValue) {
                    AppSettings.selectedLanguage = newValue
                    val code = AppSettings.languageCodes[newValue] ?: "en"
                    Locales.currentLanguage = code
                    AppSettings.persist()
                }
            }
        )

        SettingsDropdown(
            label = Locales.t("theme_label"),
            selected = if (AppSettings.isDarkMode) {
                Locales.t("theme_dark")
            } else {
                Locales.t("theme_light")
            },
            items = themeOptions,
            onSelect = { newValue ->
                AppSettings.isDarkMode = (newValue == Locales.t("theme_dark"))
                AppSettings.persist()
            }
        )

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
                AppSettings.persist()
            }
        )

        Divider()

        Column {
            Text(
                text = Locales.t("user_name_label"),
                fontSize = (16 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurface.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = if (nameEditMode) userNameDraft else AppSettings.ownerName,
                onValueChange = { if (nameEditMode) userNameDraft = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = nameEditMode,
                label = {
                    Text(
                        Locales.t("user_name_hint"),
                        color = onSurface.copy(alpha = 0.65f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = (16 * fontScale).sp,
                    color = onSurface
                ),
                colors = fieldColors
            )

            Spacer(Modifier.height(10.dp))

            if (!nameEditMode) {
                OutlinedButton(
                    onClick = {
                        nameEditMode = true
                        userNameDraft = AppSettings.ownerName
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Locales.t("support_phone_edit"), color = onSurface)
                }
            } else {
                Button(
                    onClick = {
                        AppSettings.ownerName = userNameDraft.trim()
                        AppSettings.persist()
                        nameEditMode = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Locales.t("support_phone_save"))
                }
            }
        }
    }
}