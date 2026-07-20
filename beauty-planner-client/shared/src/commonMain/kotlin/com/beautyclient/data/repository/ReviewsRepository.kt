package com.beautyclient.data.repository

import com.beautyclient.domain.models.MasterReview
import com.beautyclient.domain.models.PendingReviewPrompt
import com.beautyclient.domain.models.ReviewSubmission

/**
 * Handles review submission, retrieval, and pending review prompts.
 *
 * Rules enforced by implementations:
 * - A review can only be submitted once per [appointmentId].
 * - The appointment must have COMPLETED status.
 * - Masters can hide reviews from public display but cannot delete them.
 *
 * Future implementation will back this with a real backend API.
 */
interface ReviewsRepository {

    /** Returns all visible reviews for the given master (excludes hidden reviews). */
    suspend fun getReviewsForMaster(masterId: String): List<MasterReview>

    /** Submits a review. Throws if a review already exists for the appointment. */
    suspend fun submitReview(submission: ReviewSubmission, clientId: String, authorNickname: String): MasterReview

    /** Returns true if a review has already been submitted for the given appointment. */
    suspend fun hasReviewForAppointment(appointmentId: String): Boolean

    // --- Pending review prompts ---

    /** Returns all pending (non-dismissed) review prompts for the client. */
    suspend fun getPendingReviewPrompts(clientId: String): List<PendingReviewPrompt>

    /**
     * Snoozes a review prompt until [snoozeUntilMs].
     * The prompt will be shown again after that timestamp.
     */
    suspend fun snoozeReviewPrompt(promptId: String, snoozeUntilMs: Long)

    /** Permanently dismisses a review prompt (e.g. after review is submitted). */
    suspend fun dismissReviewPrompt(promptId: String)
}
