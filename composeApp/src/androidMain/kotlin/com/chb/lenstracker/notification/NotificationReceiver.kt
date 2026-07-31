package com.chb.lenstracker.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chb.lenstracker.MainActivity
import com.chb.lenstracker.R
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AndroidNotificationScheduler.EXTRA_TITLE)
            ?: context.getString(R.string.notification_title)
        val body = intent.getStringExtra(AndroidNotificationScheduler.EXTRA_BODY)
            ?: context.getString(R.string.notification_body)
        post(context, title, body)
        // AlarmManager's repeating modes are inexact on API 19+, so each firing chains the next
        // one itself — the reminder keeps nagging daily until the lenses are marked as changed.
        val zone = TimeZone.currentSystemDefault()
        val tomorrow = Clock.System.now().plus(1, DateTimeUnit.DAY, zone).toLocalDateTime(zone)
        AndroidNotificationScheduler(context).schedule(tomorrow, title, body)
    }

    companion object {
        private const val CHANNEL_ID = "lens_reminder"

        /** Fixed so a new reminder replaces the previous one instead of stacking up. */
        private const val NOTIFICATION_ID = 1

        /**
         * Posts the reminder immediately. Does nothing when the user has notifications turned
         * off — `notify()` is a silent no-op there, and on API 33+ would need POST_NOTIFICATIONS.
         */
        internal fun post(context: Context, title: String, body: String) {
            val manager = NotificationManagerCompat.from(context)
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.notification_channel_name))
                    .setDescription(context.getString(R.string.notification_channel_description))
                    .build()
            )
            if (!manager.areNotificationsEnabled()) return

            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}
