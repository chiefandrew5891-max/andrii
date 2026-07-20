package com.beautyclient.domain.models

/**
 * A free date/time slot that the master has made available for booking.
 *
 * @param id        Unique slot identifier.
 * @param masterId  The master this slot belongs to.
 * @param date      Date in ISO format: YYYY-MM-DD.
 * @param startTime Start time in HH:MM (24-hour local time).
 * @param endTime   End time in HH:MM (24-hour local time).
 */
data class AvailableSlot(
    val id: String,
    val masterId: String,
    val date: String,
    val startTime: String,
    val endTime: String
)
