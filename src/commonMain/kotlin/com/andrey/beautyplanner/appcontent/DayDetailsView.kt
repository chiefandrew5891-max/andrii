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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formattedDate, fontSize = (20 * fontScale).sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { onDateChange(date.minus(1, DateTimeUnit.DAY)) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = { onDateChange(date.plus(1, DateTimeUnit.DAY)) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        val timeSlots = (8..20).map { "${if (it < 10) "0$it" else it}:00" }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(timeSlots.size) { index ->
                val time = timeSlots[index]

                val isOverlapped = appointments.any { a ->
                    if (a.dateString != date.toString()) return@any false
                    val startIdx = timeSlots.indexOf(a.time)
                    index > startIdx && index < (startIdx + a.durationHours)
                }
                if (isOverlapped) return@items

                val currentAppt =
                    appointments.find { it.dateString == date.toString() && it.time == time }

                val interactionSource = remember { MutableInteractionSource() }

                Card(
                    elevation = 0.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .height(85.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = {
                                if (currentAppt == null) onTimeClick(time) else onEditClick(currentAppt)
                            }
                        ),
                    backgroundColor = if (currentAppt != null) {
                        // dark оставляем как было, light делаем серым вместо голубого
                        if (AppSettings.isDarkMode) Color(0xFF253548) else Color(0xFFF2F2F2)
                    } else Color.Transparent,
                    border = if (currentAppt == null)
                        BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                    else null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.width(75.dp)) {
                            Text(
                                text = time,
                                fontSize = (16 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAppt != null) MaterialTheme.colors.primary else Color.Gray
                            )
                            if (currentAppt != null) {
                                val endIdx = index + currentAppt.durationHours
                                val endTime =
                                    if (endIdx < timeSlots.size) timeSlots[endIdx] else "Конец"

                                Divider(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .width(44.dp),
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
                        }

                        if (currentAppt != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = currentAppt.clientName,
                                    fontSize = (17 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${currentAppt.serviceName} • ${currentAppt.durationHours} ч.",
                                    fontSize = (13 * fontScale).sp,
                                    color = Color.Gray
                                )
                            }
                            Row {
                                IconButton(onClick = { onEditClick(currentAppt) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        null,
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteClick(currentAppt) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = Locales.t("free"),
                                color = Color.Gray.copy(alpha = 0.4f),
                                fontSize = (14 * fontScale).sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}