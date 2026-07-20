package com.beautyclient.data.repository

import com.beautyclient.domain.models.AvailableSlot
import com.beautyclient.domain.models.MasterCategory
import com.beautyclient.domain.models.MasterProfile
import com.beautyclient.domain.models.MasterService

/**
 * Provides access to master profiles, their services, and available booking slots.
 *
 * Future implementation will back this with a real backend API.
 */
interface MastersRepository {

    /** Returns all active masters, optionally filtered by [category]. */
    suspend fun getMasters(category: MasterCategory? = null): List<MasterProfile>

    /** Returns a single master profile by [masterId], or null if not found. */
    suspend fun getMasterById(masterId: String): MasterProfile?

    /** Returns all active services offered by the given master. */
    suspend fun getServicesForMaster(masterId: String): List<MasterService>

    /** Returns available booking slots for a master, optionally filtered by date prefix (YYYY-MM). */
    suspend fun getAvailableSlots(masterId: String, monthPrefix: String? = null): List<AvailableSlot>
}
