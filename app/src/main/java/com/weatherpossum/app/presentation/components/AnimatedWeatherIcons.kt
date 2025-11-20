package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    FORECAST,
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

@Composable
fun AnimatedWeatherIcon(
    type: WeatherIconType,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentDescription: String? = null // Reserved for accessibility
) {
    Box(modifier = modifier) {
        when (type) {
            WeatherIconType.SUNNY -> AnimatedSunIcon(color = color)
            WeatherIconType.CLOUDY -> AnimatedCloudIcon(color = color)
            WeatherIconType.RAIN -> AnimatedRainIcon(color = color)
            WeatherIconType.THUNDER -> AnimatedThunderIcon(color = color)
            WeatherIconType.WIND -> AnimatedWindIcon(color = color)
            WeatherIconType.PARTLY_CLOUDY -> AnimatedPartlyCloudyIcon(color = color)
            WeatherIconType.FORECAST -> AnimatedForecastIcon(color = color)
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

// Helper function removed - all drawable resources have been deleted
// Use getWeatherIconType(weatherText: String) instead

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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFFFFB300) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.3f
        val rayLength = size.minDimension * 0.15f
        val rayCount = 8
        
        // Draw sun circle
        drawCircle(
            color = iconColor,
            radius = radius,
            center = center
        )
        
        // Draw rotating rays
        rotate(rotation, center) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * PI.toFloat() / 180f
                val startX = center.x + (radius + 5f) * cos(angle)
                val startY = center.y + (radius + 5f) * sin(angle)
                val endX = center.x + (radius + rayLength) * cos(angle)
                val endY = center.y + (radius + rayLength) * sin(angle)
                
                drawLine(
                    color = iconColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFF9E9E9E) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cloudWidth = size.width * 0.7f
        val cloudHeight = size.height * 0.5f
        
        val path = Path().apply {
            moveTo(center.x - cloudWidth * 0.3f + offsetX, center.y)
            cubicTo(
                center.x - cloudWidth * 0.3f + offsetX, center.y - cloudHeight * 0.3f,
                center.x - cloudWidth * 0.1f + offsetX, center.y - cloudHeight * 0.4f,
                center.x + cloudWidth * 0.1f + offsetX, center.y - cloudHeight * 0.4f
            )
            cubicTo(
                center.x + cloudWidth * 0.1f + offsetX, center.y - cloudHeight * 0.6f,
                center.x + cloudWidth * 0.3f + offsetX, center.y - cloudHeight * 0.5f,
                center.x + cloudWidth * 0.4f + offsetX, center.y - cloudHeight * 0.3f
            )
            cubicTo(
                center.x + cloudWidth * 0.5f + offsetX, center.y - cloudHeight * 0.2f,
                center.x + cloudWidth * 0.4f + offsetX, center.y,
                center.x + cloudWidth * 0.2f + offsetX, center.y
            )
            cubicTo(
                center.x + cloudWidth * 0.1f + offsetX, center.y + cloudHeight * 0.2f,
                center.x - cloudWidth * 0.1f + offsetX, center.y + cloudHeight * 0.2f,
                center.x - cloudWidth * 0.3f + offsetX, center.y
            )
            close()
        }
        
        drawPath(path, color = iconColor)
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFF64B5F6) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cloudWidth = size.width * 0.6f
        val cloudHeight = size.height * 0.4f
        
        // Draw cloud
        val cloudPath = Path().apply {
            moveTo(center.x - cloudWidth * 0.3f, center.y - cloudHeight * 0.2f)
            cubicTo(
                center.x - cloudWidth * 0.3f, center.y - cloudHeight * 0.5f,
                center.x - cloudWidth * 0.1f, center.y - cloudHeight * 0.6f,
                center.x + cloudWidth * 0.1f, center.y - cloudHeight * 0.6f
            )
            cubicTo(
                center.x + cloudWidth * 0.1f, center.y - cloudHeight * 0.8f,
                center.x + cloudWidth * 0.3f, center.y - cloudHeight * 0.7f,
                center.x + cloudWidth * 0.4f, center.y - cloudHeight * 0.5f
            )
            cubicTo(
                center.x + cloudWidth * 0.5f, center.y - cloudHeight * 0.4f,
                center.x + cloudWidth * 0.4f, center.y - cloudHeight * 0.2f,
                center.x + cloudWidth * 0.2f, center.y - cloudHeight * 0.2f
            )
            cubicTo(
                center.x + cloudWidth * 0.1f, center.y,
                center.x - cloudWidth * 0.1f, center.y,
                center.x - cloudWidth * 0.3f, center.y - cloudHeight * 0.2f
            )
            close()
        }
        drawPath(cloudPath, color = iconColor)
        
        // Draw animated rain drops
        val rainStartY = center.y + cloudHeight * 0.3f
        val rainEndY = center.y + cloudHeight * 1.2f
        val rainY = rainStartY + (rainEndY - rainStartY) * rainOffset
        
        for (i in -1..1) {
            val x = center.x + i * cloudWidth * 0.2f
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(x, rainY),
                end = Offset(x, rainY + size.height * 0.15f),
                strokeWidth = 2.dp.toPx()
            )
        }
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFFFFEB3B) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cloudWidth = size.width * 0.5f
        
        // Draw cloud
        val cloudPath = Path().apply {
            moveTo(center.x - cloudWidth * 0.3f, center.y - size.height * 0.1f)
            cubicTo(
                center.x - cloudWidth * 0.3f, center.y - size.height * 0.3f,
                center.x - cloudWidth * 0.1f, center.y - size.height * 0.4f,
                center.x + cloudWidth * 0.1f, center.y - size.height * 0.4f
            )
            cubicTo(
                center.x + cloudWidth * 0.1f, center.y - size.height * 0.5f,
                center.x + cloudWidth * 0.3f, center.y - size.height * 0.45f,
                center.x + cloudWidth * 0.4f, center.y - size.height * 0.3f
            )
            cubicTo(
                center.x + cloudWidth * 0.5f, center.y - size.height * 0.2f,
                center.x + cloudWidth * 0.4f, center.y - size.height * 0.1f,
                center.x + cloudWidth * 0.2f, center.y - size.height * 0.1f
            )
            cubicTo(
                center.x + cloudWidth * 0.1f, center.y,
                center.x - cloudWidth * 0.1f, center.y,
                center.x - cloudWidth * 0.3f, center.y - size.height * 0.1f
            )
            close()
        }
        drawPath(cloudPath, color = Color(0xFF9E9E9E))
        
        // Draw lightning bolt with flash effect
        val boltPath = Path().apply {
            moveTo(center.x, center.y - size.height * 0.2f)
            lineTo(center.x - size.width * 0.08f, center.y)
            lineTo(center.x, center.y)
            lineTo(center.x + size.width * 0.08f, center.y + size.height * 0.2f)
            lineTo(center.x, center.y + size.height * 0.15f)
            lineTo(center.x, center.y + size.height * 0.25f)
            close()
        }
        drawPath(boltPath, color = iconColor.copy(alpha = flashAlpha))
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFF90CAF9) else color
    val leafColor = Color(0xFF66BB6A) // Green for leaves
    val leafOutlineColor = Color(0xFF388E3C) // Darker green for outline
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val waveLength = size.width * 0.6f
        val waveHeight = size.height * 0.08f
        val offset = waveLength * windOffset
        
        // Draw flowing wind stream lines (horizontal wavy lines)
        for (i in 0 until 3) {
            val baseY = center.y - size.height * 0.2f + i * (size.height * 0.4f / 2f)
            val path = Path().apply {
                moveTo(0f, baseY)
                var x = 0f
                val step = 5f
                while (x <= size.width) {
                    val waveY = baseY + waveHeight * sin((x + offset) * 2 * PI.toFloat() / waveLength).toFloat()
                    if (x == 0f) {
                        moveTo(x, waveY)
                    } else {
                        lineTo(x, waveY)
                    }
                    x += step
                }
            }
            drawPath(
                path = path,
                color = iconColor.copy(alpha = 0.75f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Draw one swirling wind pattern (spiral) - simplified
        val swirlCenterX = size.width * 0.7f
        val swirlCenterY = center.y + size.height * 0.1f
        val maxRadius = size.minDimension * 0.2f
        val swirlAngle = windOffset * 2f * PI.toFloat()
        
        val swirlPath = Path().apply {
            var angle = 0f
            val angleStep = 8f
            var firstPoint = true
            
            while (angle <= 270f) { // 3/4 of a circle
                val angleRad = (swirlAngle + angle * PI.toFloat() / 180f).toDouble()
                // Spiral: radius decreases as angle increases
                val radius = maxRadius * (1f - angle / 360f).coerceAtLeast(0.2f)
                val x = swirlCenterX + radius * cos(angleRad).toFloat()
                val y = swirlCenterY + radius * sin(angleRad).toFloat()
                
                if (firstPoint) {
                    moveTo(x, y)
                    firstPoint = false
                } else {
                    lineTo(x, y)
                }
                angle += angleStep
            }
        }
        drawPath(
            path = swirlPath,
            color = iconColor.copy(alpha = 0.65f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw animated leaves being carried by the wind
        val leaves = listOf(
            // Leaf 1: Upper left, small
            Pair(0.15f, -0.2f),
            // Leaf 2: Upper right, medium
            Pair(0.7f, -0.1f),
            // Leaf 3: Mid right, large
            Pair(0.6f, 0.15f),
            // Leaf 4: Lower right, small
            Pair(0.75f, 0.25f)
        )
        
        leaves.forEachIndexed { index, (xPos, yPos) ->
            // Calculate leaf position following wind flow
            val baseX = size.width * xPos
            val baseY = center.y + size.height * yPos
            
            // Leaf moves horizontally with wind at a slower, smoother pace
            val leafX = (baseX + windOffset * size.width * 0.5f) % (size.width * 1.1f)
            val windWave = sin((leafX) * 2 * PI.toFloat() / waveLength).toFloat()
            val leafY = baseY + windWave * waveHeight * 0.6f
            
            // Leaf rotation - gentle tumbling
            val leafRot = (windOffset * 360f * 0.3f + index * 60f) % 360f
            
            // Draw leaf
            rotate(leafRot, pivot = Offset(leafX, leafY)) {
                val leafSize = when (index) {
                    0, 3 -> size.minDimension * 0.1f // Small leaves
                    1 -> size.minDimension * 0.14f // Medium leaf
                    else -> size.minDimension * 0.18f // Large leaf
                }
                
                val leafPath = Path().apply {
                    // Leaf shape: teardrop/oval
                    moveTo(leafX, leafY - leafSize * 0.5f)
                    // Right curve
                    cubicTo(
                        leafX + leafSize * 0.3f, leafY - leafSize * 0.2f,
                        leafX + leafSize * 0.4f, leafY + leafSize * 0.1f,
                        leafX, leafY + leafSize * 0.5f
                    )
                    // Left curve
                    cubicTo(
                        leafX - leafSize * 0.4f, leafY + leafSize * 0.1f,
                        leafX - leafSize * 0.3f, leafY - leafSize * 0.2f,
                        leafX, leafY - leafSize * 0.5f
                    )
                    close()
                }
                
                // Fill leaf first
                drawPath(
                    path = leafPath,
                    color = leafColor.copy(alpha = 0.95f)
                )
                
                // Draw leaf outline
                drawPath(
                    path = leafPath,
                    color = leafOutlineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Draw central vein
                drawLine(
                    color = leafOutlineColor.copy(alpha = 0.8f),
                    start = Offset(leafX, leafY - leafSize * 0.4f),
                    end = Offset(leafX, leafY + leafSize * 0.4f),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFFFFB300) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw sun (partially visible)
        val sunRadius = size.minDimension * 0.25f
        val sunCenter = Offset(size.width * 0.3f, size.height * 0.3f)
        
        rotate(sunRotation, sunCenter) {
            drawCircle(
                color = iconColor,
                radius = sunRadius,
                center = sunCenter
            )
            
            // Sun rays
            for (i in 0 until 6) {
                val angle = (i * 60f) * PI.toFloat() / 180f
                val startX = sunCenter.x + sunRadius * cos(angle)
                val startY = sunCenter.y + sunRadius * sin(angle)
                val endX = sunCenter.x + (sunRadius + size.minDimension * 0.1f) * cos(angle)
                val endY = sunCenter.y + (sunRadius + size.minDimension * 0.1f) * sin(angle)
                
                drawLine(
                    color = iconColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        // Draw cloud
        val cloudWidth = size.width * 0.5f
        val cloudHeight = size.height * 0.3f
        val cloudCenter = Offset(size.width * 0.65f + cloudOffset, size.height * 0.5f)
        
        val cloudPath = Path().apply {
            moveTo(cloudCenter.x - cloudWidth * 0.3f, cloudCenter.y)
            cubicTo(
                cloudCenter.x - cloudWidth * 0.3f, cloudCenter.y - cloudHeight * 0.3f,
                cloudCenter.x - cloudWidth * 0.1f, cloudCenter.y - cloudHeight * 0.4f,
                cloudCenter.x + cloudWidth * 0.1f, cloudCenter.y - cloudHeight * 0.4f
            )
            cubicTo(
                cloudCenter.x + cloudWidth * 0.1f, cloudCenter.y - cloudHeight * 0.6f,
                cloudCenter.x + cloudWidth * 0.3f, cloudCenter.y - cloudHeight * 0.5f,
                cloudCenter.x + cloudWidth * 0.4f, cloudCenter.y - cloudHeight * 0.3f
            )
            cubicTo(
                cloudCenter.x + cloudWidth * 0.5f, cloudCenter.y - cloudHeight * 0.2f,
                cloudCenter.x + cloudWidth * 0.4f, cloudCenter.y,
                cloudCenter.x + cloudWidth * 0.2f, cloudCenter.y
            )
            cubicTo(
                cloudCenter.x + cloudWidth * 0.1f, cloudCenter.y + cloudHeight * 0.2f,
                cloudCenter.x - cloudWidth * 0.1f, cloudCenter.y + cloudHeight * 0.2f,
                cloudCenter.x - cloudWidth * 0.3f, cloudCenter.y
            )
            close()
        }
        drawPath(cloudPath, color = Color(0xFF9E9E9E))
    }
}

// Simple icons for other types (can be enhanced later)
@Composable
fun AnimatedForecastIcon(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    AnimatedSunIcon(modifier = modifier, color = color)
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

    val iconColor = if (color == Color.Unspecified) Color(0xFF42A5F5) else color
    val sunColor = if (color == Color.Unspecified) Color(0xFFFFC107) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val waveHeight = size.minDimension * 0.15f
        val waveWidth = size.width
        val baseY = size.height * 0.6f

        // Draw sun in the sky
        val sunRadius = size.minDimension * 0.15f
        val sunCenter = Offset(size.width * 0.3f, size.height * 0.25f)
        rotate(sunRotation, pivot = sunCenter) {
            drawCircle(
                color = sunColor,
                radius = sunRadius,
                center = sunCenter
            )
            // Sun rays
            for (i in 0 until 6) {
                val angle = i * 60f
                rotate(angle, pivot = sunCenter) {
                    drawLine(
                        color = sunColor,
                        start = Offset(sunCenter.x, sunCenter.y - sunRadius * 1.3f),
                        end = Offset(sunCenter.x, sunCenter.y - sunRadius * 0.7f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        // Draw animated waves
        val wavePath = Path()
        val waveFrequency = 3f
        val numWaves = 3
        
        for (wave in 0 until numWaves) {
            val waveY = baseY + wave * (waveHeight / numWaves)
            val wavePhase = waveOffset + (wave * PI.toFloat() / numWaves)
            
            wavePath.moveTo(0f, waveY)
            var x = 0f
            val step = 4f
            while (x <= waveWidth) {
                val y = waveY + waveHeight * 0.3f * sin((x / waveWidth * waveFrequency * 2f * PI.toFloat() + wavePhase).toDouble()).toFloat()
                wavePath.lineTo(x, y)
                x += step
            }
        }
        
        drawPath(
            path = wavePath,
            color = iconColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Fill waves with water color
        wavePath.lineTo(waveWidth, size.height)
        wavePath.lineTo(0f, size.height)
        wavePath.close()
        drawPath(
            path = wavePath,
            color = iconColor.copy(alpha = 0.6f)
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

    val iconColor = if (color == Color.Unspecified) Color(0xFF1976D2) else color
    val moonColor = if (color == Color.Unspecified) Color(0xFFE1BEE7) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val waveHeight = size.minDimension * 0.15f
        val waveWidth = size.width
        val baseY = size.height * 0.6f

        // Draw moon in the sky
        val moonRadius = size.minDimension * 0.12f
        val moonCenter = Offset(size.width * 0.3f, size.height * 0.25f)
        
        // Moon glow
        drawCircle(
            color = moonColor.copy(alpha = moonGlow * 0.3f),
            radius = moonRadius * 2f,
            center = moonCenter
        )
        
        // Moon
        drawCircle(
            color = moonColor.copy(alpha = moonGlow),
            radius = moonRadius,
            center = moonCenter
        )
        
        // Stars
        for (i in 0..2) {
            val angle = (i * 120f) * PI.toFloat() / 180f
            val starX = moonCenter.x + (moonRadius + size.minDimension * 0.15f) * cos(angle)
            val starY = moonCenter.y + (moonRadius + size.minDimension * 0.15f) * sin(angle)
            drawCircle(
                color = moonColor.copy(alpha = moonGlow * 0.8f),
                radius = 1.5.dp.toPx(),
                center = Offset(starX, starY)
            )
        }

        // Draw animated waves
        val wavePath = Path()
        val waveFrequency = 3f
        val numWaves = 3
        
        for (wave in 0 until numWaves) {
            val waveY = baseY + wave * (waveHeight / numWaves)
            val wavePhase = waveOffset + (wave * PI.toFloat() / numWaves)
            
            wavePath.moveTo(0f, waveY)
            var x = 0f
            val step = 4f
            while (x <= waveWidth) {
                val y = waveY + waveHeight * 0.3f * sin((x / waveWidth * waveFrequency * 2f * PI.toFloat() + wavePhase).toDouble()).toFloat()
                wavePath.lineTo(x, y)
                x += step
            }
        }
        
        drawPath(
            path = wavePath,
            color = iconColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Fill waves with water color
        wavePath.lineTo(waveWidth, size.height)
        wavePath.lineTo(0f, size.height)
        wavePath.close()
        drawPath(
            path = wavePath,
            color = iconColor.copy(alpha = 0.6f)
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

    val iconColor = if (color == Color.Unspecified) Color(0xFFFF6B6B) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val triangleSize = size.minDimension * 0.4f * pulseScale
        
        // Draw warning triangle
        rotate(rotation, pivot = center) {
            val trianglePath = Path().apply {
                // Top point
                moveTo(center.x, center.y - triangleSize)
                // Bottom right
                lineTo(center.x + triangleSize * 0.866f, center.y + triangleSize * 0.5f)
                // Bottom left
                lineTo(center.x - triangleSize * 0.866f, center.y + triangleSize * 0.5f)
                close()
            }
            
            // Draw triangle outline
            drawPath(
                path = trianglePath,
                color = iconColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Fill triangle with semi-transparent color
            drawPath(
                path = trianglePath,
                color = iconColor.copy(alpha = 0.2f)
            )
        }
        
        // Draw exclamation mark
        val exclamationHeight = triangleSize * 0.4f
        val exclamationWidth = triangleSize * 0.15f
        val exclamationY = center.y - triangleSize * 0.1f
        
        // Exclamation dot (top)
        drawCircle(
            color = iconColor.copy(alpha = exclamationAlpha),
            radius = exclamationWidth / 2f,
            center = Offset(center.x, exclamationY - exclamationHeight * 0.3f)
        )
        
        // Exclamation line (bottom)
        drawLine(
            color = iconColor.copy(alpha = exclamationAlpha),
            start = Offset(center.x, exclamationY + exclamationHeight * 0.1f),
            end = Offset(center.x, exclamationY + exclamationHeight * 0.4f),
            strokeWidth = exclamationWidth,
            cap = StrokeCap.Round
        )
        
        // Outer glow effect
        drawCircle(
            color = iconColor.copy(alpha = (pulseScale - 1f) * 0.3f),
            radius = triangleSize * 1.3f,
            center = center
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

    val iconColor = if (color == Color.Unspecified) Color(0xFFFF6B6B) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension * 0.4f
        val centerRadius = size.minDimension * 0.08f

        // Draw the eye of the hurricane (center)
        drawCircle(
            color = iconColor.copy(alpha = 0.9f),
            radius = centerRadius,
            center = center
        )

        // Draw spiral arms (hurricane structure)
        rotate(rotation, pivot = center) {
            for (arm in 0 until 3) {
                val armAngle = arm * 120f // 3 arms, 120 degrees apart
                val spiralPath = Path()
                
                // Create spiral from center outward
                var currentRadius = centerRadius
                var angle = armAngle
                val angleStep = 5f // degrees per step
                val radiusStep = maxRadius / 60f // gradual increase
                
                while (currentRadius < maxRadius * spiralIntensity) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val x = center.x + currentRadius * cos(angleRad).toFloat()
                    val y = center.y + currentRadius * sin(angleRad).toFloat()
                    
                    if (currentRadius == centerRadius) {
                        spiralPath.moveTo(x, y)
                    } else {
                        spiralPath.lineTo(x, y)
                    }
                    
                    // Spiral outward: increase radius and angle
                    currentRadius += radiusStep
                    angle += angleStep
                }
                
                // Draw the spiral arm with varying thickness
                val strokeWidth = 3.dp.toPx()
                drawPath(
                    path = spiralPath,
                    color = iconColor.copy(alpha = 0.7f - arm * 0.1f),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Draw outer cloud bands (optional, to make it look more like a hurricane)
        for (band in 0 until 2) {
            val bandRadius = maxRadius * (0.85f + band * 0.15f)
            val bandPath = Path()
            
            for (i in 0..360 step 10) {
                val angleRad = Math.toRadians(i.toDouble())
                val waveOffset = 3.dp.toPx() * sin((i * 3 + rotation).toDouble()).toFloat()
                val currentRadius = bandRadius + waveOffset
                val x = center.x + currentRadius * cos(angleRad).toFloat()
                val y = center.y + currentRadius * sin(angleRad).toFloat()
                
                if (i == 0) {
                    bandPath.moveTo(x, y)
                } else {
                    bandPath.lineTo(x, y)
                }
            }
            bandPath.close()
            
            drawPath(
                path = bandPath,
                color = iconColor.copy(alpha = 0.15f - band * 0.05f)
            )
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

    val iconColor = if (color == Color.Unspecified) Color(0xFFFFD700) else color

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val bulbRadius = size.minDimension * 0.25f
        val baseY = size.height * 0.4f

        // Draw glow effect
        drawCircle(
            color = iconColor.copy(alpha = glowIntensity * 0.3f),
            radius = bulbRadius * 2f,
            center = center
        )

        // Draw lightbulb base (screw base)
        val baseWidth = size.minDimension * 0.15f
        val baseHeight = size.minDimension * 0.1f
        val baseTop = baseY + bulbRadius + 2.dp.toPx()
        
        rotate(rotation, pivot = center) {
            // Draw bulb (circle)
            drawCircle(
                color = iconColor.copy(alpha = 0.9f + glowIntensity * 0.1f),
                radius = bulbRadius,
                center = Offset(center.x, baseY)
            )

            // Draw filament (zigzag inside bulb)
            val filamentPath = Path().apply {
                val startX = center.x - bulbRadius * 0.3f
                val endX = center.x + bulbRadius * 0.3f
                val midY = baseY
                moveTo(startX, midY - bulbRadius * 0.2f)
                lineTo(endX, midY)
                lineTo(startX, midY + bulbRadius * 0.2f)
            }
            drawPath(
                path = filamentPath,
                color = iconColor.copy(alpha = 0.8f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw screw base (trapezoid)
            val basePath = Path().apply {
                val topWidth = baseWidth * 0.7f
                val bottomWidth = baseWidth
                moveTo(center.x - topWidth / 2f, baseTop)
                lineTo(center.x + topWidth / 2f, baseTop)
                lineTo(center.x + bottomWidth / 2f, baseTop + baseHeight)
                lineTo(center.x - bottomWidth / 2f, baseTop + baseHeight)
                close()
            }
            drawPath(
                path = basePath,
                color = iconColor.copy(alpha = 0.7f)
            )

            // Draw base lines (screw threads)
            for (i in 0..2) {
                val y = baseTop + (baseHeight / 3f) * (i + 1)
                drawLine(
                    color = iconColor.copy(alpha = 0.5f),
                    start = Offset(center.x - baseWidth / 2f, y),
                    end = Offset(center.x + baseWidth / 2f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
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
        val radius = size.minDimension * 0.3f
        val rayLength = size.minDimension * 0.15f
        val rayCount = 8
        
        // Draw sun circle with morning colors (warmer, more orange)
        drawCircle(
            color = Color(0xFFFF9500), // Orange morning sun
            radius = radius,
            center = center
        )
        
        // Draw rotating rays
        rotate(sunRotation, center) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * PI.toFloat() / 180f
                val startX = center.x + (radius + 5f) * cos(angle)
                val startY = center.y + (radius + 5f) * sin(angle)
                val endX = center.x + (radius + rayLength) * cos(angle)
                val endY = center.y + (radius + rayLength) * sin(angle)
                
                drawLine(
                    color = Color(0xFFFFB300),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
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
        val radius = size.minDimension * 0.35f // Slightly larger for afternoon
        val rayLength = size.minDimension * 0.18f
        val rayCount = 12 // More rays for brighter afternoon sun
        
        // Draw bright afternoon sun
        drawCircle(
            color = Color(0xFFFFD700), // Bright golden yellow
            radius = radius,
            center = center
        )
        
        // Draw rotating rays
        rotate(sunRotation, center) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * PI.toFloat() / 180f
                val startX = center.x + (radius + 5f) * cos(angle)
                val startY = center.y + (radius + 5f) * sin(angle)
                val endX = center.x + (radius + rayLength) * cos(angle)
                val endY = center.y + (radius + rayLength) * sin(angle)
                
                drawLine(
                    color = Color(0xFFFFEB3B),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
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
    
    val iconColor = if (color == Color.Unspecified) Color(0xFFE1BEE7) else color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.3f
        
        // Draw moon
        drawCircle(
            color = iconColor,
            radius = radius,
            center = center
        )
        
        // Draw stars
        for (i in 0..3) {
            val angle = (i * 90f) * PI.toFloat() / 180f
            val starX = center.x + (radius + size.minDimension * 0.2f) * cos(angle)
            val starY = center.y + (radius + size.minDimension * 0.2f) * sin(angle)
            drawCircle(
                color = iconColor.copy(alpha = 0.7f + moonPhase * 0.3f),
                radius = 2.dp.toPx(),
                center = Offset(starX, starY)
            )
        }
    }
}

/**
 * Animated Moon Phase Icon
 * Draws the moon phase correctly based on the actual phase and illumination
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
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )

    val iconColor = if (color == Color.Unspecified) Color(0xFFFFD700) else color
    val phaseUpper = phase.uppercase()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.35f
        
        // Draw glow effect for full moon
        if (illumination > 0.9f) {
            drawCircle(
                color = iconColor.copy(alpha = glowIntensity * 0.2f),
                radius = radius * 1.5f,
                center = center
            )
        }
        
        when {
            // New Moon - completely dark
            phaseUpper == "NEW_MOON" || illumination < 0.01 -> {
                // Draw a very faint outline or nothing
                drawCircle(
                    color = iconColor.copy(alpha = 0.1f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Full Moon - completely lit
            phaseUpper == "FULL_MOON" || illumination > 0.99 -> {
                drawCircle(
                    color = iconColor.copy(alpha = 0.95f * glowIntensity),
                    radius = radius,
                    center = center
                )
            }
            
            // First Quarter - right half lit
            phaseUpper == "FIRST_QUARTER" -> {
                drawMoonPhase(center, radius, iconColor, illumination = 0.5, litFromRight = true)
            }
            
            // Last Quarter - left half lit
            phaseUpper == "LAST_QUARTER" -> {
                drawMoonPhase(center, radius, iconColor, illumination = 0.5, litFromRight = false)
            }
            
            // Waxing Crescent - right side crescent
            phaseUpper == "WAXING_CRESCENT" -> {
                drawMoonPhase(center, radius, iconColor, illumination, litFromRight = true)
            }
            
            // Waning Crescent - left side crescent
            phaseUpper == "WANING_CRESCENT" -> {
                drawMoonPhase(center, radius, iconColor, illumination, litFromRight = false)
            }
            
            // Waxing Gibbous - mostly right side lit
            phaseUpper == "WAXING_GIBBOUS" -> {
                drawMoonPhase(center, radius, iconColor, illumination, litFromRight = true)
            }
            
            // Waning Gibbous - mostly left side lit
            phaseUpper == "WANING_GIBBOUS" -> {
                drawMoonPhase(center, radius, iconColor, illumination, litFromRight = false)
            }
            
            // Fallback: use illumination to determine phase
            else -> {
                val litFromRight = illumination < 0.5f || (illumination >= 0.5f && illumination < 1.0f && phaseUpper.contains("WAXING"))
                drawMoonPhase(center, radius, iconColor, illumination, litFromRight = litFromRight)
            }
        }
    }
}

/**
 * Helper function to draw moon phase with correct illumination
 */
private fun DrawScope.drawMoonPhase(
    center: Offset,
    radius: Float,
    color: Color,
    illumination: Double,
    litFromRight: Boolean
) {
    val illum = illumination.coerceIn(0.0, 1.0).toFloat()
    
    // Draw the full moon circle outline
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    
    if (illum < 0.01f) {
        // New moon - just outline
        return
    }
    
    if (illum > 0.99f) {
        // Full moon
        drawCircle(
            color = color.copy(alpha = 0.95f),
            radius = radius,
            center = center
        )
        return
    }
    
    if (illum <= 0.5f) {
        // Crescent: draw the lit crescent portion using path
        val crescentPath = Path()
        val sweepAngle = 180f * (illum * 2f)
        val startAngleDeg = if (litFromRight) 90f else -90f
        
        // Create crescent path: arc from center
        crescentPath.moveTo(center.x, center.y)
        
        // Draw arc points manually
        val steps = (sweepAngle / 2f).toInt().coerceAtLeast(10)
        for (i in 0..steps) {
            val angle = startAngleDeg + (sweepAngle / steps) * i
            val angleRad = Math.toRadians(angle.toDouble())
            val x = center.x + radius * cos(angleRad).toFloat()
            val y = center.y + radius * sin(angleRad).toFloat()
            if (i == 0) {
                crescentPath.lineTo(x, y)
            } else {
                crescentPath.lineTo(x, y)
            }
        }
        crescentPath.close()
        
        drawPath(
            path = crescentPath,
            color = color.copy(alpha = 0.9f)
        )
    } else {
        // Gibbous: draw full circle, then overlay shadow
        // Draw full lit circle
        drawCircle(
            color = color.copy(alpha = 0.9f),
            radius = radius,
            center = center
        )
        
        // Draw shadow using path to cover the dark portion
        val shadowAngle = 180f * (1f - (illum - 0.5f) * 2f)
        val shadowStartAngle = if (litFromRight) {
            -90f - shadowAngle / 2f // Shadow on left side
        } else {
            90f - shadowAngle / 2f // Shadow on right side
        }
        
        val shadowPath = Path()
        shadowPath.moveTo(center.x, center.y)
        
        val steps = (shadowAngle / 2f).toInt().coerceAtLeast(10)
        for (i in 0..steps) {
            val angle = shadowStartAngle + (shadowAngle / steps) * i
            val angleRad = Math.toRadians(angle.toDouble())
            val x = center.x + radius * cos(angleRad).toFloat()
            val y = center.y + radius * sin(angleRad).toFloat()
            shadowPath.lineTo(x, y)
        }
        shadowPath.close()
        
        drawPath(
            path = shadowPath,
            color = Color(0xFF0E1E3A).copy(alpha = 0.9f) // Dark shadow
        )
    }
}

