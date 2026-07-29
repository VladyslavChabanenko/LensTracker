package com.chb.lenstracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme only — this app does not support a dark theme.
private val LensTrackerColorScheme = lightColorScheme(
    primary = ColorAccent,
    onPrimary = Color.White,
    primaryContainer = Accent100,
    onPrimaryContainer = Accent800,
    secondary = ColorAccent,
    onSecondary = Color.White,
    error = ColorAccent2,
    onError = Color.White,
    errorContainer = Accent2_100,
    onErrorContainer = Accent2_800,
    background = ColorBg,
    onBackground = ColorText,
    surface = ColorSurface,
    onSurface = ColorText,
    surfaceVariant = ColorSurface,
    onSurfaceVariant = Neutral600,
    outline = Neutral300
)

@Composable
fun LensTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LensTrackerColorScheme,
        typography = LensTrackerTypography,
        content = content
    )
}
