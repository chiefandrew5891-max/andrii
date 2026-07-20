package com.beautyclient.domain.models

/**
 * A service offered by a master.
 *
 * @param id              Unique service identifier.
 * @param masterId        The master who offers this service.
 * @param title           Service name shown in the list.
 * @param description     Optional longer description.
 * @param durationMinutes Estimated duration in minutes.
 * @param price           Price amount as a string (e.g. "50", "50.00").
 * @param currency        ISO 4217 currency code (e.g. "EUR", "USD", "UAH").
 * @param isActive        Whether this service is currently available for booking.
 */
data class MasterService(
    val id: String,
    val masterId: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int,
    val price: String,
    val currency: String = "EUR",
    val isActive: Boolean = true
)
