package com.andrey.beautyplanner.auth

import kotlinx.coroutines.CompletableDeferred

actual object AuthGateway {
    private var localUser: AuthUser? = null

    actual suspend fun getCurrentUser(): AuthUser? {
        return localUser
    }

    actual suspend fun signInAnonymously(): SignInResult {
        val existing = localUser
        if (existing != null) return SignInResult.Success(existing)

        val user = AuthUser(
            uid = "ios-local-anonymous",
            provider = SignInProvider.ANONYMOUS,
            email = "",
            displayName = ""
        )
        localUser = user
        return SignInResult.Success(user)
    }

    actual suspend fun signInWithGoogle(): SignInResult {
        val deferred = CompletableDeferred<SignInResult>()
        val launcher = GoogleSignInBridge.startGoogleSignIn
            ?: return SignInResult.Error("Google Sign-In bridge is not connected.")

        launcher.invoke(deferred)

        val result = deferred.await()
        if (result is SignInResult.Success) {
            localUser = result.user
        }
        return result
    }

    actual suspend fun signInWithApple(): SignInResult {
        val deferred = CompletableDeferred<SignInResult>()
        val launcher = AppleSignInBridge.startAppleSignIn
            ?: return SignInResult.Error("Apple Sign-In bridge is not connected.")

        launcher.invoke(deferred)

        val result = deferred.await()
        if (result is SignInResult.Success) {
            localUser = result.user
        }
        return result
    }

    actual suspend fun signInWithEmail(email: String, password: String): SignInResult {
        return SignInResult.Error("Email auth on iOS is not available yet.")
    }

    actual suspend fun registerWithEmail(email: String, password: String): SignInResult {
        return SignInResult.Error("Email registration on iOS is not available yet.")
    }

    actual suspend fun sendPasswordReset(email: String): SignInResult {
        return SignInResult.Error("Password reset on iOS is not available yet.")
    }

    actual suspend fun signOut() {
        localUser = null
    }

    actual suspend fun clearCredentialState() {
    }
}