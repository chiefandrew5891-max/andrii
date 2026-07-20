package com.beautyclient.data.repository

import com.beautyclient.domain.models.ClientProfile

/**
 * Manages client session state and profile.
 *
 * Future implementation will integrate with a real auth provider
 * (Google Sign-In, Apple Sign-In, email/password, anonymous).
 */
interface SessionRepository {

    /** Returns the current client profile, or null if not signed in. */
    suspend fun getCurrentClient(): ClientProfile?

    /** Signs in as a guest with a generated display nickname. Returns the guest profile. */
    suspend fun signInAsGuest(nickname: String): ClientProfile

    /** Signs out and clears the current session. */
    suspend fun signOut()

    /** Returns true if the client is currently signed in (including guest). */
    suspend fun isSignedIn(): Boolean
}
