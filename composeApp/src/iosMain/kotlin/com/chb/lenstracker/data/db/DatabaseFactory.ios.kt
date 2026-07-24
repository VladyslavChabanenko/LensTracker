package com.chb.lenstracker.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun createDatabase(): LensTrackerDatabase {
    val dbFilePath = NSHomeDirectory() + "/Documents/lens_tracker.db"
    return Room.databaseBuilder<LensTrackerDatabase>(name = dbFilePath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
