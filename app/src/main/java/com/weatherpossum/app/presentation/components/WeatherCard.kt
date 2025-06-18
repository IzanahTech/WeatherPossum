package com.weatherpossum.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.data.model.WeatherCard as WeatherCardModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import java.time.LocalTime

@Composable
fun WeatherCard(
    card: WeatherCardModel,
    modifier: Modifier = Modifier,
    lottieRes: Int? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "cardScale"
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 8f,
        animationSpec = tween(durationMillis = 100),
        label = "cardElevation"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.9f,
        animationSpec = tween(durationMillis = 300),
        label = "contentAlpha"
    )

    val backgroundColor: Color = if (isDark) {
        when {
            card.title.contains("Sun Times", ignoreCase = true) -> Color(0xFF2D2A1A) // Darker yellow
            card.title.contains("Sea Conditions", ignoreCase = true) -> Color(0xFF1A2D35) // Darker blue
            card.title.contains("Wind Conditions", ignoreCase = true) -> Color(0xFF1A2D35) // Darker gray-blue
            card.title.contains("Forecast for Today", ignoreCase = true) -> Color(0xFF1A2D1A) // Darker green
            card.title.contains("Forecast for Tonight", ignoreCase = true) -> Color(0xFF1A2D2A) // Darker teal
            card.title.contains("Weather Outlook", ignoreCase = true) -> Color(0xFF2D251A) // Darker orange
            card.title.contains("Synopsis", ignoreCase = true) -> Color(0xFF2A1A2D) // Darker purple
            else -> MaterialTheme.colorScheme.surface
        }
    } else {
        when {
            card.title.contains("Sun Times", ignoreCase = true) -> Color(0xFFFFF9C4) // pale yellow
            card.title.contains("Sea Conditions", ignoreCase = true) -> Color(0xFFB3E5FC) // light blue
            card.title.contains("Wind Conditions", ignoreCase = true) -> Color(0xFFE1F5FE) // light gray-blue
            card.title.contains("Forecast for Today", ignoreCase = true) -> Color(0xFFC8E6C9) // light green
            card.title.contains("Forecast for Tonight", ignoreCase = true) -> Color(0xFFB2DFDB) // teal
            card.title.contains("Weather Outlook", ignoreCase = true) -> Color(0xFFFFE0B2) // light orange
            card.title.contains("Synopsis", ignoreCase = true) -> Color(0xFFE1BEE7) // light purple
            else -> Color.White
        }
    }
    
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) // Increased alpha for better visibility
    } else {
        Color.Transparent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .scale(cardScale)
            .shadow(
                elevation = cardElevation.dp,
                shape = MaterialTheme.shapes.medium,
                spotColor = if (isDark) Color.Black else Color.Transparent
            )
            .border(
                width = if (isDark) 1.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isExpanded = !isExpanded
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(contentAlpha)
        ) {
            // Weather animation with improved dark mode handling
            val animationSize = when {
                card.title.contains("Sun Times", ignoreCase = true) -> 120.dp
                card.title.contains("Wind Conditions", ignoreCase = true) -> 120.dp
                card.title.contains("Sea Conditions", ignoreCase = true) -> 120.dp
                else -> 100.dp
            }
            if (lottieRes != null) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(lottieRes),
                    onRetry = { retryCount, exception ->
                        // Log or handle retry if needed
                        true // Return true to retry, false to stop
                    }
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = true,
                    restartOnPlay = false // Prevent unnecessary restarts
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(animationSize)
                            .alpha(if (isDark) 0.85f else 1f)
                    )
                }
            } else {
                WeatherAnimation(
                    forecast = card.value,
                    title = card.title,
                    modifier = Modifier
                        .size(animationSize)
                        .alpha(if (isDark) 0.85f else 1f),
                    dimForDark = isDark
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title with improved contrast
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content with bold formatting for certain prefixes
            val boldLabels = listOf("Waves:", "Low Tide:", "High Tide:", "Sunrise:", "Sunset:", "Sea Conditions:")
            
            val annotated = buildAnnotatedString {
                card.value.lines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    val label = boldLabels.firstOrNull { trimmed.startsWith(it) }
                    if (label != null) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(label)
                        }
                        append(" " + trimmed.removePrefix(label).trim())
                    } else {
                        append(trimmed)
                    }
                    if (index != card.value.lines().lastIndex) append("\n")
                }
            }

            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isDark) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                textAlign = TextAlign.Start
            )
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
        else -> "neutral"
    }
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("animations/$animationName.json"),
        onRetry = { retryCount, exception ->
            // Log or handle retry if needed
            true // Return true to retry, false to stop
        }
    )
    
    DisposableEffect(composition) {
        onDispose {
            // No need to call cancel() as Lottie handles cleanup automatically
        }
    }
    
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
            isPlaying = true,
            restartOnPlay = false
        )
    }
} 