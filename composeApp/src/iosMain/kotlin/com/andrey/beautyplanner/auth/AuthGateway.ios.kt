package com.andrey.beautyplanner.auth

actual object AuthGateway {
    actual suspend fun getCurrentUser(): AuthUser? {
        return null
    }

    actual suspend fun signInAnonymously(): SignInResult {
        return SignInResult.Error(
            "TODO: implement Firebase anonymous auth / Sign in with Apple / Google Sign-In on iOS"
        )
    }

    actual suspend fun signInWithGoogle(): SignInResult {
        return SignInResult.Error(
            "TODO: implement Google Sign-In on iOS"
        )
    }

    actual suspend fun signInWithEmail(email: String, password: String): SignInResult {
        return SignInResult.Error(
            "TODO: implement Firebase email/password auth on iOS"
        )
    }

    actual suspend fun registerWithEmail(email: String, password: String): SignInResult {
        return SignInResult.Error(
            "TODO: implement Firebase email/password registration on iOS"
        )
    }

    actual suspend fun signOut() {
    }

    actual suspend fun clearCredentialState() {
    }
}