package com.weatherpossum.app.data.cache

import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.WeatherCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WeatherCardCacheTest {

    @Test
    fun round_trips_weather_cards_with_forecast_sections() {
        val cards = listOf(
            WeatherCard(
                title = "Synopsis",
                value = "Fair weather expected.",
                forecastSection = null
            ),
            WeatherCard(
                title = "Forecast for Tonight",
                value = "Partly cloudy.",
                forecastSection = ForecastSection.TONIGHT,
                validFrom = "6 PM"
            ),
            WeatherCard(
                title = "Custom",
                value = "Unknown section",
                forecastSection = ForecastSection.UNKNOWN("Custom")
            )
        )

        val json = WeatherCardCache.encode(cards)
        val decoded = WeatherCardCache.decode(json)

        assertNotNull(decoded)
        assertEquals(cards, decoded)
    }
}
