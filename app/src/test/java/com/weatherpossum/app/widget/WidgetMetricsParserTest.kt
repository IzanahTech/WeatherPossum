package com.weatherpossum.app.widget

import com.weatherpossum.app.data.model.WeatherCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetMetricsParserTest {

    @Test
    fun seaConditionsFromCards_extractsSeaAndWaves() {
        val cards = listOf(
            WeatherCard("Wind", "East 12 to 17 mph with higher gusts near showers."),
            WeatherCard(
                "Sea & Tides",
                "Sea Conditions: Moderate\nWaves: 4 to 6 feet\nLow Tide: 2:14 PM\nHigh Tide: 8:31 PM (5.1 ft)"
            )
        )

        val sea = WidgetMetricsParser.seaConditionsFromCards(cards)

        assertEquals("Moderate • 4 to 6 feet", sea)
    }

    @Test
    fun seaConditionsFromCards_returnsNullWhenNoSeaCard() {
        val sea = WidgetMetricsParser.seaConditionsFromCards(
            listOf(WeatherCard("Synopsis", "Fair weather across the island."))
        )

        assertNull(sea)
    }

    @Test
    fun splitSentences_keepsPunctuationBoundaries() {
        val sentences = WidgetTextWrap.splitSentences(
            "First sentence. Second sentence! Third one?"
        )

        assertEquals(3, sentences.size)
        assertTrue(sentences[0].endsWith("."))
    }
}
