package com.andrey.beautyplanner.remote

import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.CloudSyncJson
import kotlinx.serialization.encodeToString

/**
 * Convenience helper for triggering a master-profile backend sync from
 * anywhere in the app.
 *
 * Reads the current profile values directly from [AppSettings] so callers
 * do not need to gather and pass every field individually.
 *
 * This is intentionally separate from the existing auth/access/subscription
 * identity flow (bootstrapUser, syncIdentity, etc.).
 */
object MasterProfileSync {

    /**
     * Synchronize the current master profile to the backend `masters/{userId}`
     * Firestore document.
     *
     * Silently returns a successful empty result when [AppSettings.backendUserId]
     * is blank (i.e. the user has not yet completed backend authentication).
     *
     * Any backend error is caught and returned as a [Result.failure] so
     * callers can decide whether to surface it to the user.
     */
    suspend fun syncIfAuthenticated(): Result<Map<String, String>> {
        val userId = AppSettings.backendUserId.trim()
        if (userId.isBlank()) return Result.success(emptyMap())

        return runCatching {
            val serviceTemplatesJson = CloudSyncJson.json.encodeToString(AppSettings.serviceTemplates)
            BackendBridge.syncMasterProfile(
                userId = userId,
                ownerName = AppSettings.ownerName,
                profileDisplayCustomName = AppSettings.profileDisplayCustomName,
                profilePhone = AppSettings.profilePhone,
                profilePhoneVisible = AppSettings.profilePhoneVisible,
                profileSpecialization = AppSettings.profileSpecialization,
                profileRating = AppSettings.profileRating,
                profileAvatarUrl = AppSettings.profileAvatarUrl,
                profileAvatarBase64 = AppSettings.profileAvatarBase64,
                clientInteractionsEnabled = AppSettings.clientInteractionsEnabled,
                serviceTemplatesJson = serviceTemplatesJson
            )
        }
    }
}
