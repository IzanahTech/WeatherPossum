package com.weatherpossum.app.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class DMOForecastParserTest {

    private fun loadFixture(name: String): String {
        val url = javaClass.classLoader!!.getResource("fixtures/dmo/$name")
            ?: error("Fixture not found: $name")
        return url.readText(StandardCharsets.UTF_8)
    }

    @Test fun todayTonight_is_classified_and_body_present() {
        val html = loadFixture("forecast_today_tonight.html")
        val res = DMOForecastParser.parse(html)
        assertTrue(res.section is ForecastSection.TODAY_TONIGHT)
        assertTrue(res.body.isNotBlank())
    }

    @Test fun tonight_variant_is_classified() {
        val html = """
            <div class="forecast_for_today">
              <p><strong>Forecast for Tonight:</strong></p>
              <p>Partly cloudy with brief showers.</p>
            </div>
        """.trimIndent()
        val res = DMOForecastParser.parse(html)
        assertTrue(res.section is ForecastSection.TONIGHT)
        assertTrue(res.body.contains("Partly cloudy"))
    }

    @Test fun this_evening_and_tonight_maps_to_today_tonight() {
        val html = loadFixture("forecast_this_evening_and_tonight.html")
        val res = DMOForecastParser.parse(html)
        assertTrue(res.section is ForecastSection.TODAY_TONIGHT)
    }

    @Test fun missing_strong_uses_first_p_as_title() {
        val html = loadFixture("forecast_no_strong.html")
        val res = DMOForecastParser.parse(html)
        assertTrue(res.titleRaw.isNotBlank())
        assertTrue(res.body.isNotBlank())
    }

    @Test fun unknown_title_returns_UNKNOWN_but_keeps_body() {
        val html = loadFixture("forecast_unknown_title.html")
        val res = DMOForecastParser.parse(html)
        assertTrue(res.section is ForecastSection.UNKNOWN)
        assertTrue(res.body.isNotBlank())
    }
}
