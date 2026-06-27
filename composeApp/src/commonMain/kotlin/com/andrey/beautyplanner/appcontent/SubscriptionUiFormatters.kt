package com.andrey.beautyplanner.appcontent

import com.andrey.beautyplanner.Locales
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil

fun formatSubscriptionExpiry(epochMillis: Long): String {
    if (epochMillis <= 0L) return "—"
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val yyyy = dt.year.toString().padStart(4, '0')
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    return "$dd.$mm.$yyyy"
}

fun calculateSubscriptionDaysLeft(
    expiryMillis: Long,
    nowMillis: Long
): Int {
    if (expiryMillis <= 0L || expiryMillis <= nowMillis) return 0
    return ceil((expiryMillis - nowMillis).toDouble() / (24 * 60 * 60 * 1000.0))
        .toInt()
        .coerceAtLeast(0)
}

fun subscriptionStateLabel(state: String): String {
    return when (state.uppercase()) {
        "ACTIVE" -> Locales.t("premium_subscription_state_active")
        "CANCELED" -> Locales.t("premium_subscription_state_canceled")
        "EXPIRED" -> Locales.t("premium_subscription_state_expired")
        "IN_GRACE_PERIOD" -> Locales.t("premium_subscription_state_grace")
        "ON_HOLD" -> Locales.t("premium_subscription_state_on_hold")
        "PENDING_BACKEND_VERIFICATION" -> Locales.t("premium_subscription_state_verifying")
        else -> Locales.t("premium_subscription_state_none")
    }
}