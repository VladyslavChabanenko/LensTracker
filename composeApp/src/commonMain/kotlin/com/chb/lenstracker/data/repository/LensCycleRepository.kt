package com.chb.lenstracker.data.repository

import com.chb.lenstracker.data.db.LensCycleEntity
import kotlinx.coroutines.flow.Flow

interface LensCycleRepository {
    fun observe(): Flow<LensCycleEntity?>
    suspend fun save(entity: LensCycleEntity)
    suspend fun markAsChanged()
}
