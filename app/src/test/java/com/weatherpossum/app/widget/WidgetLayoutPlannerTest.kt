package com.weatherpossum.app.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutPlannerTest {

    @Test
    fun compactSize_showsGreetingAndSynopsisOnly() {
        val plan = planFor(
            size = DpSize(250.dp, 110.dp),
            synopsis = "A high pressure system is the dominant feature. A tropical wave is approaching."
        )

        assertFalse(plan.showSea)
        assertTrue(plan.synopsisText.isNotBlank())
        assertTrue(plan.synopsisLineCount >= 1)
    }

    @Test
    fun tallSize_showsSeaWhenItFits() {
        val plan = planFor(
            size = DpSize(250.dp, 210.dp),
            synopsis = "A high pressure system is the dominant feature.",
            sea = "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet."
        )

        assertTrue(plan.showSea)
        assertEquals(
            "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet.",
            plan.seaText
        )
    }

    @Test
    fun mediumSize_prefersCompleteSentences() {
        val synopsis = "First sentence is here. Second sentence adds more detail. Third sentence is extra."
        val plan = planFor(
            size = DpSize(250.dp, 155.dp),
            synopsis = synopsis,
            sea = "Moderate seas."
        )

        assertFalse(plan.synopsisText.endsWith("Third"))
        assertTrue(
            plan.synopsisText == "First sentence is here." ||
                plan.synopsisText == "First sentence is here. Second sentence adds more detail."
        )
    }

    @Test
    fun tightSize_hidesSeaInsteadOfShrinkingTooFar() {
        val plan = planFor(
            size = DpSize(250.dp, 132.dp),
            synopsis = "A high pressure system is the dominant feature across the region today.",
            sea = "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet."
        )

        assertFalse(plan.showSea)
        assertTrue(plan.synopsisFontSp >= 12f)
    }

    @Test
    fun wideLayout_wrapsSynopsisWithoutForcingSea() {
        val plan = planFor(
            size = DpSize(320.dp, 110.dp),
            synopsis = "Fair weather continues across Dominica with light winds.",
            sea = "Moderate seas with waves up to 2 meters."
        )

        assertFalse(plan.showSea)
        assertTrue(plan.synopsisLineCount >= 1)
    }

    @Test
    fun typographyTier_scalesWithExactHeight() {
        assertEquals(0, WidgetLayoutPlanner.typographyTierForHeight(210f))
        assertEquals(1, WidgetLayoutPlanner.typographyTierForHeight(155f))
        assertEquals(2, WidgetLayoutPlanner.typographyTierForHeight(110f))
    }

    private fun planFor(
        size: DpSize,
        synopsis: String,
        sea: String? = null
    ) = WidgetLayoutPlanner.plan(
        size = size,
        greetingText = "Morning, Everton! 😊",
        synopsis = synopsis,
        seaConditions = sea
    )
}
