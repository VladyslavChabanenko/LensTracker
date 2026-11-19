package com.chb.lenstracker.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.notification.NotificationScheduler
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.date_display_format
import com.chb.lenstracker.resources.month_names
import com.chb.lenstracker.resources.toast_marked_as_changed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getStringArray

/**
 * Everything the status card renders, fully derived from the cycle here so the card needs
 * neither the domain model nor any date arithmetic of its own.
 */
data class StatusCardUiState(
    val nextChangeDateString: String,
    val daysRemaining: Int,
    val isOverdue: Boolean,
    val baseProgressFraction: Float,
    val overdueProgressFraction: Float
)

data class HomeUiState(
    val lensCycle: LensCycle? = null,
    /** `null` until a cycle exists — the card shows its empty prompt in that case. */
    val statusCard: StatusCardUiState? = null,
    /** `null` until a start date is picked — the field shows its placeholder in that case. */
    val startDateString: String? = null,
    val hasNotificationPermission: Boolean = true,
    val showDatePicker: Boolean = false,
    val showConfirmDialog: Boolean = false,
    /** Transient confirmation text (FR-012); `null` when no toast is showing. */
    val toastMessage: String? = null,
    val selectedInterval: ReminderInterval = ReminderInterval.TWO_WEEKS
)

sealed class HomeUiEvent {
    data class SaveCycle(val startDate: LocalDate, val interval: ReminderInterval) : HomeUiEvent()
    data object MarkAsChanged : HomeUiEvent()
    data object OpenConfirmDialog : HomeUiEvent()
    data object CloseConfirmDialog : HomeUiEvent()
    data object OpenDatePicker : HomeUiEvent()
    data object CloseDatePicker : HomeUiEvent()
    data class SelectDate(val date: LocalDate) : HomeUiEvent()
    data class SelectInterval(val interval: ReminderInterval) : HomeUiEvent()
    /** Re-reads the OS permission status; sent whenever the screen becomes visible again. */
    data object RefreshPermission : HomeUiEvent()
}

internal const val DEFAULT_REMINDER_HOUR = 9
internal const val DEFAULT_REMINDER_MINUTE = 0
private val DEFAULT_INTERVAL = ReminderInterval.TWO_WEEKS
internal const val TOAST_DURATION_MS = 3000L

class HomeViewModel(
    private val repository: LensCycleRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val showDatePicker = MutableStateFlow(false)
    private val showConfirmDialog = MutableStateFlow(false)
    private val toastMessage = MutableStateFlow<String?>(null)
    private var toastJob: Job? = null

    // Holds the user's interval choice while no cycle exists yet to persist it on (FR-002 lets
    // interval be picked before a start date). Ignored once a cycle exists; its interval wins.
    private val pendingInterval = MutableStateFlow(DEFAULT_INTERVAL)

    private val hasNotificationPermission = MutableStateFlow(scheduler.hasPermission())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observe(),
        showDatePicker,
        pendingInterval,
        hasNotificationPermission,
        showConfirmDialog,
        toastMessage
    ) { values ->
        val lensCycle = values[0] as LensCycle?
        val showDatePicker = values[1] as Boolean
        val pendingInterval = values[2] as ReminderInterval
        val hasNotificationPermission = values[3] as Boolean
        lensCycle.toUiState().copy(
            showDatePicker = showDatePicker,
            showConfirmDialog = values[4] as Boolean,
            toastMessage = values[5] as String?,
            selectedInterval = lensCycle?.interval ?: pendingInterval,
            hasNotificationPermission = hasNotificationPermission
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SaveCycle -> saveCycle(event)
            HomeUiEvent.MarkAsChanged -> markAsChanged()
            HomeUiEvent.RefreshPermission -> hasNotificationPermission.value = scheduler.hasPermission()
            HomeUiEvent.OpenConfirmDialog -> showConfirmDialog.value = true
            HomeUiEvent.CloseConfirmDialog -> showConfirmDialog.value = false
            HomeUiEvent.OpenDatePicker -> showDatePicker.value = true
            HomeUiEvent.CloseDatePicker -> showDatePicker.value = false
            is HomeUiEvent.SelectDate -> {
                showDatePicker.value = false
                val interval = uiState.value.lensCycle?.interval ?: pendingInterval.value
                saveCycle(HomeUiEvent.SaveCycle(event.date, interval))
            }
            is HomeUiEvent.SelectInterval -> {
                val current = uiState.value.lensCycle
                if (current != null) {
                    saveCycle(HomeUiEvent.SaveCycle(current.startDate, event.interval))
                } else {
                    pendingInterval.value = event.interval
                }
            }
        }
    }

    private fun markAsChanged() {
        showConfirmDialog.value = false
        viewModelScope.launch {
            repository.markAsChanged()
            showToast(getString(Res.string.toast_marked_as_changed))
        }
    }

    private fun showToast(message: String) {
        toastJob?.cancel()
        toastMessage.value = message
        toastJob = viewModelScope.launch {
            delay(TOAST_DURATION_MS)
            toastMessage.value = null
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
        viewModelScope.launch {
            repository.save(lensCycle)
            // First cycle just created — this is when the OS permission prompt should appear.
            if (current == null) {
                hasNotificationPermission.value = scheduler.requestPermission()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun LensCycle?.toUiState(): HomeUiState {
        if (this == null) return HomeUiState()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return HomeUiState(
            lensCycle = this,
            statusCard = StatusCardUiState(
                nextChangeDateString = nextChangeDate().displayFormat(),
                daysRemaining = daysRemaining(today),
                isOverdue = isOverdue(today),
                baseProgressFraction = baseProgressFraction(today),
                overdueProgressFraction = overdueProgressFraction(today)
            ),
            startDateString = startDate.displayFormat()
        )
    }
}

/** "July 17, 2026" — kept here so the UI layer receives display-ready text, not dates. */
private suspend fun LocalDate.displayFormat(): String = getString(
    Res.string.date_display_format,
    getStringArray(Res.array.month_names)[month.ordinal],
    day.toString(),
    year.toString()
)
