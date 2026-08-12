package com.weatherpossum.app.domain.hurricane

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HurricaneOutlookSectionParserTest {

    @Test
    fun splits_central_and_eastern_tropical_atlantic_into_separate_sections() {
        val text = """
            For the North Atlantic...Caribbean Sea and the Gulf of America:
            Active Systems:
            The National Hurricane Center is issuing advisories on Tropical Storm Cristobal.
            Central Tropical Atlantic (AL92):
            An area of showers and thunderstorms associated with a tropical wave is located about 950 miles west-southwest of the Cabo Verde Islands.
            * Formation chance through 48 hours...high...80 percent.
            * Formation chance through 7 days...high...80 percent.
            Eastern Tropical Atlantic:
            A tropical wave located several hundred miles south of the Cabo Verde Islands is producing disorganized showers and thunderstorms.
            * Formation chance through 48 hours...low...20 percent.
            * Formation chance through 7 days...medium...40 percent.
            ${'$'}${'$'}
            Forecaster Adams
        """.trimIndent()

        val sections = HurricaneOutlookSectionParser.parse(text)

        assertEquals(3, sections.size)
        assertEquals("Active Systems", sections[0].title)
        assertTrue(sections[0].content.contains("Tropical Storm Cristobal"))
        assertTrue(!sections[0].content.contains("Central Tropical Atlantic"))

        assertEquals("Central Tropical Atlantic (AL92)", sections[1].title)
        assertTrue(sections[1].content.contains("west-southwest of the Cabo Verde Islands"))
        assertTrue(!sections[1].content.contains("Eastern Tropical Atlantic"))

        assertEquals("Eastern Tropical Atlantic", sections[2].title)
        assertTrue(sections[2].content.contains("south of the Cabo Verde Islands"))
    }

    @Test
    fun does_not_treat_intro_gulf_heading_as_a_section() {
        val text = """
            For the North Atlantic...Caribbean Sea and the Gulf of America:
            Eastern Tropical Atlantic:
            A tropical wave is moving west.
        """.trimIndent()

        val sections = HurricaneOutlookSectionParser.parse(text)

        assertEquals(1, sections.size)
        assertEquals("Eastern Tropical Atlantic", sections[0].title)
    }
}
