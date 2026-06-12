package com.weatherpossum.app.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class ExtendedForecastParserTest {

    private fun loadFixture(name: String): String {
        val url = javaClass.classLoader!!.getResource("fixtures/dmo/$name")
            ?: error("Fixture not found: $name")
        return url.readText(StandardCharsets.UTF_8)
    }

    @Test
    fun parses_extended_forecast_june_2026() {
        val days = ExtendedForecastParser.parse(loadFixture("extended_forecast_june_2026.html"))

        assertEquals(3, days.size)

        val wednesday = days.first()
        assertEquals("Wednesday, Jun 10", wednesday.date)
        assertTrue(wednesday.maxTemp.contains("33"))
        assertTrue(wednesday.maxTemp.contains("91"))
        assertTrue(wednesday.minTemp.contains("24"))
        assertEquals(
            "Partly cloudy to cloudy and slightly hazy with a few showers",
            wednesday.weather
        )
        assertEquals("ENE to ESE @ 15 to 30km/h", wednesday.wind)
        assertEquals("Slight to moderate", wednesday.seas)
        assertEquals("1.0 to 1.5m/ 3.0 to 5.0ft", wednesday.waves)

        assertEquals("Thursday, Jun 11", days[1].date)
        assertEquals("Friday, Jun 12", days[2].date)
    }

    @Test
    fun uses_image_alt_when_weather_paragraph_missing() {
        val html = """
            <div id="ext_forecast">
              <div class="third">
                <h3>Saturday<br><span><strong>Jun 13</strong></span></h3>
                <div class="entry">
                  <p><strong>Max Temp.</strong>: 30 C</p>
                  <p class="centre"><img alt="Cloudy with showers" src="/images/test.png"></p>
                  <p><strong>Wind</strong>: E @ 20 km/h</p>
                </div>
              </div>
            </div>
        """.trimIndent()

        val days = ExtendedForecastParser.parse(html)
        assertEquals(1, days.size)
        assertEquals("Cloudy with showers", days.first().weather)
    }

    @Test
    fun ignores_wind_legend_outside_forecast_blocks() {
        val html = """
            <div itemprop="articleBody">
              <p><strong>Wind legend</strong>: N = North</p>
              <div id="ext_forecast">
                <div class="third">
                  <h3>Sunday<br><span><strong>Jun 14</strong></span></h3>
                  <div class="entry">
                    <p><strong>Weather</strong>: Fair</p>
                    <p><strong>Wind</strong>: E @ 10 km/h</p>
                  </div>
                </div>
              </div>
            </div>
        """.trimIndent()

        val days = ExtendedForecastParser.parse(html)
        assertEquals(1, days.size)
        assertEquals("E @ 10 km/h", days.first().wind)
    }
}
