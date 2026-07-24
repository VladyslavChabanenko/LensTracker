package com.chb.lenstracker.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters

@Database(entities = [LensCycleEntity::class], version = 1, autoMigrations = [])
@TypeConverters(DateTypeConverter::class)
@ConstructedBy(LensTrackerDatabaseConstructor::class)
abstract class LensTrackerDatabase : RoomDatabase() {
    abstract fun lensCycleDao(): LensCycleDao
}

expect object LensTrackerDatabaseConstructor : RoomDatabaseConstructor<LensTrackerDatabase>
