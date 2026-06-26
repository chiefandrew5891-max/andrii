package com.andrey.beautyplanner.remote

import kotlinx.serialization.Serializable

@Serializable
data class AccessStatusResponse(
    val userId: String,
    val tier: String,
    val trialStartedAtMillis: Long,
    val trialEndsAtMillis: Long,
    val isTrialActive: Boolean,
    val hasPremium: Boolean,
    val trialDaysLeft: Int,
    val subscriptionState: String,
    val premiumProductId: String
)