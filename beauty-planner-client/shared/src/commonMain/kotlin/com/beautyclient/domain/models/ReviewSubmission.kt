package com.beautyclient.domain.models

/**
 * Form data submitted when a client leaves a new review.
 *
 * The server/repository validates that:
 * - the appointment is COMPLETED
 * - no review exists yet for this [appointmentId]
 *
 * @param appointmentId  The completed appointment being reviewed.
 * @param masterId       The master being reviewed.
 * @param rating         Star rating (1–5).
 * @param comment        Optional text comment.
 */
data class ReviewSubmission(
    val appointmentId: String,
    val masterId: String,
    val rating: Int,
    val comment: String = ""
)
