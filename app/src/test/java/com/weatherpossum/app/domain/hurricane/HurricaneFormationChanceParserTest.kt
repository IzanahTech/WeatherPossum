package com.weatherpossum.app.domain.hurricane

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HurricaneFormationChanceParserTest {

    @Test
    fun parses_formation_chance_with_percent_word() {
        val text = "* Formation chance through 48 hours...low...10 percent."
        val chances = HurricaneFormationChanceParser.parse(text)

        assertEquals(1, chances.size)
        assertEquals("48", chances[0].timeframe)
        assertEquals("hours", chances[0].unit)
        assertEquals("low", chances[0].level)
        assertEquals("10", chances[0].percentage)
    }

    @Test
    fun strips_formation_chance_from_body_text() {
        val text = "A tropical wave is moving west. * Formation chance through 48 hours...low...10 percent."
        val cleaned = HurricaneFormationChanceParser.stripFromText(text)

        assertTrue(cleaned.contains("tropical wave"))
        assertTrue(!cleaned.contains("Formation chance"))
    }
}
