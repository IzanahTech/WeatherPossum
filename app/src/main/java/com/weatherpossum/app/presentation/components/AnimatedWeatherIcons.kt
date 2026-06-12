package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Animated weather icons using Compose Canvas
 * All icons are drawn programmatically and animated with Compose animations
 */

enum class WeatherIconType {
    SUNNY,
    CLOUDY,
    RAIN,
    THUNDER,
    WIND,
    PARTLY_CLOUDY,
    NEUTRAL,
    SEA_DAY,
    SEA_NIGHT,
    OUTLOOK,
    HURRICANE,
    FACT,
    EXTRAS,
    AFTERNOON,
    MORNING,
    NIGHT,
    ADVISORY
}

private fun WeatherIconType.defaultContentDescription(): String = when (this) {
    WeatherIconType.SUNNY -> "Sunny"
    WeatherIconType.CLOUDY -> "Cloudy"
    WeatherIconType.RAIN -> "Rain"
    WeatherIconType.THUNDER -> "Thunderstorm"
    WeatherIconType.WIND -> "Windy"
    WeatherIconType.PARTLY_CLOUDY -> "Partly cloudy"
    WeatherIconType.NEUTRAL -> "Weather"
    WeatherIconType.SEA_DAY -> "Sea conditions"
    WeatherIconType.SEA_NIGHT -> "Sea conditions"
    WeatherIconType.OUTLOOK -> "Weather outlook"
    WeatherIconType.HURRICANE -> "Hurricane"
    WeatherIconType.FACT -> "Fun fact"
    WeatherIconType.EXTRAS -> "Extras"
    WeatherIconType.AFTERNOON -> "Afternoon"
    WeatherIconType.MORNING -> "Morning"
    WeatherIconType.NIGHT -> "Night"
    WeatherIconType.ADVISORY -> "Weather advisory"
}

@Composable
fun AnimatedWeatherIcon(
    type: WeatherIconType,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    val resolvedDescription = contentDescription ?: type.defaultContentDescription()
    Box(
        modifier = modifier.semantics {
            this.contentDescription = resolvedDescription
        }
    ) {
        when (type) {
            WeatherIconType.SUNNY -> AnimatedSunIcon(color = color)
            WeatherIconType.CLOUDY -> AnimatedCloudIcon(color = color)
            WeatherIconType.RAIN -> AnimatedRainIcon(color = color)
            WeatherIconType.THUNDER -> AnimatedThunderIcon(color = color)
            WeatherIconType.WIND -> AnimatedWindIcon(color = color)
            WeatherIconType.PARTLY_CLOUDY -> AnimatedPartlyCloudyIcon(color = color)
            WeatherIconType.NEUTRAL -> AnimatedNeutralIcon(color = color)
            WeatherIconType.SEA_DAY -> AnimatedSeaDayIcon(color = color)
            WeatherIconType.SEA_NIGHT -> AnimatedSeaNightIcon(color = color)
            WeatherIconType.OUTLOOK -> AnimatedOutlookIcon(color = color)
            WeatherIconType.HURRICANE -> AnimatedHurricaneIcon(color = color)
            WeatherIconType.FACT -> AnimatedFactIcon(color = color)
            WeatherIconType.EXTRAS -> AnimatedExtrasIcon(color = color)
            WeatherIconType.AFTERNOON -> AnimatedAfternoonIcon(color = color)
            WeatherIconType.MORNING -> AnimatedMorningIcon(color = color)
            WeatherIconType.NIGHT -> AnimatedNightIcon(color = color)
            WeatherIconType.ADVISORY -> AnimatedAdvisoryIcon(color = color)
        }
    }
}

// Helper function to get icon type from weather string
fun getWeatherIconType(weatherText: String): WeatherIconType {
    val text = weatherText.lowercase()
    return when {
        // Check for wind conditions first (before rain/thunder to avoid conflicts)
        text.contains("wind conditions", ignoreCase = true) || 
        (text.contains("wind") && (text.contains("conditions") || text.contains("speed") || text.contains("direction"))) ||
        text.contains("breeze", ignoreCase = true) || 
        text.contains("gust", ignoreCase = true) -> WeatherIconType.WIND
        text.contains("rain", ignoreCase = true) || text.contains("shower", ignoreCase = true) -> WeatherIconType.RAIN
        text.contains("thunder", ignoreCase = true) || text.contains("storm", ignoreCase = true) -> WeatherIconType.THUNDER
        text.contains("partly cloudy", ignoreCase = true) -> WeatherIconType.PARTLY_CLOUDY
        text.contains("cloud", ignoreCase = true) || text.contains("overcast", ignoreCase = true) -> WeatherIconType.CLOUDY
        text.contains("wind", ignoreCase = true) -> WeatherIconType.WIND
        text.contains("sun", ignoreCase = true) || text.contains("clear", ignoreCase = true) -> WeatherIconType.SUNNY
        text.contains("tide", ignoreCase = true) || text.contains("sea", ignoreCase = true) -> {
            val hour = java.time.LocalTime.now().hour
            if (hour in 6..18) WeatherIconType.SEA_DAY else WeatherIconType.SEA_NIGHT
        }
        text.contains("outlook", ignoreCase = true) -> WeatherIconType.OUTLOOK
        text.contains("warning", ignoreCase = true) || text.contains("advisory", ignoreCase = true) || text.contains("alert", ignoreCase = true) -> WeatherIconType.ADVISORY
        text.contains("synopsis", ignoreCase = true) -> WeatherIconType.SUNNY
        else -> WeatherIconType.NEUTRAL
    }
}

// Animated Sun Icon
@Composable
fun AnimatedSunIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val palette = sunPaletteFromTint(
        iconAccentColor(color, Color(0xFFFFB300), Color(0xFFFFCC80), onDark)
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.28f
        drawRealisticSun(
            center = center,
            radius = radius,
            rotationDegrees = rotation,
            palette = palette
        )
    }
}

// Animated Cloud Icon
@Composable
fun AnimatedCloudIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val shades = cloudShadesFromTint(color, onDark)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawLayeredCloud(
            center = center,
            width = size.width * 0.78f,
            height = size.height * 0.48f,
            offsetX = offsetX,
            shades = shades
        )
    }
}

// Animated Rain Icon
@Composable
fun AnimatedRainIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val cloudShades = cloudShadesFromTint(color, onDark)
    val dropColor = iconAccentColor(
        color,
        Color(0xFF42A5F5),
        Color(0xFF81D4FA),
        onDark
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.42f)
        val cloudWidth = size.width * 0.72f
        drawLayeredCloud(
            center = center,
            width = cloudWidth,
            height = size.height * 0.34f,
            shades = cloudShades
        )
        drawRainStreaks(
            center = center,
            cloudWidth = cloudWidth,
            progress = rainOffset,
            dropColor = dropColor
        )
    }
}

// Animated Thunder Icon
@Composable
fun AnimatedThunderIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thunder")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val boltColor = iconAccentColor(color, Color(0xFFFFEB3B), Color(0xFFFFF176), onDark)
    val stormCloud = cloudShadesFromTint(Color.Unspecified, onDark)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.46f)
        drawLayeredCloud(
            center = center,
            width = size.width * 0.68f,
            height = size.height * 0.34f,
            shades = stormCloud.copy(
                body = stormCloud.shadow,
                highlight = stormCloud.body
            )
        )
        drawLightningBolt(
            center = Offset(center.x, center.y + size.height * 0.08f),
            height = size.height * 0.42f,
            boltColor = boltColor,
            alpha = flashAlpha
        )
    }
}

// Animated Wind Icon
@Composable
fun AnimatedWindIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wind")
    val windOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // Slower: 4 seconds
            repeatMode = RepeatMode.Restart
        ),
        label = "windOffset"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val streamColor = iconAccentColor(color, Color(0xFF64B5F6), Color(0xFF90CAF9), onDark)
    val leafPalette = LeafPalette(
        fill = if (onDark) Color(0xFF81C784) else Color(0xFF66BB6A),
        outline = if (onDark) Color(0xFF2E7D32) else Color(0xFF388E3C),
        vein = if (onDark) Color(0xFF1B5E20) else Color(0xFF2E7D32)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val waveLength = size.width * 0.6f
        val waveHeight = size.height * 0.08f

        drawWindStreamLines(progress = windOffset, streamColor = streamColor)
        drawWindSwirl(
            center = Offset(size.width * 0.72f, center.y + size.height * 0.08f),
            maxRadius = size.minDimension * 0.18f,
            rotation = windOffset * 360f,
            swirlColor = streamColor
        )

        val leaves = listOf(
            Triple(0.12f, -0.18f, 0.1f),
            Triple(0.62f, -0.08f, 0.14f),
            Triple(0.52f, 0.16f, 0.17f),
            Triple(0.78f, 0.22f, 0.11f)
        )
        leaves.forEachIndexed { index, (xPos, yPos, sizeScale) ->
            val baseX = size.width * xPos
            val baseY = center.y + size.height * yPos
            val leafX = (baseX + windOffset * size.width * 0.48f) % (size.width * 1.08f)
            val windWave = sin(leafX * 2f * PI.toFloat() / waveLength)
            val leafY = baseY + windWave * waveHeight * 0.55f
            val leafRot = (windOffset * 360f * 0.35f + index * 72f) % 360f
            drawTumblingLeaf(
                center = Offset(leafX, leafY),
                sizeScale = sizeScale,
                rotationDegrees = leafRot,
                palette = leafPalette
            )
        }
    }
}

// Animated Partly Cloudy Icon
@Composable
fun AnimatedPartlyCloudyIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "partlyCloudy")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )
    
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudOffset"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val sunPalette = sunPaletteFromTint(
        iconAccentColor(color, Color(0xFFFFB300), Color(0xFFFFCC80), onDark)
    )
    val cloudShades = cloudShadesFromTint(Color.Unspecified, onDark)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val sunCenter = Offset(size.width * 0.32f, size.height * 0.34f)
        drawRealisticSun(
            center = sunCenter,
            radius = size.minDimension * 0.22f,
            rotationDegrees = sunRotation,
            palette = sunPalette,
            rayCount = 10
        )

        drawLayeredCloud(
            center = Offset(size.width * 0.62f, size.height * 0.52f),
            width = size.width * 0.56f,
            height = size.height * 0.32f,
            offsetX = cloudOffset,
            shades = cloudShades
        )
    }
}

@Composable
fun AnimatedNeutralIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    AnimatedCloudIcon(modifier = modifier, color = color)
}

@Composable
fun AnimatedSeaDayIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "seaDay")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )

    val waterSurface = if (color == Color.Unspecified) Color(0xFF4FC3F7) else blendColor(color, Color.White, 0.15f)
    val waterDepth = if (color == Color.Unspecified) Color(0xFF0277BD) else darkenColor(color, 0.65f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseY = size.height * 0.58f
        val waveHeight = size.minDimension * 0.08f
        val waveWidth = size.width * 0.45f

        drawRealisticSun(
            center = Offset(size.width * 0.28f, size.height * 0.26f),
            radius = size.minDimension * 0.14f,
            rotationDegrees = sunRotation,
            palette = sunPaletteFromTint(Color.Unspecified),
            rayCount = 8
        )

        drawOceanWaveLayer(
            baseY = baseY,
            waveWidth = waveWidth,
            waveHeight = waveHeight,
            phase = waveOffset,
            surfaceColor = waterSurface.copy(alpha = 0.85f),
            depthColor = waterDepth.copy(alpha = 0.92f)
        )
        drawOceanWaveLayer(
            baseY = baseY + waveHeight * 0.6f,
            waveWidth = waveWidth * 1.15f,
            waveHeight = waveHeight * 0.55f,
            phase = waveOffset + PI.toFloat() * 0.35f,
            surfaceColor = waterSurface.copy(alpha = 0.45f),
            depthColor = waterDepth.copy(alpha = 0.55f)
        )
    }
}

@Composable
fun AnimatedSeaNightIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "seaNight")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )
    val moonGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonGlow"
    )

    val waterSurface = if (color == Color.Unspecified) Color(0xFF1565C0) else blendColor(color, Color.White, 0.12f)
    val waterDepth = if (color == Color.Unspecified) Color(0xFF0D47A1) else darkenColor(color, 0.55f)
    val moonTint = if (color == Color.Unspecified) Color(0xFFE8EAF6) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseY = size.height * 0.58f
        val waveHeight = size.minDimension * 0.08f
        val waveWidth = size.width * 0.45f
        val moonCenter = Offset(size.width * 0.28f, size.height * 0.24f)
        val moonRadius = size.minDimension * 0.11f

        drawRealisticMoon(
            center = moonCenter,
            radius = moonRadius,
            tint = moonTint,
            glowAlpha = 0.18f + moonGlow * 0.22f
        )
        drawStarField(
            center = moonCenter,
            radius = moonRadius * 2.4f,
            tint = moonTint,
            twinkle = moonGlow
        )

        drawOceanWaveLayer(
            baseY = baseY,
            waveWidth = waveWidth,
            waveHeight = waveHeight,
            phase = waveOffset,
            surfaceColor = waterSurface.copy(alpha = 0.8f),
            depthColor = waterDepth.copy(alpha = 0.95f)
        )
        drawOceanWaveLayer(
            baseY = baseY + waveHeight * 0.55f,
            waveWidth = waveWidth * 1.2f,
            waveHeight = waveHeight * 0.5f,
            phase = waveOffset + PI.toFloat() * 0.4f,
            surfaceColor = waterSurface.copy(alpha = 0.35f),
            depthColor = waterDepth.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AnimatedOutlookIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    AnimatedSunIcon(modifier = modifier, color = color)
}

@Composable
fun AnimatedAdvisoryIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "advisory")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val exclamationAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exclamationAlpha"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val onDark = tintSuggestsDarkSurface(color)
    val accent = iconAccentColor(color, Color(0xFFFFB300), Color(0xFFFFCA28), onDark)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val triangleSize = size.minDimension * 0.38f * pulseScale
        drawWarningTriangle(
            center = center,
            size = triangleSize,
            rotationDegrees = rotation,
            accent = accent,
            pulse = pulseScale,
            markAlpha = exclamationAlpha
        )
    }
}

@Composable
fun AnimatedHurricaneIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "hurricane")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hurricaneRotation"
    )
    val spiralIntensity by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spiralIntensity"
    )

    val stormCore = if (color == Color.Unspecified) Color(0xFFEF5350) else color
    val stormOuter = darkenColor(stormCore, 0.72f)
    val stormCloud = cloudShadesFromTint(Color.Unspecified).copy(
        body = Color(0xFF78909C),
        shadow = Color(0xFF546E7A),
        highlight = Color(0xFFB0BEC5)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension * 0.38f
        val centerRadius = size.minDimension * 0.07f

        drawLayeredCloud(
            center = Offset(center.x, center.y - maxRadius * 0.08f),
            width = size.width * 0.82f,
            height = size.height * 0.28f,
            shades = stormCloud
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f), stormCore.copy(alpha = 0.15f)),
                center = center,
                radius = centerRadius * 1.8f
            ),
            radius = centerRadius * 1.8f,
            center = center
        )
        drawCircle(
            color = stormCore.copy(alpha = 0.95f),
            radius = centerRadius,
            center = center
        )

        rotate(rotation, pivot = center) {
            for (arm in 0 until 4) {
                val armAngle = arm * 90f
                val spiralPath = Path()
                var currentRadius = centerRadius * 1.2f
                var angle = armAngle
                val angleStep = 6f
                val radiusStep = maxRadius / 48f

                while (currentRadius < maxRadius * spiralIntensity) {
                    val angleRad = angle * PI.toFloat() / 180f
                    val x = center.x + currentRadius * cos(angleRad)
                    val y = center.y + currentRadius * sin(angleRad)

                    if (currentRadius <= centerRadius * 1.25f) {
                        spiralPath.moveTo(x, y)
                    } else {
                        spiralPath.lineTo(x, y)
                    }

                    currentRadius += radiusStep
                    angle += angleStep
                }

                drawPath(
                    path = spiralPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            stormCore.copy(alpha = 0.85f - arm * 0.12f),
                            stormOuter.copy(alpha = 0.35f)
                        ),
                        start = center,
                        end = Offset(center.x + maxRadius, center.y)
                    ),
                    style = Stroke(
                        width = (3.5f - arm * 0.35f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Composable
fun AnimatedFactIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "fact")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val onDark = tintSuggestsDarkSurface(color)
    val accent = iconAccentColor(color, Color(0xFFFFD700), Color(0xFFFFCA28), onDark)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawLightBulb(
            center = center,
            bulbRadius = size.minDimension * 0.24f,
            rotationDegrees = rotation,
            glow = glowIntensity,
            accent = accent
        )
    }
}

@Composable
fun AnimatedExtrasIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "extras")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val iconColor = if (color == Color.Unspecified) Color(0xFFFFB74D) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val gridSize = size.minDimension * 0.4f
        val cellSize = gridSize / 3f
        val spacing = cellSize * 0.15f
        val dotSize = cellSize * 0.3f * pulse
        
        // Draw a 3x3 grid with animated dots
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val cellX = center.x - gridSize / 2f + col * (cellSize + spacing) + cellSize / 2f
                val cellY = center.y - gridSize / 2f + row * (cellSize + spacing) + cellSize / 2f
                
                // Animate dots with staggered timing
                val delay = (row * 3 + col) * 0.15f
                val animatedPulse = (pulse + delay).coerceIn(0.8f, 1.2f)
                val dotRadius = dotSize * animatedPulse
                
                // Draw grid cell background (subtle)
                drawRoundRect(
                    color = iconColor.copy(alpha = 0.15f),
                    topLeft = Offset(cellX - cellSize / 2f, cellY - cellSize / 2f),
                    size = Size(cellSize, cellSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                
                // Draw animated dot in center of cell
                drawCircle(
                    color = iconColor.copy(alpha = 0.8f + (animatedPulse - 1f) * 0.5f),
                    radius = dotRadius,
                    center = Offset(cellX, cellY)
                )
            }
        }
        
        // Draw outer ring that rotates
        rotate(rotation, pivot = center) {
            drawCircle(
                color = iconColor.copy(alpha = 0.3f),
                radius = gridSize / 2f + 8.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw small accent dots on the ring
            for (i in 0 until 8) {
                val angle = (i * 45f) * PI.toFloat() / 180f
                val dotX = center.x + (gridSize / 2f + 8.dp.toPx()) * cos(angle)
                val dotY = center.y + (gridSize / 2f + 8.dp.toPx()) * sin(angle)
                drawCircle(
                    color = iconColor.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

@Composable
fun AnimatedMorningIcon(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "morning")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawRealisticSun(
            center = center,
            radius = size.minDimension * 0.28f,
            rotationDegrees = sunRotation,
            palette = sunPaletteFromTint(Color.Unspecified, warm = true),
            rayCount = 10
        )
    }
}

@Composable
fun AnimatedAfternoonIcon(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "afternoon")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawRealisticSun(
            center = center,
            radius = size.minDimension * 0.3f,
            rotationDegrees = sunRotation,
            palette = sunPaletteFromTint(Color(0xFFFFD700)),
            rayCount = 14
        )
    }
}

@Composable
fun AnimatedNightIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val infiniteTransition = rememberInfiniteTransition(label = "night")
    val moonPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonPhase"
    )
    
    val onDark = tintSuggestsDarkSurface(color)
    val iconColor = iconAccentColor(color, Color(0xFFE1BEE7), Color(0xFFE8EAF6), onDark)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.28f
        drawRealisticMoon(center = center, radius = radius, tint = iconColor, glowAlpha = 0.2f + moonPhase * 0.15f)
        drawStarField(center = center, radius = radius, tint = iconColor, twinkle = moonPhase)
    }
}

/**
 * Animated Moon Phase Icon
 * Draws the moon phase from illumination and waxing/waning using a realistic terminator.
 */
@Composable
fun AnimatedMoonPhaseIcon(
    phase: String,
    illumination: Double,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moonPhase")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    val earthshinePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earthshinePulse"
    )

    val onDark = tintSuggestsDarkSurface(color)
    val iconColor = iconAccentColor(color, Color(0xFFE8EAF6), Color(0xFFFFF9C4), onDark)
    val waxing = isWaxingMoonPhase(phase)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.32f

        drawRealisticMoonPhase(
            center = center,
            radius = radius,
            tint = iconColor,
            illumination = illumination,
            waxing = waxing,
            glowAlpha = 0.18f + glowIntensity * 0.2f * earthshinePulse.coerceIn(0.9f, 1.1f)
        )
    }
}

