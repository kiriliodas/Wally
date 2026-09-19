package com.wally.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object WallyPalette {
    val background = Color(0xFF0A0A0A)
    val card = Color(0xFF1A1A1A)
    val accent = Color(0xFFC8832A)
    val accentLight = Color(0xFFE0A04A)
    val text = Color(0xFFF2EDE6)
    val muted = Color(0xFF8A8580)
    val hairline = Color(0xFF2A2622)
    val raised = Color(0xFF211E1B)
}

private val WallyColors = darkColorScheme(
    primary = WallyPalette.accent,
    onPrimary = WallyPalette.background,
    secondary = WallyPalette.accentLight,
    onSecondary = WallyPalette.background,
    background = WallyPalette.background,
    onBackground = WallyPalette.text,
    surface = WallyPalette.card,
    onSurface = WallyPalette.text,
    surfaceVariant = WallyPalette.raised,
    onSurfaceVariant = WallyPalette.muted,
    outline = WallyPalette.hairline,
)

private val WallyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = (-2.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 25.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.8).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.8.sp,
    ),
)

@Composable
fun WallyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WallyColors,
        typography = WallyTypography,
        content = content,
    )
}
