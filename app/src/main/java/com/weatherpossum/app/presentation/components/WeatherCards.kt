package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

enum class WeatherCardStyle {
    Default,
    Outlook,
    Advisory,
    Forecast
}

@Composable
fun WeatherInfoCard(
    title: String,
    description: String,
    iconType: WeatherIconType,
    style: WeatherCardStyle = WeatherCardStyle.Default
) {
    val gradientStyle = when (style) {
        WeatherCardStyle.Outlook -> CardGradientStyle.Outlook
        WeatherCardStyle.Advisory -> CardGradientStyle.Advisory
        WeatherCardStyle.Forecast -> CardGradientStyle.Forecast
        WeatherCardStyle.Default -> CardGradientStyle.Info
    }

    ExpressiveCard(
        style = gradientStyle,
        header = { onColor ->
            CardHeader(
                title = title.uppercase(),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = iconType,
                            modifier = Modifier.fillMaxSize(),
                            color = onColor
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = onColor.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OutlookWeatherCard(
    card: WeatherCard,
    iconType: WeatherIconType
) {
    WeatherInfoCard(
        title = card.title,
        description = card.value,
        iconType = iconType,
        style = WeatherCardStyle.Outlook
    )
}
