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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import kotlinx.datetime.LocalDate

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
        .sortedWith(
            compareBy<Appointment>(
                { it.dateString },
                { parseHmToMinutes(it.time) }
            )
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpcomingAppointmentItem(
    appt: Appointment,
    status: com.andrey.beautyplanner.utils.LiveStatusKey,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val end = endTime(appt)

    // Используем общий AppointmentCard, но он сохраняет старую вёрстку Upcoming
    // (showDateInCard = true)
    AppointmentCard(
        appt = appt,
        status = status,
        showDateInCard = true,
        startHm = appt.time,
        endHm = end,
        onClick = onClick,
        onLongClick = onLongClick
    )
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

    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.ordinal
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
                    text = com.andrey.beautyplanner.Locales.t(day),
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
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
        ) {
            items(dayOfWeekOffset) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                )
            }

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