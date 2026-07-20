package com.beautyclient.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 *
 * Flow:
 *   Discover → MasterProfile → Services → DateTime → BookingForm → BookingConfirmation
 *                            ↘ Reviews → LeaveReview
 *
 * The [ReviewReminder] destination can be triggered from any screen when a
 * pending review prompt is active (shown as a modal over the current screen).
 */
sealed class Screen {

    /** Home screen — displays a list of masters, filterable by category. */
    data object Discover : Screen()

    /** Detailed profile of a single master. */
    data class MasterProfile(val masterId: String) : Screen()

    /** Services offered by a master — entry point to the booking flow. */
    data class Services(val masterId: String) : Screen()

    /** Date and time slot selection for a specific master and service. */
    data class DateTime(val masterId: String, val serviceId: String) : Screen()

    /** Final booking form — notes, confirmation details. */
    data class BookingForm(
        val masterId: String,
        val serviceId: String,
        val slotId: String
    ) : Screen()

    /** Booking submitted successfully. */
    data class BookingConfirmation(val bookingId: String) : Screen()

    /** Reviews list for a master. */
    data class Reviews(val masterId: String) : Screen()

    /** Leave a review for a completed appointment. */
    data class LeaveReview(val appointmentId: String, val masterId: String) : Screen()

    /**
     * Review reminder modal triggered from a [PendingReviewPrompt].
     * Overlay / bottom sheet that prompts the client to review or snooze.
     */
    data class ReviewReminder(val promptId: String) : Screen()
}
