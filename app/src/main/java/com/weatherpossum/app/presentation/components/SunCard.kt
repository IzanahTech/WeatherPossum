package com.weatherpossum.app.presentation.components

import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.util.SunCalculator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunCard(modifier: Modifier = Modifier) {
    val sunrise = remember { SunCalculator.calculateSunrise() }
    val sunset = remember { SunCalculator.calculateSunset() }
    val solarNoon = remember { SunCalculator.calculateSolarNoon() }
    val dayLength = remember { SunCalculator.calculateDayLength() }
    val sunPosition = remember { SunCalculator.calculateCurrentSunPosition() }
    val sunProgress = remember { SunCalculator.calculateSunProgress() }

    ExpressiveCard(
        modifier = modifier,
        style = CardGradientStyle.Sun,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_sun_title),
                subtitle = stringResource(R.string.card_sun_subtitle),
                endContent = { SunGlyph(color = onColor) },
                onColor = onColor
            )
        }
    ) { onColor ->
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                WavyCircleProgressIndicator(
                    progress = sunProgress / 100f,
                    color = onColor,
                    backgroundColor = onColor.copy(alpha = 0.18f),
                    strokeWidth = 10.dp,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = "$sunProgress%",
                    color = onColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SunDetailRow(stringResource(R.string.card_sun_sunrise), sunrise, onColor)
                SunDetailRow(stringResource(R.string.card_sun_sunset), sunset, onColor)
                SunDetailRow(stringResource(R.string.card_sun_day_length), dayLength, onColor)
                SunDetailRow(stringResource(R.string.card_sun_solar_noon), solarNoon, onColor)
                SunDetailRow(stringResource(R.string.card_sun_altitude), sunPosition.first, onColor)
                SunDetailRow(stringResource(R.string.card_sun_azimuth), sunPosition.second, onColor)
            }
        }
    }
}

@Composable
private fun SunDetailRow(label: String, value: String, onColor: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label:",
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.width(90.dp)
        )
        Text(value, color = onColor.copy(alpha = 0.96f), fontSize = 13.sp)
    }
}

@Composable
private fun SunGlyph(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunGlyph")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = WeatherPossumMotion.AmbientDrift)),
        label = "rotation"
    )
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = WeatherPossumMotion.AmbientDrift),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = WeatherPossumMotion.AmbientDrift),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(WeatherPossumDimens.iconLarge)
            .background(color.copy(alpha = 0.15f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.25f
            val radius = baseRadius * scale

            for (i in 1..3) {
                val glowRadius = radius * (1f + i * 0.3f) * glowIntensity
                drawCircle(
                    color = color.copy(alpha = (0.15f / i) * (glowIntensity - 0.8f) * 2.5f),
                    radius = glowRadius,
                    center = center
                )
            }

            rotate(rotation, pivot = center) {
                drawCircle(color = color, radius = radius, center = center)
                for (rayLayer in 0 until 2) {
                    val rayLength = radius * (0.6f + rayLayer * 0.2f)
                    val rayCount = if (rayLayer == 0) 12 else 8
                    val rayOffset = if (rayLayer == 0) 0f else 15f
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount) + rayOffset) * PI.toFloat() / 180f
                        val startX = center.x + radius * cos(angle)
                        val startY = center.y + radius * sin(angle)
                        val endX = center.x + (radius + rayLength) * cos(angle)
                        val endY = center.y + (radius + rayLength) * sin(angle)
                        drawLine(
                            color = color.copy(alpha = 0.9f - rayLayer * 0.2f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = (3f - rayLayer).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            drawCircle(
                color = color.copy(alpha = 0.4f),
                radius = radius * 0.4f,
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f)
            )
        }
    }
}
