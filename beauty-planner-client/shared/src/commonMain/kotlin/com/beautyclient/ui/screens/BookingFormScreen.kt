package com.beautyclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyclient.di.AppModule
import com.beautyclient.domain.models.AvailableSlot
import com.beautyclient.domain.models.BookingRequest
import com.beautyclient.domain.models.BookingStatus
import com.beautyclient.domain.models.MasterService

/**
 * Booking Form screen — review details and add optional notes before confirming.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    masterId: String,
    serviceId: String,
    slotId: String,
    onBack: () -> Unit,
    onBookingSubmitted: (bookingId: String) -> Unit
) {
    var service by remember { mutableStateOf<MasterService?>(null) }
    var slot by remember { mutableStateOf<AvailableSlot?>(null) }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(masterId, serviceId, slotId) {
        service = AppModule.mastersRepository.getServicesForMaster(masterId).find { it.id == serviceId }
        slot = AppModule.mastersRepository.getAvailableSlots(masterId).find { it.id == slotId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm booking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            service?.let { svc ->
                Text("Service", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(svc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text("${svc.durationMinutes} min · ${svc.price} ${svc.currency}", style = MaterialTheme.typography.bodySmall)
            }
            slot?.let { s ->
                Spacer(Modifier.height(4.dp))
                Text("Date & Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${s.date} · ${s.startTime} – ${s.endTime}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes for master (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    isSubmitting = true
                },
                enabled = !isSubmitting && service != null && slot != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSubmitting) "Submitting…" else "Submit booking request")
            }
        }
    }

    // Submit booking after composing (LaunchedEffect triggers when isSubmitting flips)
    if (isSubmitting) {
        LaunchedEffect(Unit) {
            val client = AppModule.sessionRepository.getCurrentClient()
            val slotData = AppModule.mastersRepository.getAvailableSlots(masterId).find { it.id == slotId }
            val draft = BookingRequest(
                id = "",
                clientId = client?.id ?: "client_guest",
                masterId = masterId,
                serviceId = serviceId,
                slotId = slotId,
                date = slotData?.date ?: "",
                startTime = slotData?.startTime ?: "",
                notes = notes,
                status = BookingStatus.PENDING
            )
            val created = AppModule.bookingRepository.submitBooking(draft)
            isSubmitting = false
            onBookingSubmitted(created.id)
        }
    }
}
