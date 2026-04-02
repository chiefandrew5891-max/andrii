package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.utils.LiveStatusKey
import kotlinx.datetime.LocalDate

// Вспомогательные функции (БЕЗ ИЗМЕНЕНИЙ)
private fun parseHmToMinutes(hm: String): Int {
    val parts = hm.trim().split(":")
    if (parts.size != 2) return 0
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    return h * 60 + m
}

private fun minutesToHm(minutes: Int): String {
    val m = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
    val hPart = (m / 60).toString().padStart(2, '0')
    val mPart = (m % 60).toString().padStart(2, '0')
    return "$hPart:$mPart"
}

private fun apptDurationMinutes(appt: Appointment): Int {
    return if (appt.durationMinutes > 0) appt.durationMinutes else appt.durationHours.coerceAtLeast(1) * 60
}

private fun endTime(appt: Appointment): String {
    val startMin = parseHmToMinutes(appt.time)
    val endMin = startMin + apptDurationMinutes(appt)
    return minutesToHm(endMin)
}

fun getUpcomingAppointments(
    appointments: List<Appointment>,
    today: LocalDate,
    nowTime: String
): List<Appointment> {
    val nowMin = parseHmToMinutes(nowTime)
    return appointments
        .filter { appt ->
            val apptDate = runCatching { LocalDate.parse(appt.dateString) }.getOrNull()
                ?: return@filter false
            when {
                apptDate > today -> true
                apptDate < today -> false
                else -> {
                    val apptEndMin = parseHmToMinutes(endTime(appt))
                    apptEndMin > nowMin
                }
            }
        }
        .sortedWith(compareBy({ it.dateString }, { parseHmToMinutes(it.time) }))
}

// НОВЫЙ ОБЩИЙ КОМПОНЕНТ (Перенесен из DayDetailsView для унификации)
@Composable
fun AppointmentViewDialog(
    appt: Appointment,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onTransferClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val startHm = appt.time
    val endHm = endTime(appt)
    val serviceDisplay = if (appt.serviceName.startsWith("service_")) Locales.t(appt.serviceName) else appt.serviceName

    val priceText = appt.price.trim().let { p ->
        if (p.isBlank()) "" else "$p €"
    }

    // Твоя тема диалога
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
                Spacer(Modifier.height(18.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(Locales.t("edit")) }
                    OutlinedButton(
                        onClick = onTransferClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(Locales.t("transfer_appt")) }
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) { Text(Locales.t("delete_btn"), color = Color.Red) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Locales.t("cancel")) }
        },
        shape = RoundedCornerShape(28.dp) // Соответствует AppDialogShape
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpcomingAppointmentCard(
    appt: Appointment,
    showDate: Boolean = true, // Добавлено для управления отображением
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onTransferClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val fontScale = AppSettings.getFontScale()
    val interactionSource = remember { MutableInteractionSource() }
    var showQuickMenu by remember { mutableStateOf(false) }

    val dateParts = appt.dateString.split("-")
    val formattedDate = if (dateParts.size == 3) "${dateParts[2]}.${dateParts[1]}.${dateParts[0]}" else appt.dateString

    val start = appt.time
    val end = endTime(appt)
    val translatedService = if (appt.serviceName.startsWith("service_")) Locales.t(appt.serviceName) else appt.serviceName
    val durationLabel = Locales.hoursCount((apptDurationMinutes(appt) / 60.0).let { kotlin.math.ceil(it).toInt().coerceAtLeast(1) })

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = { if (onEditClick != null) showQuickMenu = true }
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
                    Text(
                        // Логика: если showDate=false, выводим только время
                        text = if (showDate) "$formattedDate  $start–$end" else "$start–$end",
                        fontSize = (13 * fontScale).sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${appt.price}€",
                        fontSize = (13 * fontScale).sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = (15 * fontScale).sp, color = MaterialTheme.colors.onSurface)) {
                            append(appt.clientName)
                        }
                        append("  ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = (13 * fontScale).sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))) {
                            append("$translatedService ($durationLabel)")
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(expanded = showQuickMenu, onDismissRequest = { showQuickMenu = false }) {
            if (onEditClick != null) DropdownMenuItem(onClick = { showQuickMenu = false; onEditClick() }) { Text(Locales.t("edit")) }
            if (onTransferClick != null) DropdownMenuItem(onClick = { showQuickMenu = false; onTransferClick() }) { Text(Locales.t("transfer_appt")) }
            if (onDeleteClick != null) DropdownMenuItem(onClick = { showQuickMenu = false; onDeleteClick() }) { Text(Locales.t("delete_btn"), color = Color.Red) }
        }
    }
}

@Composable
fun MonthCalendarGrid(
    monthDate: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val isLeap = monthDate.year % 4 == 0 && (monthDate.year % 100 != 0 || monthDate.year % 400 == 0)
    val daysInMonth = when (monthDate.monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeap) 29 else 28
        else -> 30
    }
    val firstDayOfMonth = LocalDate(monthDate.year, monthDate.month, 1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.ordinal) % 7
    val days = (1..daysInMonth).toList()
    val fontScale = AppSettings.getFontScale()

    val totalCells = dayOfWeekOffset + daysInMonth
    val rows = ((totalCells + 6) / 7).coerceAtLeast(5)
    val rowHeight = 48.dp
    val gridHeight = rowHeight * rows

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
            weekdays.forEach { day ->
                Text(
                    text = Locales.t(day),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = (12 * fontScale).sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(gridHeight),
            userScrollEnabled = false
        ) {
            items(dayOfWeekOffset) { Box(modifier = Modifier.aspectRatio(1f).padding(4.dp)) }
            items(days) { day ->
                val dateForCell = LocalDate(monthDate.year, monthDate.month, day)
                val isToday = dateForCell == today
                val isSelected = dateForCell == selectedDate
                val isPast = dateForCell < today
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSelected -> MaterialTheme.colors.primary
                                isToday -> MaterialTheme.colors.primary.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                        )
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = { onDateClick(dateForCell) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = when {
                            isSelected -> Color.White
                            isPast -> Color.Gray.copy(alpha = 0.4f)
                            isToday -> MaterialTheme.colors.primary
                            else -> MaterialTheme.colors.onBackground
                        },
                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Normal,
                        fontSize = (16 * fontScale).sp
                    )
                }
            }
        }
    }
}