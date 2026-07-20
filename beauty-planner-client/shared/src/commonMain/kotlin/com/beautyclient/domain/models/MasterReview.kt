package com.beautyclient.domain.models

/**
 * A review left by a client after a completed appointment.
 *
 * Rules:
 * - Only one review per appointment (enforced via [appointmentId]).
 * - Reviews can only be submitted after an appointment reaches COMPLETED status.
 * - Masters may set [hiddenByMaster] = true to hide the review from public display,
 *   but they cannot delete it or change its [rating].
 *
 * @param id              Unique review identifier.
 * @param appointmentId   Links this review to exactly one completed booking.
 * @param masterId        The master being reviewed.
 * @param clientId        The client who left the review.
 * @param authorNickname  Client's public display nickname at the time of submission.
 * @param rating          Star rating from 1 to 5.
 * @param comment         Optional text comment.
 * @param createdAtMs     Submission timestamp (epoch ms).
 * @param hiddenByMaster  If true, the review is not shown publicly (master request).
 */
data class MasterReview(
    val id: String,
    val appointmentId: String,
    val masterId: String,
    val clientId: String,
    val authorNickname: String,
    val rating: Int,
    val comment: String = "",
    val createdAtMs: Long = 0L,
    val hiddenByMaster: Boolean = false
)
