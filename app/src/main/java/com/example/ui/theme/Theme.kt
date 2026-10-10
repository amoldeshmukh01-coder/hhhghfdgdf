package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimaryDark,
    onPrimary = Color(0xFF003822),
    primaryContainer = ForestGreenContainerDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = LeafGreenSecondary,
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = CleanSurfaceDark,
    surface = CleanSurfaceCardDark,
    onBackground = CleanTextPrimaryDark,
    onSurface = CleanTextPrimaryDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = LeafGreenSecondary,
    onSecondary = LeafGreenOnSecondary,
    secondaryContainer = LeafGreenContainer,
    onSecondaryContainer = LeafGreenOnContainer,
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberAccentContainer,
    onTertiaryContainer = AmberAccentOnContainer,
    background = CleanSurfaceLight,
    surface = CleanSurfaceCardLight,
    onBackground = CleanTextPrimary,
    onSurface = CleanTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    outline = CleanBorder,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve brand green colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
