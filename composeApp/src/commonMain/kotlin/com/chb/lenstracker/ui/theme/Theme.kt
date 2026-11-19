package com.chb.lenstracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
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
    outline = Neutral300,
    // Material's defaults for these roles are lavender-greys; keep every container neutral.
    outlineVariant = ColorDivider,
    secondaryContainer = Accent100,
    onSecondaryContainer = Accent800,
    surfaceDim = ColorSurface,
    surfaceBright = ColorSurface,
    surfaceContainerLowest = ColorSurface,
    surfaceContainerLow = ColorSurface,
    surfaceContainer = ColorSurface,
    surfaceContainerHigh = ColorSurface,
    surfaceContainerHighest = ColorSurface,
    inverseSurface = ColorText,
    inverseOnSurface = ColorBg
)

/** App-wide page background: a top-to-bottom fade from the lightest accent to white. */
val AppBackgroundBrush: Brush = Brush.verticalGradient(listOf(Accent100, Color.White))

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
