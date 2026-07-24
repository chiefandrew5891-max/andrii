package com.andrey.beautyplanner.remote

expect object BackendBridge {
    suspend fun ensureAuthenticated(): String

    suspend fun checkAppUpdate(
        platform: String,
        versionName: String,
        buildNumber: String
    ): Map<String, String>

    suspend fun bootstrapUser(
        installId: String,
        firebaseUid: String,
        platform: String,
        authProvider: String,
        email: String,
        displayName: String
    ): AccessStatusResponse

    suspend fun verifySubscription(
        userId: String,
        productId: String,
        purchaseToken: String,
        platform: String = "PLAY",
        transactionId: String = ""
    ): AccessStatusResponse

    suspend fun getAccessStatus(userId: String): AccessStatusResponse

    suspend fun syncIdentity(
        firebaseUid: String,
        email: String,
        displayName: String,
        authProvider: String
    ): AccessStatusResponse

    suspend fun syncMasterProfile(
        userId: String,
        ownerName: String,
        profileDisplayCustomName: Boolean,
        profilePhone: String,
        profilePhoneVisible: Boolean,
        profileSpecialization: String,
        profileRating: Float,
        profileAvatarUrl: String,
        profileAvatarBase64: String,
        clientInteractionsEnabled: Boolean,
        serviceTemplatesJson: String
    ): Map<String, String>
}