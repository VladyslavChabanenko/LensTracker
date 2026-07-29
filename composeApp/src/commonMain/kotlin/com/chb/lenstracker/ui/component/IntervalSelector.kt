package com.chb.lenstracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.interval_daily
import com.chb.lenstracker.resources.interval_monthly
import com.chb.lenstracker.resources.interval_two_weeks
import com.chb.lenstracker.resources.interval_weekly
import com.chb.lenstracker.ui.theme.ColorAccent
import com.chb.lenstracker.ui.theme.ColorBg
import com.chb.lenstracker.ui.theme.ColorDivider
import com.chb.lenstracker.ui.theme.ColorText
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import com.chb.lenstracker.ui.theme.SegOptionStyle
import androidx.compose.material3.Text
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderInterval.displayLabel(): String = stringResource(
    when (this) {
        ReminderInterval.DAILY -> Res.string.interval_daily
        ReminderInterval.WEEKLY -> Res.string.interval_weekly
        ReminderInterval.TWO_WEEKS -> Res.string.interval_two_weeks
        ReminderInterval.MONTHLY -> Res.string.interval_monthly
    }
)

/** Segmented interval control — .seg/.seg-opt in the design: bordered pill, selected segment filled with the accent. */
@Composable
fun IntervalSelector(
    selected: ReminderInterval,
    onSelect: (ReminderInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, ColorDivider, RoundedCornerShape(2.dp))
    ) {
        ReminderInterval.entries.forEachIndexed { index, interval ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(ColorDivider)
                )
            }
            val isSelected = interval == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) ColorAccent else ColorBg)
                    .clickable { onSelect(interval) }
                    .padding(vertical = 7.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = interval.displayLabel(),
                    style = SegOptionStyle,
                    color = if (isSelected) ColorBg else ColorText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun IntervalSelectorPreview() {
    LensTrackerTheme {
        IntervalSelector(selected = ReminderInterval.TWO_WEEKS, onSelect = {})
    }
}
