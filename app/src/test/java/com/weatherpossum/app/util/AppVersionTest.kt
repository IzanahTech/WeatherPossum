package com.weatherpossum.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun normalize_stripsTagPrefixAndAppName() {
        assert(AppVersion.normalize("v1.6.0") == "1.6.0")
        assert(AppVersion.normalize("WeatherPossum 1.6.0") == "1.6.0")
    }

    @Test
    fun isNewer_detectsPatchAndMinorBumps() {
        assertTrue(AppVersion.isNewer("v1.6.1", "1.6.0"))
        assertTrue(AppVersion.isNewer("WeatherPossum 1.7.0", "1.6.0"))
        assertFalse(AppVersion.isNewer("v1.6.0", "1.6.0"))
        assertFalse(AppVersion.isNewer("v1.5.9", "1.6.0"))
    }
}
