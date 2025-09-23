package com.weatherpossum.app.data.parser

import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets

class TwoTextParserTest {

    private fun loadFixture(name: String): String {
        val url = javaClass.classLoader!!.getResource("fixtures/nhc/$name")
            ?: error("Fixture not found: $name")
        return url.readText(StandardCharsets.UTF_8)
    }

    @Test fun two_1_extracts_issuance_time_and_items() {
        val html = loadFixture("two_1.txt")
        val res = TwoTextParser.parse(html)
        
        assertNotNull("Issued time should be extracted", res.issued)
        assertTrue("Issued time should contain AM EDT", res.issued!!.contains("AM EDT"))
        assertTrue("Should have items", res.items.isNotEmpty())
        assertTrue("Cleaned text should not be empty", res.cleaned.isNotBlank())
        assertTrue("Should contain Active Systems", res.cleaned.contains("Active Systems"))
    }

    @Test fun two_2_extracts_forecaster_and_numbered_items() {
        val html = loadFixture("two_2.txt")
        val res = TwoTextParser.parse(html)
        
        assertNotNull("Forecaster should be extracted", res.forecaster)
        assertTrue("Forecaster should contain 'Brown'", res.forecaster!!.contains("Brown"))
        assertTrue("Should have numbered items", res.items.any { it.startsWith("1.") })
        assertTrue("Cleaned text should contain formation chances", res.cleaned.contains("Formation chance"))
    }

    @Test fun two_3_handles_no_active_systems() {
        val html = loadFixture("two_3.txt")
        val res = TwoTextParser.parse(html)
        
        assertTrue("Should have items even with no active systems", res.items.isNotEmpty())
        assertTrue("Should contain 'None at this time'", res.cleaned.contains("None at this time"))
        assertTrue("Should contain numbered disturbances", res.items.any { it.startsWith("1.") })
    }

    @Test fun parse_handles_malformed_html() {
        val malformedHtml = "<html><body><p>800 AM EDT Tue Sep 23 2025</p><p>Some content</p></body></html>"
        val res = TwoTextParser.parse(malformedHtml)
        
        assertNotNull("Should still extract issued time", res.issued)
        assertTrue("Cleaned text should not be empty", res.cleaned.isNotBlank())
    }

    @Test fun parse_handles_empty_html() {
        val emptyHtml = "<html><body></body></html>"
        val res = TwoTextParser.parse(emptyHtml)
        
        assertTrue("Cleaned text should have fallback content", res.cleaned.isNotBlank())
        assertEquals("Should have fallback title", "Atlantic Tropical Weather Outlook", res.cleaned)
    }
}
