package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.util.SunCalculator
import kotlin.math.cos
import kotlin.math.sin
import com.weatherpossum.app.presentation.components.CardHeader

@Composable
fun SunCard(
    modifier: Modifier = Modifier
) {
    val sunrise = remember { SunCalculator.calculateSunrise() }
    val sunset = remember { SunCalculator.calculateSunset() }
    val solarNoon = remember { SunCalculator.calculateSolarNoon() }
    val dayLength = remember { SunCalculator.calculateDayLength() }
    val sunPosition = remember { SunCalculator.calculateCurrentSunPosition() }
    val sunProgress = remember { SunCalculator.calculateSunProgress() }
    val sunriseSunsetMoments = remember { SunCalculator.getSunriseSunsetMoments() }
    val solarNoonMoment = remember { SunCalculator.getSolarNoonMoment() }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFFB75E), Color(0xFFED8F03)))
    val onColor = Color.White

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                // Header
                CardHeader(
                    title = "SUN",
                    subtitle = "Daylight progress & times",
                    endContent = {
                        SunGlyph(color = onColor)
                    },
                    onColor = onColor
                )

                // Content: ring + details
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SunProgressRing(
                        progress = sunProgress / 100f,
                        showNoonTick = sunriseSunsetMoments.first != null && 
                                      sunriseSunsetMoments.second != null && 
                                      solarNoonMoment != null,
                        size = 120.dp,
                        stroke = 10.dp,
                        bgAlpha = 0.18f,
                        fg = onColor
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(
                        Modifier.weight(1f), 
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LabelRow("Sunrise", sunrise, onColor)
                        LabelRow("Sunset", sunset, onColor)
                        LabelRow("Day length", dayLength, onColor)
                        LabelRow("Solar noon", solarNoon, onColor)
                        LabelRow("Altitude", sunPosition.first, onColor)
                        LabelRow("Azimuth", sunPosition.second, onColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelRow(label: String, value: String, onColor: Color) {
    Row(
        Modifier.fillMaxWidth(), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label:",
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.width(90.dp)
        )
        Text(
            value, 
            color = onColor.copy(alpha = 0.96f), 
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SunGlyph(color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color.copy(alpha = 0.15f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(color = color, radius = this.size.minDimension / 2f)
        }
    }
}

@Composable
private fun SunProgressRing(
    progress: Float,
    showNoonTick: Boolean,
    size: Dp,
    stroke: Dp,
    bgAlpha: Float,
    fg: Color
) {
    val density = LocalDensity.current
    val strokePx = with(density) { stroke.toPx() }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val inset = strokePx / 2f
            val arcSize = Size(this.size.minDimension - strokePx, this.size.minDimension - strokePx)

            // Base ring
            drawArc(
                color = fg.copy(alpha = bgAlpha),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val clamped = progress.coerceIn(0f, 1f)
            if (clamped > 0f) {
                val sweep = clamped * 360f
                // SOLID COLOR (reliable on all Compose versions)
                drawArc(
                    color = fg,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // End dot
                val r = (this.size.minDimension - strokePx) / 2f
                val endAngleRad = Math.toRadians((sweep - 90f).toDouble())
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val dotX = (cx + r * cos(endAngleRad)).toFloat()
                val dotY = (cy + r * sin(endAngleRad)).toFloat()
                drawCircle(color = fg, radius = strokePx / 2.4f, center = Offset(dotX, dotY))
            }

            // Noon tick (simplified - draw at 50% progress since we don't have exact noon time)
            if (showNoonTick) {
                val noonFrac = 0.5f // Simplified - assume noon is at 50% of daylight
                val noonSweep = noonFrac * 360f
                val noonRad = Math.toRadians((noonSweep - 90f).toDouble())

                val r = (this.size.minDimension - strokePx) / 2f
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val tickOuter = Offset(
                    (cx + r * cos(noonRad)).toFloat(),
                    (cy + r * sin(noonRad)).toFloat()
                )
                val tickInner = Offset(
                    (cx + (r - strokePx * 0.9f) * cos(noonRad)).toFloat(),
                    (cy + (r - strokePx * 0.9f) * sin(noonRad)).toFloat()
                )
                drawLine(
                    color = fg.copy(alpha = 0.9f), 
                    start = tickInner, 
                    end = tickOuter, 
                    strokeWidth = strokePx * 0.18f
                )
            }
        }

        // Inner label so you can see if progress is > 0 at a glance
        Text(
            text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
