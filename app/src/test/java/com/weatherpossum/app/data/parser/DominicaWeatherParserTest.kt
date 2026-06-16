package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.ForecastSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class DominicaWeatherParserTest {

    private fun loadFixture(name: String): String {
        val url = javaClass.classLoader!!.getResource("fixtures/dmo/$name")
            ?: error("Fixture not found: $name")
        return url.readText(StandardCharsets.UTF_8)
    }

    @Test
    fun parses_full_article_june_2026() {
        val parsed = DominicaWeatherParser.parse(loadFixture("forecast_article_june_2026.html"))

        assertEquals("12:00 PM on Wednesday 10 June 2026", parsed.validFrom)
        assertEquals("A high pressure system is becoming the dominant feature", parsed.synopsis)
        assertEquals("E to SE @ 10 to 30 km/h", parsed.wind)
        assertEquals("Slight to moderate", parsed.seaConditions)
        assertEquals("1.0 to 2.0 meters or 3.0 to 7.0 feet.", parsed.waves)
        assertNull(parsed.advisory)
        assertEquals("5:34 AM", parsed.sunrise)
        assertEquals("6:36 PM", parsed.sunset)
        assertEquals("5:32 AM and 4:57 PM", parsed.lowTide)
        assertEquals("11:45 AM and 11:18 PM", parsed.highTide)

        val shortTerm = parsed.shortTermForecast
        requireNotNull(shortTerm)
        assertTrue(shortTerm.section is ForecastSection.TODAY_TONIGHT)
        assertTrue(shortTerm.titleRaw.contains("Afternoon", ignoreCase = true))
        assertEquals(
            "Partly cloudy to cloudy and hazy with a few, brief showers",
            shortTerm.body
        )

        assertEquals(
            "Weather Outlook for Dominica and the Lesser Antilles",
            parsed.outlookTitle
        )
        assertEquals("12:00 PM on Wednesday 10 June 2026", parsed.outlookValidFrom)
        assertTrue(parsed.outlookText.orEmpty().contains("Saharan dust"))
        assertTrue(parsed.outlookText.orEmpty().contains("high pressure system will rebuild"))
    }

    @Test
    fun maps_article_to_weather_cards() {
        val parsed = DominicaWeatherParser.parse(loadFixture("forecast_article_june_2026.html"))
        val cards = WeatherCardMapper.toWeatherCards(parsed)

        assertTrue(cards.any { it.title == "Synopsis" })
        assertTrue(cards.any { it.title.contains("Afternoon & Tonight") })
        assertTrue(cards.any { it.title.contains("Weather Outlook for Dominica") })
        assertTrue(cards.none { it.title == "Warning/Advisory" })

        val forecastCard = cards.first { it.isForecastCard() }
        assertTrue(forecastCard.forecastSection is ForecastSection.TODAY_TONIGHT)
        assertEquals("12:00 PM on Wednesday 10 June 2026", forecastCard.validFrom)
        assertTrue(forecastCard.value.startsWith("Valid from:"))

        val outlookCard = cards.first { it.title.contains("Weather Outlook") }
        assertTrue(outlookCard.value.startsWith("Valid from:"))
    }

    @Test
    fun parses_flat_layout_without_column_wrappers() {
        val parsed = DominicaWeatherParser.parse(loadFixture("forecast_article_flat_layout.html"))

        assertEquals("A high pressure system is becoming the dominant feature", parsed.synopsis)
        assertEquals("E to SE @ 10 to 30 km/h", parsed.wind)
        val shortTermForecast = requireNotNull(parsed.shortTermForecast)
        assertEquals(
            "Partly cloudy to cloudy and hazy with a few, brief showers",
            shortTermForecast.body
        )
        assertEquals(
            "Weather Outlook for Dominica and the Lesser Antilles",
            parsed.outlookTitle
        )
        assertTrue(parsed.outlookText.orEmpty().contains("Saharan dust"))
    }

    @Test
    fun parses_renamed_wrapper_classes() {
        val parsed = DominicaWeatherParser.parse(loadFixture("forecast_article_renamed_wrappers.html"))

        assertEquals("Breezy with passing showers", parsed.synopsis)
        assertEquals("E @ 15 to 25 km/h", parsed.wind)
        val shortTermForecast = requireNotNull(parsed.shortTermForecast)
        assertTrue(shortTermForecast.section is ForecastSection.TONIGHT)
        assertEquals(
            "Weather Outlook for Dominica and the Lesser Antilles",
            parsed.outlookTitle
        )
        assertTrue(parsed.outlookText.orEmpty().contains("Moisture levels"))
    }

    @Test
    fun parseLoose_stops_at_next_labeled_field() {
        val html = """
            <div itemprop="articleBody">
              <p><strong>Forecast for Tonight:</strong></p>
              <p>Cloudy with showers.</p>
              <p><strong>Wind</strong>: E @ 20 km/h</p>
            </div>
        """.trimIndent()

        val result = DMOForecastParser.parseLoose(
            org.jsoup.Jsoup.parse(html).selectFirst("div[itemprop=articleBody]")!!
        )
        requireNotNull(result)
        assertEquals("Cloudy with showers.", result.body)
    }

    @Test
    fun parses_without_forecast_block() {
        val html = """
            <div itemprop="articleBody">
              <div class="col-sm-6">
                <p><strong>Synopsis</strong>: Breezy conditions continue</p>
                <p><strong>Wind</strong>: E @ 20 km/h</p>
              </div>
            </div>
        """.trimIndent()

        val parsed = DominicaWeatherParser.parse(html)
        assertNull(parsed.shortTermForecast)
        assertEquals("Breezy conditions continue", parsed.synopsis)
        assertEquals("E @ 20 km/h", parsed.wind)
    }
}
