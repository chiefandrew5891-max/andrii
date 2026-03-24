package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.andrey.beautyplanner.appcontent.AppDialogShape
import com.andrey.beautyplanner.appcontent.AppDialogTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.*

private fun parseHmToMinutes(hm: String): Int? {
    val parts = hm.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23) return null
    if (m !in 0..59) return null
    return h * 60 + m
}

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

private data class Block(
    val kind: Kind,
    val startMin: Int,
    val endMin: Int,
    val appt: Appointment? = null
) {
    enum class Kind { FREE, APPOINTMENT }
}

/**
 * UI-only status scaffold (for now it’s always DONE in the dialog).
 * Later you can store it in Appointment and choose based on actual data.
 */
private enum class AppointmentStatusKey(val localeKey: String) {
    DONE("appt_status_done"),
    RESCHEDULED("appt_status_rescheduled"),
    CANCELED("appt_status_canceled")
}

@Composable
private fun AppointmentViewDialog(
    appt: Appointment,
    startHm: String,
    endHm: String,
    serviceDisplay: String,
    status: AppointmentStatusKey,
    onDismiss: () -> Unit
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
                    text = appt.clientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = (18 * fontScale).sp,
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    // Time range
                    Text(
                        text = "$startHm–$endHm",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Service
                    Text(
                        text = "${Locales.t("service")}: $serviceDisplay",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )

                    // Price
                    if (priceText.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${Locales.t("price")}: $priceText",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                        )
                    }

                    // Status (localized)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${Locales.t("appt_status_label")}: ${Locales.t(status.localeKey)}",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.70f),
                        fontSize = (13 * fontScale).sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(Locales.t("close"))
                }
            },
            shape = AppDialogShape
        )
    }
}

@Composable
fun DayDetailsView(
    date: LocalDate,
    appointments: List<Appointment>,
    onDateChange: (LocalDate) -> Unit,
    onTimeClick: (String) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onDeleteClick: (Appointment) -> Unit
) {
    val fontScale = AppSettings.getFontScale()

    // Today (device local timezone)
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Current time (HH:mm) updates each minute, same pattern as in AppRootContent.kt
    var nowTimeHm by remember { mutableStateOf(getCurrentTimeHm()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTimeHm = getCurrentTimeHm()
            delay(60_000)
        }
    }
    val nowMin = remember(nowTimeHm) { parseHmToMinutes(nowTimeHm) ?: 0 }

    val monthNames = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    val formattedDate = "${date.dayOfMonth} ${monthNames[date.monthNumber - 1]} ${date.year}"

    val dayStart = 8 * 60
    val dayEnd = 21 * 60

    // Snapshot to avoid stale recomposition issues
    val apptsSnapshot = appointments.toList()

    val dayAppts = remember(apptsSnapshot, date) {
        apptsSnapshot
            .filter { it.dateString == date.toString() }
            .mapNotNull { a ->
                val s = parseHmToMinutes(a.time) ?: return@mapNotNull null
                val d = if (a.durationMinutes > 0) a.durationMinutes else a.durationHours.coerceAtLeast(1) * 60
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

    // unified time layout
    val timeColWidth = 60.dp
    val timeFont = (16 * fontScale).sp
    val timeFontWeight = FontWeight.Bold

    val busyTimeColor = MaterialTheme.colors.primary
    val freeTimeColor = MaterialTheme.colors.onSurface.copy(alpha = 0.55f)
    val pastTimeColor = MaterialTheme.colors.onSurface.copy(alpha = 0.80f)

    // Dialog view state
    var viewingAppt by remember { mutableStateOf<Appointment?>(null) }
    var viewingStartHm by remember { mutableStateOf("") }
    var viewingEndHm by remember { mutableStateOf("") }
    var viewingService by remember { mutableStateOf("") }
    var viewingStatus by remember { mutableStateOf(AppointmentStatusKey.DONE) }

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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(blocks.size) { idx ->
                val b = blocks[idx]
                val interactionSource = remember { MutableInteractionSource() }

                val startHm = minutesToHm(b.startMin)
                val endHm = minutesToHm(b.endMin)

                if (b.kind == Block.Kind.APPOINTMENT && b.appt != null) {
                    val appt = b.appt

                    val serviceDisplay =
                        if (appt.serviceName.startsWith("service_")) Locales.t(appt.serviceName)
                        else appt.serviceName

                    val durationMin =
                        if (appt.durationMinutes > 0) appt.durationMinutes else appt.durationHours.coerceAtLeast(1) * 60
                    val apptStartMin = parseHmToMinutes(appt.time) ?: b.startMin
                    val apptEndMin = apptStartMin + durationMin

                    // Past logic: by date OR (today and ended)
                    val isPastAppt = when {
                        date < today -> true
                        date > today -> false
                        else -> apptEndMin <= nowMin
                    }

                    val cardBg = when {
                        isPastAppt -> if (AppSettings.isDarkMode) Color(0xFF1F2A36) else Color(0xFFECECEC)
                        else -> if (AppSettings.isDarkMode) Color(0xFF253548) else Color(0xFFF2F2F2)
                    }

                    Card(
                        elevation = 0.dp,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(92.dp)
                            // ✅ whole card clickable -> opens view dialog
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current
                            ) {
                                viewingAppt = appt
                                viewingStartHm = startHm
                                viewingEndHm = endHm
                                viewingService = serviceDisplay
                                viewingStatus = if (isPastAppt) AppointmentStatusKey.DONE else AppointmentStatusKey.DONE
                            },
                        backgroundColor = cardBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.width(timeColWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val timeColor = if (isPastAppt) pastTimeColor else busyTimeColor

                                Text(
                                    text = startHm,
                                    fontSize = timeFont,
                                    fontWeight = timeFontWeight,
                                    color = timeColor
                                )
                                Divider(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .width(30.dp),
                                    thickness = 1.dp,
                                    color = timeColor.copy(alpha = 0.35f)
                                )
                                Text(
                                    text = endHm,
                                    fontSize = timeFont,
                                    fontWeight = timeFontWeight,
                                    color = timeColor
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = appt.clientName,
                                    fontSize = (15 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = if (isPastAppt) 0.85f else 1f)
                                )
                                Text(
                                    text = serviceDisplay,
                                    fontSize = (13 * fontScale).sp,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.60f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val priceText = appt.price.trim().let { p ->
                                if (p.isBlank()) "" else "$p €"
                            }

                            if (priceText.isNotBlank()) {
                                Text(
                                    text = priceText,
                                    fontSize = (14 * fontScale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = if (isPastAppt) 0.85f else 1f),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPastAppt) {
                                    // ✅ instead of edit pencil -> check
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary.copy(alpha = 0.75f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    IconButton(onClick = { onEditClick(appt) }) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(22.dp))
                                    }
                                }

                                // ✅ delete stays always (client may not come)
                                IconButton(onClick = { onDeleteClick(appt) }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                } else {
                    // FREE SLOT
                    Card(
                        elevation = 0.dp,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(78.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = { onTimeClick(startHm) }
                            ),
                        backgroundColor = Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier.width(timeColWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = startHm,
                                    fontSize = timeFont,
                                    fontWeight = timeFontWeight,
                                    color = freeTimeColor
                                )
                            }

                            Text(
                                text = Locales.t("free"),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.35f),
                                fontSize = (14 * fontScale).sp
                            )
                        }
                    }
                }
            }
        }
    }

    val apptToView = viewingAppt
    if (apptToView != null) {
        AppointmentViewDialog(
            appt = apptToView,
            startHm = viewingStartHm,
            endHm = viewingEndHm,
            serviceDisplay = viewingService,
            status = viewingStatus,
            onDismiss = { viewingAppt = null }
        )
    }
}