package com.andrey.beautyplanner.appcontent

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatBackupCreatedAt(epochMillis: Long?): String {
    if (epochMillis == null || epochMillis <= 0L) return "—"
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val yyyy = dt.year.toString().padStart(4, '0')
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val min = dt.minute.toString().padStart(2, '0')
    return "$dd.$mm.$yyyy $hh:$min"
}