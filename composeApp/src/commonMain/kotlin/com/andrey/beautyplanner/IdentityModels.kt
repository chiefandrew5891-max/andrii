package com.andrey.beautyplanner

import kotlinx.serialization.Serializable

@Serializable
data class UserIdentitySnapshot(
    val installId: String = "",
    val backendUserId: String = ""
)