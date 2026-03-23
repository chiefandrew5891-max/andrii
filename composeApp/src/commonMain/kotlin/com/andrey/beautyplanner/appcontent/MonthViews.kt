package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import kotlinx.datetime.LocalDate
import kotlin.math.ceil

// ------------------------- time helpers -------------------------

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

private fun endTime(startHm: String, durationHours: Int): String {
    val startMin = parseHmToMinutes(startHm)
    val endMin = startMin + (durationHours.coerceAtLeast(1) * 60)
    return minutesToHm(endMin)
}

// ------------------------- upcoming logic -------------------------

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
                    // сегодня: оставляем те, которые ещё не закончились
                    val apptEndMin = parseHmToMinutes(endTime(appt.time, appt.durationHours))
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

@Composable
fun UpcomingAppointmentCard(
    appt: Appointment,
    onClick: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val interactionSource = remember { MutableInteractionSource() }

    val dateParts = appt.dateString.split("-")
    val formattedDate =
        if (dateParts.size == 3) "${dateParts[2]}.${dateParts[1]}.${dateParts[0]}"
        else appt.dateString

    val start = appt.time
    val end = endTime(appt.time, appt.durationHours)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(Modifier.padding(14.dp)) {
            // верхняя строка: дата + диапазон времени | цена справа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$formattedDate  $start–$end",
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

            // вторая строка: Имя (bold) + услуга и длительность
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * fontScale).sp,
                            color = MaterialTheme.colors.onSurface
                        )
                    ) { append(appt.clientName) }

                    append("  ")

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = (13 * fontScale).sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    ) { append("${appt.serviceName} (${appt.durationHours} ч.)") }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------- calendar -------------------------

@Composable
fun MonthCalendarGrid(
    monthDate: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val isLeap = monthDate.year % 4 == 0 && (monthDate.year % 100 != 0 || monthDate.year % 400 == 0)
    val daysInMonth = monthDate.month.length(isLeap)
    val firstDayOfMonth = LocalDate(monthDate.year, monthDate.month, 1)

    // MONDAY=0 ... SUNDAY=6
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.ordinal

    val days = (1..daysInMonth).toList()
    val fontScale = AppSettings.getFontScale()

    // динамическая высота под 5/6 недель
    val totalCells = dayOfWeekOffset + daysInMonth
    val rows = ceil(totalCells / 7.0).toInt().coerceAtLeast(5)
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
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
        ) {
            // пустые ячейки делаем квадратами, чтобы сетка не "ехала"
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
                        .clickable(
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