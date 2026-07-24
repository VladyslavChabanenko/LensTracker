package com.chb.lenstracker.data.db

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

fun createDatabase(context: Context): LensTrackerDatabase {
    return Room.databaseBuilder<LensTrackerDatabase>(
        context = context.applicationContext,
        name = "lens_tracker.db"
    )
        .setDriver(AndroidSQLiteDriver())
        .build()
}
