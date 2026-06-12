package com.weatherpossum.app.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes as MaterialShapes
import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Shape tokens ─────────────────────────────────────────────────────────────

val Shapes = MaterialShapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ── Typography ───────────────────────────────────────────────────────────────

val Typography = MaterialTypography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ── Color schemes ────────────────────────────────────────────────────────────
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
    
    // Surfaces
    background = Color(0xFFE8F4FC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFDCE3EA),
    onSurfaceVariant = Color(0xFF334155),
    
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
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}