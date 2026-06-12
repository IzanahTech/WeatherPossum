package com.weatherpossum.app.data.cache

import com.weatherpossum.app.data.model.ForecastDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExtendedForecastCacheTest {

    @Test
    fun round_trips_extended_forecast_days() {
        val days = listOf(
            ForecastDay(
                date = "Monday 10 June 2026",
                maxTemp = "31°C",
                minTemp = "24°C",
                weather = "Partly cloudy",
                wind = "E 15–20 kt",
                seas = "Moderate",
                waves = "1.5 m"
            )
        )

        val json = ExtendedForecastCache.encode(days)
        val decoded = ExtendedForecastCache.decode(json)

        assertNotNull(decoded)
        assertEquals(days, decoded)
    }
}
