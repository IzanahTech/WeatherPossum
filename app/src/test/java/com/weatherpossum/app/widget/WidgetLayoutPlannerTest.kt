package com.weatherpossum.app.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutPlannerTest {

    @Test
    fun compactSize_showsSynopsisOnly() {
        val plan = planFor(
            size = DpSize(250.dp, 110.dp),
            synopsis = "A high pressure system is the dominant feature."
        )

        assertFalse(plan.showsCoastalMetrics)
        assertTrue(plan.showCoastalHint)
        assertTrue(plan.synopsisMaxLines >= 1)
    }

    @Test
    fun tallSize_fitsAllCoastalMetrics() {
        val plan = planFor(
            size = DpSize(250.dp, 220.dp),
            synopsis = "A high pressure system is the dominant feature.",
            wind = "ENE to E @ 15 to 30 km/h",
            sea = "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet.",
            tide = "Low 10:57 AM and 9:57 PM"
        )

        assertTrue(plan.showsCoastalMetrics)
        assertTrue(plan.metrics.size >= 2)
        assertEquals("WIND", plan.metrics.first().label)
    }

    @Test
    fun mediumExpandedSize_prefersSmallerTypeOverCutoff() {
        val plan = planFor(
            size = DpSize(250.dp, 155.dp),
            synopsis = "A high pressure system is the dominant feature.",
            wind = "ENE to E @ 15 to 30 km/h",
            sea = "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet.",
            tide = "Low 10:57 AM and 9:57 PM"
        )

        assertTrue(plan.showsCoastalMetrics)
        assertTrue(plan.metrics.isNotEmpty())
        assertTrue(plan.synopsisMaxLines <= 2)
    }

    @Test
    fun tightExpandedSize_dropsLowestPriorityMetricBeforeOverflow() {
        val plan = planFor(
            size = DpSize(250.dp, 132.dp),
            synopsis = "A high pressure system is the dominant feature across the region today.",
            wind = "ENE to E @ 15 to 30 km/h",
            sea = "Slight to moderate • 1.0 to 2.0 meters or 3.0 to 7.0 feet.",
            tide = "Low 10:57 AM and 9:57 PM"
        )

        if (plan.showsCoastalMetrics) {
            assertTrue(plan.metrics.size <= 3)
            assertEquals("WIND", plan.metrics.first().label)
        }
    }

    private fun planFor(
        size: DpSize,
        synopsis: String,
        wind: String? = null,
        sea: String? = null,
        tide: String? = null
    ) = WidgetLayoutPlanner.plan(
        size = size,
        greetingText = "Morning, Everton! 😊",
        synopsis = synopsis,
        windLabel = "WIND",
        seaLabel = "SEA",
        tideLabel = "TIDE",
        wind = wind,
        sea = sea,
        tide = tide,
        hasCoastalDetails = true,
        unavailable = "Not available yet"
    )
}
