package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RevelaPurple,
    secondary = RevelaCoral,
    tertiary = RevelaYellow,
    background = SlateMidnight,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = OnDarkSurface,
    onSecondary = OnDarkSurface,
    onTertiary = SlateMidnight,
    onBackground = OnDarkBackground,
    onSurface = OnDarkSurface,
    onSurfaceVariant = OnDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = RevelaPurple,
    secondary = RevelaCoral,
    tertiary = RevelaTurquoise,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = OnLightSurface,
    onSecondary = OnLightSurface,
    onTertiary = OnLightSurface,
    onBackground = OnLightBackground,
    onSurface = OnLightSurface,
    onSurfaceVariant = OnLightSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desabilitamos dynamicColor por padrão para preservar as cores fortes do Design System Revela
    dynamicColor: Boolean = false,
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
