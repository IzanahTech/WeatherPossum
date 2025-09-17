package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.presentation.ForecastDay

/* ──────────────────────────────────────────────────────────────────────────────
   EXTENDED FORECAST CARD – enhanced styling
   - Gradient background inside the Card (so elevation shadow still renders)
   - Strong header row (title + first-day date + Lottie glyph)
   - Each day is a sub-card with rounded corners & subtle overlay for separation
   - Bigger day headers (18sp) for quick scanning
   - Aligned label/value rows with fixed label column width
   - Polished expand/collapse chip (CENTERED)
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun ExtendedForecastCard(forecast: List<ForecastDay>) {
    var expanded by remember { mutableStateOf(false) }

    val gradientTop = Color(0xFF5EB7FF)
    val gradientBottom = Color(0xFF2F80ED)
    val onGradient = Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(Brush.verticalGradient(listOf(gradientTop, gradientBottom)))
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .heightIn(min = 200.dp)
            ) {
                // ── Header ───────────────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXTENDED FORECAST",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onGradient,
                            letterSpacing = 0.5.sp
                        )
                        val dateLabel = forecast.firstOrNull()?.date.orEmpty()
                        if (dateLabel.isNotBlank()) {
                            Text(
                                text = dateLabel,
                                fontSize = 14.sp,
                                color = onGradient.copy(alpha = 0.92f)
                            )
                        }
                    }

                    // Header Lottie from the first day
                    if (forecast.isNotEmpty()) {
                        val head = forecast.first()
                        val lottieRes = when {
                            head.weather.contains("rain", true) || head.weather.contains("shower", true)
                            -> com.weatherpossum.app.R.raw.rain
                            head.weather.contains("cloud", true)
                            -> com.weatherpossum.app.R.raw.cloudy
                            head.weather.contains("sun", true) || head.weather.contains("clear", true)
                            -> com.weatherpossum.app.R.raw.sunny
                            else -> com.weatherpossum.app.R.raw.neutral
                        }
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                        val progress by animateLottieCompositionAsState(
                            composition,
                            iterations = LottieConstants.IterateForever
                        )
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = onGradient.copy(alpha = 0.18f), thickness = 1.dp)
                Spacer(Modifier.height(6.dp))

                // ── Collapsed vs Expanded content ───────────────────────────────
                if (forecast.isNotEmpty()) {
                    if (!expanded) {
                        // Collapsed: show the first day only
                        DayForecastBlock(
                            day = forecast.first(),
                            onTextColor = onGradient
                        )

                        Spacer(Modifier.height(10.dp))
                        // CENTERED expand chip
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CenterChip(
                                iconRotation = 0f,
                                onClick = { expanded = true },
                                background = Color.White.copy(alpha = 0.95f),
                                tint = Color(0xFF2B2B2B)
                            )
                        }
                    } else {
                        Column {
                            // Expanded: show every day as its own sub-card
                            forecast.forEach { day ->
                                DayForecastBlock(
                                    day = day,
                                    onTextColor = onGradient
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            // CENTERED collapse chip
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CenterChip(
                                    iconRotation = 180f,
                                    onClick = { expanded = false },
                                    background = Color.White.copy(alpha = 0.95f),
                                    tint = Color(0xFF2B2B2B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   DAY BLOCK – large header + separated surface
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun DayForecastBlock(
    day: ForecastDay,
    onTextColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.10f),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            // Large, scannable day header
            Text(
                text = day.date,                  // e.g., "Sunday, Sep 14"
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = onTextColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(8.dp))

            // Details row (no date inside)
            ForecastSummaryRow(
                day = day,
                onTextColor = onTextColor
            )
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   SUMMARY ROW – aligned label/value, Lottie on the left
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun ForecastSummaryRow(
    day: ForecastDay,
    onTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Lottie (same logic as before)
        val lottieRes = when {
            day.weather.contains("rain", true) || day.weather.contains("shower", true)
            -> com.weatherpossum.app.R.raw.rain
            day.weather.contains("cloud", true)
            -> com.weatherpossum.app.R.raw.cloudy
            day.weather.contains("sun", true) || day.weather.contains("clear", true)
            -> com.weatherpossum.app.R.raw.sunny
            else -> com.weatherpossum.app.R.raw.neutral
        }
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            AlignedDetailRow(label = "Weather", value = day.weather, color = onTextColor)
            AlignedDetailRow(label = "Wind",    value = day.wind,    color = onTextColor)
            AlignedDetailRow(label = "Seas",    value = day.seas,    color = onTextColor)
            AlignedDetailRow(label = "Waves",   value = day.waves,   color = onTextColor)
        }
    }
}

/* Label/value with fixed label width for tidy columns */
@Composable
private fun AlignedDetailRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(78.dp) // tweak to balance line breaks
        )
        Text(
            text = value,
            color = color.copy(alpha = 0.96f),
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

/* Centered circular chip for expand/collapse */
@Composable
private fun CenterChip(
    iconRotation: Float,
    onClick: () -> Unit,
    background: Color,
    tint: Color
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = background,
        shadowElevation = 6.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}

