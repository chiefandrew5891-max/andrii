package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun StatsDatePickerDialog(
    title: String,
    initialSelectedDate: LocalDate,
    initialMonthDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val fontScale = AppSettings.getFontScale()

    var monthDate by remember {
        mutableStateOf(LocalDate(initialMonthDate.year, initialMonthDate.month, 1))
    }
    var selectedDate by remember { mutableStateOf(initialSelectedDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${monthDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${monthDate.year}",
                        fontWeight = FontWeight.Bold,
                        fontSize = (16 * fontScale).sp
                    )

                    Row {
                        IconButton(onClick = { monthDate = monthDate.minus(1, DateTimeUnit.MONTH) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                        }
                        IconButton(onClick = { monthDate = monthDate.plus(1, DateTimeUnit.MONTH) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                MonthCalendarGrid(
                    monthDate = monthDate,
                    today = initialSelectedDate,
                    selectedDate = selectedDate,
                    onDateClick = { selectedDate = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate) }) {
                Text(Locales.t("confirm"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Locales.t("cancel"))
            }
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}