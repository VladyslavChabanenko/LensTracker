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

    fun progressFraction(today: LocalDate): Float {
        val remaining = daysRemaining(today)
        val fraction = (interval.days - remaining).toFloat() / interval.days
        return fraction.coerceAtMost(1.5f)
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
