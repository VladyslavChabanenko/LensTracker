package com.chb.lenstracker.data.db

import androidx.room3.TypeConverter
import com.chb.lenstracker.domain.model.ReminderInterval
import kotlinx.datetime.LocalDate

class DateTypeConverter {
    @TypeConverter
    fun fromLocalDate(value: LocalDate): String = value.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromReminderInterval(value: ReminderInterval): String = value.name

    @TypeConverter
    fun toReminderInterval(value: String): ReminderInterval = ReminderInterval.valueOf(value)
}
