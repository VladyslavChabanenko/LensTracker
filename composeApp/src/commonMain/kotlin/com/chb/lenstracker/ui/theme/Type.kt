package com.chb.lenstracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.SansSerif
private const val HeadingWeight = 600

val LensTrackerTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        displayMedium = base.displayMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        displaySmall = base.displaySmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        headlineLarge = base.headlineLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        headlineMedium = base.headlineMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        headlineSmall = base.headlineSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        titleLarge = base.titleLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight)),
        titleMedium = base.titleMedium.copy(fontFamily = AppFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = AppFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = AppFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = AppFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = AppFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = AppFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = AppFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = AppFontFamily)
    )
}

/** Large numeric status figure ("14", "2") — .card .status number, 52px/600 in the design. */
val StatusFigureStyle: TextStyle
    get() = LensTrackerTypography.displayLarge.copy(fontWeight = FontWeight(HeadingWeight))

/** Uppercase section kicker ("STATUS") — .card-kicker: 10px, letter-spacing 0.1em, accent color. */
val SectionKickerStyle: TextStyle
    get() = LensTrackerTypography.labelSmall.copy(letterSpacing = 1.1.sp)

/** Screen title ("Lens Reminder") — inline h1 override on the main screen: 26px/600. */
val ScreenTitleStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 22.sp)

/** Status card's big number ("14", "Today") — 52px/600, line-height 1. */
val BigNumberStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight(HeadingWeight), fontSize = 52.sp, lineHeight = 52.sp)

/** Status card's sub-label ("days left", "change is due") — 15px, 75% opacity at call site. */
val SubLabelStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp)

/** Field label ("Start date", "Replacement interval") — 12px, 70% opacity at call site. */
val FieldLabelStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp)

/** Muted caption ("Next change: …") — 12px, 60% opacity at call site. */
val CaptionStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp)

/** Segmented-control option label — .seg-opt: 13px. */
val SegOptionStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp)

/** Pill tag label ("2 Weeks") — .tag: 11px, letter-spacing 0.02em. */
val TagStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, letterSpacing = 0.22.sp)

/** Input field text — .input: 14px. */
val InputTextStyle: TextStyle
    get() = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp)
