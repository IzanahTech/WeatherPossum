package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.presentation.ForecastDay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun ExtendedForecastCard(forecast: List<ForecastDay>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Title outside the main content area
        Text(
            text = "EXTENDED FORECAST",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 0.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp, end = 12.dp)
                .animateContentSize()
                .heightIn(min = 200.dp)
        ) {
            if (forecast.isNotEmpty()) {
                if (!expanded) {
                    ForecastSummaryRow(forecast.first())
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(modifier = Modifier.height(12.dp)) // Ensures space for the arrow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp) // Slightly larger for better tap area
                                .clickable { expanded = true },
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.9f), // More solid for contrast
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp), // Padding around icon
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand forecast",
                                    modifier = Modifier.size(28.dp), // Smaller icon inside bigger surface
                                    tint = Color.DarkGray // Stronger contrast
                                )
                            }
                        }
                    }
                } else if (expanded) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = expanded,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            forecast.forEachIndexed { _, day ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        ForecastSummaryRow(day)
                                    }
                                }
                            }

                            // Collapse arrow at bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { expanded = false },
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White.copy(alpha = 0.9f),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = "Collapse forecast",
                                            modifier = Modifier
                                                .size(28.dp)
                                                .graphicsLayer { rotationZ = 180f },
                                            tint = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastSummaryRow(day: ForecastDay) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Use Lottie or fallback icon
        val lottieRes = when {
            day.weather.contains("rain", ignoreCase = true) || day.weather.contains("shower", ignoreCase = true) -> com.weatherpossum.app.R.raw.rain
            day.weather.contains("cloud", ignoreCase = true) -> com.weatherpossum.app.R.raw.cloudy
            day.weather.contains("sun", ignoreCase = true) || day.weather.contains("clear", ignoreCase = true) -> com.weatherpossum.app.R.raw.sunny
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
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (day.date.isNotBlank()) {
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ForecastLine("🌤️ Weather", day.weather)
            ForecastLine("🌬️ Wind", day.wind)
            ForecastLine("🌊 Seas", day.seas)
            ForecastLine("🌊🌊 Waves", day.waves)
        }
    }
}

@Composable
fun ForecastLine(label: String, detail: String) {
    val annotated = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(label)
        }
        append(": ")
        append(detail)
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
} 