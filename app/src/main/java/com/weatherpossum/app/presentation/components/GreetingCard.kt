package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R

@Composable
fun GreetingCard(
    userName: String?,
    synopsis: String?,
    modifier: Modifier = Modifier
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_good_morning)
        in 12..17 -> stringResource(R.string.greeting_good_afternoon)
        else -> stringResource(R.string.greeting_good_night)
    }

    val displayName = userName?.replaceFirstChar { it.uppercase() } ?: ""
    val personalizedGreeting = if (displayName.isNotBlank()) "$greeting, $displayName" else greeting

    // Select greeting animation based on time of day
    val greetingAnimation = when (hour) {
        in 5..11 -> R.raw.gmorning
        in 12..17 -> R.raw.afternoon
        else -> R.raw.night
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(greetingAnimation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = personalizedGreeting.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!synopsis.isNullOrBlank()) {
                    Text(
                        text = "SYNOPSIS: ${synopsis}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
} 