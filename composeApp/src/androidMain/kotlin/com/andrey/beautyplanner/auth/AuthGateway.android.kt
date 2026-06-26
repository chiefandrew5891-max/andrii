package com.andrey.beautyplanner.auth

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.andrey.beautyplanner.AndroidAppContext
import com.andrey.beautyplanner.Locales
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual object AuthGateway {
    actual suspend fun getCurrentUser(): AuthUser? {
        val user = Firebase.auth.currentUser ?: return null
        val provider = when {
            user.isAnonymous -> SignInProvider.ANONYMOUS
            user.providerData.any { it.providerId == "google.com" } -> SignInProvider.GOOGLE
            user.providerData.any { it.providerId == "apple.com" } -> SignInProvider.APPLE
            else -> SignInProvider.ANONYMOUS
        }

        return AuthUser(
            uid = user.uid,
            provider = provider,
            email = user.email.orEmpty(),
            displayName = user.displayName.orEmpty()
        )
    }

    actual suspend fun signInAnonymously(): SignInResult {
        val current = getCurrentUser()
        if (current != null) {
            return SignInResult.Success(current)
        }

        return try {
            val authResult = suspendCancellableCoroutine<com.google.firebase.auth.AuthResult> { cont ->
                Firebase.auth.signInAnonymously()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            val user = authResult.user
                ?: return SignInResult.Error("Anonymous sign-in returned null user")

            SignInResult.Success(
                AuthUser(
                    uid = user.uid,
                    provider = SignInProvider.ANONYMOUS,
                    email = user.email.orEmpty(),
                    displayName = user.displayName.orEmpty()
                )
            )
        } catch (e: Exception) {
            SignInResult.Error(e.message ?: "Anonymous sign-in failed")
        }
    }

    actual suspend fun signInWithGoogle(): SignInResult {
        val activity = AndroidAppContext.activity
            ?: return SignInResult.Error(Locales.t("auth_google_failed"))

        return try {
            val credentialManager = CredentialManager.create(activity)

            val webClientIdRes = activity.resources.getIdentifier(
                "default_web_client_id",
                "string",
                activity.packageName
            )

            if (webClientIdRes == 0) {
                return SignInResult.Error(Locales.t("auth_google_failed"))
            }

            val serverClientId = activity.getString(webClientIdRes)
            if (serverClientId.isBlank()) {
                return SignInResult.Error(Locales.t("auth_google_failed"))
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val googleIdTokenCredential = try {
                GoogleIdTokenCredential.createFrom(result.credential.data)
            } catch (_: GoogleIdTokenParsingException) {
                return SignInResult.Error(Locales.t("auth_google_failed"))
            }

            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )

            val authResult = suspendCancellableCoroutine<com.google.firebase.auth.AuthResult> { cont ->
                Firebase.auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            val user = authResult.user
                ?: return SignInResult.Error(Locales.t("auth_google_failed"))

            SignInResult.Success(
                AuthUser(
                    uid = user.uid,
                    provider = SignInProvider.GOOGLE,
                    email = user.email.orEmpty(),
                    displayName = user.displayName.orEmpty()
                )
            )
        } catch (e: Exception) {
            Log.e("AuthGateway", "Google sign-in failed", e)

            val message = e.message.orEmpty()

            return when {
                message.contains("No credentials available", ignoreCase = true) ->
                    SignInResult.Error(Locales.t("auth_google_no_credentials"))

                message.contains("cancel", ignoreCase = true) ->
                    SignInResult.Cancelled

                else ->
                    SignInResult.Error(Locales.t("auth_google_failed"))
            }
        }
    }

    actual suspend fun signOut() {
        Firebase.auth.signOut()
    }

    actual suspend fun clearCredentialState() {
        val activity = AndroidAppContext.activity ?: return
        val credentialManager = CredentialManager.create(activity)
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }.onFailure {
            Log.e("AuthGateway", "Failed to clear credential state", it)
        }
    }
}