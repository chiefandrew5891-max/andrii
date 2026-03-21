package com.andrey.beautyplanner.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.andrey.beautyplanner.AndroidAppContext
import com.andrey.beautyplanner.Appointment
import kotlinx.datetime.TimeZone

actual object Notifications {

    private fun ctx(): Context =
        AndroidAppContext.context ?: error("AndroidAppContext.context is not set")

    actual suspend fun requestPermissionIfNeeded(): Boolean {
        return true
    }

    actual fun cancelAll() {
        // MVP: без полного списка requestCode не можем "снести всё".
        // Перетираем при rescheduleAll() через alarm.cancel(pi)
    }

    actual fun rescheduleAll(
        appointments: List<Appointment>,
        reminderMinutes: List<Int>,
        sound: NotificationSound,
        nowEpochMillis: Long
    ) {
        val context = ctx()
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val tz = TimeZone.currentSystemDefault()

        val minutesList = reminderMinutes
            .filter { it > 0 }
            .distinct()
            .sortedDescending()

        apptLoop@ for (appt in appointments) {
            val startMs = appt.startEpochMillis(tz) ?: continue@apptLoop

            minsLoop@ for (mins in minutesList) {
                val triggerAt = startMs - mins * 60_000L
                if (triggerAt <= nowEpochMillis) continue@minsLoop

                val title = "Beauty Planner"
                val body = "${appt.clientName}: ${appt.serviceName} • ${appt.dateString} ${appt.time}"

                val requestCode = stableRequestCode(appt.id, mins)

                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra(ReminderReceiver.EXTRA_TITLE, title)
                    putExtra(ReminderReceiver.EXTRA_BODY, body)
                    putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
                }

                val pi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Сначала отменяем старый, если был
                alarm.cancel(pi)

                // ВАЖНО: не exact, чтобы не требовать SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }

    private fun stableRequestCode(apptId: String, mins: Int): Int {
        return (apptId.hashCode() * 31 + mins).absoluteValue()
    }

    private fun Int.absoluteValue(): Int = if (this < 0) -this else this
}