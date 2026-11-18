package com.chb.lenstracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.action_enable
import com.chb.lenstracker.resources.action_open_settings
import com.chb.lenstracker.resources.settings_notifications_label
import com.chb.lenstracker.resources.settings_permission_allowed
import com.chb.lenstracker.resources.settings_permission_blocked_explanation
import com.chb.lenstracker.resources.settings_permission_denied
import com.chb.lenstracker.resources.settings_permission_denied_explanation
import com.chb.lenstracker.resources.settings_reminder_time_label
import com.chb.lenstracker.resources.settings_title
import com.chb.lenstracker.ui.component.ConfirmationToast
import com.chb.lenstracker.ui.component.ReminderTimePicker
import com.chb.lenstracker.ui.component.ScreenHeader
import com.chb.lenstracker.ui.component.screenContentPadding
import com.chb.lenstracker.ui.theme.Accent2_600
import com.chb.lenstracker.ui.theme.CaptionStyle
import com.chb.lenstracker.ui.theme.ColorAccent
import com.chb.lenstracker.ui.theme.ColorText
import com.chb.lenstracker.ui.theme.FieldLabelStyle
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import com.chb.lenstracker.ui.theme.TagStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The scheduler only reports a final granted/denied result, not whether the OS actually showed
    // its prompt, so "a request was made and permission is still denied" is the closest signal
    // available here for offering the system-settings shortcut (FR-011).
    var requestAttempted by rememberSaveable { mutableStateOf(false) }
    val isDenied = !uiState.hasNotificationPermission
    val showOpenSettings = isDenied && requestAttempted && !uiState.permissionRequestInFlight

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ScreenHeader(title = stringResource(Res.string.settings_title), onBack = onBack)

            Column(
                modifier = Modifier.screenContentPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_reminder_time_label),
                        style = FieldLabelStyle,
                        color = ColorText.copy(alpha = 0.7f)
                    )
                    ReminderTimePicker(
                        hour = uiState.reminderHour,
                        minute = uiState.reminderMinute,
                        onTimeChange = { hour, minute ->
                            onEvent(SettingsUiEvent.UpdateReminderTime(hour, minute))
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_notifications_label),
                        style = FieldLabelStyle,
                        color = ColorText.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                if (isDenied) Res.string.settings_permission_denied
                                else Res.string.settings_permission_allowed
                            ),
                            style = TagStyle,
                            color = if (isDenied) Accent2_600 else ColorAccent
                        )
                        if (isDenied) {
                            Button(
                                onClick = {
                                    requestAttempted = true
                                    onEvent(SettingsUiEvent.RequestPermission)
                                },
                                enabled = !uiState.permissionRequestInFlight
                            ) {
                                Text(stringResource(Res.string.action_enable))
                            }
                        }
                    }
                    if (isDenied) {
                        Text(
                            text = stringResource(Res.string.settings_permission_denied_explanation),
                            style = CaptionStyle,
                            color = ColorText.copy(alpha = 0.75f)
                        )
                    }
                    if (showOpenSettings) {
                        Text(
                            text = stringResource(Res.string.settings_permission_blocked_explanation),
                            style = CaptionStyle,
                            color = ColorText.copy(alpha = 0.75f)
                        )
                        OutlinedButton(onClick = { onEvent(SettingsUiEvent.OpenAppSettings) }) {
                            Text(stringResource(Res.string.action_open_settings))
                        }
                    }
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
}

@Preview
@Composable
private fun SettingsScreenAllowedPreview() {
    LensTrackerTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(reminderHour = 9, reminderMinute = 30),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDeniedPreview() {
    LensTrackerTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(hasNotificationPermission = false),
            onEvent = {},
            onBack = {}
        )
    }
}
