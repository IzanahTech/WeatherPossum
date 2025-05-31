package com.weatherpossum.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.data.model.WeatherCard as WeatherCardModel
import java.time.LocalTime
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.draw.alpha

@Composable
fun WeatherCard(
    card: WeatherCardModel,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var animationState by remember { mutableStateOf(false) }
    
    // Trigger animation when the card becomes visible
    LaunchedEffect(Unit) {
        animationState = true
    }

    val isDark = isSystemInDarkTheme()
    // Choose background color based on card type
    val backgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else when {
        card.title.contains("Sun Times", ignoreCase = true) -> Color(0xFFFFF9C4) // pale yellow
        card.title.contains("Sea Conditions", ignoreCase = true) -> Color(0xFFB3E5FC) // light blue
        card.title.contains("Wind Conditions", ignoreCase = true) -> Color(0xFFE1F5FE) // light gray-blue
        card.title.contains("Forecast for Today", ignoreCase = true) -> Color(0xFFC8E6C9) // light green
        card.title.contains("Forecast for Tonight", ignoreCase = true) -> Color(0xFFB2DFDB) // teal
        card.title.contains("Weather Outlook", ignoreCase = true) -> Color(0xFFFFE0B2) // light orange
        card.title.contains("Synopsis", ignoreCase = true) -> Color(0xFFE1BEE7) // light purple
        else -> Color.White
    }
    val borderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent

    AnimatedVisibility(
        visible = animationState,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(width = if (isDark) 1.dp else 0.dp, color = borderColor, shape = MaterialTheme.shapes.medium),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Weather animation based on content and title
                WeatherAnimation(
                    forecast = card.value,
                    title = card.title,
                    modifier = Modifier.size(100.dp),
                    dimForDark = isDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title (centered and bold)
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Content (bold, split into paragraphs)
                val paragraphs = card.value.split(Regex("(?<=[.?!])\\s+(?=[A-Z])")).filter { it.isNotBlank() }
                paragraphs.forEachIndexed { idx, para ->
                    Text(
                        text = para.trim(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (idx != paragraphs.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherAnimation(
    forecast: String,
    title: String,
    modifier: Modifier = Modifier,
    dimForDark: Boolean = false
) {
    val isDaytime = remember {
        val currentTime = LocalTime.now()
        currentTime.isAfter(LocalTime.of(6, 0)) && currentTime.isBefore(LocalTime.of(18, 0))
    }

    val animationName = when {
        // Check title first for specific card types
        title.contains("Outlook", ignoreCase = true) -> "outlook"
        title.contains("Synopsis", ignoreCase = true) -> "synopsis"
        // Use today.json for all forecast cards related to today and tonight
        title.contains("Forecast for Today", ignoreCase = true) ||
        title.contains("Forecast for Today and Tonight", ignoreCase = true) ||
        title.contains("Forecast for Tonight", ignoreCase = true) -> "today"
        // Use sun.json for sun times
        title.contains("Sun Times", ignoreCase = true) -> "sun"
        // Use wind.json for wind conditions
        title.contains("Wind Conditions", ignoreCase = true) -> "wind"
        // Use different sea condition animations based on time of day
        title.contains("Sea Conditions", ignoreCase = true) -> if (isDaytime) "seaconday" else "seaconnight"
        // Then check forecast content for weather conditions
        forecast.contains("rain", ignoreCase = true) || forecast.contains("shower", ignoreCase = true) -> "rain"
        forecast.contains("thunder", ignoreCase = true) -> "thunder"
        forecast.contains("sunny", ignoreCase = true) || forecast.contains("clear", ignoreCase = true) -> "sunny"
        forecast.contains("cloud", ignoreCase = true) || forecast.contains("hazy", ignoreCase = true) -> "cloudy"
        else -> "default"
    }
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("animations/$animationName.json")
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = if (dimForDark) {
                modifier.alpha(0.7f)
            } else {
                modifier
            },
            isPlaying = true
        )
    }
} 