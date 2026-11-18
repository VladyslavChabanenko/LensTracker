package com.chb.lenstracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.chb.lenstracker.ui.navigation.AppScreen
import com.chb.lenstracker.ui.navigation.AppScreenSaver
import com.chb.lenstracker.ui.screen.HomeScreen
import com.chb.lenstracker.ui.screen.SettingsScreen
import com.chb.lenstracker.ui.theme.LensTrackerTheme

@Composable
@Preview
fun App() {
    LensTrackerTheme {
        var screen by rememberSaveable(stateSaver = AppScreenSaver) {
            mutableStateOf<AppScreen>(AppScreen.Main)
        }
        when (screen) {
            AppScreen.Main -> HomeScreen(onOpenSettings = { screen = AppScreen.Settings })
            AppScreen.Settings -> SettingsScreen(onBack = { screen = AppScreen.Main })
        }
    }
}
