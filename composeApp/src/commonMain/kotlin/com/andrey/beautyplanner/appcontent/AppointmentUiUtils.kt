package com.andrey.beautyplanner.appcontent

import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales

fun parseHmToMinutesSafe(hm: String): Int {
    val parts = hm.trim().split(":")
    if (parts.size != 2) return 0
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    return h * 60 + m
}

fun minutesToHmSafe(minutes: Int): String {
    val m = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
    val hPart = (m / 60).toString().padStart(2, '0')
    val mPart = (m % 60).toString().padStart(2, '0')
    return "$hPart:$mPart"
}

fun appointmentDurationMinutes(appt: Appointment): Int {
    return if (appt.durationMinutes > 0) {
        appt.durationMinutes
    } else {
        appt.durationHours.coerceAtLeast(1) * 60
    }
}

fun appointmentEndTime(appt: Appointment): String {
    val startMin = parseHmToMinutesSafe(appt.time)
    val endMin = startMin + appointmentDurationMinutes(appt)
    return minutesToHmSafe(endMin)
}

fun appointmentServiceDisplay(appt: Appointment): String {
    return if (appt.serviceName.startsWith("service_")) {
        Locales.t(appt.serviceName)
    } else {
        appt.serviceName
    }
}
