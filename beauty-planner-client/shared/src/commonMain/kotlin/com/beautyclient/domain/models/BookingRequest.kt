package com.beautyclient.domain.models

/**
 * Possible states of a booking after submission.
 */
enum class BookingStatus {
    PENDING,    // Submitted, awaiting master confirmation
    CONFIRMED,  // Confirmed by the master
    COMPLETED,  // Appointment has taken place
    CANCELLED   // Cancelled by client or master
}

/**
 * A booking request submitted by the client.
 *
 * @param id            Unique booking identifier (backend-assigned or local UUID).
 * @param clientId      Client who submitted the request.
 * @param masterId      Master being booked.
 * @param serviceId     The service being requested.
 * @param slotId        The slot being reserved.
 * @param date          Date of the appointment (YYYY-MM-DD).
 * @param startTime     Start time (HH:MM).
 * @param notes         Optional client notes for the master.
 * @param status        Current booking state.
 * @param createdAtMs   Timestamp when the booking was submitted (epoch ms).
 */
data class BookingRequest(
    val id: String,
    val clientId: String,
    val masterId: String,
    val serviceId: String,
    val slotId: String,
    val date: String,
    val startTime: String,
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val createdAtMs: Long = 0L
)
