package com.weatherpossum.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired liquid glass: frosted base + colored tint + specular rim.
 * Tints reuse [WeatherPossumGradients] hue families at lower alpha so the
 * screen background subtly shows through.
 */
object WeatherPossumGlass {

    val cardCornerRadius = WeatherPossumDimens.cardCornerRadius
    val blurRadius = 28.dp
    val noiseFactor = 0.1f

    fun frostBrush(isDarkMode: Boolean): Brush = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF2A3548).copy(alpha = 0.28f),
                Color(0xFF121A28).copy(alpha = 0.38f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.14f),
                Color(0xFFE3F2FD).copy(alpha = 0.1f)
            )
        )
    }

    fun tintBrush(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Brush {
        val topAlpha = if (isDarkMode) 0.22f else 0.46f
        val bottomAlpha = if (isDarkMode) 0.28f else 0.52f
        return Brush.verticalGradient(
            listOf(
                tintTop.copy(alpha = topAlpha),
                tintBottom.copy(alpha = bottomAlpha)
            )
        )
    }

    fun rimBrush(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Brush {
        val highlight = if (isDarkMode) 0.55f else 0.82f
        val mid = if (isDarkMode) 0.28f else 0.45f
        return Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = highlight),
                tintTop.copy(alpha = mid),
                tintBottom.copy(alpha = mid * 0.85f),
                Color.White.copy(alpha = highlight * 0.35f)
            ),
            start = Offset.Zero,
            end = Offset(800f, 1200f)
        )
    }

    fun colorsForStyle(style: CardGradientStyle, isDarkMode: Boolean): Pair<Color, Color> =
        WeatherPossumGradients.colors(style, isDarkMode)

    fun onColorForStyle(style: CardGradientStyle, isDarkMode: Boolean): Color =
        WeatherPossumColors.onColorForStyle(style, isDarkMode)

    fun onColorForTint(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Color =
        WeatherPossumColors.onColorForTint(tintTop, tintBottom, isDarkMode)

    fun drawSpecularSheen(scope: DrawScope, cornerRadius: Dp, isDarkMode: Boolean) {
        val radius = with(scope) { cornerRadius.toPx() }
        scope.drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDarkMode) 0.14f else 0.48f),
                    Color.White.copy(alpha = if (isDarkMode) 0.04f else 0.12f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = scope.size.height * 0.42f
            ),
            cornerRadius = CornerRadius(radius)
        )
        scope.drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = if (isDarkMode) 0.1f else 0.22f), Color.Transparent),
                center = Offset(scope.size.width * 0.18f, scope.size.height * 0.12f),
                radius = scope.size.maxDimension * 0.65f
            ),
            cornerRadius = CornerRadius(radius)
        )
    }

    fun drawInnerRim(scope: DrawScope, cornerRadius: Dp, isDarkMode: Boolean) {
        val radius = with(scope) { cornerRadius.toPx() }
        scope.drawRoundRect(
            color = Color.White.copy(alpha = if (isDarkMode) 0.12f else 0.28f),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = with(scope) { 1.25.dp.toPx() })
        )
    }

    fun insetBrush(onColor: Color, isDarkMode: Boolean): Brush = Brush.verticalGradient(
        listOf(
            onColor.copy(alpha = if (isDarkMode) 0.14f else 0.1f),
            onColor.copy(alpha = if (isDarkMode) 0.08f else 0.05f)
        )
    )

    /** Opaque frosted base for the bottom pill nav — readable over scrolling content. */
    fun navScrimColor(isDarkMode: Boolean): Color = if (isDarkMode) {
        Color(0xFF151C28).copy(alpha = 0.92f)
    } else {
        Color(0xFFF3F7FC).copy(alpha = 0.94f)
    }

    fun navFrostBrush(isDarkMode: Boolean): Brush = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF2E3A4E).copy(alpha = 0.72f),
                Color(0xFF182030).copy(alpha = 0.82f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.72f),
                Color(0xFFE3F2FD).copy(alpha = 0.68f)
            )
        )
    }

    fun navTintBrush(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Brush {
        val topAlpha = if (isDarkMode) 0.42f else 0.44f
        val bottomAlpha = if (isDarkMode) 0.48f else 0.5f
        return Brush.verticalGradient(
            listOf(
                tintTop.copy(alpha = topAlpha),
                tintBottom.copy(alpha = bottomAlpha)
            )
        )
    }

    fun navSelectedBrush(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Brush {
        val topAlpha = if (isDarkMode) 0.94f else 0.9f
        val bottomAlpha = if (isDarkMode) 0.98f else 0.94f
        return Brush.verticalGradient(
            listOf(
                tintTop.copy(alpha = topAlpha),
                tintBottom.copy(alpha = bottomAlpha)
            )
        )
    }
}

/** Soft color pools behind glass cards so frosted surfaces pick up ambient hue. */
fun Modifier.glassAmbientLayer(isDarkMode: Boolean): Modifier = drawBehind {
    val teal = Color(0xFF00BCD4).copy(alpha = if (isDarkMode) 0.24f else 0.3f)
    val violet = Color(0xFFAB47BC).copy(alpha = if (isDarkMode) 0.2f else 0.26f)
    val sky = Color(0xFF29B6F6).copy(alpha = if (isDarkMode) 0.18f else 0.24f)

    drawCircle(
        brush = Brush.radialGradient(listOf(teal, Color.Transparent)),
        radius = size.maxDimension * 0.55f,
        center = Offset(size.width * 0.12f, size.height * 0.08f)
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(violet, Color.Transparent)),
        radius = size.maxDimension * 0.5f,
        center = Offset(size.width * 0.88f, size.height * 0.22f)
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(sky, Color.Transparent)),
        radius = size.maxDimension * 0.45f,
        center = Offset(size.width * 0.5f, size.height * 0.92f)
    )
}
