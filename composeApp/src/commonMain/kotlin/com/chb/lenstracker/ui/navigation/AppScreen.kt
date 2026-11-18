package com.chb.lenstracker.ui.navigation

import androidx.compose.runtime.saveable.Saver

/** The app's two destinations. Held as plain state in the app root — there is no back stack. */
sealed class AppScreen {
    data object Main : AppScreen()
    data object Settings : AppScreen()
}

/** Lets the current screen survive rotation and process restart via `rememberSaveable`. */
val AppScreenSaver: Saver<AppScreen, String> = Saver(
    save = {
        when (it) {
            AppScreen.Main -> "main"
            AppScreen.Settings -> "settings"
        }
    },
    restore = {
        when (it) {
            "settings" -> AppScreen.Settings
            else -> AppScreen.Main
        }
    }
)
