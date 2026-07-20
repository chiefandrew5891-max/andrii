package com.beautyclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beautyclient.di.AppModule
import com.beautyclient.domain.models.ReviewSubmission

/**
 * Leave a Review screen — shown after a completed appointment.
 *
 * Rules:
 * - Only one review per appointment (enforced by [ReviewsRepository]).
 * - The client submits a star rating (1–5) and optional text comment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewScreen(
    appointmentId: String,
    masterId: String,
    onBack: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var alreadyReviewed by remember { mutableStateOf(false) }

    LaunchedEffect(appointmentId) {
        alreadyReviewed = AppModule.reviewsRepository.hasReviewForAppointment(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a review") },
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
            if (alreadyReviewed) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You have already reviewed this appointment.", style = MaterialTheme.typography.bodyMedium)
                }
                return@Column
            }

            Text("Your rating", style = MaterialTheme.typography.titleMedium)

            // Star selector
            Row {
                for (star in 1..5) {
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { isSubmitting = true },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSubmitting) "Submitting…" else "Submit review")
            }
        }
    }

    if (isSubmitting) {
        LaunchedEffect(Unit) {
            val client = AppModule.sessionRepository.getCurrentClient()
            val submission = ReviewSubmission(
                appointmentId = appointmentId,
                masterId = masterId,
                rating = rating,
                comment = comment
            )
            AppModule.reviewsRepository.submitReview(
                submission = submission,
                clientId = client?.id ?: "client_guest",
                authorNickname = client?.nickname ?: "Anonymous"
            )
            // Dismiss pending review prompt if present
            val prompts = AppModule.reviewsRepository.getPendingReviewPrompts(client?.id ?: "")
            prompts.find { it.appointmentId == appointmentId }?.let {
                AppModule.reviewsRepository.dismissReviewPrompt(it.id)
            }
            isSubmitting = false
            onReviewSubmitted()
        }
    }
}
