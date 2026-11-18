package com.chb.lenstracker.ui.navigation

/** The app's two destinations. Held as plain state in the app root — there is no back stack. */
sealed class AppScreen {
    data object Main : AppScreen()
    data object Settings : AppScreen()
}
