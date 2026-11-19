package com.chb.lenstracker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chb.lenstracker.resources.Res
import com.chb.lenstracker.resources.action_back
import com.chb.lenstracker.resources.settings_title
import com.chb.lenstracker.ui.theme.ColorAccent
import com.chb.lenstracker.ui.theme.LensTrackerTheme
import com.chb.lenstracker.ui.theme.ScreenTitleStyle
import org.jetbrains.compose.resources.stringResource

/**
 * Screen header with the [title] centered. [onBack], when set, adds a back arrow at the start;
 * [onOpenSettings], when set, adds a gear at the end.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            HeaderIcon(
                icon = BackArrowIcon,
                glyphMargin = BackArrowMargin,
                atStart = true,
                contentDescription = stringResource(Res.string.action_back),
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        Text(text = title, style = ScreenTitleStyle, color = ColorAccent)
        if (onOpenSettings != null) {
            HeaderIcon(
                icon = SettingsIcon,
                glyphMargin = SettingsMargin,
                atStart = false,
                contentDescription = stringResource(Res.string.settings_title),
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/** Distance from the screen edge to the visible edge of a header icon and of the page content. */
private val ScreenEdgeInset = 16.dp

/**
 * Padding for everything below the [ScreenHeader]: [ScreenEdgeInset] on each side, measured from the
 * screen edge. Apply it inside `safeDrawingPadding()`, which already handles the system bars and
 * cutouts — adding vertical insets here would count them twice on iOS.
 */
fun Modifier.screenContentPadding(): Modifier = padding(horizontal = ScreenEdgeInset)

// Each Material glyph has a little blank space inside its 24dp viewport; these are subtracted from
// the edge inset so the *drawn* shape, not the viewport, sits [ScreenEdgeInset] from the edge.
private val BackArrowMargin = 4.dp
private val SettingsMargin = 2.74.dp

/**
 * Icon whose drawn edge is [ScreenEdgeInset] from the screen edge on its [atStart] side. The 48dp
 * touch target starts at the screen edge. Plain clickable with no indication: IconButton draws a
 * circular container and ripple.
 */
@Composable
private fun HeaderIcon(
    icon: ImageVector,
    glyphMargin: Dp,
    atStart: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgePadding = ScreenEdgeInset - glyphMargin
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = if (atStart) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = ColorAccent,
            modifier = Modifier.padding(
                start = if (atStart) edgePadding else 0.dp,
                end = if (atStart) 0.dp else edgePadding
            )
        )
    }
}

private fun materialIcon(pathData: String): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).addPath(
    pathData = addPathNodes(pathData),
    fill = SolidColor(Color.Black)
).build()

// The project has no icon-set dependency, so the Material glyphs are drawn from their path data.
private val BackArrowIcon: ImageVector =
    materialIcon("M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")

private val SettingsIcon: ImageVector = materialIcon(
    "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58" +
        "c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96" +
        "c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84" +
        "c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33" +
        "c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58" +
        "C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61" +
        "l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54" +
        "c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54" +
        "c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32" +
        "c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94z" +
        "M12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"
)

@Preview
@Composable
private fun ScreenHeaderPreview() {
    LensTrackerTheme {
        ScreenHeader(title = "Settings", onBack = {}, onOpenSettings = {})
    }
}
