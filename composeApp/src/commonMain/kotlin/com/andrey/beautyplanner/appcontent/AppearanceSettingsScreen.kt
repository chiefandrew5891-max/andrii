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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.appcontent.approot.AppRootState
import com.andrey.beautyplanner.appcontent.appFontFamily

@Composable
fun AppearanceSettingsScreen(state: AppRootState) {
    val languages = AppSettings.languageCodes.keys.toList()

    val themeItems = listOf(
        "light" to Locales.t("theme_light"),
        "dark" to Locales.t("theme_dark")
    )

    val fontItems = listOf(
        "small" to Locales.t("font_small"),
        "medium" to Locales.t("font_medium"),
        "large" to Locales.t("font_large")
    )

    val currencyItems = listOf("EUR (€)", "USD ($)", "RUB (₽)", "UAH (₴)")

    val fontScale = state.fontScale
    val onSurface = MaterialTheme.colors.onSurface
    val onBg = MaterialTheme.colors.onBackground

    var selectedLanguageDraft by remember { mutableStateOf(AppSettings.selectedLanguage) }
    var selectedThemeDraftKey by remember {
        mutableStateOf(if (AppSettings.isDarkMode) "dark" else "light")
    }
    var selectedFontDraftKey by remember {
        mutableStateOf(
            when (AppSettings.fontSizeMode) {
                "small" -> "small"
                "large" -> "large"
                else -> "medium"
            }
        )
    }
    var selectedCurrencyDraft by remember {
        mutableStateOf(
            when (AppSettings.selectedCurrency) {
                "USD" -> "USD ($)"
                "RUB" -> "RUB (₽)"
                "UAH" -> "UAH (₴)"
                else -> "EUR (€)"
            }
        )
    }
    var userNameDraft by remember { mutableStateOf(AppSettings.ownerName) }
    var useShortTextCurrencyDraft by remember { mutableStateOf(AppSettings.useShortTextCurrency) }

    LaunchedEffect(selectedFontDraftKey) {
        val previewScale = when (selectedFontDraftKey) {
            "small" -> 0.80f
            "large" -> 1.22f
            else -> 1.10f
        }
        state.fontScale = previewScale
        AppSettings.previewFontScaleOverride = previewScale
    }

    DisposableEffect(Unit) {
        onDispose {
            AppSettings.previewFontScaleOverride = null
            state.resetLivePreviews()
        }
    }

    val currentThemeKey = if (AppSettings.isDarkMode) "dark" else "light"
    val currentFontKey = when (AppSettings.fontSizeMode) {
        "small" -> "small"
        "large" -> "large"
        else -> "medium"
    }

    val currentCurrencyValue = when (AppSettings.selectedCurrency) {
        "USD" -> "USD ($)"
        "RUB" -> "RUB (₽)"
        "UAH" -> "UAH (₴)"
        else -> "EUR (€)"
    }

    val selectedThemeLabel = themeItems.first { it.first == selectedThemeDraftKey }.second
    val selectedFontLabel = fontItems.first { it.first == selectedFontDraftKey }.second

    val hasChanges =
        selectedLanguageDraft != AppSettings.selectedLanguage ||
                selectedThemeDraftKey != currentThemeKey ||
                selectedFontDraftKey != currentFontKey ||
                selectedCurrencyDraft != currentCurrencyValue ||
                useShortTextCurrencyDraft != AppSettings.useShortTextCurrency ||
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
                selected = selectedThemeLabel,
                items = themeItems.map { it.second },
                onSelect = { newValue ->
                    val selectedKey = themeItems.firstOrNull { it.second == newValue }?.first ?: "light"
                    selectedThemeDraftKey = selectedKey
                    state.currentLiveDarkMode = (selectedKey == "dark")
                }
            )

            SettingsDropdown(
                label = Locales.t("font_size_label"),
                selected = selectedFontLabel,
                items = fontItems.map { it.second },
                onSelect = { newValue ->
                    val selectedKey = fontItems.firstOrNull { it.second == newValue }?.first ?: "medium"
                    selectedFontDraftKey = selectedKey

                    val previewScale = when (selectedKey) {
                        "small" -> 0.80f
                        "large" -> 1.22f
                        else -> 1.10f
                    }
                    state.fontScale = previewScale
                    AppSettings.previewFontScaleOverride = previewScale
                }
            )

            SettingsDropdown(
                label = Locales.t("currency_label"),
                selected = selectedCurrencyDraft,
                items = currencyItems,
                onSelect = { newValue ->
                    selectedCurrencyDraft = newValue
                }
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = Locales.t("currency_text_format_label"),
                    fontSize = (16 * fontScale).sp,
                    color = onSurface
                )

                androidx.compose.material.Switch(
                    checked = useShortTextCurrencyDraft,
                    onCheckedChange = { useShortTextCurrencyDraft = it },
                    colors = androidx.compose.material.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                    )
                )
            }

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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = Locales.t("user_name_placeholder"),
                            color = onSurface.copy(alpha = 0.50f)
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = appFontFamily(),
                        fontSize = (16 * fontScale).sp,
                        color = onSurface
                    ),
                    colors = androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors(
                        textColor = onSurface,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = onSurface.copy(alpha = 0.28f),
                        focusedLabelColor = MaterialTheme.colors.primary,
                        unfocusedLabelColor = onSurface.copy(alpha = 0.68f),
                        cursorColor = MaterialTheme.colors.primary,
                        backgroundColor = MaterialTheme.colors.surface,
                        placeholderColor = onSurface.copy(alpha = 0.50f)
                    )
                )
            }

            Spacer(Modifier.padding(top = 4.dp))

            PrimaryActionButton(
                text = Locales.t("save"),
                onClick = {
                    val targetCurrencyCode = when (selectedCurrencyDraft) {
                        "USD ($)" -> "USD"
                        "RUB (₽)" -> "RUB"
                        "UAH (₴)" -> "UAH"
                        else -> "EUR"
                    }

                    AppSettings.isDarkMode = (selectedThemeDraftKey == "dark")
                    AppSettings.fontSizeMode = selectedFontDraftKey
                    AppSettings.ownerName = userNameDraft.trim()

                    AppSettings.saveCurrencySynchronously(
                        targetCurrencyCode,
                        useShortTextCurrencyDraft
                    )

                    AppSettings.selectedLanguage = selectedLanguageDraft
                    val code = AppSettings.languageCodes[selectedLanguageDraft] ?: "en"
                    Locales.currentLanguage = code

                    AppSettings.previewFontScaleOverride = null
                    AppSettings.persist()
                },
                enabled = hasChanges
            )
        }
    }
}