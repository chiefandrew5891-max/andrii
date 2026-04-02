package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.getCurrentTimeHm
import com.andrey.beautyplanner.utils.LiveStatusKey
import com.andrey.beautyplanner.utils.getLiveStatus
import com.andrey.beautyplanner.utils.parseHmToMinutes
import kotlinx.coroutines.delay
import kotlinx.datetime.*

private fun minutesToHm(mins: Int): String {
    val safe = mins.coerceIn(0, 24 * 60 - 1)
    val h = safe / 60
    val m = safe % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

private fun nextHourBoundary(mins: Int): Int {
    val mod = mins % 60
    return if (mod == 0) mins + 60 else mins + (60 - mod)
}

private fun apptDurationMinutesCompat(appt: Appointment): Int {
    return if (appt.durationMinutes > 0) appt.durationMinutes else appt.durationHours.coerceAtLeast(1) * 60
}

private data class Block(
    val kind: Kind,
    val startMin: Int,
    val endMin: Int,
    val appt: Appointment? = null
) {
    enum class Kind { FREE, APPOINTMENT }
}

@Composable
private fun AppointmentViewDialog(
    appt: Appointment,
    startHm: String,
    endHm: String,
    serviceDisplay: String,
    status: LiveStatusKey,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onTransferClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val priceText = appt.price.trim().let { p ->
        if (p.isBlank()) "" else "$p €"
    }

    AppDialogTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = Locales.t("view_appointment_title"),
                    fontWeight = FontWeight.Bold,
                    fontSize = (18 * fontScale).sp,
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = appt.clientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = (18 * fontScale).sp,
                        color = MaterialTheme.colors.onSurface
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "$startHm–$endHm",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "${Locales.t("service")}: $serviceDisplay",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )

                    if (priceText.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${Locales.t("price")}: $priceText",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${Locales.t("appt_status_label")}: ${Locales.t(status.localeKey)}",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.70f),
                        fontSize = (13 * fontScale).sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEditClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(Locales.t("edit"))
                        }

                        OutlinedButton(
                            onClick = onTransferClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(Locales.t("transfer_appt"))
                        }

                        OutlinedButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text(Locales.t("delete_btn"), color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(Locales.t("close"))
                }
            },
            shape = AppDialogShape
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayDetailsView(
    date: LocalDate,
    appointments: List<Appointment>,
    onDateChange: (LocalDate) -> Unit,
    onTimeClick: (String) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onTransferClick: (Appointment) -> Unit,
    onDeleteClick: (Appointment) -> Unit
) {
    val fontScale = AppSettings.getFontScale()

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    var nowTimeHm by remember { mutableStateOf(getCurrentTimeHm()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTimeHm = getCurrentTimeHm()
            delay(60_000)
        }
    }
    val nowMin = remember(nowTimeHm) { parseHmToMinutes(nowTimeHm) ?: 0 }

    val monthKeyGen = when (date.monthNumber) {
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
        else -> "month_jan_gen"
    }
    val formattedDate = "${date.dayOfMonth} ${Locales.t(monthKeyGen)} ${date.year}"

    val dayStart = 8 * 60
    val dayEnd = 21 * 60

    val apptsSnapshot = appointments.toList()

    val dayAppts = remember(apptsSnapshot, date) {
        apptsSnapshot
            .filter { it.dateString == date.toString() }
            .mapNotNull { a ->
                val s = parseHmToMinutes(a.time) ?: return@mapNotNull null
                val d = apptDurationMinutesCompat(a)
                val e = s + d
                Triple(a, s, e)
            }
            .sortedBy { it.second }
    }

    val blocks = remember(dayAppts) {
        val out = mutableListOf<Block>()
        var cursor = dayStart
        var i = 0

        while (cursor < dayEnd) {
            val next = dayAppts.getOrNull(i)
            val nextStart = next?.second
            val nextEnd = next?.third

            if (next != null && nextStart != null && nextEnd != null && nextStart <= cursor) {
                val start = nextStart.coerceAtLeast(dayStart)
                val end = nextEnd.coerceAtMost(dayEnd)
                if (end > cursor) {
                    out.add(Block(Block.Kind.APPOINTMENT, startMin = start, endMin = end, appt = next.first))
                    cursor = end
                }
                i++
                continue
            }

            val freeStart = cursor
            val freeEndCandidate = nextHourBoundary(cursor).coerceAtMost(dayEnd)
            val freeEnd =
                if (nextStart != null) minOf(freeEndCandidate, nextStart.coerceAtMost(dayEnd)) else freeEndCandidate

            if (freeEnd > freeStart) {
                out.add(Block(Block.Kind.FREE, startMin = freeStart, endMin = freeEnd, appt = null))
                cursor = freeEnd
            } else {
                cursor += 10
            }
        }
        out
    }

    var viewingAppt by remember { mutableStateOf<Appointment?>(null) }
    var viewingStartHm by remember { mutableStateOf("") }
    var viewingEndHm by remember { mutableStateOf("") }
    var viewingService by remember { mutableStateOf("") }
    var viewingStatus by remember { mutableStateOf<LiveStatusKey?>(null) }

    var quickMenuAppt by remember { mutableStateOf<Appointment?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDate,
                fontSize = (24 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )

            Row {
                val arrowTint = MaterialTheme.colors.primary

                IconButton(onClick = { onDateChange(date.minus(1, DateTimeUnit.DAY)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = arrowTint)
                }
                IconButton(onClick = { onDateChange(date.plus(1, DateTimeUnit.DAY)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = arrowTint)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(blocks.size) { idx ->
                val block = blocks[idx]
                val interactionSource = remember { MutableInteractionSource() }
                val startHm = minutesToHm(block.startMin)
                val endHm = minutesToHm(block.endMin)

                if (block.kind == Block.Kind.APPOINTMENT && block.appt != null) {
                    val appt = block.appt
                    val serviceDisplay =
                        if (appt.serviceName.startsWith("service_")) Locales.t(appt.serviceName)
                        else appt.serviceName

                    val apptStartMin = parseHmToMinutes(appt.time) ?: block.startMin
                    val apptEndMin = apptStartMin + apptDurationMinutesCompat(appt)

                    val liveStatus = getLiveStatus(
                        appt = appt,
                        nowDate = today,
                        nowMinutes = nowMin
                    )

                    val dateParts = appt.dateString.split("-")
                    val formattedCardDate =
                        if (dateParts.size == 3) "${dateParts[2]}.${dateParts[1]}.${dateParts[0]}"
                        else appt.dateString

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = {
                                    viewingAppt = appt
                                    viewingStartHm = startHm
                                    viewingEndHm = endHm
                                    viewingService = serviceDisplay
                                    viewingStatus = liveStatus
                                },
                                onLongClick = {
                                    quickMenuAppt = appt
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val timeColor =
                                    if (date < today || apptEndMin <= nowMin) {
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
                                    } else {
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                                    }

                                Text(
                                    text = "$formattedCardDate  $startHm–$endHm",
                                    fontSize = (13 * fontScale).sp,
                                    fontWeight = FontWeight.Medium,
                                    color = timeColor
                                )

                                Text(
                                    text = "${appt.price}€",
                                    fontSize = (13 * fontScale).sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appt.clientName,
                                        fontSize = (15 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colors.onSurface
                                    )

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = serviceDisplay,
                                        fontSize = (13 * fontScale).sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (liveStatus == LiveStatusKey.DONE) {
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary.copy(alpha = 0.75f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = { onTimeClick(startHm) }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.surface.copy(alpha = if (AppSettings.isDarkMode) 0.22f else 0.45f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colors.onSurface.copy(alpha = if (AppSettings.isDarkMode) 0.18f else 0.10f)
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = "$startHm–$endHm",
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = Locales.t("free"),
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
        }
    }

    val apptToView = viewingAppt
    val liveStatusToView = viewingStatus
    if (apptToView != null && liveStatusToView != null) {
        AppointmentViewDialog(
            appt = apptToView,
            startHm = viewingStartHm,
            endHm = viewingEndHm,
            serviceDisplay = viewingService,
            status = liveStatusToView,
            onDismiss = {
                viewingAppt = null
                viewingStatus = null
            },
            onEditClick = {
                viewingAppt = null
                viewingStatus = null
                onEditClick(apptToView)
            },
            onTransferClick = {
                viewingAppt = null
                viewingStatus = null
                onTransferClick(apptToView)
            },
            onDeleteClick = {
                viewingAppt = null
                viewingStatus = null
                onDeleteClick(apptToView)
            }
        )
    }

    val quickAppt = quickMenuAppt
    if (quickAppt != null) {
        AlertDialog(
            onDismissRequest = { quickMenuAppt = null },
            title = { Text(Locales.t("quick_actions_title")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            quickMenuAppt = null
                            onEditClick(quickAppt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(Locales.t("edit"))
                    }

                    OutlinedButton(
                        onClick = {
                            quickMenuAppt = null
                            onTransferClick(quickAppt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(Locales.t("transfer_appt"))
                    }

                    OutlinedButton(
                        onClick = {
                            quickMenuAppt = null
                            onDeleteClick(quickAppt)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text(Locales.t("delete_btn"), color = Color.Red)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { quickMenuAppt = null }) {
                    Text(Locales.t("cancel"))
                }
            },
            shape = AppDialogShape
        )
    }
}
