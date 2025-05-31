package com.weatherpossum.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    primary = Color(0xFF6CDBBC),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFF89F8D7),
    secondary = Color(0xFFB3CCBE),
    onSecondary = Color(0xFF1F352B),
    secondaryContainer = Color(0xFF354B41),
    onSecondaryContainer = Color(0xFFCEE9DA),
    tertiary = Color(0xFFA5CCDF),
    onTertiary = Color(0xFF073543),
    tertiaryContainer = Color(0xFF254C5B),
    onTertiaryContainer = Color(0xFFC1E8FB),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outline = Color(0xFF89938D),
    inverseOnSurface = Color(0xFF191C1A),
    inverseSurface = Color(0xFFE1E3DF),
    inversePrimary = Color(0xFF006C51),
    surfaceTint = Color(0xFF6CDBBC),
    outlineVariant = Color(0xFF404944),
    scrim = Color(0xFF000000),
)

@Composable
fun WeatherPossumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 