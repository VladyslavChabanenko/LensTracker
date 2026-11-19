package com.chb.lenstracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.chb.lenstracker.notification.AndroidPermissionBridge
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val permissionBridge: AndroidPermissionBridge by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light-only (see LensTrackerTheme), so pin the system bars to light: bare
        // enableEdgeToEdge() defaults to auto(), which takes its icon colour from the system
        // dark-mode setting and paints white icons over our light background on a dark device.
        // `light` here describes the bar background — it is what yields dark icon content.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            NotificationPermissionHost(permissionBridge) {
                App()
            }
        }
    }
}

/**
 * Owns the single POST_NOTIFICATIONS launcher and services [AndroidPermissionBridge] requests.
 * Shows the system prompt when the OS will show it; otherwise (the user has denied it enough times
 * that Android stops asking) resolves immediately with the current status so the Settings screen
 * can send the user to the system settings page instead.
 */
@Composable
private fun NotificationPermissionHost(
    bridge: AndroidPermissionBridge,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        context.markNotificationPermissionRequested()
        bridge.complete(granted)
    }

    LaunchedEffect(bridge, launcher) {
        bridge.requestSignal.collect {
            // shouldShowRequestPermissionRationale() is also false before the very first request,
            // so "blocked" needs the persisted "already asked once" flag as well.
            val promptBlocked = activity == null || (
                context.wasNotificationPermissionRequested() &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            if (promptBlocked) {
                bridge.complete(context.hasNotificationPermission())
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    content()
}

private const val PREFS_NAME = "lens_tracker_permissions"
private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"

private fun Context.wasNotificationPermissionRequested(): Boolean =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)

private fun Context.markNotificationPermissionRequested() {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
    }
}

private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
