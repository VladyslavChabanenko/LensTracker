package com.chb.lenstracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import kotlin.math.abs

/**
 * Two-layer bar matching the design's base (elapsed) + overdue overlay: the overdue
 * segment is drawn on top of the base segment from the left edge, not appended after it.
 */
@Composable
fun LensProgressBar(
    baseFraction: Float,
    overdueFraction: Float,
    daysRemaining: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(baseFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(overdueFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error)
            )
        }
        val label = if (daysRemaining < 0) {
            "${abs(daysRemaining)} days overdue"
        } else {
            "$daysRemaining days left"
        }
        Text(text = label)
    }
}

@Preview
@Composable
private fun LensProgressBarNotFilledPreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 0f, overdueFraction = 0f, daysRemaining = 14)
    }
}

@Preview
@Composable
private fun LensProgressBarPartiallyFilledPreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 0.5f, overdueFraction = 0f, daysRemaining = 7)
    }
}

@Preview
@Composable
private fun LensProgressBarOverduePreview() {
    LensTrackerTheme {
        LensProgressBar(baseFraction = 1f, overdueFraction = 3f / 14f, daysRemaining = -3)
    }
}
