package com.andrey.beautyplanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppointmentLimitUtilsTest {

    @Test
    fun visibleAppointmentsCount_ignoresSoftDeletedAppointments() {
        val appointments = listOf(
            Appointment(
                id = "1",
                dateString = "2026-07-23",
                time = "10:00",
                clientName = "A",
                phone = "",
                serviceName = "Service",
                price = "10"
            ),
            Appointment(
                id = "2",
                dateString = "2026-07-23",
                time = "11:00",
                clientName = "B",
                phone = "",
                serviceName = "Service",
                price = "10",
                isDeleted = true
            ),
            Appointment(
                id = "3",
                dateString = "2026-07-23",
                time = "12:00",
                clientName = "C",
                phone = "",
                serviceName = "Service",
                price = "10"
            )
        )

        assertEquals(2, AppointmentSyncUtils.visibleAppointmentsCount(appointments))
    }

    @Test
    fun shouldShowFreeLimitWarning_onlyForConfiguredThresholds() {
        assertTrue(AccessManager.shouldShowFreeLimitWarning(5))
        assertTrue(AccessManager.shouldShowFreeLimitWarning(3))
        assertTrue(AccessManager.shouldShowFreeLimitWarning(1))
        assertFalse(AccessManager.shouldShowFreeLimitWarning(4))
        assertFalse(AccessManager.shouldShowFreeLimitWarning(2))
        assertFalse(AccessManager.shouldShowFreeLimitWarning(0))
    }
}
