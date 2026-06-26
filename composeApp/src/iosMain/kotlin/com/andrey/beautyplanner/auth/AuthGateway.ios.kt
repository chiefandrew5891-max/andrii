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

    actual suspend fun signOut() {
        // TODO: implement iOS sign out
    }

    actual suspend fun clearCredentialState() {
        // TODO: implement iOS credential cleanup if needed
    }
}