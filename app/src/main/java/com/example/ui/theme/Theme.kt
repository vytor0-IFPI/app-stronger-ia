package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StrongerCastDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF141414),
    primaryContainer = GoldSecondary,
    onPrimaryContainer = Color(0xFFFFF6E5),
    secondary = GoldSecondary,
    onSecondary = Color(0xFF141414),
    secondaryContainer = Color(0xFF33240E),
    onSecondaryContainer = GoldPrimary,
    tertiary = GoldTertiary,
    onTertiary = Color(0xFF141414),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun StrongerCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Stronger Cast enforces its signature dark & gold identity across all devices
    MaterialTheme(
        colorScheme = StrongerCastDarkColorScheme,
        typography = Typography,
        content = content
    )
}
