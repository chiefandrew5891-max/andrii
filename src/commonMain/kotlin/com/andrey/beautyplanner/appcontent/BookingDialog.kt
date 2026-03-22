package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.ServicesCatalog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookingDialog(
    time: String,
    initialData: Appointment?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Int) -> Unit, // time, name, phone, serviceKey, price, hours
    onTransferRequest: (Appointment) -> Unit
) {
    val fontScale = AppSettings.getFontScale()

    // --------- state ----------
    var name by remember { mutableStateOf(initialData?.clientName ?: "") }
    var phone by remember { mutableStateOf(initialData?.phone ?: "") }
    var serviceKey by remember { mutableStateOf(initialData?.serviceName ?: "") } // service_* key
    var price by remember { mutableStateOf(initialData?.price ?: "") }
    var hours by remember { mutableStateOf(initialData?.durationHours ?: 1) }

    // Time (hour fixed from base time, minutes chosen from dropdown)
    val initialTime = remember(time, initialData) { initialData?.time ?: time }
    val baseHour = remember(initialTime) { initialTime.substringBefore(":").toIntOrNull() ?: 0 }
    val initialMin = remember(initialTime) { initialTime.substringAfter(":", "00").toIntOrNull() ?: 0 }

    // Minutes dropdown: кратно 10 (10, 20, 30, 40, 50) + 00
    // Если хочешь без 00 — уберём, но обычно удобно оставить.
    val minuteOptions = remember { listOf(0, 10, 20, 30, 40, 50) }
    var minutes by remember {
        mutableStateOf(
            // если в базе уже стояло 22 — округлим к ближайшему вниз (20)
            minuteOptions.lastOrNull { it <= initialMin } ?: 0
        )
    }

    val finalTime = remember(baseHour, minutes) {
        val hh = baseHour.toString().padStart(2, '0')
        val mm = minutes.toString().padStart(2, '0')
        "$hh:$mm"
    }

    // --------- validation ----------
    var triedSave by remember { mutableStateOf(false) }

    val nameOk = name.trim().isNotBlank()
    val phoneOk = phone.trim().isNotBlank()
    val serviceOk = serviceKey.trim().isNotBlank()

    // price можешь сделать обязательным/необязательным — пока считаю обязательным
    val priceOk = price.trim().isNotBlank()

    val formOk = nameOk && phoneOk && serviceOk && priceOk

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = 12.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (initialData != null) Locales.t("save") else Locales.t("nav_day"),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (22 * fontScale).sp,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            "${Locales.t("start_time")}: $finalTime",
                            fontSize = (14 * fontScale).sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --------- Time + Duration block (duration moved up) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time picker (hour fixed + minutes dropdown)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Locales.t("start_time"),
                            fontSize = (12 * fontScale).sp,
                            color = Color.Gray
                        )

                        var minutesExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = minutesExpanded,
                            onExpandedChange = { minutesExpanded = !minutesExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${baseHour.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minutesExpanded) },
                                shape = RoundedCornerShape(14.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = minutesExpanded,
                                onDismissRequest = { minutesExpanded = false }
                            ) {
                                minuteOptions.forEach { m ->
                                    DropdownMenuItem(onClick = {
                                        minutes = m
                                        minutesExpanded = false
                                    }) {
                                        Text(text = "${baseHour.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}")
                                    }
                                }
                            }
                        }
                    }

                    // Duration hours (moved up next to time)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Locales.t("duration_hours"),
                            fontSize = (12 * fontScale).sp,
                            color = Color.Gray
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (hours > 1) hours-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colors.primary)
                            }
                            Text(
                                text = hours.toString(),
                                fontSize = (18 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(onClick = { if (hours < 5) hours++ }) {
                                Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colors.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --------- Name ----------
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Locales.t("client_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colors.primary) },
                    isError = triedSave && !nameOk
                )
                if (triedSave && !nameOk) {
                    Text(
                        text = "Обязательно",
                        color = MaterialTheme.colors.error,
                        fontSize = (12 * fontScale).sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --------- Phone ----------
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Locales.t("phone")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colors.primary) },
                    isError = triedSave && !phoneOk
                )
                if (triedSave && !phoneOk) {
                    Text(
                        text = "Обязательно",
                        color = MaterialTheme.colors.error,
                        fontSize = (12 * fontScale).sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --------- Service dropdown ----------
                var serviceExpanded by remember { mutableStateOf(false) }
                val services = remember { ServicesCatalog.keys }

                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded }
                ) {
                    val displayText = if (serviceKey.isBlank()) "" else Locales.t(serviceKey)

                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Locales.t("service")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Default.Brush, null, tint = MaterialTheme.colors.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        isError = triedSave && !serviceOk
                    )

                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false }
                    ) {
                        services.forEach { key ->
                            DropdownMenuItem(onClick = {
                                serviceKey = key
                                serviceExpanded = false
                            }) {
                                Text(Locales.t(key))
                            }
                        }
                    }
                }
                if (triedSave && !serviceOk) {
                    Text(
                        text = "Обязательно",
                        color = MaterialTheme.colors.error,
                        fontSize = (12 * fontScale).sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --------- Price ----------
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(Locales.t("price") + " (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colors.primary) },
                    isError = triedSave && !priceOk
                )
                if (triedSave && !priceOk) {
                    Text(
                        text = "Обязательно",
                        color = MaterialTheme.colors.error,
                        fontSize = (12 * fontScale).sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Global hint
                if (triedSave && !formOk) {
                    Text(
                        text = "Заполните обязательные поля",
                        color = MaterialTheme.colors.error,
                        fontSize = (13 * fontScale).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        triedSave = true
                        if (!formOk) return@Button
                        onSave(finalTime, name.trim(), phone.trim(), serviceKey.trim(), price.trim(), hours)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Text(
                        Locales.t("save").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (initialData != null) {
                    TextButton(
                        onClick = { onTransferRequest(initialData) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(Locales.t("transfer_appt"), color = MaterialTheme.colors.primary)
                    }
                }
            }
        }
    }
}