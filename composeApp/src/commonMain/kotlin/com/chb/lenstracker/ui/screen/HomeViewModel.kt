package com.chb.lenstracker.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val baseProgressFraction: Float = 0f,
    val overdueProgressFraction: Float = 0f,
    val hasNotificationPermission: Boolean = true,
    val showDatePicker: Boolean = false
)

sealed class HomeUiEvent {
    data class SaveCycle(val startDate: LocalDate, val interval: ReminderInterval) : HomeUiEvent()
    data object MarkAsChanged : HomeUiEvent()
    data object OpenDatePicker : HomeUiEvent()
    data object CloseDatePicker : HomeUiEvent()
    data class SelectDate(val date: LocalDate) : HomeUiEvent()
}

private const val DEFAULT_REMINDER_HOUR = 9
private const val DEFAULT_REMINDER_MINUTE = 0
private val DEFAULT_INTERVAL = ReminderInterval.TWO_WEEKS

class HomeViewModel(
    private val repository: LensCycleRepository
) : ViewModel() {

    private val showDatePicker = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observe(),
        showDatePicker
    ) { lensCycle, showDatePicker ->
        lensCycle.toUiState().copy(showDatePicker = showDatePicker)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SaveCycle -> saveCycle(event)
            HomeUiEvent.MarkAsChanged -> viewModelScope.launch { repository.markAsChanged() }
            HomeUiEvent.OpenDatePicker -> showDatePicker.value = true
            HomeUiEvent.CloseDatePicker -> showDatePicker.value = false
            is HomeUiEvent.SelectDate -> {
                showDatePicker.value = false
                val interval = uiState.value.lensCycle?.interval ?: DEFAULT_INTERVAL
                saveCycle(HomeUiEvent.SaveCycle(event.date, interval))
            }
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
        viewModelScope.launch { repository.save(lensCycle) }
    }

    @OptIn(ExperimentalTime::class)
    private fun LensCycle?.toUiState(): HomeUiState {
        if (this == null) return HomeUiState()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return HomeUiState(
            lensCycle = this,
            daysRemaining = daysRemaining(today),
            isOverdue = isOverdue(today),
            baseProgressFraction = baseProgressFraction(today),
            overdueProgressFraction = overdueProgressFraction(today)
        )
    }
}
