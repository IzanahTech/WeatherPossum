package com.weatherpossum.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/** Shared [HazeState] for backdrop blur on glass surfaces. */
val LocalWeatherHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun rememberWeatherHazeState(): HazeState = rememberHazeState()

@Composable
fun Modifier.weatherHazeSource(state: HazeState): Modifier = hazeSource(state = state)

@Composable
fun Modifier.backdropGlassBlur(
    shape: Shape,
    tintTop: Color,
    tintBottom: Color,
    isDarkMode: Boolean
): Modifier {
    val hazeState = LocalWeatherHazeState.current ?: return this
    val tintColor = remember(tintTop, tintBottom, isDarkMode) {
        val alpha = if (isDarkMode) 0.36f else 0.3f
        Color(
            red = (tintTop.red + tintBottom.red) / 2f,
            green = (tintTop.green + tintBottom.green) / 2f,
            blue = (tintTop.blue + tintBottom.blue) / 2f,
            alpha = alpha
        )
    }

    return clip(shape).hazeEffect(state = hazeState) {
        blurRadius = WeatherPossumGlass.blurRadius
        tints = listOf(HazeTint(tintColor))
        noiseFactor = WeatherPossumGlass.noiseFactor
    }
}

@Composable
fun Modifier.backdropNavGlassBlur(
    shape: Shape,
    tintTop: Color,
    tintBottom: Color,
    isDarkMode: Boolean
): Modifier {
    val hazeState = LocalWeatherHazeState.current ?: return this
    val tintColor = remember(tintTop, tintBottom, isDarkMode) {
        val alpha = if (isDarkMode) 0.58f else 0.52f
        Color(
            red = (tintTop.red + tintBottom.red) / 2f,
            green = (tintTop.green + tintBottom.green) / 2f,
            blue = (tintTop.blue + tintBottom.blue) / 2f,
            alpha = alpha
        )
    }

    return clip(shape).hazeEffect(state = hazeState) {
        blurRadius = WeatherPossumGlass.blurRadius
        tints = listOf(HazeTint(tintColor))
        noiseFactor = WeatherPossumGlass.noiseFactor
    }
}
