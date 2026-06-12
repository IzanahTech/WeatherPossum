package com.weatherpossum.app.util

import net.time4j.PlainTimestamp
import net.time4j.calendar.astro.MoonPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonPhaseCalculatorTest {

    private fun utcMoment(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 18,
        minute: Int = 13,
        second: Int = 42
    ) = PlainTimestamp.of(year, month, day, hour, minute, second).atUTC()

    @Test
    fun new_moon_has_low_illumination_and_new_phase() {
        val moment = utcMoment(2000, 1, 6)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("NEW_MOON", state.phase)
        assertTrue(state.illumination < 0.02)
        assertTrue(state.elongationDeg < 10.0)
    }

    @Test
    fun full_moon_has_high_illumination_and_full_phase() {
        val moment = utcMoment(2000, 1, 20)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("FULL_MOON", state.phase)
        assertTrue(state.illumination > 0.98)
        assertTrue(state.elongationDeg > 170.0)
    }

    @Test
    fun first_quarter_is_waxing_near_ninety_degrees() {
        val moment = utcMoment(2000, 1, 14)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("FIRST_QUARTER", state.phase)
        assertTrue(state.waxing)
        assertTrue(state.elongationDeg in 80.0..100.0)
        assertTrue(state.illumination in 0.45..0.55)
    }

    @Test
    fun last_quarter_is_waning_near_ninety_degrees() {
        val moment = utcMoment(2000, 1, 27)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("LAST_QUARTER", state.phase)
        assertTrue(!state.waxing)
        assertTrue(state.elongationDeg in 80.0..100.0)
        assertTrue(state.illumination in 0.45..0.55)
    }

    @Test
    fun phase_label_uses_elongation_not_illumination_for_quarters() {
        assertEquals("First Quarter", MoonPhaseCalculator.phaseLabel(90.0, waxing = true))
        assertEquals("Last Quarter", MoonPhaseCalculator.phaseLabel(90.0, waxing = false))
        assertEquals("Waxing Gibbous", MoonPhaseCalculator.phaseLabel(120.0, waxing = true))
        assertEquals("Waning Crescent", MoonPhaseCalculator.phaseLabel(45.0, waxing = false))
    }

    @Test
    fun june_10_2026_matches_almanac_waning_crescent() {
        // Reference: USNO / major almanacs — Waning Crescent, ~25–31% lit on 2026-06-10
        // (new moon follows on 2026-06-15). Noon Atlantic (Dominica) ≈ 16:00 UTC.
        val moment = utcMoment(2026, 6, 10, hour = 16, minute = 0, second = 0)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("WANING_CRESCENT", state.phase)
        assertTrue("Expected ~25–32% illumination, got ${state.illumination}", state.illumination in 0.22..0.34)
        assertTrue(!state.waxing)
        assertTrue(state.elongationDeg in 20.0..75.0)
    }

    @Test
    fun computeMoonState_returns_valid_phase_key_and_illumination_range() {
        val moment = MoonPhase.FULL_MOON.atLunation(5)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertTrue(state.phase.matches(Regex("[A-Z_]+")))
        assertTrue(state.illumination in 0.0..1.0)
        assertTrue(state.elongationDeg in 0.0..180.0)
    }
}
