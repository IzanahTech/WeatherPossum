package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherpossum.app.R
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.ui.theme.WeatherPossumMotion

@Composable
fun CreditsCard() {
    var expanded by remember { mutableStateOf(false) }
    val expandRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = WeatherPossumMotion.fluidSpring(),
        label = "creditsExpandRotation"
    )

    ExpressiveCard(
        style = CardGradientStyle.Info,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_credits_title),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = WeatherIconType.CREDITS,
                            modifier = Modifier.fillMaxSize(),
                            color = onColor
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        Column(
            modifier = Modifier.animateContentSize(animationSpec = WeatherPossumMotion.fluidSpring())
        ) {
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CreditEntry(
                        text = stringResource(R.string.credits_local_weather),
                        onColor = onColor
                    )
                    CreditEntry(
                        text = stringResource(R.string.credits_hurricane_data),
                        onColor = onColor
                    )
                    CreditEntry(
                        text = stringResource(R.string.credits_time4j),
                        onColor = onColor
                    )
                    CreditEntry(
                        text = stringResource(R.string.credits_app_development),
                        onColor = onColor
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CreditsExpandChip(
                    iconRotation = expandRotation,
                    onClick = { expanded = !expanded },
                    onColor = onColor,
                    contentDescription = stringResource(
                        if (expanded) R.string.card_credits_collapse else R.string.card_credits_expand
                    )
                )
            }
        }
    }
}

@Composable
private fun CreditEntry(
    text: String,
    onColor: Color
) {
    val isDarkMode = isSystemInDarkTheme()
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .glassInset(onColor, isDarkMode)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        color = onColor.copy(alpha = 0.95f),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun CreditsExpandChip(
    iconRotation: Float,
    onClick: () -> Unit,
    onColor: Color,
    contentDescription: String
) {
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable { onClick() }
            .glassInset(onColor, isDarkMode, cornerRadius = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = contentDescription,
                tint = onColor,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}
