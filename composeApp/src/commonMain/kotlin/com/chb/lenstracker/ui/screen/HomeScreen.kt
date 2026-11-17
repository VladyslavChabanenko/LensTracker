package com.chb.lenstracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chb.lenstracker.domain.model.LensCycle
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.action_cancel
import com.chb.lenstracker.resources.action_mark_as_changed
import com.chb.lenstracker.resources.confirm_mark_as_changed_body
import com.chb.lenstracker.resources.confirm_mark_as_changed_title
import com.chb.lenstracker.resources.action_ok
import com.chb.lenstracker.resources.home_interval_label
import com.chb.lenstracker.resources.home_start_date_label
import com.chb.lenstracker.resources.home_start_date_placeholder
import com.chb.lenstracker.resources.home_title
import com.chb.lenstracker.resources.message_notifications_disabled
import com.chb.lenstracker.resources.status_change_due
import com.chb.lenstracker.resources.status_days_left
import com.chb.lenstracker.resources.status_days_overdue
import com.chb.lenstracker.resources.status_empty
import com.chb.lenstracker.resources.status_kicker
import com.chb.lenstracker.resources.status_next_change
import com.chb.lenstracker.resources.status_today
import com.chb.lenstracker.ui.component.ConfirmDialog
import com.chb.lenstracker.ui.component.ConfirmationToast
import com.chb.lenstracker.ui.component.IntervalSelector
import com.chb.lenstracker.ui.component.LensProgressBar
import com.chb.lenstracker.ui.component.MessageBannerSlot
import com.chb.lenstracker.ui.component.MessageSeverity
import com.chb.lenstracker.ui.component.UiMessage
import com.chb.lenstracker.ui.theme.Accent2_600
import com.chb.lenstracker.ui.theme.BigNumberStyle
import com.chb.lenstracker.ui.theme.CaptionStyle
import com.chb.lenstracker.ui.theme.ColorAccent
import com.chb.lenstracker.ui.theme.ColorDivider
import com.chb.lenstracker.ui.theme.ColorSurface
import com.chb.lenstracker.ui.theme.ColorText
import com.chb.lenstracker.ui.theme.FieldLabelStyle
import com.chb.lenstracker.ui.theme.InputTextStyle
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import com.chb.lenstracker.ui.theme.ScreenTitleStyle
import com.chb.lenstracker.ui.theme.SectionKickerStyle
import com.chb.lenstracker.ui.theme.SubLabelStyle
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    HomeScreenContent(uiState = uiState, today = today, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    today: LocalDate,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val lensCycle = uiState.lensCycle
    val notificationsDisabledMessage = stringResource(Res.string.message_notifications_disabled)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(text = stringResource(Res.string.home_title), style = ScreenTitleStyle, color = ColorAccent)
            }

            MessageBannerSlot(
                messages = buildList {
                    if (!uiState.hasNotificationPermission) {
                        add(
                            UiMessage(
                                text = notificationsDisabledMessage,
                                severity = MessageSeverity.WARNING
                            )
                        )
                    }
                }
            )

            StatusCard(state = uiState.statusCard)

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(Res.string.home_start_date_label),
                    style = FieldLabelStyle,
                    color = ColorText.copy(alpha = 0.7f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ColorSurface)
                        .border(1.dp, ColorDivider, RoundedCornerShape(2.dp))
                        .clickable { onEvent(HomeUiEvent.OpenDatePicker) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.startDateString
                                ?: stringResource(Res.string.home_start_date_placeholder),
                            style = InputTextStyle
                        )
                        Text(text = "▾", style = InputTextStyle, color = ColorText.copy(alpha = 0.5f))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(Res.string.home_interval_label),
                    style = FieldLabelStyle,
                    color = ColorText.copy(alpha = 0.7f)
                )
                IntervalSelector(
                    selected = uiState.selectedInterval,
                    onSelect = { interval -> onEvent(HomeUiEvent.SelectInterval(interval)) }
                )
            }

            if (lensCycle != null) {
                Button(
                    onClick = { onEvent(HomeUiEvent.OpenConfirmDialog) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.action_mark_as_changed))
                }
            }
        }

        ConfirmationToast(
            message = uiState.toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeContentPadding()
        )
    }

    if (uiState.showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(Res.string.confirm_mark_as_changed_title),
            body = stringResource(Res.string.confirm_mark_as_changed_body),
            confirmLabel = stringResource(Res.string.action_mark_as_changed),
            onConfirm = { onEvent(HomeUiEvent.MarkAsChanged) },
            onDismiss = { onEvent(HomeUiEvent.CloseConfirmDialog) }
        )
    }

    if (uiState.showDatePicker) {
        val todayMillis = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (lensCycle?.startDate ?: today)
                .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { onEvent(HomeUiEvent.CloseDatePicker) },
            confirmButton = {
                Button(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        onEvent(HomeUiEvent.SelectDate(date))
                    } else {
                        onEvent(HomeUiEvent.CloseDatePicker)
                    }
                }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onEvent(HomeUiEvent.CloseDatePicker) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Height the card holds in both its empty and populated states, so the screen doesn't reflow
 * when the first cycle is saved. Sized to the populated layout, which is the taller of the two.
 */
private val StatusCardMinHeight = 164.dp

@Composable
private fun StatusCard(state: StatusCardUiState?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = StatusCardMinHeight)
            .clip(RoundedCornerShape(2.dp))
            .background(ColorSurface)
            .padding(15.dp),
        verticalArrangement = if (state == null) Arrangement.Center else Arrangement.spacedBy(10.dp)
    ) {
        if (state == null) {
            Text(text = stringResource(Res.string.status_empty))
        } else {
            Text(text = stringResource(Res.string.status_kicker), style = SectionKickerStyle, color = ColorAccent)
            val (bigLabel, subLabel) = statusLabels(state.daysRemaining, state.isOverdue)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = bigLabel,
                    style = BigNumberStyle,
                    color = if (state.isOverdue) Accent2_600 else ColorAccent,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = subLabel,
                    style = SubLabelStyle,
                    color = ColorText.copy(alpha = 0.75f),
                    modifier = Modifier.alignByBaseline()
                )
            }
            LensProgressBar(
                baseFraction = state.baseProgressFraction,
                overdueFraction = state.overdueProgressFraction
            )
            Text(
                text = stringResource(Res.string.status_next_change, state.nextChangeDateString),
                style = CaptionStyle,
                color = ColorText.copy(alpha = 0.6f)
            )
        }
    }
}

/** "Today"/"change is due", "N day(s) overdue", or "N day(s) left" — mirrors the design's status copy. */
@Composable
private fun statusLabels(daysRemaining: Int, isOverdue: Boolean): Pair<String, String> = when {
    daysRemaining == 0 ->
        stringResource(Res.string.status_today) to stringResource(Res.string.status_change_due)
    isOverdue -> {
        val overdueDays = -daysRemaining
        overdueDays.toString() to
            pluralStringResource(Res.plurals.status_days_overdue, overdueDays)
    }
    else -> daysRemaining.toString() to
        pluralStringResource(Res.plurals.status_days_left, daysRemaining)
}

@Preview
@Composable
private fun HomeScreenOnTrackPreview() {
    val today = LocalDate(2026, 7, 29)
    LensTrackerTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                lensCycle = LensCycle(
                    startDate = LocalDate(2026, 7, 17),
                    interval = ReminderInterval.TWO_WEEKS,
                    reminderHour = 9,
                    reminderMinute = 0
                ),
                statusCard = StatusCardUiState(
                    nextChangeDateString = "July 31, 2026",
                    daysRemaining = 2,
                    isOverdue = false,
                    baseProgressFraction = 12f / 14f,
                    overdueProgressFraction = 0f
                ),
                startDateString = "July 17, 2026",
                hasNotificationPermission = true
            ),
            today = today,
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun HomeScreenOverduePreview() {
    val today = LocalDate(2026, 7, 29)
    LensTrackerTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                lensCycle = LensCycle(
                    startDate = LocalDate(2026, 7, 8),
                    interval = ReminderInterval.WEEKLY,
                    reminderHour = 9,
                    reminderMinute = 0
                ),
                statusCard = StatusCardUiState(
                    nextChangeDateString = "July 15, 2026",
                    daysRemaining = -3,
                    isOverdue = true,
                    baseProgressFraction = 1f,
                    overdueProgressFraction = 3f / 7f
                ),
                startDateString = "July 8, 2026",
                hasNotificationPermission = false
            ),
            today = today,
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    val today = LocalDate(2026, 7, 29)
    LensTrackerTheme {
        HomeScreenContent(uiState = HomeUiState(), today = today, onEvent = {})
    }
}
