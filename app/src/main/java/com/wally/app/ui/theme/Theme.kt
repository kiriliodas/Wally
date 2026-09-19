package com.wally.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WallyBronze,
    onPrimary = WallyBg,
    primaryContainer = WallyCard,
    onPrimaryContainer = WallyBronzeLight,
    secondary = WallyBronzeLight,
    onSecondary = WallyBg,
    background = WallyBg,
    onBackground = WallyText,
    surface = WallyCard,
    onSurface = WallyText,
    surfaceVariant = WallyCard,
    onSurfaceVariant = WallyMuted,
    outline = WallyHairline,
    outlineVariant = WallyHairline
)

@Composable
fun WallyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = WallyBg.toArgb()
            window.navigationBarColor = WallyBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = WallyTypography,
        content = content
    )
}
