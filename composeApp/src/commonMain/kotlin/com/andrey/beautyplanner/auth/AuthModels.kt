package com.andrey.beautyplanner.auth

enum class SignInProvider {
    ANONYMOUS,
    GOOGLE,
    EMAIL,
    APPLE
}

data class AuthUser(
    val uid: String,
    val provider: SignInProvider,
    val email: String = "",
    val displayName: String = ""
)

sealed class SignInResult {
    data class Success(val user: AuthUser) : SignInResult()
    data object Cancelled : SignInResult()
    data class Error(val message: String) : SignInResult()
}