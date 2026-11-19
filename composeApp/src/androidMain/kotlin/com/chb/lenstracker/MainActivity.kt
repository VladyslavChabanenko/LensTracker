package com.chb.lenstracker

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
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
 * When Android will no longer show the prompt (the user denied it enough times), launching it
 * returns "denied" immediately without any UI, so the Settings screen can then offer the
 * system-settings shortcut — no need to predict that case up front.
 */
@Composable
private fun NotificationPermissionHost(
    bridge: AndroidPermissionBridge,
    content: @Composable () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        bridge.complete(granted)
    }

    LaunchedEffect(bridge, launcher) {
        bridge.requestSignal.collect {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    content()
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
