package com.chb.lenstracker.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.domain.model.ReminderInterval
import com.chb.lenstracker.ui.theme.LensTrackerTheme

private fun ReminderInterval.label(): String = when (this) {
    ReminderInterval.DAILY -> "Daily"
    ReminderInterval.WEEKLY -> "Weekly"
    ReminderInterval.TWO_WEEKS -> "2 Weeks"
    ReminderInterval.MONTHLY -> "Monthly"
}

@Composable
fun IntervalSelector(
    selected: ReminderInterval,
    onSelect: (ReminderInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReminderInterval.entries.forEach { interval ->
            FilterChip(
                selected = interval == selected,
                onClick = { onSelect(interval) },
                label = {
                    Text(
                        text = interval.label(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                modifier = Modifier.weight(1f)
            )
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
