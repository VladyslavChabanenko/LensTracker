package com.chb.lenstracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.ui.theme.ColorDivider
import com.chb.lenstracker.ui.theme.ColorSurface
import com.chb.lenstracker.ui.theme.ColorText
import com.chb.lenstracker.ui.theme.InputTextStyle
import com.chb.lenstracker.ui.theme.LensTrackerTheme

private val Hours = 0..23
private val Minutes = listOf(0, 15, 30, 45)

/** Reminder time as an hour dropdown (0–23) plus a minute dropdown limited to 15-minute steps. */
@Composable
fun ReminderTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeDropdown(
            value = hour,
            options = Hours.toList(),
            onSelect = { onTimeChange(it, minute) }
        )
        Text(text = ":", style = InputTextStyle)
        TimeDropdown(
            value = minute,
            options = Minutes,
            onSelect = { onTimeChange(hour, it) }
        )
    }
}

@Composable
private fun TimeDropdown(
    value: Int,
    options: List<Int>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .widthIn(min = 72.dp)
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ColorSurface)
                .border(1.dp, ColorDivider, RoundedCornerShape(2.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value.twoDigits(), style = InputTextStyle)
            Text(
                text = "▾",
                style = InputTextStyle,
                color = ColorText.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.twoDigits(), style = InputTextStyle) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

@Preview
@Composable
private fun ReminderTimePickerPreview() {
    LensTrackerTheme {
        ReminderTimePicker(hour = 9, minute = 30, onTimeChange = { _, _ -> })
    }
}
