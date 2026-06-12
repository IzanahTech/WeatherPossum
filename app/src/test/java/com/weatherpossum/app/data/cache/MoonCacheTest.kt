package com.weatherpossum.app.data.cache

import com.weatherpossum.app.data.model.MoonData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MoonCacheTest {

    @Test
    fun round_trips_moon_data() {
        val data = MoonData(
            phase = "WAXING_GIBBOUS",
            moonrise = "8:42 PM",
            moonset = "7:15 AM",
            illumination = 0.82
        )

        val json = MoonCache.encode(data)
        val decoded = MoonCache.decode(json)

        assertNotNull(decoded)
        assertEquals(data.phase, decoded!!.phase)
        assertEquals(data.moonrise, decoded.moonrise)
        assertEquals(data.moonset, decoded.moonset)
        assertEquals(data.illumination, decoded.illumination, 0.001)
    }
}
