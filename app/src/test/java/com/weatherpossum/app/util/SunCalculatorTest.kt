package com.weatherpossum.app.util

import net.time4j.PlainTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SunCalculatorTest {

    private val timeParser = DateTimeFormatter.ofPattern("h:mm a")

    private fun assertTimeNear(expected: String, actual: String, toleranceMinutes: Int) {
        val expectedMinutes = LocalTime.parse(expected, timeParser).toSecondOfDay() / 60
        val actualMinutes = LocalTime.parse(actual, timeParser).toSecondOfDay() / 60
        assertTrue(
            "Expected $expected ±${toleranceMinutes}m but got $actual",
            kotlin.math.abs(expectedMinutes - actualMinutes) <= toleranceMinutes
        )
    }

    private val dominicaLat = 15.414999
    private val dominicaLon = -61.370976
    private val june10_2026 = LocalDate.of(2026, 6, 10)

    @Test
    fun june_10_2026_matches_dominica_met_office_sun_times() {
        // Dominica Meteorological Service forecast for 10 June 2026
        val state = SunCalculator.computeSunState(
            latitude = dominicaLat,
            longitude = dominicaLon,
            date = june10_2026,
            moment = PlainTimestamp.of(2026, 6, 10, 16, 0, 0).atUTC()
        )

        assertEquals("5:34 AM", state.sunrise)
        assertTimeNear("6:36 PM", state.sunset, toleranceMinutes = 2)
        assertTrue(state.dayLength.contains("13h"))
        assertTrue(state.progressPercent in 1..99)
    }

    @Test
    fun day_length_is_difference_between_sunrise_and_sunset() {
        val sunrise = PlainTimestamp.of(2026, 6, 10, 9, 34, 0).atUTC()
        val sunset = PlainTimestamp.of(2026, 6, 10, 22, 36, 0).atUTC()

        assertEquals("13h 2m", SunCalculator.formatDayLength(sunrise, sunset))
    }

    @Test
    fun progress_is_zero_before_sunrise_and_hundred_after_sunset() {
        val sunrise = PlainTimestamp.of(2026, 6, 10, 9, 34, 0).atUTC()
        val sunset = PlainTimestamp.of(2026, 6, 10, 22, 36, 0).atUTC()
        val before = PlainTimestamp.of(2026, 6, 10, 8, 0, 0).atUTC()
        val after = PlainTimestamp.of(2026, 6, 10, 23, 0, 0).atUTC()
        val midday = PlainTimestamp.of(2026, 6, 10, 16, 0, 0).atUTC()

        assertEquals(0, SunCalculator.calculateProgress(sunrise, sunset, before))
        assertEquals(100, SunCalculator.calculateProgress(sunrise, sunset, after))
        assertTrue(SunCalculator.calculateProgress(sunrise, sunset, midday) in 40..60)
    }

    @Test
    fun normalize_azimuth_wraps_to_zero_through_three_sixty() {
        assertEquals(90.0, SunCalculator.normalizeAzimuth(90.0), 0.001)
        assertEquals(10.0, SunCalculator.normalizeAzimuth(370.0), 0.001)
        assertEquals(350.0, SunCalculator.normalizeAzimuth(-10.0), 0.001)
    }
}
