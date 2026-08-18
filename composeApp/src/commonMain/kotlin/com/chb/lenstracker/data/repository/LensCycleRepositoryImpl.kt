package com.chb.lenstracker.data.repository

import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.notification.NotificationScheduler
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.notification_body
import com.chb.lenstracker.resources.notification_title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString

class LensCycleRepositoryImpl(
    private val dao: LensCycleDao,
    private val scheduler: NotificationScheduler
) : LensCycleRepository {

    override fun observe(): Flow<LensCycle?> = dao.observeLensCycle().map { it?.toDomain() }

    override suspend fun save(lensCycle: LensCycle) {
        dao.upsertLensCycle(lensCycle.toEntity())
        val nextTrigger = LocalDateTime(
            lensCycle.nextChangeDate(),
            LocalTime(lensCycle.reminderHour, lensCycle.reminderMinute)
        )
        scheduler.schedule(nextTrigger, getString(Res.string.notification_title), getString(Res.string.notification_body))
    }

    override suspend fun markAsChanged() {
        TODO("Implemented in Phase 5 (T031)")
    }
}
