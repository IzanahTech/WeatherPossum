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
    fun new_moon_has_low_illumination() {
        val moment = utcMoment(2000, 1, 6)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertTrue(state.illumination < 0.02)
        assertTrue(state.elongationDeg < 10.0)
    }

    @Test
    fun full_moon_has_high_illumination() {
        val moment = utcMoment(2000, 1, 20)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertTrue(state.illumination > 0.98)
        assertTrue(state.elongationDeg > 170.0)
    }

    @Test
    fun after_full_moon_instant_still_reports_full_moon_period() {
        val moment = utcMoment(2000, 1, 14)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("FULL_MOON", state.phase)
        assertTrue(state.waxing)
        assertTrue(state.elongationDeg in 80.0..100.0)
        assertTrue(state.illumination in 0.45..0.55)
    }

    @Test
    fun last_quarter_period_is_waning_near_ninety_degrees() {
        val moment = utcMoment(2000, 1, 27)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("LAST_QUARTER", state.phase)
        assertTrue(!state.waxing)
        assertTrue(state.elongationDeg in 80.0..100.0)
        assertTrue(state.illumination in 0.45..0.55)
    }

    @Test
    fun current_phase_uses_most_recent_time4j_instant() {
        val moment = utcMoment(2026, 6, 10, hour = 16, minute = 0, second = 0)
        val phase = MoonPhaseCalculator.currentAstronomicalPhase(moment)

        assertEquals(MoonPhase.NEW_MOON, phase)
    }

    @Test
    fun june_10_2026_matches_time4j_new_moon_period() {
        val moment = utcMoment(2026, 6, 10, hour = 16, minute = 0, second = 0)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertEquals("NEW_MOON", state.phase)
        assertTrue("Expected ~25–32% illumination, got ${state.illumination}", state.illumination in 0.22..0.34)
        assertTrue(!state.waxing)
        assertTrue(state.elongationDeg in 20.0..75.0)
    }

    @Test
    fun computeMoonState_returns_valid_phase_key_and_illumination_range() {
        val moment = MoonPhase.FULL_MOON.atLunation(5)
        val state = MoonPhaseCalculator.computeMoonState(moment = moment)

        assertTrue(state.phase in MoonPhaseCalculator.phaseCycle)
        assertTrue(state.illumination in 0.0..1.0)
        assertTrue(state.elongationDeg in 0.0..180.0)
    }

    @Test
    fun nextPhaseKey_follows_four_phase_cycle() {
        assertEquals("FIRST_QUARTER", MoonPhaseCalculator.nextPhaseKey("NEW_MOON"))
        assertEquals("FULL_MOON", MoonPhaseCalculator.nextPhaseKey("FIRST_QUARTER"))
        assertEquals("LAST_QUARTER", MoonPhaseCalculator.nextPhaseKey("FULL_MOON"))
        assertEquals("NEW_MOON", MoonPhaseCalculator.nextPhaseKey("LAST_QUARTER"))
    }

    @Test
    fun june_10_2026_next_phase_is_first_quarter_from_time4j() {
        val moment = utcMoment(2026, 6, 10, hour = 16, minute = 0, second = 0)
        val next = MoonPhaseCalculator.computeNextPhase(moment)
        val expected = MoonPhase.FIRST_QUARTER.after(moment)

        assertEquals("FIRST_QUARTER", next.phase)
        assertEquals(MoonPhaseCalculator.formatPhaseDate(expected), next.dateLabel)
    }

    @Test
    fun full_moon_period_next_phase_is_last_quarter() {
        val moment = utcMoment(2000, 1, 14)
        val next = MoonPhaseCalculator.computeNextPhase(moment)

        assertEquals("LAST_QUARTER", next.phase)
        assertEquals(
            MoonPhaseCalculator.formatPhaseDate(MoonPhase.LAST_QUARTER.after(moment)),
            next.dateLabel
        )
    }
}
