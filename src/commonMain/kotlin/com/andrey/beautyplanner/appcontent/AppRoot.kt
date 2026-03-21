package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.DataManager
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.Screen
import com.andrey.beautyplanner.getCurrentTimeHm
import com.andrey.beautyplanner.notifications.Notifications
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun AppRoot() {
    val appointments = remember { mutableStateListOf<Appointment>() }
    var currentScreen by remember { mutableStateOf(Screen.MONTH) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    var calendarViewDate by remember { mutableStateOf(LocalDate(today.year, today.month, 1)) }
    var selectedDate by remember { mutableStateOf(today) }

    var showBookingDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Appointment?>(null) }
    var selectedTimeSlot by remember { mutableStateOf("") }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }

    // перенос: инициатор (A)
    var transferA by remember { mutableStateOf<Appointment?>(null) }
    var showTransferPickDialog by remember { mutableStateOf(false) }

    // подтверждение "занято" (A на слот B)
    var showTransferConflictConfirm by remember { mutableStateOf(false) }
    var conflictB by remember { mutableStateOf<Appointment?>(null) }
    var pendingTargetDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingTargetTime by remember { mutableStateOf("") }

    // перенос: модалка для переназначения клиента B
    var showRescheduleBDialog by remember { mutableStateOf(false) }

    // Backup dialogs (Export/Import)
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val loaded = DataManager.loadFromDatabase()
            if (loaded.isNotEmpty()) {
                appointments.clear()
                appointments.addAll(loaded)
            }
        } catch (_: Exception) {
            // intentionally ignored
        }
    }

    val colors = if (AppSettings.isDarkMode) {
        darkColors(
            primary = Color(0xFF8AB4F8),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSurface = Color.White
        )
    } else {
        lightColors(
            primary = Color(0xFF4285F4),
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = Color.Black
        )
    }

    val fontScale = AppSettings.getFontScale()
    val customTypography = Typography(
        body1 = TextStyle(fontSize = (16 * fontScale).sp),
        h6 = TextStyle(fontSize = (20 * fontScale).sp, fontWeight = FontWeight.Bold),
        subtitle1 = TextStyle(fontSize = (14 * fontScale).sp)
    )

    fun saveAll() {
        DataManager.saveToDatabase(appointments.toList())

        // Перепланируем уведомления после любого изменения записей
        val mins = AppSettings.reminderMinutesComputed()
        if (AppSettings.notificationsEnabled && mins.isNotEmpty()) {
            Notifications.rescheduleAll(
                appointments = appointments.toList(),
                reminderMinutes = mins,
                sound = AppSettings.notificationSound,
                nowEpochMillis = Clock.System.now().toEpochMilliseconds()
            )
        } else {
            Notifications.cancelAll()
        }
    }

    fun findAppointment(date: LocalDate, time: String): Appointment? =
        appointments.find { it.dateString == date.toString() && it.time == time }

    fun moveAppointment(appt: Appointment, toDate: LocalDate, toTime: String) {
        val idx = appointments.indexOfFirst { it.id == appt.id }
        if (idx >= 0) {
            appointments[idx] = appt.copy(dateString = toDate.toString(), time = toTime)
        } else {
            appointments.remove(appt)
            appointments.add(appt.copy(dateString = toDate.toString(), time = toTime))
        }
    }

    MaterialTheme(colors = colors, typography = customTypography) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 2.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = { /* Drawer */ },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colors.primary)
                        }

                        val titleText = when (currentScreen) {
                            Screen.MONTH -> Locales.t("nav_main")
                            Screen.SETTINGS -> Locales.t("nav_settings")
                            Screen.DAY_DETAILS -> Locales.t("nav_day")
                        }

                        Text(
                            text = titleText,
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontSize = (18 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentScreen != Screen.MONTH) {
                                IconButton(onClick = { currentScreen = Screen.MONTH }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Reply,
                                        "Back",
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = {
                                currentScreen =
                                    if (currentScreen == Screen.SETTINGS) Screen.MONTH else Screen.SETTINGS
                            }) {
                                Icon(
                                    Icons.Default.Settings,
                                    "Settings",
                                    tint = if (currentScreen == Screen.SETTINGS)
                                        MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentScreen) {
                    Screen.SETTINGS -> SettingsPage(
                        onExport = {
                            exportJson = DataManager.exportBackup(appointments)
                            showExportDialog = true
                        },
                        onImport = {
                            importJson = ""
                            importError = null
                            showImportDialog = true
                        }
                    )

                    Screen.MONTH -> {
                        var nowTimeHm by remember { mutableStateOf(getCurrentTimeHm()) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                nowTimeHm = getCurrentTimeHm()
                                delay(60_000)
                            }
                        }

                        val listState = rememberLazyListState()

                        val upcoming by remember(nowTimeHm, today) {
                            derivedStateOf {
                                getUpcomingAppointments(
                                    appointments = appointments,
                                    today = today,
                                    nowTime = nowTimeHm
                                )
                            }
                        }

                        val isCollapsed by remember {
                            derivedStateOf {
                                listState.firstVisibleItemIndex > 0 ||
                                        listState.firstVisibleItemScrollOffset > 40
                            }
                        }

                        val headerText by remember(isCollapsed, calendarViewDate, today) {
                            derivedStateOf {
                                if (!isCollapsed) {
                                    val monthKey = when (calendarViewDate.monthNumber) {
                                        1 -> "month_jan"
                                        2 -> "month_feb"
                                        3 -> "month_mar"
                                        4 -> "month_apr"
                                        5 -> "month_may"
                                        6 -> "month_jun"
                                        7 -> "month_jul"
                                        8 -> "month_aug"
                                        9 -> "month_sep"
                                        10 -> "month_oct"
                                        11 -> "month_nov"
                                        12 -> "month_dec"
                                        else -> ""
                                    }
                                    "${Locales.t(monthKey)} ${calendarViewDate.year}"
                                } else {
                                    val monthKeyGen = when (today.monthNumber) {
                                        1 -> "month_jan_gen"
                                        2 -> "month_feb_gen"
                                        3 -> "month_mar_gen"
                                        4 -> "month_apr_gen"
                                        5 -> "month_may_gen"
                                        6 -> "month_jun_gen"
                                        7 -> "month_jul_gen"
                                        8 -> "month_aug_gen"
                                        9 -> "month_sep_gen"
                                        10 -> "month_oct_gen"
                                        11 -> "month_nov_gen"
                                        12 -> "month_dec_gen"
                                        else -> ""
                                    }
                                    "${today.dayOfMonth} ${Locales.t(monthKeyGen)} ${calendarViewDate.year}"
                                }
                            }
                        }

                        Column(Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = headerText,
                                    fontSize = (24 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onBackground
                                )

                                Row {
                                    val arrowsEnabled = !isCollapsed
                                    val arrowTint = if (arrowsEnabled) {
                                        MaterialTheme.colors.primary
                                    } else {
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.35f)
                                    }

                                    IconButton(
                                        enabled = arrowsEnabled,
                                        onClick = { calendarViewDate = calendarViewDate.minus(1, DateTimeUnit.MONTH) }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            null,
                                            tint = arrowTint
                                        )
                                    }

                                    IconButton(
                                        enabled = arrowsEnabled,
                                        onClick = { calendarViewDate = calendarViewDate.plus(1, DateTimeUnit.MONTH) }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            null,
                                            tint = arrowTint
                                        )
                                    }
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                item {
                                    MonthCalendarGrid(
                                        monthDate = calendarViewDate,
                                        today = today,
                                        selectedDate = selectedDate
                                    ) { date ->
                                        selectedDate = date
                                        currentScreen = Screen.DAY_DETAILS
                                    }
                                }

                                item {
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp),
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )

                                    Text(
                                        text = Locales.t("upcoming_appointments_list"),
                                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                                        fontSize = (16 * fontScale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                }

                                if (upcoming.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = Locales.t("no_upcoming_appointments"),
                                                color = Color.Gray,
                                                fontSize = (14 * fontScale).sp
                                            )
                                        }
                                    }
                                } else {
                                    items(upcoming.size) { idx ->
                                        val appt = upcoming[idx]
                                        UpcomingAppointmentCard(appt = appt) {
                                            editingAppointment = appt
                                            showBookingDialog = true
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Screen.DAY_DETAILS -> DayDetailsView(
                        date = selectedDate,
                        appointments = appointments,
                        onDateChange = { selectedDate = it },
                        onTimeClick = { time ->
                            selectedTimeSlot = time
                            editingAppointment = null
                            showBookingDialog = true
                        },
                        onEditClick = { appt ->
                            editingAppointment = appt
                            showBookingDialog = true
                        },
                        onDeleteClick = { appt -> showDeleteConfirm = appt }
                    )
                }

                if (showBookingDialog) {
                    BookingDialog(
                        time = editingAppointment?.time ?: selectedTimeSlot,
                        initialData = editingAppointment ?: transferA,
                        onDismiss = {
                            showBookingDialog = false
                            editingAppointment = null
                            transferA = null
                        },
                        onSave = { name, phone, service, price, hours ->
                            editingAppointment?.let { appointments.remove(it) }
                            transferA?.let { appointments.remove(it); transferA = null }

                            val newAppt = Appointment(
                                id = editingAppointment?.id
                                    ?: Clock.System.now().toEpochMilliseconds().toString(),
                                dateString = selectedDate.toString(),
                                time = editingAppointment?.time ?: selectedTimeSlot,
                                clientName = name,
                                phone = phone,
                                serviceName = service,
                                price = price,
                                durationHours = hours
                            )
                            appointments.add(newAppt)
                            saveAll()

                            showBookingDialog = false
                            editingAppointment = null
                        },
                        onTransferRequest = { appt ->
                            transferA = appt
                            showBookingDialog = false
                            showTransferPickDialog = true
                        }
                    )
                }

                if (showTransferPickDialog && transferA != null) {
                    TransferPickDialog(
                        initialSelectedDate = LocalDate.parse(transferA!!.dateString),
                        initialMonthDate = LocalDate.parse(transferA!!.dateString),
                        onDismiss = {
                            showTransferPickDialog = false
                            transferA = null
                        },
                        onConfirm = { newDate, newTime ->
                            pendingTargetDate = newDate
                            pendingTargetTime = newTime

                            val b = findAppointment(newDate, newTime)
                            if (b != null && b.id != transferA!!.id) {
                                conflictB = b
                                showTransferConflictConfirm = true
                            } else {
                                moveAppointment(transferA!!, newDate, newTime)
                                saveAll()
                                showTransferPickDialog = false
                                transferA = null
                            }
                        }
                    )
                }

                if (showTransferConflictConfirm && transferA != null && conflictB != null && pendingTargetDate != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showTransferConflictConfirm = false
                            conflictB = null
                        },
                        title = { Text(Locales.t("transfer_conflict_title")) },
                        text = {
                            Text(
                                "${Locales.t("transfer_conflict_text")}\n\n" +
                                        "${Locales.t("transfer_conflict_a")}: ${transferA!!.clientName}\n" +
                                        "${Locales.t("transfer_conflict_b")}: ${conflictB!!.clientName}"
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                moveAppointment(transferA!!, pendingTargetDate!!, pendingTargetTime)
                                showTransferConflictConfirm = false
                                showTransferPickDialog = false
                                showRescheduleBDialog = true
                            }) { Text(Locales.t("transfer_agree")) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showTransferConflictConfirm = false
                                conflictB = null
                            }) { Text(Locales.t("cancel")) }
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }

                if (showRescheduleBDialog && conflictB != null) {
                    RescheduleClientBDialog(
                        clientName = conflictB!!.clientName, // <-- вот это важно
                        initialSelectedDate = LocalDate.parse(conflictB!!.dateString),
                        initialMonthDate = LocalDate.parse(conflictB!!.dateString),
                        onDismiss = {
                            showRescheduleBDialog = false
                            saveAll()
                            transferA = null
                            conflictB = null
                            pendingTargetDate = null
                            pendingTargetTime = ""
                        },
                        onConfirm = { newDate, newTime ->
                            moveAppointment(conflictB!!, newDate, newTime)
                            saveAll()

                            showRescheduleBDialog = false
                            transferA = null
                            conflictB = null
                            pendingTargetDate = null
                            pendingTargetTime = ""
                        }
                    )
                }

                if (showDeleteConfirm != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = null },
                        title = { Text(Locales.t("delete_title")) },
                        text = {
                            val client = showDeleteConfirm?.clientName ?: ""
                            val time = showDeleteConfirm?.time ?: ""
                            Text(
                                "${Locales.t("delete_confirm_prefix")} $client " +
                                        "${Locales.t("delete_confirm_at")} $time. " +
                                        "${Locales.t("continue_question")}"
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                appointments.remove(showDeleteConfirm)
                                saveAll()
                                showDeleteConfirm = null
                            }) {
                                Text(
                                    Locales.t("delete_btn"),
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = null }) {
                                Text(Locales.t("cancel").uppercase())
                            }
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }

                if (showExportDialog) {
                    ExportBackupDialog(
                        json = exportJson,
                        onDismiss = { showExportDialog = false }
                    )
                }

                if (showImportDialog) {
                    ImportBackupDialog(
                        json = importJson,
                        errorText = importError,
                        onJsonChange = { importJson = it },
                        onDismiss = { showImportDialog = false },
                        onImport = {
                            val imported = DataManager.importBackup(importJson)
                            if (imported.isEmpty()) {
                                importError = Locales.t("import_invalid_json")
                                return@ImportBackupDialog
                            }

                            appointments.clear()
                            appointments.addAll(imported)
                            saveAll()

                            showImportDialog = false
                            importError = null
                        }
                    )
                }
            }
        }
    }
}