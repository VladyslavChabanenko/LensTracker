package com.chb.lenstracker.data.repository

import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.data.db.LensCycleEntity
import kotlinx.coroutines.flow.Flow

class LensCycleRepositoryImpl(
    private val dao: LensCycleDao
) : LensCycleRepository {

    override fun observe(): Flow<LensCycleEntity?> = dao.observeLensCycle()

    override suspend fun save(entity: LensCycleEntity) {
        dao.upsertLensCycle(entity)
    }

    override suspend fun markAsChanged() {
        TODO("Implemented in Phase 5 (T031)")
    }
}
