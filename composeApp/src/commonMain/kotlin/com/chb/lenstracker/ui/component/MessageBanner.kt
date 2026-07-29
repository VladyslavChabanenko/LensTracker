package com.chb.lenstracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chb.lenstracker.ui.theme.Accent100
import com.chb.lenstracker.ui.theme.Accent2_100
import com.chb.lenstracker.ui.theme.Accent2_200
import com.chb.lenstracker.ui.theme.Accent2_800
import com.chb.lenstracker.ui.theme.Accent2_900
import com.chb.lenstracker.ui.theme.Accent800
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.message_change_due_today
import com.chb.lenstracker.resources.message_notifications_disabled
import com.chb.lenstracker.resources.message_save_failed
import com.chb.lenstracker.ui.theme.CaptionStyle
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import org.jetbrains.compose.resources.stringResource

enum class MessageSeverity { ERROR, WARNING, INFO }

data class UiMessage(
    val text: String,
    val severity: MessageSeverity = MessageSeverity.WARNING
)

/**
 * Height [MessageBannerSlot] reserves whether or not it has anything to show, so nothing below
 * it shifts as messages appear and clear. Sized to a two-line message at this screen width.
 */
val MessageBannerSlotHeight = 68.dp

/**
 * Inline notice for errors, warnings and hints. The palette carries one alert ink (accent-2),
 * so errors and warnings share it and differ by step on the ramp; info takes the teal accent.
 */
@Composable
fun MessageBanner(message: UiMessage, modifier: Modifier = Modifier) {
    val (background, foreground) = when (message.severity) {
        MessageSeverity.ERROR -> Accent2_200 to Accent2_900
        MessageSeverity.WARNING -> Accent2_100 to Accent2_800
        MessageSeverity.INFO -> Accent100 to Accent800
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(background)
            .padding(15.dp)
    ) {
        Text(
            text = message.text,
            style = CaptionStyle.copy(fontSize = 13.sp),
            color = foreground
        )
    }
}

/**
 * Fixed-height region holding whatever notices the screen currently has, empty list included —
 * see [MessageBannerSlotHeight] for why the space is held even when there is nothing to show.
 */
@Composable
fun MessageBannerSlot(messages: List<UiMessage>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MessageBannerSlotHeight),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        messages.forEach { message -> MessageBanner(message = message) }
    }
}

@Preview
@Composable
private fun MessageBannerSeveritiesPreview() {
    LensTrackerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MessageBanner(
                UiMessage(stringResource(Res.string.message_save_failed), MessageSeverity.ERROR)
            )
            MessageBanner(
                UiMessage(
                    stringResource(Res.string.message_notifications_disabled),
                    MessageSeverity.WARNING
                )
            )
            MessageBanner(
                UiMessage(stringResource(Res.string.message_change_due_today), MessageSeverity.INFO)
            )
        }
    }
}
