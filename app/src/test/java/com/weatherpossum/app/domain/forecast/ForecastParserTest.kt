package com.weatherpossum.app.domain.forecast

import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.WeatherCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalTime

class ForecastParserTest {

    @Test
    fun afternoon_and_tonight_forecast_visible_at_night() {
        val card = WeatherCard(
            title = "Forecast for This Afternoon & Tonight",
            value = "Partly cloudy with brief showers",
            forecastSection = ForecastSection.TODAY_TONIGHT
        )

        val parser = ForecastParser(listOf(card))
        val result = parser.getForecastForNow()

        assertNotNull(result)
        assertEquals(card, result)
    }

    @Test
    fun tonight_forecast_not_visible_in_morning() {
        val card = WeatherCard(
            title = "Forecast for Tonight",
            value = "Mostly clear",
            forecastSection = ForecastSection.TONIGHT
        )

        val matches = ForecastSection.TONIGHT.matchesNow(
            title = card.title,
            now = LocalTime.of(9, 0)
        )
        assertEquals(false, matches)
    }

    @Test
    fun today_and_tonight_visible_all_day() {
        val matchesMorning = ForecastSection.TODAY_TONIGHT.matchesNow(
            title = "Forecast for Today and Tonight",
            now = LocalTime.of(9, 0)
        )
        val matchesNight = ForecastSection.TODAY_TONIGHT.matchesNow(
            title = "Forecast for Today and Tonight",
            now = LocalTime.of(21, 0)
        )

        assertEquals(true, matchesMorning)
        assertEquals(true, matchesNight)
    }

    @Test
    fun legacy_title_with_and_tonight_normalizes_to_full_day() {
        assertEquals(
            ForecastPeriod.FULL_DAY,
            normalizeTitle("Forecast for This Afternoon & Tonight")
        )
    }
}
