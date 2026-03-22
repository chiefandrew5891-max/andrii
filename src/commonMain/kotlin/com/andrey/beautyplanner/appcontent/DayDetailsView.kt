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

    val dayStart = 8 * 60
    val dayEnd = 21 * 60

    // IMPORTANT: snapshot, so UI updates immediately when mutableStateList changes
    val apptsSnapshot = remember(appointments.size) { appointments.toList() }

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
            val freeEnd = if (nextStart != null) minOf(freeEndCandidate, nextStart.coerceAtMost(dayEnd)) else freeEndCandidate

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
    val timeColWidth = 86.dp
    val timeFont = (16 * fontScale).sp
    val timeFontWeight = FontWeight.Bold
    val busyTimeColor = MaterialTheme.colors.primary
    val freeTimeColor = Color.Gray.copy(alpha = 0.65f)

    Column(modifier = Modifier.fillMaxSize()) {
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

                    Card(
                        elevation = 0.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(92.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = { onEditClick(appt) }
                            ),
                        backgroundColor = if (AppSettings.isDarkMode) Color(0xFF253548) else Color(0xFFF2F2F2)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.width(timeColWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = startHm,
                                    fontSize = timeFont,
                                    fontWeight = timeFontWeight,
                                    color = busyTimeColor
                                )
                                Divider(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .width(30.dp),
                                    thickness = 1.dp,
                                    color = busyTimeColor.copy(alpha = 0.35f)
                                )
                                Text(
                                    text = endHm,
                                    fontSize = timeFont,
                                    fontWeight = timeFontWeight,
                                    color = busyTimeColor
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = appt.clientName,
                                    fontSize = (17 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = serviceDisplay,
                                    fontSize = (13 * fontScale).sp,
                                    color = Color.Gray
                                )
                            }

                            Row {
                                IconButton(onClick = { onEditClick(appt) }) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(22.dp))
                                }
                                IconButton(onClick = { onDeleteClick(appt) }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        elevation = 0.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
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
                                color = Color.Gray.copy(alpha = 0.35f),
                                fontSize = (14 * fontScale).sp
                            )
                        }
                    }
                }
            }
        }
    }
}