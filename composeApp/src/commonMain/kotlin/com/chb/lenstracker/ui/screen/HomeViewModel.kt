package com.chb.lenstracker.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.domain.model.toDomain
import com.chb.lenstracker.domain.model.toEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class HomeUiState(
    val lensCycle: LensCycle? = null,
    val daysRemaining: Int = 0,
    val isOverdue: Boolean = false,
    val progressFraction: Float = 0f,
    val hasNotificationPermission: Boolean = true
)

sealed class HomeUiEvent {
    data class SaveCycle(val startDate: LocalDate, val interval: ReminderInterval) : HomeUiEvent()
    data object MarkAsChanged : HomeUiEvent()
}

private const val DEFAULT_REMINDER_HOUR = 9
private const val DEFAULT_REMINDER_MINUTE = 0

class HomeViewModel(
    private val repository: LensCycleRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observe()
        .map { entity -> entity?.toDomain().toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SaveCycle -> saveCycle(event)
            HomeUiEvent.MarkAsChanged -> viewModelScope.launch { repository.markAsChanged() }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveCycle(event: HomeUiEvent.SaveCycle) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (event.startDate > today) return

        val current = uiState.value.lensCycle
        val lensCycle = LensCycle(
            startDate = event.startDate,
            interval = event.interval,
            reminderHour = current?.reminderHour ?: DEFAULT_REMINDER_HOUR,
            reminderMinute = current?.reminderMinute ?: DEFAULT_REMINDER_MINUTE
        )
        viewModelScope.launch { repository.save(lensCycle.toEntity()) }
    }

    @OptIn(ExperimentalTime::class)
    private fun LensCycle?.toUiState(): HomeUiState {
        if (this == null) return HomeUiState()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return HomeUiState(
            lensCycle = this,
            daysRemaining = daysRemaining(today),
            isOverdue = isOverdue(today),
            progressFraction = progressFraction(today)
        )
    }
}
