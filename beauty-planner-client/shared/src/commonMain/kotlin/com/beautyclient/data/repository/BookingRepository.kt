package com.beautyclient.data.repository

import com.beautyclient.domain.models.BookingRequest
import com.beautyclient.domain.models.BookingStatus

/**
 * Handles creation and retrieval of booking requests.
 *
 * Future implementation will back this with a real backend API.
 */
interface BookingRepository {

    /** Submits a new booking and returns the created [BookingRequest] with an assigned ID. */
    suspend fun submitBooking(booking: BookingRequest): BookingRequest

    /** Returns all bookings for the given client, optionally filtered by [status]. */
    suspend fun getBookingsForClient(clientId: String, status: BookingStatus? = null): List<BookingRequest>

    /** Returns a single booking by ID, or null if not found. */
    suspend fun getBookingById(bookingId: String): BookingRequest?

    /** Cancels an existing booking. Returns the updated booking. */
    suspend fun cancelBooking(bookingId: String): BookingRequest
}
