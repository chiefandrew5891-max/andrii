package com.beautyclient.domain.models

/**
 * A pending reminder shown to the client to leave a review after a completed appointment.
 *
 * Snooze / reminder behaviour:
 * - The prompt is shown once after an appointment is marked COMPLETED.
 * - The client can snooze it; [snoozeUntilMs] records when it should be shown again.
 * - After the review is submitted, [isDismissed] is set to true and the prompt is removed.
 *
 * @param id                Unique prompt identifier.
 * @param appointmentId     The appointment the review is about.
 * @param masterId          The master involved (used to pre-fill the review screen).
 * @param masterDisplayName Shown in the prompt so the client knows which master to review.
 * @param appointmentDate   Human-readable date string displayed in the prompt.
 * @param isDismissed       True once the review has been submitted or prompt permanently dismissed.
 * @param snoozeUntilMs     If non-null, do not show the prompt until after this timestamp (epoch ms).
 */
data class PendingReviewPrompt(
    val id: String,
    val appointmentId: String,
    val masterId: String,
    val masterDisplayName: String,
    val appointmentDate: String,
    val isDismissed: Boolean = false,
    val snoozeUntilMs: Long? = null
)
