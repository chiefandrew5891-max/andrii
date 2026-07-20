package com.beautyclient.domain.models

/**
 * Public profile of a beauty master visible to clients.
 *
 * @param id           Unique master identifier (backend-assigned).
 * @param displayName  Name shown in the app (can be a business name or first name).
 * @param category     Primary specialty used for catalogue filtering.
 * @param bio          Short freeform description shown on the profile screen.
 * @param photoUrl     Avatar/photo URL (empty = default placeholder).
 * @param city         City where the master operates.
 * @param ratingAvg    Average rating from all visible reviews (0.0–5.0).
 * @param reviewCount  Total count of visible (non-hidden) reviews.
 * @param isVerified   Whether the master has been verified by the platform.
 */
data class MasterProfile(
    val id: String,
    val displayName: String,
    val category: MasterCategory,
    val bio: String = "",
    val photoUrl: String = "",
    val city: String = "",
    val ratingAvg: Float = 0f,
    val reviewCount: Int = 0,
    val isVerified: Boolean = false
)
