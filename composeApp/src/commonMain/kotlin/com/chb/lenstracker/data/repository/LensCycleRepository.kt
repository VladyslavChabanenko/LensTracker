package com.chb.lenstracker.data.repository

import com.chb.lenstracker.domain.model.LensCycle
import kotlinx.coroutines.flow.Flow

interface LensCycleRepository {
    fun observe(): Flow<LensCycle?>
    suspend fun save(lensCycle: LensCycle)
    suspend fun markAsChanged()
}
