package com.chb.lenstracker.data.repository

import com.chb.lenstracker.data.db.LensCycleEntity
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import kotlinx.datetime.LocalDate

internal fun LensCycleEntity.toDomain(): LensCycle = LensCycle(
    startDate = LocalDate.parse(startDate),
    interval = ReminderInterval.valueOf(interval),
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)

internal fun LensCycle.toEntity(): LensCycleEntity = LensCycleEntity(
    startDate = startDate.toString(),
    interval = interval.name,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute
)
