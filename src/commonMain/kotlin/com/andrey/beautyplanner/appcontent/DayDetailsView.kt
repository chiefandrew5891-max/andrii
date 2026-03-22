package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private fun parseHmToMinutes(hm: String): Int? {
    val parts = hm.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

private fun minutesToHm(mins: Int): String {
    val safe = mins.coerceIn(0, 24 * 60 - 1)
    val h = safe / 60
    val m = safe % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
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

    val monthNames = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    val formattedDate = "${date.dayOfMonth} ${monthNames[date.monthNumber - 1]} ${date.year}"

    // Базовые часы, как раньше
    val hours = (8..20).toList()

    // Записи на выбранный день, отсортированные по времени
    val dayApptsSorted = remember(appointments, date) {
        appointments
            .filter { it.dateString == date.toString() }
            .sortedBy { parseHmToMinutes(it.time) ?: Int.MAX_VALUE }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formattedDate, fontSize = (20 * fontScale).sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { onDateChange(date.minus(1, DateTimeUnit.DAY)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { onDateChange(date.plus(1, DateTimeUnit.DAY)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(32.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            hours.forEach { h ->
                // Заголовок часа (опционально). Если не нужен — скажи, уберу.
                item {
                    Text(
                        text = "${h.toString().padStart(2, '0')}:00",
                        fontSize = (13 * fontScale).sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp, start = 4.dp)
                    )
                }

                // Все записи, начинающиеся в этом часе (08:xx)
                val apptsInThisHour = dayApptsSorted.filter { ap ->
                    val m = parseHmToMinutes(ap.time) ?: return@filter false
                    val apptHour = m / 60
                    apptHour == h
                }

                // Рисуем каждую запись отдельной карточкой
                apptsInThisHour.forEach { appt ->
                    item {
                        val interactionSource = remember { MutableInteractionSource() }

                        // end = start + durationHours*60 (пока так, до миграции на minutes)
                        val startMin = parseHmToMinutes(appt.time) ?: (h * 60)
                        val dur = if (appt.durationMinutes > 0) appt.durationMinutes else appt.durationHours.coerceAtLeast(1) * 60
                        val endMin = startMin + dur
                        val endTime = minutesToHm(endMin)

                        val serviceDisplay =
                            if (appt.serviceName.startsWith("service_")) Locales.t(appt.serviceName)
                            else appt.serviceName

                        Card(
                            elevation = 0.dp,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .height(90.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { onEditClick(appt) }
                                ),
                            backgroundColor = if (AppSettings.isDarkMode) Color(0xFF253548) else Color(0xFFF2F2F2)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.width(86.dp)) {
                                    Text(
                                        text = appt.time,
                                        fontSize = (16 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )

                                    Divider(
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .width(50.dp),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colors.primary.copy(alpha = 0.35f)
                                    )

                                    Text(
                                        text = endTime,
                                        fontSize = (14 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary.copy(alpha = 0.6f)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = appt.clientName,
                                        fontSize = (17 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$serviceDisplay • ${appt.durationHours} ч.",
                                        fontSize = (13 * fontScale).sp,
                                        color = Color.Gray
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onEditClick(appt) }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            null,
                                            tint = MaterialTheme.colors.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeleteClick(appt) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Карточка "Свободно" для этого часа (быстро добавить)
                item {
                    val baseTime = "${h.toString().padStart(2, '0')}:00"
                    val interactionSource = remember { MutableInteractionSource() }

                    Card(
                        elevation = 0.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .height(70.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = { onTimeClick(baseTime) }
                            ),
                        backgroundColor = Color.Transparent,
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = Locales.t("free"),
                                color = Color.Gray.copy(alpha = 0.55f),
                                fontSize = (14 * fontScale).sp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = baseTime,
                                color = Color.Gray.copy(alpha = 0.35f),
                                fontSize = (12 * fontScale).sp
                            )
                        }
                    }
                }
            }
        }
    }
}