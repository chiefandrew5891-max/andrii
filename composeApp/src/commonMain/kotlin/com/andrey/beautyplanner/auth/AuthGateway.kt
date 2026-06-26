package com.andrey.beautyplanner.auth

expect object AuthGateway {
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signInAnonymously(): SignInResult
    suspend fun signInWithGoogle(): SignInResult
    suspend fun signInWithEmail(email: String, password: String): SignInResult
    suspend fun registerWithEmail(email: String, password: String): SignInResult
    suspend fun signOut()
    suspend fun clearCredentialState()
}