package com.chb.lenstracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.Default
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
