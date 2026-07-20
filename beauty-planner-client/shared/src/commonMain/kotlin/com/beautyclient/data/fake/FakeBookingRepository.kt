package com.beautyclient.data.fake

import com.beautyclient.data.repository.BookingRepository
import com.beautyclient.domain.models.BookingRequest
import com.beautyclient.domain.models.BookingStatus

/**
 * In-memory fake implementation of [BookingRepository].
 * Bookings are stored in a mutable list for the lifetime of the app session.
 */
class FakeBookingRepository : BookingRepository {

    private val bookings = mutableListOf(
        // Pre-seeded completed booking so the review flow can be demoed immediately
        BookingRequest(
            id = "booking_demo_1",
            clientId = "client_guest",
            masterId = "master_1",
            serviceId = "svc_1_3",
            slotId = "slot_1_1",
            date = "2025-07-20",
            startTime = "10:00",
            notes = "Please use a light touch on the fringe",
            status = BookingStatus.COMPLETED,
            createdAtMs = 1_721_000_000_000L
        )
    )

    private var idCounter = 1000

    override suspend fun submitBooking(booking: BookingRequest): BookingRequest {
        val assigned = booking.copy(id = "booking_${++idCounter}")
        bookings.add(assigned)
        return assigned
    }

    override suspend fun getBookingsForClient(
        clientId: String,
        status: BookingStatus?
    ): List<BookingRequest> {
        return bookings.filter { b ->
            b.clientId == clientId && (status == null || b.status == status)
        }
    }

    override suspend fun getBookingById(bookingId: String): BookingRequest? {
        return bookings.find { it.id == bookingId }
    }

    override suspend fun cancelBooking(bookingId: String): BookingRequest {
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index == -1) error("Booking $bookingId not found")
        val cancelled = bookings[index].copy(status = BookingStatus.CANCELLED)
        bookings[index] = cancelled
        return cancelled
    }
}
