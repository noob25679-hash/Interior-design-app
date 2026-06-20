package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EditorialWhite,
    onPrimary = EditorialButtonBg,
    background = EditorialButtonBg,
    onBackground = EditorialBg,
    surface = Color(0xFF4A433E),
    onSurface = EditorialBg,
    outline = EditorialTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialButtonBg,
    onPrimary = EditorialWhite,
    background = EditorialBg,
    onBackground = EditorialTextMain,
    surface = EditorialSurface,
    onSurface = EditorialTextMain,
    outline = EditorialBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve brand-specific editorial design
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
