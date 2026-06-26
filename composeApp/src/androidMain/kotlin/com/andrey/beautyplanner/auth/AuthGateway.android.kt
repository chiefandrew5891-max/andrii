package com.andrey.beautyplanner.auth

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.andrey.beautyplanner.AndroidAppContext
import com.andrey.beautyplanner.Locales
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual object AuthGateway {
    actual suspend fun getCurrentUser(): AuthUser? {
        val user = Firebase.auth.currentUser ?: return null
        val provider = when {
            user.isAnonymous -> SignInProvider.ANONYMOUS
            user.providerData.any { it.providerId == "google.com" } -> SignInProvider.GOOGLE
            user.providerData.any { it.providerId == "password" } -> SignInProvider.EMAIL
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

        val serverClientId = getServerClientId(activity)
            ?: return SignInResult.Error(Locales.t("auth_google_failed"))

        val credentialManagerResult = tryCredentialManagerGoogleSignIn(
            activity = activity,
            serverClientId = serverClientId
        )

        if (credentialManagerResult is SignInResult.Success) {
            return credentialManagerResult
        }

        Log.w(
            "AuthGateway",
            "Credential Manager Google sign-in failed, fallback to GoogleSignInClient"
        )

        return tryLegacyGoogleSignIn(
            activity = activity,
            serverClientId = serverClientId,
            primaryFailure = credentialManagerResult
        )
    }

    actual suspend fun signInWithEmail(email: String, password: String): SignInResult {
        return try {
            val authResult = suspendCancellableCoroutine<com.google.firebase.auth.AuthResult> { cont ->
                Firebase.auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            val user = authResult.user
                ?: return SignInResult.Error(Locales.t("auth_email_sign_in_failed"))

            SignInResult.Success(
                AuthUser(
                    uid = user.uid,
                    provider = SignInProvider.EMAIL,
                    email = user.email.orEmpty(),
                    displayName = user.displayName.orEmpty()
                )
            )
        } catch (e: Exception) {
            Log.e("AuthGateway", "Email sign-in failed", e)
            SignInResult.Error(
                e.message ?: Locales.t("auth_email_sign_in_failed")
            )
        }
    }

    actual suspend fun registerWithEmail(email: String, password: String): SignInResult {
        return try {
            val authResult = suspendCancellableCoroutine<com.google.firebase.auth.AuthResult> { cont ->
                Firebase.auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            val user = authResult.user
                ?: return SignInResult.Error(Locales.t("auth_email_register_failed"))

            SignInResult.Success(
                AuthUser(
                    uid = user.uid,
                    provider = SignInProvider.EMAIL,
                    email = user.email.orEmpty(),
                    displayName = user.displayName.orEmpty()
                )
            )
        } catch (e: Exception) {
            Log.e("AuthGateway", "Email registration failed", e)
            SignInResult.Error(
                e.message ?: Locales.t("auth_email_register_failed")
            )
        }
    }

    actual suspend fun signOut() {
        Firebase.auth.signOut()

        AndroidAppContext.activity?.let { activity ->
            runCatching {
                GoogleSignIn.getClient(
                    activity,
                    buildGoogleSignInOptions(
                        getServerClientId(activity).orEmpty()
                    )
                ).signOut()
            }.onFailure {
                Log.w("AuthGateway", "GoogleSignInClient signOut failed", it)
            }
        }
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

    private fun getServerClientId(activity: android.app.Activity): String? {
        val webClientIdRes = activity.resources.getIdentifier(
            "default_web_client_id",
            "string",
            activity.packageName
        )
        if (webClientIdRes == 0) return null
        return activity.getString(webClientIdRes).takeIf { it.isNotBlank() }
    }

    private suspend fun tryCredentialManagerGoogleSignIn(
        activity: android.app.Activity,
        serverClientId: String
    ): SignInResult {
        return try {
            val credentialManager = CredentialManager.create(activity)

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

            signInFirebaseWithGoogleIdToken(googleIdTokenCredential.idToken)
        } catch (e: Exception) {
            Log.e("AuthGateway", "Credential Manager Google sign-in failed", e)

            val message = e.message.orEmpty()

            when {
                message.contains("No credentials available", ignoreCase = true) ->
                    SignInResult.Error(Locales.t("auth_google_no_credentials"))

                message.contains("cancel", ignoreCase = true) ->
                    SignInResult.Cancelled

                else ->
                    SignInResult.Error(Locales.t("auth_google_failed"))
            }
        }
    }

    private suspend fun tryLegacyGoogleSignIn(
        activity: android.app.Activity,
        serverClientId: String,
        primaryFailure: SignInResult
    ): SignInResult {
        return try {
            val signInClient = buildGoogleSignInClient(activity, serverClientId)
            val intent = signInClient.signInIntent

            val deferred = CompletableDeferred<GoogleSignInFallbackResult>()
            val launcher = GoogleSignInFallbackBridge.launchSignInIntent
                ?: return SignInResult.Error(Locales.t("auth_google_failed"))

            launcher.invoke(intent, deferred)

            when (val result = deferred.await()) {
                is GoogleSignInFallbackResult.Success -> {
                    signInFirebaseWithGoogleIdToken(result.idToken)
                }

                is GoogleSignInFallbackResult.Cancelled -> {
                    SignInResult.Cancelled
                }

                is GoogleSignInFallbackResult.Error -> {
                    when (primaryFailure) {
                        is SignInResult.Error -> primaryFailure
                        else -> SignInResult.Error(Locales.t("auth_google_failed"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AuthGateway", "Legacy Google sign-in failed", e)
            when (primaryFailure) {
                is SignInResult.Error -> primaryFailure
                else -> SignInResult.Error(Locales.t("auth_google_failed"))
            }
        }
    }

    private fun buildGoogleSignInOptions(serverClientId: String): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(serverClientId)
            .build()
    }

    private fun buildGoogleSignInClient(
        activity: android.app.Activity,
        serverClientId: String
    ): GoogleSignInClient {
        return GoogleSignIn.getClient(
            activity,
            buildGoogleSignInOptions(serverClientId)
        )
    }

    private suspend fun signInFirebaseWithGoogleIdToken(idToken: String): SignInResult {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

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
            Log.e("AuthGateway", "Firebase Google credential sign-in failed", e)
            SignInResult.Error(Locales.t("auth_google_failed"))
        }
    }
}