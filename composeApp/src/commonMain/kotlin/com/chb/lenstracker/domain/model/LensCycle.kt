package com.chb.lenstracker.domain.model

import com.chb.lenstracker.data.db.LensCycleEntity
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until

data class LensCycle(
    val startDate: LocalDate,
    val interval: ReminderInterval,
    val reminderHour: Int,
    val reminderMinute: Int
) {
    fun nextChangeDate(): LocalDate =
        startDate.plus(interval.days, DateTimeUnit.DAY)

    fun daysRemaining(today: LocalDate): Int =
        today.until(nextChangeDate(), DateTimeUnit.DAY).toInt()

    /** Elapsed portion of the interval, 0f at start to 1f at (or past) the due date. */
    fun baseProgressFraction(today: LocalDate): Float {
        val remaining = daysRemaining(today)
        val elapsed = (interval.days - remaining).toFloat() / interval.days
        return elapsed.coerceIn(0f, 1f)
    }

    /** Overdue portion drawn on top of the base bar, 0f while on-track, up to 1f at one full interval overdue. */
    fun overdueProgressFraction(today: LocalDate): Float {
        val remaining = daysRemaining(today)
        if (remaining >= 0) return 0f
        val fraction = (-remaining).toFloat() / interval.days
        return fraction.coerceIn(0f, 1f)
    }

    fun isOverdue(today: LocalDate): Boolean = daysRemaining(today) < 0
}

fun LensCycleEntity.toDomain(): LensCycle = LensCycle(
    startDate = LocalDate.parse(startDate),
    interval = ReminderInterval.valueOf(interval),
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)

fun LensCycle.toEntity(): LensCycleEntity = LensCycleEntity(
    startDate = startDate.toString(),
    interval = interval.name,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)
