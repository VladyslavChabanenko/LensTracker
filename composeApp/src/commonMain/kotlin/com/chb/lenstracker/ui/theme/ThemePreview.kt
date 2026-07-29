package com.chb.lenstracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun ColorSwatch(name: String, background: androidx.compose.ui.graphics.Color, foreground: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(background),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = name, color = foreground, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun ThemeSwatches() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ColorSwatch("primary", colors.primary, colors.onPrimary)
        ColorSwatch("primaryContainer", colors.primaryContainer, colors.onPrimaryContainer)
        ColorSwatch("error", colors.error, colors.onError)
        ColorSwatch("errorContainer", colors.errorContainer, colors.onErrorContainer)
        ColorSwatch("surface", colors.surface, colors.onSurface)
        ColorSwatch("surfaceVariant", colors.surfaceVariant, colors.onSurfaceVariant)
        ColorSwatch("background", colors.background, colors.onBackground)
    }
}

@Preview
@Composable
private fun ThemeSwatchesPreview() {
    LensTrackerTheme {
        ThemeSwatches()
    }
}
