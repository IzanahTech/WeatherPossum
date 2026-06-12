package com.weatherpossum.app.data.cache

import com.weatherpossum.app.data.model.Hurricane
import com.weatherpossum.app.data.model.HurricaneData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HurricaneCacheTest {

    @Test
    fun round_trips_hurricane_data() {
        val data = HurricaneData(
            activeStorms = listOf(
                Hurricane(
                    id = "al012026",
                    name = "Test Storm",
                    category = 1,
                    classification = "TS",
                    location = "15.0N, 60.0W",
                    windSpeed = 45,
                    pressure = 1002,
                    lastUpdated = "2026-06-10T12:00:00Z"
                )
            ),
            tropicalOutlook = "No significant development expected.",
            forecaster = "Forecaster Smith",
            issued = "10/1200 UTC",
            lastUpdated = 1_748_000_000_000L,
            isFromCache = false
        )

        val json = HurricaneCache.encode(data)
        val decoded = HurricaneCache.decode(json)

        assertNotNull(decoded)
        assertEquals(data.activeStorms, decoded!!.activeStorms)
        assertEquals(data.tropicalOutlook, decoded.tropicalOutlook)
        assertEquals(data.forecaster, decoded.forecaster)
        assertEquals(data.issued, decoded.issued)
        assertEquals(data.lastUpdated, decoded.lastUpdated)
        assertEquals(true, decoded.isFromCache)
    }
}
