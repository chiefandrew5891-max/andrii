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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.Appointment

@Composable
fun BookingDialog(
    time: String,
    initialData: Appointment?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int) -> Unit,
    onTransferRequest: (Appointment) -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    var name by remember { mutableStateOf(initialData?.clientName ?: "") }
    var phone by remember { mutableStateOf(initialData?.phone ?: "") }
    var service by remember { mutableStateOf(initialData?.serviceName ?: "") }
    var price by remember { mutableStateOf(initialData?.price ?: "") }
    var hours by remember { mutableStateOf(initialData?.durationHours ?: 1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = 12.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (initialData != null) Locales.t("save") else Locales.t("nav_day"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (22 * fontScale).sp,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    "${Locales.t("start_time")}: $time",
                    fontSize = (14 * fontScale).sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Locales.t("client_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colors.primary)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Locales.t("phone")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colors.primary)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text(Locales.t("service")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Face, null, tint = MaterialTheme.colors.primary)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(Locales.t("price") + " (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colors.primary)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    Locales.t("duration_hours"),
                    fontSize = (14 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = { if (hours > 1) hours-- }) {
                        Icon(
                            Icons.Default.RemoveCircleOutline,
                            null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    Text(
                        text = hours.toString(),
                        fontSize = (20 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { if (hours < 5) hours++ }) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(name, phone, service, price, hours) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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