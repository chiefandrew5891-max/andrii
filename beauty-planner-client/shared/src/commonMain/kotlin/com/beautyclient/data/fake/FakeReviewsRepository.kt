package com.beautyclient.data.fake

import com.beautyclient.data.repository.ReviewsRepository
import com.beautyclient.domain.models.MasterReview
import com.beautyclient.domain.models.PendingReviewPrompt
import com.beautyclient.domain.models.ReviewSubmission

/**
 * In-memory fake implementation of [ReviewsRepository].
 * Provides a pre-seeded set of reviews and one pending review prompt for the demo.
 */
class FakeReviewsRepository : ReviewsRepository {

    private val reviews = mutableListOf(
        MasterReview(
            id = "review_1", appointmentId = "appt_old_1",
            masterId = "master_1", clientId = "client_a",
            authorNickname = "Julia S.", rating = 5,
            comment = "Absolutely loved the result! Anna is incredibly talented.",
            createdAtMs = 1_720_000_000_000L
        ),
        MasterReview(
            id = "review_2", appointmentId = "appt_old_2",
            masterId = "master_1", clientId = "client_b",
            authorNickname = "Maria P.", rating = 5,
            comment = "Best balayage I've ever had. Will definitely come back!",
            createdAtMs = 1_718_000_000_000L
        ),
        MasterReview(
            id = "review_3", appointmentId = "appt_old_3",
            masterId = "master_1", clientId = "client_c",
            authorNickname = "Olha K.", rating = 4,
            comment = "Great result, was a bit longer than expected but worth it.",
            createdAtMs = 1_716_000_000_000L
        ),
        MasterReview(
            id = "review_4", appointmentId = "appt_old_4",
            masterId = "master_2", clientId = "client_a",
            authorNickname = "Julia S.", rating = 5,
            comment = "Perfect gel nails, great nail art design.",
            createdAtMs = 1_715_000_000_000L
        ),
        MasterReview(
            id = "review_5", appointmentId = "appt_old_5",
            masterId = "master_3", clientId = "client_b",
            authorNickname = "Maria P.", rating = 5,
            comment = "Brow lamination looks so natural. Very happy!",
            createdAtMs = 1_714_000_000_000L
        )
    )

    // One pending prompt so the review reminder flow can be demoed
    private val prompts = mutableListOf(
        PendingReviewPrompt(
            id = "prompt_1",
            appointmentId = "booking_demo_1",
            masterId = "master_1",
            masterDisplayName = "Anna K.",
            appointmentDate = "20.07.2025"
        )
    )

    private var reviewIdCounter = 100

    override suspend fun getReviewsForMaster(masterId: String): List<MasterReview> {
        return reviews.filter { it.masterId == masterId && !it.hiddenByMaster }
    }

    override suspend fun submitReview(
        submission: ReviewSubmission,
        clientId: String,
        authorNickname: String
    ): MasterReview {
        if (hasReviewForAppointment(submission.appointmentId)) {
            error("A review for appointment ${submission.appointmentId} already exists.")
        }
        val review = MasterReview(
            id = "review_${++reviewIdCounter}",
            appointmentId = submission.appointmentId,
            masterId = submission.masterId,
            clientId = clientId,
            authorNickname = authorNickname,
            rating = submission.rating,
            comment = submission.comment,
            createdAtMs = System.currentTimeMillis()
        )
        reviews.add(review)
        return review
    }

    override suspend fun hasReviewForAppointment(appointmentId: String): Boolean {
        return reviews.any { it.appointmentId == appointmentId }
    }

    override suspend fun getPendingReviewPrompts(clientId: String): List<PendingReviewPrompt> {
        return prompts.filter { !it.isDismissed }
    }

    override suspend fun snoozeReviewPrompt(promptId: String, snoozeUntilMs: Long) {
        val index = prompts.indexOfFirst { it.id == promptId }
        if (index != -1) prompts[index] = prompts[index].copy(snoozeUntilMs = snoozeUntilMs)
    }

    override suspend fun dismissReviewPrompt(promptId: String) {
        val index = prompts.indexOfFirst { it.id == promptId }
        if (index != -1) prompts[index] = prompts[index].copy(isDismissed = true)
    }
}
