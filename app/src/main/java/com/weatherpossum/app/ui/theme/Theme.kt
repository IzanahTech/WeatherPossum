package com.weatherpossum.app.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF89F8D7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEE9DA),
    onSecondaryContainer = Color(0xFF092016),
    tertiary = Color(0xFF3D6373),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC1E8FB),
    onTertiaryContainer = Color(0xFF001F29),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    inverseOnSurface = Color(0xFFEFF1ED),
    inverseSurface = Color(0xFF2E312F),
    inversePrimary = Color(0xFF6CDBBC),
    surfaceTint = Color(0xFF006C51),
    outlineVariant = Color(0xFFBFC9C2),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DE8C8),
    onPrimary = Color(0xFF002B1D),
    primaryContainer = Color(0xFF004D3A),
    onPrimaryContainer = Color(0xFFA0FFE0),
    secondary = Color(0xFFC4DED0),
    onSecondary = Color(0xFF1A2F26),
    secondaryContainer = Color(0xFF2F453B),
    onSecondaryContainer = Color(0xFFD8F5E4),
    tertiary = Color(0xFFB5DCEF),
    onTertiary = Color(0xFF052D3B),
    tertiaryContainer = Color(0xFF1E4352),
    onTertiaryContainer = Color(0xFFD1F0FF),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111413),
    onBackground = Color(0xFFE8EAE6),
    surface = Color(0xFF111413),
    onSurface = Color(0xFFE8EAE6),
    surfaceVariant = Color(0xFF2D3530),
    onSurfaceVariant = Color(0xFFC5CFC8),
    outline = Color(0xFF8F9A93),
    inverseOnSurface = Color(0xFF111413),
    inverseSurface = Color(0xFFE8EAE6),
    inversePrimary = Color(0xFF005A42),
    surfaceTint = Color(0xFF7DE8C8),
    outlineVariant = Color(0xFF2D3530),
    scrim = Color(0xFF000000),
)

@Composable
fun WeatherPossumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme) {
            val activity = view.context as ComponentActivity
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.surface.toArgb(),
                    darkScrim = colorScheme.surface.toArgb()
                ),
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.surface.toArgb(),
                    darkScrim = colorScheme.surface.toArgb()
                )
            )
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 