package com.chb.lenstracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chb.lenstracker.R
import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.data.repository.toDomain
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val dao: LensCycleDao by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        // Reading Room is too slow for onReceive's main-thread budget.
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                restoreReminder(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * AlarmManager drops every pending alarm on reboot, so the stored cycle is the only record
     * of when the next reminder was due.
     */
    private suspend fun restoreReminder(context: Context) {
        val cycle = dao.getLensCycle()?.toDomain() ?: return
        val title = context.getString(R.string.notification_title)
        val body = context.getString(R.string.notification_body)
        val zone = TimeZone.currentSystemDefault()
        val due = LocalDateTime(
            date = cycle.nextChangeDate(),
            time = LocalTime(cycle.reminderHour, cycle.reminderMinute)
        )
        val scheduler = AndroidNotificationScheduler(context)

        if (due.toInstant(zone) > Clock.System.now()) {
            scheduler.schedule(due, title, body)
            return
        }
        // The reminder came due while the device was off — fire it now, then resume the daily chain.
        NotificationReceiver.post(context, title, body)
        val tomorrow = Clock.System.now().plus(1, DateTimeUnit.DAY, zone).toLocalDateTime(zone)
        scheduler.schedule(tomorrow, title, body)
    }
}
