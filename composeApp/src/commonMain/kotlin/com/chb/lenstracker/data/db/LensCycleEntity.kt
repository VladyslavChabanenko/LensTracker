package com.chb.lenstracker.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "lens_cycle")
data class LensCycleEntity(
    @PrimaryKey val id: Int = 1,
    val startDate: String,
    val interval: String,
    val reminderHour: Int,
    val reminderMinute: Int
)
