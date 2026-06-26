package com.andrey.beautyplanner.auth

expect object AuthGateway {
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signInAnonymously(): SignInResult
    suspend fun signInWithGoogle(): SignInResult
    suspend fun signOut()
    suspend fun clearCredentialState()
}