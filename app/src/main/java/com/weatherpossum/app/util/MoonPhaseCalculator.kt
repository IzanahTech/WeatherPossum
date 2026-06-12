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

/**
 * Moon phase calculator using Time4A for astronomical calculations.
 * Illumination uses Time4A's Meeus-based [MoonPhase.getIllumination].
 * Phase names combine illumination-derived elongation with a short-term illumination trend
 * to distinguish waxing from waning (including near first/last quarter).
 */
data class MoonState(
    val phase: String,
    val illumination: Double,
    val elongationDeg: Double,
    val waxing: Boolean
)

object MoonPhaseCalculator {

    private const val DOMINICA_LAT = 15.414999
    private const val DOMINICA_LON = -61.370976
    private const val DOMINICA_TZ = "America/Dominica"

    /**
     * Computes phase, illumination, and elongation in one pass.
     */
    fun computeMoonState(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        @Suppress("UNUSED_PARAMETER") elevationMeters: Double = 0.0,
        moment: Moment = Moment.nowInSystemTime()
    ): MoonState {
        val illumination = MoonPhase.getIllumination(moment)

        if (illumination < 0.02) {
            return MoonState(
                phase = "NEW_MOON",
                illumination = illumination,
                elongationDeg = 0.0,
                waxing = true
            )
        }
        if (illumination > 0.98) {
            return MoonState(
                phase = "FULL_MOON",
                illumination = illumination,
                elongationDeg = 180.0,
                waxing = false
            )
        }

        val phaseAngleDeg = Math.toDegrees(
            acos((2.0 * illumination - 1.0).coerceIn(-1.0, 1.0))
        )
        val elongationDeg = 180.0 - phaseAngleDeg
        val waxing = isWaxing(moment)
        val label = phaseLabel(elongationDeg, waxing)

        return MoonState(
            phase = label.uppercase().replace(' ', '_'),
            illumination = illumination,
            elongationDeg = elongationDeg,
            waxing = waxing
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

    private fun isWaxing(moment: Moment): Boolean {
        val earlier = moment.minus(6, TimeUnit.HOURS)
        val later = moment.plus(6, TimeUnit.HOURS)
        val illuminationNow = MoonPhase.getIllumination(moment)
        val illuminationEarlier = MoonPhase.getIllumination(earlier)
        val illuminationLater = MoonPhase.getIllumination(later)
        return illuminationLater >= illuminationNow && illuminationNow >= illuminationEarlier
    }

    /**
     * Names the eight canonical phases from sun–moon elongation (0° = new, 180° = full)
     * and whether the moon is waxing or waning.
     */
    internal fun phaseLabel(elongationDeg: Double, waxing: Boolean): String {
        val e = elongationDeg.coerceIn(0.0, 180.0)
        return when {
            e < 6.0 -> "New Moon"
            e > 174.0 -> "Full Moon"
            e < 84.0 -> if (waxing) "Waxing Crescent" else "Waning Crescent"
            e < 96.0 -> if (waxing) "First Quarter" else "Last Quarter"
            else -> if (waxing) "Waxing Gibbous" else "Waning Gibbous"
        }
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
