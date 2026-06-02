package com.andrey.beautyplanner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.andrey.beautyplanner.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val soundMode = intent.getStringExtra(EXTRA_SOUND_MODE)
            ?: NotificationSound.DEFAULT.name

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = if (soundMode == NotificationSound.SILENT.name) {
                CHANNEL_ID_SILENT
            } else {
                CHANNEL_ID_DEFAULT
            }

            val channelName = if (soundMode == NotificationSound.SILENT.name) {
                "Reminders Silent"
            } else {
                "Reminders"
            }

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            )

            if (soundMode == NotificationSound.SILENT.name) {
                channel.setSound(null, null)
                channel.enableVibration(false)
            }

            nm.createNotificationChannel(channel)

            val notif = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .build()

            nm.notify(notificationId, notif)
            return
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        nm.notify(notificationId, notif)
    }

    companion object {
        const val CHANNEL_ID_DEFAULT = "beautyplanner_reminders"
        const val CHANNEL_ID_SILENT = "beautyplanner_reminders_silent"

        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_NOTIFICATION_ID = "nid"
        const val EXTRA_SOUND_MODE = "sound_mode"
    }
}