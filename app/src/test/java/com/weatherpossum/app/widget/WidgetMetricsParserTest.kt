package com.weatherpossum.app.widget

import com.weatherpossum.app.data.model.WeatherCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetMetricsParserTest {

    @Test
    fun fromCards_extractsWindSeaAndTide() {
        val cards = listOf(
            WeatherCard("Wind", "East 12 to 17 mph with higher gusts near showers."),
            WeatherCard(
                "Sea & Tides",
                "Sea Conditions: Moderate\nWaves: 4 to 6 feet\nLow Tide: 2:14 PM\nHigh Tide: 8:31 PM (5.1 ft)"
            )
        )

        val metrics = WidgetMetricsParser.fromCards(cards)

        assertEquals("East 12 to 17 mph with higher gusts near showers.", metrics.wind)
        assertEquals("Moderate • 4 to 6 feet", metrics.sea)
        assertEquals("Low 2:14 PM | High 8:31 PM (5.1 ft)", metrics.tide)
        assertTrue(metrics.hasAny)
    }

    @Test
    fun fromCards_returnsEmptyWhenNoCoastalCards() {
        val metrics = WidgetMetricsParser.fromCards(
            listOf(WeatherCard("Synopsis", "Fair weather across the island."))
        )

        assertFalse(metrics.hasAny)
    }
}
