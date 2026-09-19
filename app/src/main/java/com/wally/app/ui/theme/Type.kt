package com.wally.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Condensed display face (Bebas Neue style)
val CondensedDisplayFont = FontFamily.SansSerif

// Body face (Inter style)
val InterBodyFont = FontFamily.SansSerif

val WallyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CondensedDisplayFont,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 1.5.sp,
        color = WallyText
    ),
    displayMedium = TextStyle(
        fontFamily = CondensedDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 1.2.sp,
        color = WallyText
    ),
    displaySmall = TextStyle(
        fontFamily = CondensedDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.0.sp,
        color = WallyText
    ),
    headlineMedium = TextStyle(
        fontFamily = CondensedDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.8.sp,
        color = WallyText
    ),
    titleLarge = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = WallyText
    ),
    titleMedium = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = WallyText
    ),
    bodyLarge = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        color = WallyText
    ),
    bodyMedium = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
        color = WallyMuted
    ),
    labelLarge = TextStyle(
        fontFamily = CondensedDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp,
        color = WallyText
    ),
    labelMedium = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = WallyMuted
    ),
    labelSmall = TextStyle(
        fontFamily = InterBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.4.sp,
        color = WallyMuted
    )
)
