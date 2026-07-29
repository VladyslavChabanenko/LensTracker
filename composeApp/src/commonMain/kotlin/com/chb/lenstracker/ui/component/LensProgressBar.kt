package com.chb.lenstracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.ui.theme.Accent2_600
import com.chb.lenstracker.ui.theme.ColorAccent
import com.chb.lenstracker.ui.theme.ColorBg
import com.chb.lenstracker.ui.theme.LensTrackerTheme

/**
 * Two-layer bar matching the design's base (elapsed) + overdue overlay: the overdue
 * segment is drawn on top of the base segment from the left edge, not appended after it.
 */
@Composable
fun LensProgressBar(
    baseFraction: Float,
    overdueFraction: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(ColorBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(baseFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(ColorAccent)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(overdueFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Accent2_600)
        )
    }
}

@Preview
@Composable
private fun LensProgressBarNotFilledPreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 0f, overdueFraction = 0f)
    }
}

@Preview
@Composable
private fun LensProgressBarPartiallyFilledPreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 0.5f, overdueFraction = 0f)
    }
}

@Preview
@Composable
private fun LensProgressBarOverduePreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 1f, overdueFraction = 3f / 14f)
    }
}
