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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.BackupFilePicker
import com.andrey.beautyplanner.DataManager
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.PhoneCaller
import com.andrey.beautyplanner.Screen
import com.andrey.beautyplanner.getCurrentTimeHm
import com.andrey.beautyplanner.notifications.Notifications
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // Drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var calendarViewDate by remember { mutableStateOf(LocalDate(today.year, today.month, 1)) }
    var selectedDate by remember { mutableStateOf(today) }

    var showBookingDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Appointment?>(null) }
    var selectedTimeSlot by remember { mutableStateOf("") }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var bookingReadOnly by remember { mutableStateOf(false) }

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

    // Backup flow
    var showExportNameDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("beautyplanner-backup") }

    var pendingImportText by remember { mutableStateOf<String?>(null) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showImportError by remember { mutableStateOf<String?>(null) }

    // Save error dialog
    var showSaveError by remember { mutableStateOf<String?>(null) }

    // NEW: auto-shift chain confirm (when overlap on save)
    data class ShiftItem(val apptId: String, val newStartMin: Int)
    var showAutoShiftConfirm by remember { mutableStateOf(false) }
    var pendingNewAppt by remember { mutableStateOf<Appointment?>(null) }
    var shiftChain by remember { mutableStateOf<List<ShiftItem>>(emptyList()) }
    var shiftBlockedApptId by remember { mutableStateOf<String?>(null) } // which client needs manual reschedule

    fun parseHmToMinutes(hm: String): Int? {
        val parts = hm.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23) return null
        if (m !in 0..59) return null
        return h * 60 + m
    }

    fun minutesToHm(mins: Int): String {
        val safe = mins.coerceIn(0, 24 * 60 - 1)
        val h = safe / 60
        val m = safe % 60
        return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
    }

    fun apptDurationMinutes(a: Appointment): Int =
        if (a.durationMinutes > 0) a.durationMinutes else a.durationHours.coerceAtLeast(1) * 60

    fun apptStartEndMinutes(a: Appointment): Pair<Int, Int>? {
        val start = parseHmToMinutes(a.time) ?: return null
        val end = start + apptDurationMinutes(a)
        return start to end
    }

    fun saveAll() {
        DataManager.saveToDatabase(appointments.toList())

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

    fun replaceById(updated: Appointment) {
        val idx = appointments.indexOfFirst { it.id == updated.id }
        if (idx >= 0) appointments[idx] = updated
        else {
            appointments.removeAll { it.id == updated.id }
            appointments.add(updated)
        }
    }

    // Find first overlapping appointment with [start,end) on same day, excluding ignoreId
    fun firstOverlapOnDay(day: String, start: Int, end: Int, ignoreId: String?): Appointment? {
        return appointments
            .asSequence()
            .filter { it.dateString == day }
            .filter { ignoreId == null || it.id != ignoreId }
            .mapNotNull { a ->
                val se = apptStartEndMinutes(a) ?: return@mapNotNull null
                Triple(a, se.first, se.second)
            }
            .firstOrNull { (_, s, e) -> start < e && s < end }
            ?.first
    }

    /**
     * Build chain of shifts if we place new/edited appointment at [newStartMin, newEndMin).
     * Strategy: shift the conflicting appointment to the end of the previous interval, keeping its duration.
     * Repeat until no conflicts or until we exceed dayEnd.
     */
    fun tryBuildShiftChain(
        day: String,
        baseIgnoreId: String?,
        newStartMin: Int,
        newEndMin: Int,
        dayEnd: Int = 21 * 60
    ): Pair<List<ShiftItem>, String?> {
        val chain = mutableListOf<ShiftItem>()

        // We'll simulate with a map of "virtual starts" for moved appts
        val movedStart = mutableMapOf<String, Int>()

        fun virtualStart(a: Appointment): Int {
            return movedStart[a.id] ?: (parseHmToMinutes(a.time) ?: 0)
        }

        fun virtualEnd(a: Appointment): Int {
            return virtualStart(a) + apptDurationMinutes(a)
        }

        var cursorStart = newStartMin
        var cursorEnd = newEndMin

        // protect from infinite loop
        repeat(50) {
            // find any overlap with current interval against appointments with virtual positions
            val conflict = appointments
                .asSequence()
                .filter { it.dateString == day }
                .filter { baseIgnoreId == null || it.id != baseIgnoreId }
                .filter { it.id != baseIgnoreId }
                .mapNotNull { a ->
                    val s = virtualStart(a)
                    val e = virtualEnd(a)
                    Triple(a, s, e)
                }
                .firstOrNull { (a, s, e) ->
                    // note: when editing, the original appt is ignored by baseIgnoreId
                    cursorStart < e && s < cursorEnd
                }
                ?.first

            if (conflict == null) return chain to null

            // shift conflict to cursorEnd
            val newS = cursorEnd
            val newE = newS + apptDurationMinutes(conflict)

            if (newE > dayEnd) {
                // blocked: need manual reschedule for this conflict appt
                return chain to conflict.id
            }

            movedStart[conflict.id] = newS
            chain.add(ShiftItem(conflict.id, newS))

            // now move cursor to represent this shifted appointment and continue chain
            cursorStart = newS
            cursorEnd = newE
        }

        // if we got here, consider blocked
        return chain to null
    }

    fun applyShiftChain(day: String, chain: List<ShiftItem>) {
        chain.forEach { item ->
            val a = appointments.firstOrNull { it.id == item.apptId && it.dateString == day } ?: return@forEach
            val updated = a.copy(time = minutesToHm(item.newStartMin))
            replaceById(updated)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val loaded = DataManager.loadFromDatabase()
            if (loaded.isNotEmpty()) {
                appointments.clear()
                appointments.addAll(loaded)
            }
        } catch (_: Exception) {
            // ignored
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

    @Composable
    fun DrawerItem(
        title: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        val bg = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(backgroundColor = bg)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }

    // --- Save error dialog ---
    if (showSaveError != null) {
        AlertDialog(
            onDismissRequest = { showSaveError = null },
            title = { Text(Locales.t("import_db")) },
            text = { Text(showSaveError ?: "") },
            confirmButton = {
                TextButton(onClick = { showSaveError = null }) { Text(Locales.t("close")) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Auto shift confirm dialog ---
    if (showAutoShiftConfirm && pendingNewAppt != null) {
        val day = pendingNewAppt!!.dateString
        val blockedId = shiftBlockedApptId
        val blockedAppt = blockedId?.let { id -> appointments.firstOrNull { it.id == id && it.dateString == day } }

        val chainText = buildString {
            append("Есть пересечение по времени.\n\n")
            append("Сдвинуть следующие записи автоматически?\n\n")
            if (shiftChain.isNotEmpty()) {
                shiftChain.forEachIndexed { idx, item ->
                    val a = appointments.firstOrNull { it.id == item.apptId && it.dateString == day }
                    val name = a?.clientName ?: "?"
                    append("${idx + 1}) $name → ${minutesToHm(item.newStartMin)}\n")
                }
            }
            if (blockedAppt != null) {
                append("\nНе хватает места в конце дня для: ${blockedAppt.clientName}")
            }
        }

        AlertDialog(
            onDismissRequest = {
                showAutoShiftConfirm = false
                pendingNewAppt = null
                shiftChain = emptyList()
                shiftBlockedApptId = null
            },
            title = { Text("Конфликт времени") },
            text = { Text(chainText) },
            confirmButton = {
                Button(onClick = {
                    // apply chain + save appointment
                    val newAppt = pendingNewAppt!!

                    applyShiftChain(day, shiftChain)
                    replaceById(newAppt)

                    saveAll()

                    showAutoShiftConfirm = false
                    pendingNewAppt = null

                    // if blocked => open manual reschedule for that client
                    if (blockedAppt != null) {
                        conflictB = blockedAppt
                        showRescheduleBDialog = true
                    }

                    shiftChain = emptyList()
                    shiftBlockedApptId = null

                    showBookingDialog = false
                    editingAppointment = null
                }) { Text("Сдвинуть") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAutoShiftConfirm = false
                    pendingNewAppt = null
                    shiftChain = emptyList()
                    shiftBlockedApptId = null
                }) { Text(Locales.t("cancel")) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Export name dialog ---
    if (showExportNameDialog) {
        AlertDialog(
            onDismissRequest = { showExportNameDialog = false },
            title = { Text(Locales.t("export_db")) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(Locales.t("backup_export_name_hint"), style = MaterialTheme.typography.body2)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(Locales.t("backup_file_name")) }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = Locales.t("backup_extension_note"),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showExportNameDialog = false
                    val json = DataManager.exportBackup(appointments)
                    val safeName = exportFileName.trim().ifBlank { "beautyplanner-backup" }
                    BackupFilePicker.exportJson(
                        suggestedFileName = safeName,
                        json = json
                    )
                }) { Text(Locales.t("save")) }
            },
            dismissButton = {
                TextButton(onClick = { showExportNameDialog = false }) {
                    Text(Locales.t("cancel"))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Import confirm dialog ---
    if (showImportConfirm && pendingImportText != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportText = null
            },
            title = { Text(Locales.t("import_db")) },
            text = { Text(Locales.t("backup_import_confirm_text")) },
            confirmButton = {
                Button(onClick = {
                    val text = pendingImportText.orEmpty()
                    val imported = DataManager.importBackup(text)
                    if (imported.isEmpty()) {
                        showImportError = Locales.t("import_invalid_json")
                    } else {
                        appointments.clear()
                        appointments.addAll(imported)
                        saveAll()
                        showImportError = null
                    }

                    showImportConfirm = false
                    pendingImportText = null
                }) { Text(Locales.t("import_btn")) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    pendingImportText = null
                }) { Text(Locales.t("cancel")) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Import error dialog ---
    if (showImportError != null) {
        AlertDialog(
            onDismissRequest = { showImportError = null },
            title = { Text(Locales.t("import_db")) },
            text = { Text(showImportError ?: "") },
            confirmButton = {
                TextButton(onClick = { showImportError = null }) { Text(Locales.t("close")) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    MaterialTheme(colors = colors, typography = customTypography) {
        ModalDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Locales.t("nav_menu"),
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    DrawerItem(
                        title = Locales.t("nav_main"),
                        selected = currentScreen == Screen.MONTH,
                        onClick = {
                            currentScreen = Screen.MONTH
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        title = Locales.t("nav_stats"),
                        selected = currentScreen == Screen.STATS,
                        onClick = {
                            currentScreen = Screen.STATS
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        title = Locales.t("nav_settings"),
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = {
                            currentScreen = Screen.SETTINGS
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerItem(
                        title = Locales.t("nav_feedback"),
                        selected = currentScreen == Screen.FEEDBACK,
                        onClick = {
                            currentScreen = Screen.FEEDBACK
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
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
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colors.primary)
                            }

                            val titleText = when (currentScreen) {
                                Screen.MONTH -> Locales.t("nav_main")
                                Screen.SETTINGS -> Locales.t("nav_settings")
                                Screen.DAY_DETAILS -> Locales.t("nav_day")
                                Screen.STATS -> Locales.t("nav_stats")
                                Screen.FEEDBACK -> Locales.t("nav_feedback")
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
                                exportFileName = "beautyplanner-backup"
                                showExportNameDialog = true
                            },
                            onImport = {
                                BackupFilePicker.importJson(
                                    onPicked = { jsonText ->
                                        pendingImportText = jsonText
                                        showImportConfirm = true
                                    },
                                    onError = { errorText ->
                                        showImportError = errorText
                                    }
                                )
                            }
                        )

                        Screen.STATS -> StatsPage(
                            appointments = appointments,
                            today = today
                        )

                        Screen.FEEDBACK -> FeedbackPage(
                            phone = AppSettings.servicePhone,
                            onCallClick = { phone ->
                                if (phone.isNotBlank()) PhoneCaller.call(phone)
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
                                                bookingReadOnly = true
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
                                bookingReadOnly = false
                                showBookingDialog = true
                            },
                            onEditClick = { appt ->
                                editingAppointment = appt
                                bookingReadOnly = false
                                showBookingDialog = true
                            },
                            onDeleteClick = { appt -> showDeleteConfirm = appt }
                        )
                    }

                    if (showBookingDialog) {
                        BookingDialog(
                            time = editingAppointment?.time ?: selectedTimeSlot,
                            initialData = editingAppointment ?: transferA,
                            readOnly = bookingReadOnly && editingAppointment != null,
                            onDismiss = {
                                showBookingDialog = false
                                editingAppointment = null
                                transferA = null
                                bookingReadOnly = false
                            },
                            onSave = { startTime, durationMinutes, name, phone, service, price ->
                                // твоя текущая логика сохранения (конфликт/автосдвиг или простая) должна быть здесь.
                                // Если ты уже вставил "авто-сдвиг цепочкой" — оставь её.
                                // Если нет — минимальный рабочий вариант:

                                editingAppointment?.let { appointments.remove(it) }
                                transferA?.let { appointments.remove(it); transferA = null }

                                val newAppt = Appointment(
                                    id = editingAppointment?.id ?: Clock.System.now().toEpochMilliseconds().toString(),
                                    dateString = selectedDate.toString(),
                                    time = startTime,
                                    clientName = name,
                                    phone = phone,
                                    serviceName = service,
                                    price = price,
                                    durationMinutes = durationMinutes,
                                    durationHours = ((durationMinutes + 59) / 60).coerceAtLeast(1)
                                )

                                appointments.add(newAppt)
                                saveAll()

                                showBookingDialog = false
                                editingAppointment = null
                                bookingReadOnly = false
                            },
                            onTransferRequest = { appt ->
                                transferA = appt
                                showBookingDialog = false
                                showTransferPickDialog = true
                                bookingReadOnly = false
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
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (showRescheduleBDialog && conflictB != null) {
                        RescheduleClientBDialog(
                            clientName = conflictB!!.clientName,
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
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
    }
}