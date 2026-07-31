package com.chb.lenstracker.notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(nextTrigger: LocalDateTime, title: String, body: String) {
        val triggerAtMillis = nextTrigger
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        val pendingIntent = alarmPendingIntent(
            title = title,
            body = body,
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        )!!
        // Inexact on purpose: a lens reminder tolerates the OS deferring it, and this needs no
        // exact-alarm permission while still firing in Doze (research.md #2).
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    override fun cancel() {
        // FLAG_NO_CREATE returns null when no alarm was ever scheduled — nothing to cancel.
        val pendingIntent = alarmPendingIntent(flags = PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    override fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    // Placeholder: requesting POST_NOTIFICATIONS needs an Activity-scoped launcher, wired in T043.
    override suspend fun requestPermission(): Boolean = hasPermission()

    override fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * The single reminder alarm. Extras are ignored when matching an existing [PendingIntent],
     * so [cancel] finds the pending alarm without knowing its title and body.
     */
    private fun alarmPendingIntent(title: String = "", body: String = "", flags: Int): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java)
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_BODY, body)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_TITLE: String = "com.chb.lenstracker.extra.TITLE"
        const val EXTRA_BODY: String = "com.chb.lenstracker.extra.BODY"

        /** Fixed so every scheduled alarm replaces the previous one — the app has one reminder slot. */
        private const val REQUEST_CODE = 1001
    }
}
