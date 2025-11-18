package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Material You Expressive Loading Indicator
 * Uses the standard CircularProgressIndicator with Expressive styling
 */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = 4.dp
    )
}

/**
 * Wavy Line Progress Indicator
 * Material You Expressive style wavy progress bar
 */
@Composable
fun WavyLineProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = 0.2f),
    height: Dp = 4.dp,
    waveAmplitude: Dp = 2.dp,
    waveFrequency: Float = 3f
) {
    val density = LocalDensity.current
    val heightPx = with(density) { height.toPx() }
    val amplitudePx = with(density) { waveAmplitude.toPx() }
    
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val width = size.width
            val clampedProgress = progress.coerceIn(0f, 1f)
            val progressWidth = width * clampedProgress
            
            // Background wavy line
            drawWavyLine(
                startX = 0f,
                endX = width,
                y = size.height / 2f,
                amplitude = amplitudePx,
                frequency = waveFrequency,
                phase = phase,
                color = backgroundColor,
                strokeWidth = heightPx
            )
            
            // Progress wavy line
            if (progressWidth > 0) {
                drawWavyLine(
                    startX = 0f,
                    endX = progressWidth,
                    y = size.height / 2f,
                    amplitude = amplitudePx,
                    frequency = waveFrequency,
                    phase = phase,
                    color = color,
                    strokeWidth = heightPx
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWavyLine(
    startX: Float,
    endX: Float,
    y: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = androidx.compose.ui.graphics.Path()
    val step = 2f
    var x = startX
    
    path.moveTo(x, y + amplitude * sin((x * frequency / size.width + phase).toDouble()).toFloat())
    
    while (x < endX) {
        x += step
        val waveY = y + amplitude * sin((x * frequency / size.width + phase).toDouble()).toFloat()
        path.lineTo(x, waveY)
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

/**
 * Wavy Circle Progress Indicator
 * Material You Expressive style wavy circular progress
 */
@Composable
fun WavyCircleProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = 0.2f),
    strokeWidth: Dp = 8.dp,
    waveAmplitude: Dp = 2.dp,
    waveFrequency: Float = 12f
) {
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    val amplitudePx = with(density) { waveAmplitude.toPx() }
    
    val infiniteTransition = rememberInfiniteTransition(label = "wavyCircle")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension - strokePx) / 2f
            val inset = strokePx / 2f
            val arcSize = Size(size.minDimension - strokePx, size.minDimension - strokePx)
            
            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            
            // Wavy progress path
            val clampedProgress = progress.coerceIn(0f, 1f)
            if (clampedProgress > 0f) {
                val path = androidx.compose.ui.graphics.Path()
                val totalAngle = clampedProgress * 360f
                val startAngle = -90f
                val step = 2f // degrees per point
                
                var angle = startAngle
                val baseRadius = radius
                
                while (angle <= startAngle + totalAngle) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val waveOffset = amplitudePx * sin((angleRad * waveFrequency + phase).toDouble()).toFloat()
                    val currentRadius = baseRadius + waveOffset
                    
                    val x = center.x + currentRadius * cos(angleRad).toFloat()
                    val y = center.y + currentRadius * sin(angleRad).toFloat()
                    
                    if (angle == startAngle) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    
                    angle += step
                }
                
                // Draw the wavy path
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

