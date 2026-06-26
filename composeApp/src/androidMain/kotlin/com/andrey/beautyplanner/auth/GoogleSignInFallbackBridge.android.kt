package com.andrey.beautyplanner.auth

import android.content.Intent
import kotlinx.coroutines.CompletableDeferred

object GoogleSignInFallbackBridge {
    var launchSignInIntent: ((Intent, CompletableDeferred<GoogleSignInFallbackResult>) -> Unit)? = null
}

sealed class GoogleSignInFallbackResult {
    data class Success(val idToken: String) : GoogleSignInFallbackResult()
    data object Cancelled : GoogleSignInFallbackResult()
    data class Error(val message: String) : GoogleSignInFallbackResult()
}