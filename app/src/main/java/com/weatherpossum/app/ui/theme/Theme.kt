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

// -------------------------------------------------------------------
// EXPRESSIVE LIGHT COLOR SCHEME: High contrast, saturated colors
// -------------------------------------------------------------------
private val LightColorScheme = lightColorScheme(
    // Primary: Vibrant Teal/Aqua for main focus
    primary = Color(0xFF00BFA5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF64FFDA), // Brighter container
    onPrimaryContainer = Color(0xFF004D40), // Darker, higher contrast text
    
    // Secondary: Bold Yellow/Gold for accents
    secondary = Color(0xFFFFC107),
    onSecondary = Color(0xFF2B2B2B), // Dark text on bright yellow
    secondaryContainer = Color(0xFFFFEB3B),
    onSecondaryContainer = Color(0xFF424242),
    
    // Tertiary: Deep Royal Blue for secondary accents
    tertiary = Color(0xFF3F51B5),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC5CAE9),
    onTertiaryContainer = Color(0xFF1A237E),
    
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFCDD2),
    onError = Color.White,
    onErrorContainer = Color(0xFF421415),
    
    // Surfaces: Clean White for maximum pop of color
    background = Color(0xFFF5F5F5), // Slightly off-white
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFFFFFF), 
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF404944),
    
    outline = Color(0xFFBDBDBD),
    inverseOnSurface = Color(0xFF111413),
    inverseSurface = Color(0xFFE0E0E0),
    inversePrimary = Color(0xFF6CDBBC),
    surfaceTint = Color(0xFF00BFA5),
    outlineVariant = Color(0xFFBFC9C2),
    scrim = Color(0xFF000000),
)

// -------------------------------------------------------------------
// EXPRESSIVE DARK COLOR SCHEME: Deep background, neon-like highlights
// -------------------------------------------------------------------
private val DarkColorScheme = darkColorScheme(
    // Primary: Brighter Aqua for high visibility
    primary = Color(0xFF26A69A),
    onPrimary = Color(0xFF000000), // Pure black text for ultimate pop
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2FF59), // Neon green highlight
    
    // Secondary: Bright Orange for energy
    secondary = Color(0xFFFF9800),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFC64900),
    onSecondaryContainer = Color(0xFFFFD180),
    
    // Tertiary: Electric Purple/Pink
    tertiary = Color(0xFFD1C4E9),
    onTertiary = Color(0xFF280A4B),
    tertiaryContainer = Color(0xFF512DA8),
    onTertiaryContainer = Color(0xFFEFEBE9),
    
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Surfaces: Near-Black for contrast and depth
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1D1D1D), 
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF383838),
    onSurfaceVariant = Color(0xFFD6D6D6),
    
    outline = Color(0xFF8F9A93),
    inverseOnSurface = Color(0xFF212121),
    inverseSurface = Color(0xFFE8EAE6),
    inversePrimary = Color(0xFF005A42),
    surfaceTint = Color(0xFF26A69A),
    outlineVariant = Color(0xFF404944),
    scrim = Color(0xFF000000),
)

// -------------------------------------------------------------------
// THEME COMPOSABLE
// -------------------------------------------------------------------

@Composable
fun WeatherPossumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            // Note: Dynamic color will override the expressive themes above if enabled
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme) {
            val activity = view.context as ComponentActivity
            // Enable edge-to-edge drawing
            activity.enableEdgeToEdge(
                // Use a subtle, expressive dimming of the surface for status bar scrims
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.surface.copy(alpha = 0.9f).toArgb(),
                    darkScrim = colorScheme.surface.copy(alpha = 0.9f).toArgb()
                ),
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.surface.copy(alpha = 0.9f).toArgb(),
                    darkScrim = colorScheme.surface.copy(alpha = 0.9f).toArgb()
                )
            )
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Assuming Typography and Shapes are defined in companion files (Typography.kt and Shapes.kt)
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}