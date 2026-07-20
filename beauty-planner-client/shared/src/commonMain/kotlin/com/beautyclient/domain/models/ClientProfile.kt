package com.beautyclient.domain.models

/**
 * The client's own identity within the app.
 *
 * @param id          Unique client identifier.
 * @param nickname    Public display name used in reviews (visible to masters and other clients).
 * @param email       Email address (used for auth, not displayed publicly).
 * @param photoUrl    Optional avatar URL.
 */
data class ClientProfile(
    val id: String,
    val nickname: String,
    val email: String = "",
    val photoUrl: String = ""
)
