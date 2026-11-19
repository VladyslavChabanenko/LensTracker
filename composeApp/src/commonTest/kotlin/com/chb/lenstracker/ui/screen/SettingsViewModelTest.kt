package com.chb.lenstracker.ui.screen

import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.notification.NotificationScheduler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val cycle = LensCycle(
        startDate = LocalDate(2026, 1, 1),
        interval = ReminderInterval.WEEKLY,
        reminderHour = 9,
        reminderMinute = 0
    )

    private val repository = FakeLensCycleRepository(initial = cycle)
    private val scheduler = FakeNotificationScheduler()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** `uiState` uses `WhileSubscribed`, so something must collect it for state to flow. */
    private fun TestScope.viewModel(): SettingsViewModel {
        val viewModel = SettingsViewModel(repository, scheduler, permissionGrantedMessage = { "Notifications enabled" })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        runCurrent()
        return viewModel
    }

    @Test
    fun uiStateExposesReminderTimeFromCycle() = runTest {
        val viewModel = viewModel()

        assertEquals(9, viewModel.uiState.value.reminderHour)
        assertEquals(0, viewModel.uiState.value.reminderMinute)
    }

    @Test
    fun updateReminderTimeSavesCycleWithNewTimeAndKeepsDateAndInterval() = runTest {
        val viewModel = viewModel()

        viewModel.onEvent(SettingsUiEvent.UpdateReminderTime(hour = 21, minute = 45))
        runCurrent()

        val saved = repository.saved.single()
        assertEquals(cycle.copy(reminderHour = 21, reminderMinute = 45), saved)
        assertEquals(21, viewModel.uiState.value.reminderHour)
        assertEquals(45, viewModel.uiState.value.reminderMinute)
    }

    @Test
    fun permissionStatusIsDerivedFromScheduler() = runTest {
        scheduler.granted = true
        assertTrue(viewModel().uiState.value.hasNotificationPermission)

        scheduler.granted = false
        assertFalse(viewModel().uiState.value.hasNotificationPermission)
    }

    @Test
    fun permissionRequestInFlightIsTrueOnlyWhileRequestIsPending() = runTest {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.permissionRequestInFlight)

        viewModel.onEvent(SettingsUiEvent.RequestPermission)
        runCurrent()
        assertTrue(viewModel.uiState.value.permissionRequestInFlight)

        scheduler.pendingRequest.complete(true)
        runCurrent()
        assertFalse(viewModel.uiState.value.permissionRequestInFlight)
    }

    @Test
    fun refreshPermissionPicksUpAChangeMadeOutsideTheApp() = runTest {
        scheduler.granted = false
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.hasNotificationPermission)

        scheduler.granted = true
        viewModel.onEvent(SettingsUiEvent.RefreshPermission)
        runCurrent()

        assertTrue(viewModel.uiState.value.hasNotificationPermission)
    }

    @Test
    fun grantedRequestUpdatesPermissionStatusAndSetsToast() = runTest {
        scheduler.granted = false
        val viewModel = viewModel()

        viewModel.onEvent(SettingsUiEvent.RequestPermission)
        runCurrent()
        scheduler.pendingRequest.complete(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.hasNotificationPermission)
        assertNotNull(viewModel.uiState.value.toastMessage)
    }

    @Test
    fun deniedRequestLeavesToastUnset() = runTest {
        scheduler.granted = false
        val viewModel = viewModel()

        viewModel.onEvent(SettingsUiEvent.RequestPermission)
        runCurrent()
        scheduler.pendingRequest.complete(false)
        runCurrent()

        assertFalse(viewModel.uiState.value.hasNotificationPermission)
        assertNull(viewModel.uiState.value.toastMessage)
    }
}

private class FakeLensCycleRepository(initial: LensCycle?) : LensCycleRepository {
    private val cycle = MutableStateFlow(initial)
    val saved = mutableListOf<LensCycle>()

    override fun observe(): Flow<LensCycle?> = cycle

    override suspend fun save(lensCycle: LensCycle) {
        saved += lensCycle
        cycle.value = lensCycle
    }

    override suspend fun markAsChanged() = Unit
}

private class FakeNotificationScheduler : NotificationScheduler {
    var granted = true

    /** Completed by the test to simulate the user answering the permission prompt. */
    val pendingRequest = CompletableDeferred<Boolean>()

    override fun schedule(nextTrigger: LocalDateTime, title: String, body: String) = Unit

    override fun cancel() = Unit

    override fun hasPermission(): Boolean = granted

    override suspend fun requestPermission(): Boolean {
        granted = pendingRequest.await()
        return granted
    }

    override fun openAppSettings() = Unit
}
