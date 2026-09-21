package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCobalt,
    onPrimary = Color.White,
    primaryContainer = TechCardDark,
    onPrimaryContainer = ElectricCyan,
    secondary = VividAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3B2E1E),
    onSecondaryContainer = VividAmber,
    tertiary = ElectricCyan,
    onTertiary = Color.Black,
    background = TechNavyDark,
    onBackground = TextPrimary,
    surface = TechSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = TechCardDark,
    onSurfaceVariant = TextSecondary,
    outline = TechBorder,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
