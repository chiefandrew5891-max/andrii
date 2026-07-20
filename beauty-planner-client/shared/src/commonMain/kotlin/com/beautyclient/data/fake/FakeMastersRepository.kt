package com.beautyclient.data.fake

import com.beautyclient.data.repository.MastersRepository
import com.beautyclient.domain.models.AvailableSlot
import com.beautyclient.domain.models.MasterCategory
import com.beautyclient.domain.models.MasterProfile
import com.beautyclient.domain.models.MasterService

/**
 * In-memory fake implementation of [MastersRepository].
 * Provides a realistic set of mock masters, services, and available slots
 * so the app can run through the full booking flow without a backend.
 */
class FakeMastersRepository : MastersRepository {

    private val masters = listOf(
        MasterProfile(
            id = "master_1",
            displayName = "Anna K.",
            category = MasterCategory.HAIR,
            bio = "Hair colourist with 8 years of experience. Specialising in balayage and highlights.",
            city = "Kyiv",
            ratingAvg = 4.9f,
            reviewCount = 143,
            isVerified = true
        ),
        MasterProfile(
            id = "master_2",
            displayName = "Oksana Nails",
            category = MasterCategory.NAILS,
            bio = "Nail art studio. Gel manicure, pedicure, nail extensions.",
            city = "Kyiv",
            ratingAvg = 4.7f,
            reviewCount = 89,
            isVerified = true
        ),
        MasterProfile(
            id = "master_3",
            displayName = "Daria Brows",
            category = MasterCategory.BROWS,
            bio = "Certified brow artist. Microblading, lamination, and tinting.",
            city = "Lviv",
            ratingAvg = 4.8f,
            reviewCount = 61,
            isVerified = false
        ),
        MasterProfile(
            id = "master_4",
            displayName = "Lash Studio by Maria",
            category = MasterCategory.LASHES,
            bio = "Classic, volume, and mega-volume lash extensions.",
            city = "Kyiv",
            ratingAvg = 4.6f,
            reviewCount = 34,
            isVerified = false
        ),
        MasterProfile(
            id = "master_5",
            displayName = "Sofiya Beauty",
            category = MasterCategory.MAKEUP,
            bio = "Bridal and event makeup. Portfolio available on Instagram.",
            city = "Kharkiv",
            ratingAvg = 5.0f,
            reviewCount = 22,
            isVerified = true
        )
    )

    private val services = mapOf(
        "master_1" to listOf(
            MasterService("svc_1_1", "master_1", "Balayage", durationMinutes = 180, price = "90", currency = "EUR"),
            MasterService("svc_1_2", "master_1", "Root colour", durationMinutes = 90, price = "55", currency = "EUR"),
            MasterService("svc_1_3", "master_1", "Haircut & blowdry", durationMinutes = 60, price = "35", currency = "EUR")
        ),
        "master_2" to listOf(
            MasterService("svc_2_1", "master_2", "Gel manicure", durationMinutes = 60, price = "30", currency = "EUR"),
            MasterService("svc_2_2", "master_2", "Pedicure", durationMinutes = 75, price = "35", currency = "EUR"),
            MasterService("svc_2_3", "master_2", "Nail art (per nail)", durationMinutes = 10, price = "3", currency = "EUR")
        ),
        "master_3" to listOf(
            MasterService("svc_3_1", "master_3", "Brow lamination", durationMinutes = 60, price = "40", currency = "EUR"),
            MasterService("svc_3_2", "master_3", "Microblading", durationMinutes = 120, price = "120", currency = "EUR"),
            MasterService("svc_3_3", "master_3", "Brow tinting", durationMinutes = 30, price = "20", currency = "EUR")
        ),
        "master_4" to listOf(
            MasterService("svc_4_1", "master_4", "Classic lashes", durationMinutes = 90, price = "50", currency = "EUR"),
            MasterService("svc_4_2", "master_4", "Volume lashes", durationMinutes = 120, price = "70", currency = "EUR")
        ),
        "master_5" to listOf(
            MasterService("svc_5_1", "master_5", "Daytime makeup", durationMinutes = 60, price = "60", currency = "EUR"),
            MasterService("svc_5_2", "master_5", "Bridal makeup", durationMinutes = 90, price = "100", currency = "EUR")
        )
    )

    private val slots = mapOf(
        "master_1" to listOf(
            AvailableSlot("slot_1_1", "master_1", "2025-08-05", "10:00", "12:00"),
            AvailableSlot("slot_1_2", "master_1", "2025-08-05", "14:00", "16:00"),
            AvailableSlot("slot_1_3", "master_1", "2025-08-06", "11:00", "13:00"),
            AvailableSlot("slot_1_4", "master_1", "2025-08-08", "09:00", "11:00")
        ),
        "master_2" to listOf(
            AvailableSlot("slot_2_1", "master_2", "2025-08-04", "12:00", "13:00"),
            AvailableSlot("slot_2_2", "master_2", "2025-08-04", "15:00", "16:00"),
            AvailableSlot("slot_2_3", "master_2", "2025-08-07", "10:00", "11:15")
        ),
        "master_3" to listOf(
            AvailableSlot("slot_3_1", "master_3", "2025-08-05", "13:00", "14:00"),
            AvailableSlot("slot_3_2", "master_3", "2025-08-06", "16:00", "17:00")
        ),
        "master_4" to listOf(
            AvailableSlot("slot_4_1", "master_4", "2025-08-06", "10:00", "11:30"),
            AvailableSlot("slot_4_2", "master_4", "2025-08-09", "12:00", "14:00")
        ),
        "master_5" to listOf(
            AvailableSlot("slot_5_1", "master_5", "2025-08-07", "14:00", "15:00"),
            AvailableSlot("slot_5_2", "master_5", "2025-08-10", "11:00", "12:30")
        )
    )

    override suspend fun getMasters(category: MasterCategory?): List<MasterProfile> {
        return if (category == null) masters else masters.filter { it.category == category }
    }

    override suspend fun getMasterById(masterId: String): MasterProfile? {
        return masters.find { it.id == masterId }
    }

    override suspend fun getServicesForMaster(masterId: String): List<MasterService> {
        return services[masterId]?.filter { it.isActive } ?: emptyList()
    }

    override suspend fun getAvailableSlots(masterId: String, monthPrefix: String?): List<AvailableSlot> {
        val masterSlots = slots[masterId] ?: return emptyList()
        return if (monthPrefix != null) {
            masterSlots.filter { it.date.startsWith(monthPrefix) }
        } else {
            masterSlots
        }
    }
}
