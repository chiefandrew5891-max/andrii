package com.andrey.beautyplanner.notifications

import com.andrey.beautyplanner.Appointment
import kotlinx.datetime.TimeZone
import platform.UserNotifications.*

actual object Notifications {

    actual suspend fun requestPermissionIfNeeded(): Boolean {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        // iOS запрос асинхронный, но в KMP проще сделать “best effort”
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { _, _ -> }
        return true
    }

    actual fun cancelAll() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }

    actual fun rescheduleAll(
        appointments: List<Appointment>,
        reminderMinutes: List<Int>,
        sound: NotificationSound,
        nowEpochMillis: Long
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllPendingNotificationRequests()

        val tz = TimeZone.currentSystemDefault()

        appointments.forEach { appt ->
            val startMs = appt.startEpochMillis(tz) ?: return@forEach

            reminderMinutes.forEach { mins ->
                val triggerAt = startMs - mins * 60_000L
                if (triggerAt <= nowEpochMillis) return@forEach

                val content = UNMutableNotificationContent().apply {
                    title = "Beauty Planner"
                    body = "${appt.clientName}: ${appt.serviceName} • ${appt.dateString} ${appt.time}"
                    this.sound = when (sound) {
                        NotificationSound.SILENT -> null
                        NotificationSound.DEFAULT -> UNNotificationSound.defaultSound()
                    }
                }

                val seconds = ((triggerAt - nowEpochMillis) / 1000.0).coerceAtLeast(1.0)
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)

                val id = "${appt.id}_$mins"
                val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)
                center.addNotificationRequest(request) { _ -> }
            }
        }
    }
}