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
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun ExtendedForecastCard(forecast: List<ForecastDay>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA))
    ) {
        // Title outside the main content area
        Text(
            text = "EXTENDED FORECAST",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 0.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp, end = 12.dp)
                .animateContentSize()
        ) {
            if (forecast.isNotEmpty()) {
                if (!expanded) {
                    ForecastSummaryRow(forecast.first())
                    Spacer(modifier = Modifier.height(8.dp))
                    // Make the arrow more visible with a background and proper padding
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { expanded = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand forecast",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    // Scrollable list of cards for each day
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(forecast) { idx, day ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        ForecastSummaryRow(day)
                                    }
                                }
                                if (idx == forecast.lastIndex) {
                                    // Make the collapse arrow more visible with a background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clickable { expanded = false },
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.7f),
                                            shadowElevation = 2.dp
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExpandMore,
                                                    contentDescription = "Collapse forecast",
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .graphicsLayer { rotationZ = 180f },
                                                    tint = Color.Gray
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
                    color = Color.Black,
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
    Text(
        text = "$label: $detail",
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 20.sp,
            color = Color.Black
        )
    )
} 