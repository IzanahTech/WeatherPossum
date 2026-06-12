package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.weatherpossum.app.ui.theme.WeatherPossumMotion

/**
 * Soft scroll-linked depth for list cards.
 */
fun Modifier.parallaxScrollEffect(
    index: Int,
    firstVisibleIndex: Int,
    scrollOffset: Int
): Modifier = composed {
    val progress = ((index - firstVisibleIndex) - scrollOffset / 320f).coerceIn(-1f, 1f)
    val targetScale = 1f - (0.022f * progress.coerceAtLeast(0f))
    val targetTranslation = progress * 10f
    val targetAlpha = 1f - (0.06f * progress.coerceAtLeast(0f))

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "parallaxScale_$index"
    )
    val translation by animateFloatAsState(
        targetValue = targetTranslation,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "parallaxTranslation_$index"
    )
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "parallaxAlpha_$index"
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = translation
        this.alpha = alpha
    }
}
