package com.example.ai.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TravelBlue,
    onPrimary = TravelSurfaceLight,
    primaryContainer = TravelSurfaceVariantLight,
    onPrimaryContainer = TravelOnSurfaceLight,
    secondary = TravelTeal,
    onSecondary = TravelSurfaceLight,
    tertiary = TravelAmber,
    background = TravelBackgroundLight,
    onBackground = TravelOnSurfaceLight,
    surface = TravelSurfaceLight,
    onSurface = TravelOnSurfaceLight,
    surfaceVariant = TravelSurfaceVariantLight,
    onSurfaceVariant = TravelOnSurfaceVariantLight,
    outline = TravelOutlineLight,
    error = TravelErrorLight,
    onError = TravelSurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.DarkPrimary,
    onPrimary = TravelBackgroundDark,
    primaryContainer = TravelSurfaceVariantDark,
    onPrimaryContainer = TravelOnSurfaceDark,
    secondary = TravelTeal,
    onSecondary = TravelBackgroundDark,
    tertiary = TravelAmber,
    background = TravelBackgroundDark,
    onBackground = TravelOnSurfaceDark,
    surface = TravelSurfaceDark,
    onSurface = TravelOnSurfaceDark,
    surfaceVariant = TravelSurfaceVariantDark,
    onSurfaceVariant = TravelOnSurfaceVariantDark,
    outline = TravelOutlineDark,
    error = TravelErrorDark,
    onError = TravelBackgroundDark
)

private object ColorTokens {
    val DarkPrimary = androidx.compose.ui.graphics.Color(0xFF93C5FD)
}

@Composable
fun AITravelMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TravelTypography,
        shapes = TravelShapes,
        content = content
    )
}
