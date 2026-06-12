package com.weatherpossum.app.util

import android.util.Log
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.calendar.astro.SolarTime
import net.time4j.calendar.astro.SunPosition
import net.time4j.tz.Timezone
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "SunCalculator"

/**
 * Sun times and position using Time4A [SolarTime] / [SunPosition] (NOAA-based).
 */
data class SunState(
    val sunrise: String,
    val sunset: String,
    val solarNoon: String,
    val dayLength: String,
    val altitude: String,
    val azimuth: String,
    val progressPercent: Int
)

object SunCalculator {

    private const val DOMINICA_LAT = 15.414999
    private const val DOMINICA_LON = -61.370976
    private const val DOMINICA_TZ = "America/Dominica"

    fun computeSunState(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now(),
        moment: Moment = Moment.nowInSystemTime()
    ): SunState {
        val solar = SolarTime.ofLocation(latitude, longitude)
        val plainDate = PlainDate.of(date.year, date.monthValue, date.dayOfMonth)
        val timezone = Timezone.of(DOMINICA_TZ)

        val sunriseMoment = plainDate.get(solar.sunrise())
        val sunsetMoment = plainDate.get(solar.sunset())
        val noonMoment = plainDate.get(solar.transitAtNoon())

        val sunrise = formatMoment(sunriseMoment, timezone)
        val sunset = formatMoment(sunsetMoment, timezone)
        val solarNoon = formatMoment(noonMoment, timezone)
        val dayLength = formatDayLength(sunriseMoment, sunsetMoment)

        val position = SunPosition.at(moment, solar)
        val altitude = "${position.elevation.roundToInt()}°"
        val azimuth = "${normalizeAzimuth(position.azimuth).roundToInt()}°"
        val progress = calculateProgress(sunriseMoment, sunsetMoment, moment)

        return SunState(
            sunrise = sunrise,
            sunset = sunset,
            solarNoon = solarNoon,
            dayLength = dayLength,
            altitude = altitude,
            azimuth = azimuth,
            progressPercent = progress
        )
    }

    fun calculateSunrise(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): String = computeSunState(latitude, longitude, date).sunrise

    fun calculateSunset(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): String = computeSunState(latitude, longitude, date).sunset

    fun calculateSolarNoon(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): String = computeSunState(latitude, longitude, date).solarNoon

    fun calculateDayLength(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): String = computeSunState(latitude, longitude, date).dayLength

    fun calculateCurrentSunPosition(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON
    ): Pair<String, String> {
        val state = computeSunState(latitude, longitude)
        return state.altitude to state.azimuth
    }

    fun calculateSunProgress(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now(),
        moment: Moment = Moment.nowInSystemTime()
    ): Int = computeSunState(latitude, longitude, date, moment).progressPercent

    fun getSunriseSunsetMoments(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): Pair<Moment?, Moment?> {
        return try {
            val solar = SolarTime.ofLocation(latitude, longitude)
            val plainDate = PlainDate.of(date.year, date.monthValue, date.dayOfMonth)
            plainDate.get(solar.sunrise()) to plainDate.get(solar.sunset())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get sunrise/sunset moments", e)
            null to null
        }
    }

    fun getSolarNoonMoment(
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON,
        date: LocalDate = LocalDate.now()
    ): Moment? {
        return try {
            val solar = SolarTime.ofLocation(latitude, longitude)
            val plainDate = PlainDate.of(date.year, date.monthValue, date.dayOfMonth)
            plainDate.get(solar.transitAtNoon())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get solar noon moment", e)
            null
        }
    }

    internal fun formatMoment(moment: Moment?, timezone: Timezone): String {
        if (moment == null) return "N/A"
        return formatTimeTo12Hour(moment.toZonalTimestamp(timezone.id).toString())
    }

    internal fun formatDayLength(sunrise: Moment?, sunset: Moment?): String {
        if (sunrise == null || sunset == null) return "N/A"
        val minutes = sunrise.until(sunset, TimeUnit.MINUTES).toInt()
        if (minutes <= 0) return "N/A"
        val hours = minutes / 60
        val remainder = minutes % 60
        return if (remainder > 0) "${hours}h ${remainder}m" else "${hours}h"
    }

    internal fun calculateProgress(sunrise: Moment?, sunset: Moment?, now: Moment): Int {
        if (sunrise == null || sunset == null) return 0
        if (now.isBefore(sunrise)) return 0
        if (now.isAfter(sunset)) return 100
        val totalMinutes = sunrise.until(sunset, TimeUnit.MINUTES)
        if (totalMinutes <= 0) return 0
        val elapsedMinutes = sunrise.until(now, TimeUnit.MINUTES)
        return ((elapsedMinutes.toDouble() / totalMinutes) * 100.0)
            .coerceIn(0.0, 100.0)
            .roundToInt()
    }

    internal fun normalizeAzimuth(azimuthDeg: Double): Double {
        var value = azimuthDeg % 360.0
        if (value < 0) value += 360.0
        return value
    }

    fun formatTimeTo12Hour(timeString: String): String {
        if (timeString.isBlank() || timeString == "N/A") return "N/A"

        if (timeString.contains("AM", ignoreCase = true) || timeString.contains("PM", ignoreCase = true)) {
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
}
