package com.beautyclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyclient.di.AppModule
import com.beautyclient.domain.models.PendingReviewPrompt

/**
 * Review reminder modal/bottom sheet.
 * Shown when a [PendingReviewPrompt] is active.
 *
 * The client can:
 * - Review now (navigates to [LeaveReviewScreen])
 * - Snooze (reminds again after 24 h)
 * - Dismiss permanently
 */
@Composable
fun ReviewReminderModal(
    promptId: String,
    onReviewNow: (appointmentId: String, masterId: String) -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf<PendingReviewPrompt?>(null) }

    LaunchedEffect(promptId) {
        val client = AppModule.sessionRepository.getCurrentClient()
        prompt = AppModule.reviewsRepository
            .getPendingReviewPrompts(client?.id ?: "")
            .find { it.id == promptId }
    }

    prompt?.let { p ->
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("How did it go?", fontWeight = FontWeight.Bold) },
            text = {
                Text("You had an appointment with ${p.masterDisplayName} on ${p.appointmentDate}. Leave a review to help other clients!")
            },
            confirmButton = {
                Button(onClick = { onReviewNow(p.appointmentId, p.masterId) }) {
                    Text("Leave review")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        // Snooze for 24 h
                        val snoozeUntil = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
                        kotlinx.coroutines.MainScope().launch {
                            AppModule.reviewsRepository.snoozeReviewPrompt(p.id, snoozeUntil)
                        }
                        onSnooze()
                    }) { Text("Remind me later") }
                    TextButton(onClick = {
                        kotlinx.coroutines.MainScope().launch {
                            AppModule.reviewsRepository.dismissReviewPrompt(p.id)
                        }
                        onDismiss()
                    }) { Text("Dismiss") }
                }
            }
        )
    }
}
