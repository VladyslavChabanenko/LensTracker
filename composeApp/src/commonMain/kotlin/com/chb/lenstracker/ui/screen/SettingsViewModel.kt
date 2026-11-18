package com.chb.lenstracker.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.notification.NotificationScheduler
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.toast_notifications_enabled
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

data class SettingsUiState(
    val reminderHour: Int = DEFAULT_REMINDER_HOUR,
    val reminderMinute: Int = DEFAULT_REMINDER_MINUTE,
    val hasNotificationPermission: Boolean = true,
    /** True while a `requestPermission()` call is pending, so the Enable control can't double-fire. */
    val permissionRequestInFlight: Boolean = false,
    /** Transient confirmation text (FR-012); `null` when no toast is showing. */
    val toastMessage: String? = null
)

sealed class SettingsUiEvent {
    data class UpdateReminderTime(val hour: Int, val minute: Int) : SettingsUiEvent()
    data object RequestPermission : SettingsUiEvent()
    data object OpenAppSettings : SettingsUiEvent()
}

class SettingsViewModel(
    private val repository: LensCycleRepository,
    private val scheduler: NotificationScheduler,
    // Injected so unit tests can run without the platform resource loader.
    private val permissionGrantedMessage: suspend () -> String = {
        getString(Res.string.toast_notifications_enabled)
    }
) : ViewModel() {

    private val hasNotificationPermission = MutableStateFlow(scheduler.hasPermission())
    private val permissionRequestInFlight = MutableStateFlow(false)
    private val toastMessage = MutableStateFlow<String?>(null)
    private var toastJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observe(),
        hasNotificationPermission,
        permissionRequestInFlight,
        toastMessage
    ) { lensCycle, hasNotificationPermission, permissionRequestInFlight, toastMessage ->
        SettingsUiState(
            reminderHour = lensCycle?.reminderHour ?: DEFAULT_REMINDER_HOUR,
            reminderMinute = lensCycle?.reminderMinute ?: DEFAULT_REMINDER_MINUTE,
            hasNotificationPermission = hasNotificationPermission,
            permissionRequestInFlight = permissionRequestInFlight,
            toastMessage = toastMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.UpdateReminderTime -> updateReminderTime(event)
            SettingsUiEvent.RequestPermission -> requestPermission()
            SettingsUiEvent.OpenAppSettings -> scheduler.openAppSettings()
        }
    }

    private fun updateReminderTime(event: SettingsUiEvent.UpdateReminderTime) {
        viewModelScope.launch {
            // The reminder time lives on the cycle, so there is nothing to update before one exists.
            val current = repository.observe().first() ?: return@launch
            repository.save(current.copy(reminderHour = event.hour, reminderMinute = event.minute))
        }
    }

    private fun requestPermission() {
        if (permissionRequestInFlight.value) return
        permissionRequestInFlight.value = true
        viewModelScope.launch {
            try {
                val granted = scheduler.requestPermission()
                hasNotificationPermission.value = granted
                if (granted) showToast(permissionGrantedMessage())
            } finally {
                permissionRequestInFlight.value = false
            }
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
}
