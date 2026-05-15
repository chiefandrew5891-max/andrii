package com.andrey.beautyplanner.appcontent.approot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.*
import com.andrey.beautyplanner.appcontent.*
import com.andrey.beautyplanner.utils.LiveStatusKey
import com.andrey.beautyplanner.utils.getLiveStatus
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private enum class ApptAction { EDIT, TRANSFER, DELETE }

@Composable
fun AppRootContent(
    state: AppRootState,
    padding: PaddingValues
) {
    var showSplash by remember { mutableStateOf(true) }
    var pendingPinAfterSplash by remember { mutableStateOf(false) }
    val ownerName = remember { AppSettings.ownerName ?: "" }

    if (showSplash) {
        AnimatedSplashScreen(
            ownerName = if (ownerName.isBlank()) "Evgi" else ownerName,
            onAnimationFinished = {
                showSplash = false
                if (state.mustCreatePin || (state.locked && !state.mustCreatePin)) {
                    pendingPinAfterSplash = true
                }
            }
        )
        return
    }

    if (pendingPinAfterSplash) {
        LaunchedEffect(Unit) { pendingPinAfterSplash = false }
    }

    // ===== Unified dropdown + confirmations (shared for Upcoming + DayDetails) =====
    var menuAppt by remember { mutableStateOf<Appointment?>(null) }
    var menuStatus by remember { mutableStateOf<LiveStatusKey?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    var pendingAction by remember { mutableStateOf<ApptAction?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    fun openApptMenu(appt: Appointment, status: LiveStatusKey) {
        menuAppt = appt
        menuStatus = status
        showMenu = true
    }

    fun requestAction(action: ApptAction) {
        pendingAction = action
        showMenu = false
        showConfirm = true
    }

    fun applyAction(appt: Appointment, action: ApptAction) {
        when (action) {
            ApptAction.EDIT -> {
                state.editingAppointment = appt
                state.bookingReadOnly = false
                state.showBookingDialog = true
            }
            ApptAction.TRANSFER -> {
                state.transferA = appt
                state.showTransferPickDialog = true
                state.bookingReadOnly = false
            }
            ApptAction.DELETE -> {
                state.showDeleteConfirm = appt
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        when (state.currentScreen) {
            Screen.SETTINGS -> SettingsPage(
                onExport = {
                    state.runProtected(
                        title = Locales.t("pin_required"),
                        text = Locales.t("export_requires_pin"),
                        confirmText = Locales.t("confirm")
                    ) {
                        state.exportFileName = "beautyplanner-backup"
                        state.showExportNameDialog = true
                    }
                },
                onImport = {
                    state.runProtected(
                        title = Locales.t("pin_required"),
                        text = Locales.t("import_requires_pin"),
                        confirmText = Locales.t("confirm")
                    ) {
                        BackupFilePicker.importJson(
                            onPicked = { jsonText ->
                                state.pendingImportText = jsonText
                                state.showImportConfirm = true
                            },
                            onError = { errorText ->
                                state.showImportError = errorText
                            }
                        )
                    }
                },
                onSetOrChangePin = { state.showSetPinDialog = true },
                onRemovePin = {
                    state.runProtected(
                        title = Locales.t("pin_required"),
                        text = Locales.t("pin_required"),
                        confirmText = Locales.t("confirm")
                    ) {
                        state.showRemovePinConfirm = true
                    }
                },
                onClearDatabase = {
                    state.runProtected(
                        title = Locales.t("clear_db_title"),
                        text = Locales.t("clear_db_requires_pin"),
                        confirmText = Locales.t("confirm")
                    ) {
                        state.showClearDbBackupPrompt = true
                    }
                }
            )

            Screen.STATS -> StatsPage(
                appointments = state.appointments,
                today = state.today
            )

            Screen.FEEDBACK -> FeedbackPage(
                phone = AppSettings.servicePhone,
                onCallClick = { phone ->
                    if (phone.isNotBlank()) PhoneCaller.call(phone)
                }
            )

            Screen.MONTH -> {
                val today = state.today

                var nowTimeHm by remember { mutableStateOf(getCurrentTimeHm()) }
                LaunchedEffect(Unit) {
                    while (true) {
                        nowTimeHm = getCurrentTimeHm()
                        delay(60_000)
                    }
                }
                val nowMin = remember(nowTimeHm) { com.andrey.beautyplanner.utils.parseHmToMinutes(nowTimeHm) }

                val listState = rememberLazyListState()

                val upcoming by remember(nowTimeHm, state.today, state.appointments.size) {
                    derivedStateOf {
                        getUpcomingAppointments(
                            appointments = state.appointments,
                            today = state.today,
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

                val headerText by remember(isCollapsed, state.calendarViewDate, state.today) {
                    derivedStateOf {
                        if (!isCollapsed) {
                            val monthKey = when (state.calendarViewDate.monthNumber) {
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
                            "${Locales.t(monthKey)} ${state.calendarViewDate.year}"
                        } else {
                            val monthKeyGen = when (state.today.monthNumber) {
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
                            "${state.today.dayOfMonth} ${Locales.t(monthKeyGen)} ${state.calendarViewDate.year}"
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
                            fontSize = (24 * state.fontScale).sp,
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
                                onClick = { state.calendarViewDate = state.calendarViewDate.minus(1, DateTimeUnit.MONTH) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = arrowTint
                                )
                            }

                            IconButton(
                                enabled = arrowsEnabled,
                                onClick = { state.calendarViewDate = state.calendarViewDate.plus(1, DateTimeUnit.MONTH) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
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
                                monthDate = state.calendarViewDate,
                                today = state.today,
                                selectedDate = state.selectedDate
                            ) { date ->
                                state.selectedDate = date
                                state.currentScreen = Screen.DAY_DETAILS
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
                                fontSize = (16 * state.fontScale).sp,
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
                                        fontSize = (14 * state.fontScale).sp
                                    )
                                }
                            }
                        } else {
                            items(upcoming.size) { idx ->
                                val appt = upcoming[idx]

                                val status = getLiveStatus(
                                    appt = appt,
                                    nowDate = today,
                                    nowMinutes = nowMin
                                )

                                UpcomingAppointmentItem(
                                    appt = appt,
                                    status = status,
                                    onClick = {
                                        state.editingAppointment = appt
                                        state.bookingReadOnly = true
                                        state.showBookingDialog = true
                                    },
                                    onLongClick = {
                                        openApptMenu(appt, status)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Screen.DAY_DETAILS -> DayDetailsView(
                date = state.selectedDate,
                appointments = state.appointments,
                onDateChange = { state.selectedDate = it },
                onTimeClick = { time ->
                    state.selectedTimeSlot = time
                    state.editingAppointment = null
                    state.bookingReadOnly = false
                    state.showBookingDialog = true
                },
                onAppointmentLongPress = { appt, status ->
                    openApptMenu(appt, status)
                },
                onEditClick = { appt ->
                    state.editingAppointment = appt
                    state.bookingReadOnly = false
                    state.showBookingDialog = true
                },
                onTransferClick = { appt ->
                    state.transferA = appt
                    state.showTransferPickDialog = true
                    state.bookingReadOnly = false
                },
                onDeleteClick = { appt ->
                    state.showDeleteConfirm = appt
                }
            )
        }

        // ===== DropdownMenu anchored "in overlay" (единый) =====
        // Технически это menu без якоря (как в твоем Upcoming было через DropdownMenu у карточки),
        // но дизайн карточек не трогаем; меню — единое и вызывается одинаково.
        val appt = menuAppt
        val status = menuStatus
        if (appt != null && status != null) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                val editTransferEnabled = status != LiveStatusKey.DONE

                DropdownMenuItem(
                    enabled = editTransferEnabled,
                    onClick = { requestAction(ApptAction.EDIT) }
                ) { Text(Locales.t("edit")) }

                DropdownMenuItem(
                    enabled = editTransferEnabled,
                    onClick = { requestAction(ApptAction.TRANSFER) }
                ) { Text(Locales.t("transfer_appt")) }

                DropdownMenuItem(
                    onClick = { requestAction(ApptAction.DELETE) }
                ) { Text(Locales.t("delete_btn")) }
            }
        }

        // ===== Confirm AlertDialog for menu actions =====
        if (showConfirm && menuAppt != null && pendingAction != null) {
            val appt0 = menuAppt!!
            val act = pendingAction!!

            val title = when (act) {
                ApptAction.EDIT -> Locales.t("edit_appointment_title")
                ApptAction.TRANSFER -> Locales.t("transfer_title")
                ApptAction.DELETE -> Locales.t("delete_title")
            }

            val text = when (act) {
                ApptAction.EDIT -> Locales.t("edit_appointment_confirm")
                ApptAction.TRANSFER -> Locales.t("transfer_conflict_text") // максимально близкий существующий текст
                ApptAction.DELETE -> {
                    val client = appt0.clientName
                    val time = appt0.time
                    "${Locales.t("delete_confirm_prefix")} $client ${Locales.t("delete_confirm_at")} $time. ${Locales.t("continue_question")}"
                }
            }

            AlertDialog(
                onDismissRequest = {
                    showConfirm = false
                    pendingAction = null
                },
                title = { Text(title) },
                text = { Text(text) },
                confirmButton = {
                    Button(onClick = {
                        showConfirm = false
                        pendingAction = null
                        applyAction(appt0, act)
                    }) {
                        Text(Locales.t("confirm"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirm = false
                        pendingAction = null
                    }) {
                        Text(Locales.t("cancel"))
                    }
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
        }

        if (state.showBookingDialog) {
            BookingDialog(
                time = state.editingAppointment?.time ?: state.selectedTimeSlot,
                initialData = state.editingAppointment ?: state.transferA,
                readOnly = state.bookingReadOnly && state.editingAppointment != null,
                onDismiss = {
                    state.showBookingDialog = false
                    state.editingAppointment = null
                    state.transferA = null
                    state.bookingReadOnly = false
                },
                onSave = { startTime, durationMinutes, name, phone, service, price ->
                    val id = state.editingAppointment?.id
                        ?: state.transferA?.id
                        ?: Clock.System.now().toEpochMilliseconds().toString()

                    val targetDate = state.selectedDate.toString()

                    val newAppt = Appointment(
                        id = id,
                        dateString = targetDate,
                        time = startTime,
                        clientName = name,
                        phone = phone,
                        serviceName = service,
                        price = price,
                        durationMinutes = durationMinutes,
                        durationHours = ((durationMinutes + 59) / 60).coerceAtLeast(1)
                    )

                    state.transferA?.let {
                        state.appointments.remove(it)
                        state.transferA = null
                    }

                    state.replaceById(newAppt)
                    state.saveAll()

                    state.showBookingDialog = false
                    state.editingAppointment = null
                    state.bookingReadOnly = false
                },
                onTransferRequest = { appt ->
                    state.transferA = appt
                    state.showBookingDialog = false
                    state.showTransferPickDialog = true
                    state.bookingReadOnly = false
                }
            )
        }

        if (state.showTransferPickDialog && state.transferA != null) {
            val a = state.transferA!!
            TransferPickDialog(
                initialSelectedDate = LocalDate.parse(a.dateString),
                initialMonthDate = LocalDate.parse(a.dateString),
                onDismiss = {
                    state.showTransferPickDialog = false
                    state.transferA = null
                },
                onConfirm = { newDate, newTime ->
                    state.pendingTargetDate = newDate
                    state.pendingTargetTime = newTime

                    val b = state.findAppointment(newDate, newTime)
                    if (b != null && b.id != a.id) {
                        state.conflictB = b
                        state.showTransferConflictConfirm = true
                    } else {
                        state.moveAppointment(a, newDate, newTime)
                        state.saveAll()
                        state.showTransferPickDialog = false
                        state.transferA = null
                    }
                }
            )
        }
    }
}