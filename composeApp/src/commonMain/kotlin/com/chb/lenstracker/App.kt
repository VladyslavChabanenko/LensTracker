package com.chb.lenstracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import com.chb.lenstracker.ui.navigation.AppScreen
import com.chb.lenstracker.ui.navigation.AppScreenSaver
import com.chb.lenstracker.ui.screen.HomeScreen
import com.chb.lenstracker.ui.screen.SettingsScreen
import com.chb.lenstracker.ui.theme.AppBackgroundBrush
import com.chb.lenstracker.ui.theme.LensTrackerTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    LensTrackerTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppBackgroundBrush)) {
            var screen by rememberSaveable(stateSaver = AppScreenSaver) {
                mutableStateOf<AppScreen>(AppScreen.Main)
            }
            // System back (Android back button/gesture, iOS back swipe) leaves Settings instead of the app.
            BackHandler(enabled = screen == AppScreen.Settings) { screen = AppScreen.Main }
            when (screen) {
                AppScreen.Main -> HomeScreen(onOpenSettings = { screen = AppScreen.Settings })
                AppScreen.Settings -> SettingsScreen(onBack = { screen = AppScreen.Main })
            }
        }
    }
}
