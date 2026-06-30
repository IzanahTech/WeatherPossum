package com.weatherpossum.app.util

import android.util.Log
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.calendar.astro.LunarTime
import net.time4j.calendar.astro.MoonPhase
import net.time4j.tz.Timezone
import net.time4j.tz.TZID
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.acos

private const val TAG = "MoonPhaseCalculator"
private const val PHASE_MATCH_TOLERANCE_SECONDS = 3_600L

/**
 * Moon phase calculator using Time4J for astronomical calculations.
 * Phase names and transition dates use Time4J's four major instants:
 * new moon, first quarter, full moon, and last quarter.
 */
data class MoonState(
    val phase: String,
    val illumination: Double,
    val elongationDeg: Double,
    val waxing: Boolean
)

data class NextMoonPhase(
    val phase: String,
    val dateLabel: String
)

object MoonPhaseCalculator {

    private const val DOMINICA_LAT = 15.414999
    private const val DOMINICA_LON = -61.370976
    private const val DOMINICA_TZ = "America/Dominica"

    private val astronomicalPhases = listOf(
        MoonPhase.NEW_MOON,
        MoonPhase.FIRST_QUARTER,
        MoonPhase.FULL_MOON,
        MoonPhase.LAST_QUARTER
    )

    val phaseCycle: List<String> = astronomicalPhases.map { it.name }

    fun computeMoonState(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        @Suppress("UNUSED_PARAMETER") elevationMeters: Double = 0.0,
        moment: Moment = Moment.nowInSystemTime()
    ): MoonState {
        val illumination = MoonPhase.getIllumination(moment)
        val phase = currentAstronomicalPhase(moment)
        val elongationDeg = elongationFromIllumination(illumination)

        return MoonState(
            phase = phase.name,
            illumination = illumination,
            elongationDeg = elongationDeg,
            waxing = isWaxing(moment)
        )
    }

    fun calculateMoonPhase(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON
    ): String = computeMoonState(latitude, longitude).phase

    fun calculateIllumination(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON
    ): Double = computeMoonState(latitude, longitude).illumination

    fun nextPhaseKey(currentPhase: String): String {
        val current = runCatching { MoonPhase.valueOf(currentPhase.uppercase()) }
            .getOrDefault(MoonPhase.NEW_MOON)
        return nextAstronomicalPhase(current).name
    }

    fun computeNextPhase(
        moment: Moment = Moment.nowInSystemTime()
    ): NextMoonPhase {
        val current = currentAstronomicalPhase(moment)
        val next = nextAstronomicalPhase(current)
        val nextMoment = next.after(moment)
        return NextMoonPhase(
            phase = next.name,
            dateLabel = formatPhaseDate(nextMoment)
        )
    }

    fun formatPhaseDate(moment: Moment): String {
        val tzid = Timezone.of(DOMINICA_TZ).id
        val plainDate = moment.toZonalTimestamp(tzid).toDate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern(
            "MMMM d, yyyy",
            java.util.Locale.ENGLISH
        )
        return LocalDate.of(
            plainDate.year,
            plainDate.getInt(PlainDate.MONTH_AS_NUMBER),
            plainDate.dayOfMonth
        ).format(formatter)
    }

    fun calculateMoonTimes(
        date: LocalDate = LocalDate.now(),
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON
    ): Pair<String, String> {
        return try {
            val plainDate = PlainDate.of(date.year, date.monthValue, date.dayOfMonth)
            val tzid: TZID = Timezone.of(DOMINICA_TZ).id
            val moonlight = LunarTime.ofLocation(tzid, latitude, longitude, 0).on(plainDate)

            val moonrise = formatMoonEvent(moonlight.moonrise(tzid), plainDate, tzid, latitude, longitude, rise = true)
            val moonset = formatMoonEvent(moonlight.moonset(tzid), plainDate, tzid, latitude, longitude, rise = false)
            Pair(moonrise, moonset)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate moonrise/moonset", e)
            Pair("N/A", "N/A")
        }
    }

    fun formatTimeTo12Hour(timeString: String): String {
        if (timeString.isBlank() || timeString == "N/A") {
            return "N/A"
        }

        if (timeString.contains("AM") || timeString.contains("PM")) {
            return timeString
        }

        if (timeString.contains("T") && timeString.length >= 19) {
            return try {
                val timePart = timeString.substringAfter("T").substringBeforeLast(":")
                val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
                val outputFormat = DateTimeFormatter.ofPattern("h:mm a")
                LocalTime.parse(timePart, inputFormat).format(outputFormat)
            } catch (e: Exception) {
                "N/A"
            }
        }

        if (timeString.matches(Regex("\\d{1,2}:\\d{2}"))) {
            return try {
                val inputFormat = DateTimeFormatter.ofPattern("H:mm")
                val outputFormat = DateTimeFormatter.ofPattern("h:mm a")
                LocalTime.parse(timeString, inputFormat).format(outputFormat)
            } catch (e: Exception) {
                "N/A"
            }
        }

        return "N/A"
    }

    internal fun elongationFromIllumination(illumination: Double): Double {
        val phaseAngleDeg = Math.toDegrees(
            acos((2.0 * illumination - 1.0).coerceIn(-1.0, 1.0))
        )
        return 180.0 - phaseAngleDeg
    }

    internal fun currentAstronomicalPhase(moment: Moment): MoonPhase {
        astronomicalPhases.forEach { phase ->
            val at = phase.atOrAfter(moment)
            if (moment.until(at, TimeUnit.SECONDS) <= PHASE_MATCH_TOLERANCE_SECONDS) {
                return phase
            }
        }

        return astronomicalPhases.maxBy { phase -> phase.before(moment) }
    }

    private fun nextAstronomicalPhase(current: MoonPhase): MoonPhase {
        val index = astronomicalPhases.indexOf(current).coerceAtLeast(0)
        return astronomicalPhases[(index + 1) % astronomicalPhases.size]
    }

    private fun isWaxing(moment: Moment): Boolean {
        val earlier = moment.minus(6, TimeUnit.HOURS)
        val later = moment.plus(6, TimeUnit.HOURS)
        val illuminationNow = MoonPhase.getIllumination(moment)
        val illuminationEarlier = MoonPhase.getIllumination(earlier)
        val illuminationLater = MoonPhase.getIllumination(later)
        return illuminationLater >= illuminationNow && illuminationNow >= illuminationEarlier
    }

    private fun formatMoonEvent(
        eventTime: net.time4j.PlainTimestamp?,
        plainDate: PlainDate,
        tzid: TZID,
        latitude: Double,
        longitude: Double,
        rise: Boolean
    ): String {
        if (eventTime != null) {
            val formatted = formatTime4ATimestamp(eventTime)
            return if (eventTime.toDate().isAfter(plainDate)) {
                "$formatted (tomorrow)"
            } else {
                formatted
            }
        }

        if (!rise) return "N/A"

        val tomorrow = plainDate.plus(1, net.time4j.CalendarUnit.DAYS)
        val tomorrowMoonlight = LunarTime.ofLocation(tzid, latitude, longitude, 0).on(tomorrow)
        val tomorrowRise = tomorrowMoonlight.moonrise(tzid) ?: return "N/A"
        return "${formatTime4ATimestamp(tomorrowRise)} (tomorrow)"
    }

    private fun formatTime4ATimestamp(timestamp: net.time4j.PlainTimestamp): String {
        return try {
            formatTimeTo12Hour(timestamp.toString())
        } catch (e: Exception) {
            "N/A"
        }
    }
}
