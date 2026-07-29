package com.chb.lenstracker.data.repository

import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.domain.model.LensCycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LensCycleRepositoryImpl(
    private val dao: LensCycleDao
) : LensCycleRepository {

    override fun observe(): Flow<LensCycle?> = dao.observeLensCycle().map { it?.toDomain() }

    override suspend fun save(lensCycle: LensCycle) {
        dao.upsertLensCycle(lensCycle.toEntity())
    }

    override suspend fun markAsChanged() {
        TODO("Implemented in Phase 5 (T031)")
    }
}
