package com.weatherpossum.app.presentation.components

import androidx.compose.runtime.Composable
import com.weatherpossum.app.data.model.WeatherCard

@Composable
fun WeatherCardRouter(card: WeatherCard) {
    val iconType = getWeatherIconType("${card.title} ${card.value}")

    when {
        card.title.contains("Weather Outlook", ignoreCase = true) -> {
            OutlookWeatherCard(card = card, iconType = iconType)
        }
        card.title.contains("Forecast", ignoreCase = true) -> {
            WeatherInfoCard(
                title = card.title,
                description = card.value,
                iconType = iconType,
                style = WeatherCardStyle.Forecast
            )
        }
        card.title.contains("sun", ignoreCase = true) ||
            card.value.contains("sunrise", ignoreCase = true) ||
            card.value.contains("sunset", ignoreCase = true) -> {
            SunCard()
        }
        else -> {
            WeatherInfoCard(
                title = card.title,
                description = card.value,
                iconType = iconType,
                style = WeatherCardStyle.Default
            )
        }
    }
}
