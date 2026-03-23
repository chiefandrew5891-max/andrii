package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.DataManager
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.notifications.NotificationSound
import com.andrey.beautyplanner.notifications.Notifications
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPage(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onSetOrChangePin: () -> Unit,
    onRemovePin: () -> Unit,
    onClearDatabase: () -> Unit
) {
    val languages = AppSettings.languageCodes.keys.toList()
    val themeOptions = listOf(Locales.t("theme_light"), Locales.t("theme_dark"))
    val fontOptions = listOf(Locales.t("font_small"), Locales.t("font_medium"), Locales.t("font_large"))

    val fontScale = AppSettings.getFontScale()

    var daysSlider by remember { mutableStateOf(AppSettings.reminderDaysBefore.toFloat()) }
    var hoursSlider by remember { mutableStateOf(AppSettings.reminderHoursBefore.toFloat()) }

    val notificationsEnabled = AppSettings.notificationsEnabled
    val notificationSound = AppSettings.notificationSound
    val reminderDays = AppSettings.reminderDaysBefore
    val reminderHours = AppSettings.reminderHoursBefore

    // Support phone edit state
    var supportEditMode by remember { mutableStateOf(false) }
    var supportPhoneDraft by remember { mutableStateOf(AppSettings.servicePhone) }
    var showSupportEditConfirm by remember { mutableStateOf(false) }

    // ✅ Confirm disable PIN dialog
    var showDisablePinConfirm by remember { mutableStateOf(false) }
    var pendingPinEnabledValue by remember { mutableStateOf(AppSettings.pinEnabled) }

    val dbOpsAllowed = AppSettings.pinEnabled && AppSettings.isPinSet()

    LaunchedEffect(notificationsEnabled, notificationSound, reminderDays, reminderHours) {
        delay(600)

        val all = runCatching { DataManager.loadFromDatabase() }.getOrNull().orEmpty()
        val mins = AppSettings.reminderMinutesComputed()

        runCatching {
            if (AppSettings.notificationsEnabled && mins.isNotEmpty()) {
                Notifications.rescheduleAll(
                    appointments = all,
                    reminderMinutes = mins,
                    sound = AppSettings.notificationSound,
                    nowEpochMillis = Clock.System.now().toEpochMilliseconds()
                )
            } else {
                Notifications.cancelAll()
            }
        }
    }

    // --- Support phone edit confirm dialog (styled) ---
    if (showSupportEditConfirm) {
        AlertDialog(
            onDismissRequest = { showSupportEditConfirm = false },

            // ✅ Заголовок меньше, чем основной текст (второстепенный)
            title = {
                Text(
                    text = Locales.t("support_phone_edit_confirm_title"),
                    style = MaterialTheme.typography.subtitle2.copy(
                        fontSize = (14 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
                )
            },

            // основной текст оставляем как есть (можно не трогать вообще)
            text = { Text(Locales.t("support_phone_edit_confirm_text")) },

            // ✅ Кнопки оставляем теми же (Button + TextButton), меняем только отступы как в PinDialog
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showSupportEditConfirm = false }) {
                        Text(Locales.t("cancel"))
                    }

                    Spacer(Modifier.width(15.dp))

                    Button(onClick = {
                        showSupportEditConfirm = false
                        supportEditMode = true
                        supportPhoneDraft = AppSettings.servicePhone
                    }) {
                        Text(Locales.t("support_phone_edit_confirm_yes"))
                    }
                }
            },

            shape = RoundedCornerShape(16.dp)
        )
    }

    // ✅ Disable PIN confirmation dialog
    if (showDisablePinConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDisablePinConfirm = false
                pendingPinEnabledValue = AppSettings.pinEnabled
            },
            title = { Text(Locales.t("security_section")) },
            text = {
                Text(
                    "При отключении PIN-кода будут ограничены операции с базой данных:\n" +
                            "• импорт\n" +
                            "• экспорт\n" +
                            "• очистка базы\n\n" +
                            "Продолжить?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    AppSettings.pinEnabled = false
                    AppSettings.persist()
                    pendingPinEnabledValue = false
                    showDisablePinConfirm = false
                }) { Text(Locales.t("confirm")) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDisablePinConfirm = false
                    pendingPinEnabledValue = true
                }) { Text(Locales.t("cancel")) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = Locales.t("nav_settings"),
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold
        )

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
            selected = if (AppSettings.isDarkMode) Locales.t("theme_dark") else Locales.t("theme_light"),
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

        Spacer(modifier = Modifier.height(10.dp))
        Divider()

        // -------------------- Notifications --------------------
        Column {
            Text(
                text = Locales.t("notifications_section"),
                fontSize = (14 * fontScale).sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Locales.t("notifications_enabled"), fontSize = (16 * fontScale).sp)
                Switch(
                    checked = AppSettings.notificationsEnabled,
                    onCheckedChange = {
                        AppSettings.notificationsEnabled = it
                        AppSettings.persist()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.35f),
                        uncheckedThumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.45f),
                        uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val soundItems = listOf(
                Locales.t("notif_sound_default") to NotificationSound.DEFAULT,
                Locales.t("notif_sound_silent") to NotificationSound.SILENT
            )

            SettingsDropdown(
                label = Locales.t("notif_sound_label"),
                selected = soundItems.firstOrNull { it.second == AppSettings.notificationSound }?.first
                    ?: Locales.t("notif_sound_default"),
                items = soundItems.map { it.first },
                onSelect = { selected ->
                    val s = soundItems.firstOrNull { it.first == selected }?.second ?: NotificationSound.DEFAULT
                    AppSettings.notificationSound = s
                    AppSettings.persist()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = Locales.t("reminders_when"), fontSize = (14 * fontScale).sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${Locales.t("remind_days")}: ${Locales.daysCount(AppSettings.reminderDaysBefore)}",
                fontSize = (15 * fontScale).sp,
                fontWeight = FontWeight.SemiBold
            )

            Slider(
                value = daysSlider,
                onValueChange = { daysSlider = it },
                onValueChangeFinished = {
                    AppSettings.reminderDaysBefore = daysSlider.roundToInt().coerceIn(0, 3)
                    AppSettings.persist()
                },
                valueRange = 0f..3f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.85f),
                    inactiveTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${Locales.t("remind_hours")}: ${Locales.hoursCount(AppSettings.reminderHoursBefore)}",
                fontSize = (15 * fontScale).sp,
                fontWeight = FontWeight.SemiBold
            )

            Slider(
                value = hoursSlider,
                onValueChange = { hoursSlider = it },
                onValueChangeFinished = {
                    AppSettings.reminderHoursBefore = hoursSlider.roundToInt().coerceIn(0, 12)
                    AppSettings.persist()
                },
                valueRange = 0f..12f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.85f),
                    inactiveTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
                )
            )

            val totalMinutes = AppSettings.reminderDaysBefore * 24 * 60 + AppSettings.reminderHoursBefore * 60
            val summary = if (totalMinutes <= 0) Locales.t("remind_off")
            else "${Locales.daysCount(AppSettings.reminderDaysBefore)} • ${Locales.hoursCount(AppSettings.reminderHoursBefore)}"

            Text(
                text = "${Locales.t("remind_summary")}: $summary",
                fontSize = (13 * fontScale).sp,
                color = Color.Gray
            )
        }

        Divider()

        // -------------------- Support phone --------------------
        Column {
            Text(
                text = Locales.t("support_section"),
                fontSize = (14 * fontScale).sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = if (supportEditMode) supportPhoneDraft else AppSettings.servicePhone,
                onValueChange = { if (supportEditMode) supportPhoneDraft = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = supportEditMode,
                label = { Text(Locales.t("support_phone_label")) }
            )

            Text(
                text = Locales.t("support_phone_hint"),
                fontSize = (12 * fontScale).sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            if (!supportEditMode) {
                OutlinedButton(
                    onClick = { showSupportEditConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Locales.t("support_phone_edit"))
                }
            } else {
                Button(
                    onClick = {
                        AppSettings.servicePhone = supportPhoneDraft.trim()
                        AppSettings.persist()
                        supportEditMode = false
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Locales.t("support_phone_save"))
                }
            }
        }

        Divider()

        // -------------------- Backup --------------------
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
                    enabled = dbOpsAllowed,
                    modifier = Modifier.weight(0.45f).height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Text(text = Locales.t("export_db"), fontSize = (14 * fontScale).sp)
                }

                Spacer(modifier = Modifier.weight(0.1f))

                OutlinedButton(
                    onClick = onImport,
                    enabled = dbOpsAllowed,
                    modifier = Modifier.weight(0.45f).height(38.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Locales.t("import_db"), fontSize = (14 * fontScale).sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClearDatabase,
                enabled = dbOpsAllowed,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text(Locales.t("clear_db"), color = Color.Red, fontWeight = FontWeight.SemiBold)
            }

            if (!dbOpsAllowed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Чтобы использовать импорт/экспорт/очистку базы, включите PIN и установите его.",
                    color = Color.Gray,
                    fontSize = (12 * fontScale).sp
                )
            }
        }

        Divider()

        // -------------------- Security --------------------
        Column {
            Text(
                text = Locales.t("security_section"),
                fontSize = (14 * fontScale).sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Locales.t("pin_enabled"), fontSize = (16 * fontScale).sp)

                Switch(
                    checked = pendingPinEnabledValue,
                    onCheckedChange = { newValue ->
                        // Включение: просто включаем
                        if (newValue) {
                            AppSettings.pinEnabled = true
                            AppSettings.persist()
                            pendingPinEnabledValue = true
                        } else {
                            // Выключение: только через confirm
                            pendingPinEnabledValue = false
                            showDisablePinConfirm = true
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.primary,
                        checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.35f),
                        uncheckedThumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.45f),
                        uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            val pinBtnText = if (AppSettings.isPinSet()) Locales.t("pin_change") else Locales.t("pin_set")
            Button(
                onClick = onSetOrChangePin,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = true
            ) {
                Text(pinBtnText)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRemovePin,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = AppSettings.isPinSet()
            ) {
                Text(Locales.t("pin_remove"))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = Locales.t("privacy_policy"), fontSize = (16 * fontScale).sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
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