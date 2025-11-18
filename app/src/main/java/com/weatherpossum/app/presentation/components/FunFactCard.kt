package com.weatherpossum.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Assuming these are defined elsewhere
import com.weatherpossum.app.presentation.components.AnimatedWeatherIcon
import com.weatherpossum.app.presentation.components.WeatherIconType
import com.weatherpossum.app.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FunFactCardExpressive(facts: List<String>, modifier: Modifier = Modifier) {
    // 1. STATE MANAGEMENT with Rich Motion Hook
    var currentFact by remember { mutableStateOf(facts.random()) }
    // A key change for smooth text transition
    val factKey by remember { mutableStateOf(0) }

    // 2. DYNAMIC COLOR ADOPTION
    // Use Tertiary/Accent colors for an expressive, non-primary look
    val expressiveContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val expressiveContentColor = MaterialTheme.colorScheme.onTertiaryContainer

    // Determine the next fact before the state update for animation logic
    val onFactClicked: () -> Unit = {
        val newFact = facts.filter { it != currentFact }.randomOrNull() ?: currentFact
        currentFact = newFact // Triggers recomposition and animation
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            // Highlighting the interactive area
            .clickable(onClick = onFactClicked),

        // EXPRESSIVE SHAPE: Maintain large, welcoming shape
        shape = MaterialTheme.shapes.extraLarge, // Use built-in Material 3 shape scale

        colors = CardDefaults.cardColors(
            containerColor = expressiveContainerColor // Use dynamic color
        ),
        // EXPRESSIVE DEPTH: Use tonal elevation for a sense of floating
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp // Animated press
        )
    ) {
        // We remove the custom gradient and overlay logic to rely on the Card's defaults
        // and Material 3's built-in Tonal Elevation effects for color shifts.

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp) // Slightly increased padding for 'breathing room'
        ) {

            Column {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💡 DISCOVER",
                            color = expressiveContentColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Weather Possum Fact",
                            color = expressiveContentColor,
                            style = MaterialTheme.typography.headlineSmall, // Use M3 typography scale
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // AVD animation icon - retains expressive motion
                    AnimatedContent(
                        targetState = currentFact, // Animate content changes based on fact
                        transitionSpec = {
                            fadeIn(tween(300)) + slideInVertically(tween(300)) { fullHeight -> fullHeight } with
                                    fadeOut(tween(300)) + slideOutVertically(tween(300)) { fullHeight -> -fullHeight }
                        },
                        label = "FactTextTransition"
                    ) { _ ->
                        // This Box ensures the icon has a fixed size and animates the fact text smoothly
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedWeatherIcon(
                                type = WeatherIconType.FACT,
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFFFA000) // Darker yellow/amber for better visibility on light background
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // EXPRESSIVE DIVIDER: Use a simple, theme-aware M3 divider
                HorizontalDivider(
                    color = expressiveContentColor.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Fact content with RICH MOTION (AnimatedContent)
                AnimatedContent(
                    targetState = currentFact, // Key change: Animate content changes based on fact
                    transitionSpec = {
                        // Expressive slide and fade transition
                        slideInVertically(
                            animationSpec = tween(400, delayMillis = 50),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(tween(400)) togetherWith
                                slideOutVertically(
                                    animationSpec = tween(400),
                                    targetOffsetY = { -it / 2 }
                                ) + fadeOut(tween(400))
                    },
                    label = "FactTextChangeAnimation"
                ) { targetFact ->
                    Text(
                        text = targetFact,
                        color = expressiveContentColor,
                        style = MaterialTheme.typography.bodyLarge, // Use M3 typography
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth() // Important for measuring animation
                    )
                }
            }
        }
    }
}