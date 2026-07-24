package com.chb.lenstracker.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LensCycleDao {

    @Query("SELECT * FROM lens_cycle WHERE id = 1")
    fun observeLensCycle(): Flow<LensCycleEntity?>

    @Query("SELECT * FROM lens_cycle WHERE id = 1")
    suspend fun getLensCycle(): LensCycleEntity?

    @Upsert
    suspend fun upsertLensCycle(entity: LensCycleEntity)
}
