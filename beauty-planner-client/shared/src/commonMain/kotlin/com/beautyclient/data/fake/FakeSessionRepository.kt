package com.beautyclient.data.fake

import com.beautyclient.data.repository.SessionRepository
import com.beautyclient.domain.models.ClientProfile

/**
 * In-memory fake implementation of [SessionRepository].
 * Starts with a pre-signed-in guest client so the app is immediately usable without auth.
 */
class FakeSessionRepository : SessionRepository {

    private var currentClient: ClientProfile? = ClientProfile(
        id = "client_guest",
        nickname = "Guest User",
        email = ""
    )

    override suspend fun getCurrentClient(): ClientProfile? = currentClient

    override suspend fun signInAsGuest(nickname: String): ClientProfile {
        val guest = ClientProfile(id = "client_guest_${nickname.hashCode()}", nickname = nickname)
        currentClient = guest
        return guest
    }

    override suspend fun signOut() {
        currentClient = null
    }

    override suspend fun isSignedIn(): Boolean = currentClient != null
}
