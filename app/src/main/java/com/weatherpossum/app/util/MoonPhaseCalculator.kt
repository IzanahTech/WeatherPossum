package com.weatherpossum.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.util.Log
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.calendar.astro.MoonPhase
import net.time4j.calendar.astro.LunarTime
import net.time4j.calendar.astro.GeoLocation
import net.time4j.calendar.astro.SolarTime
import net.time4j.calendar.astro.SunPosition
import net.time4j.calendar.astro.MoonPosition
import net.time4j.tz.Timezone
import net.time4j.tz.TZID
import kotlin.math.*

/**
 * Moon phase calculator using Time4A library for professional astronomical calculations
 * Provides accurate moon phase, illumination, and moonrise/moonset calculations
 */

data class Illumination(
    val fraction: Double,       // 0.0 .. 1.0
    val elongationDeg: Double,  // ψ in degrees
    val waxing: Boolean,        // true = waxing, false = waning
    val phaseLabel: String
)

object MoonPhaseCalculator {
    
    // Dominica coordinates
    private const val DOMINICA_LAT = 15.414999
    private const val DOMINICA_LON = -61.370976
    
    /**
     * Calculate the current moon phase using Time4A library
     * @return Moon phase as a string
     */
    fun calculateMoonPhase(): String {
        return try {
            // Use the accurate Sun-Moon elongation calculation
            val ill = computeIllumination(DOMINICA_LAT, DOMINICA_LON, 0.0)
            val phaseName = ill.phaseLabel.uppercase().replace(" ", "_")
            
            phaseName
        } catch (e: Exception) {
            "WANING_CRESCENT"
        }
    }
    
    /**
     * Calculate moonrise and moonset times using Time4A library
     * @param date The date to calculate for
     * @param latitude Latitude in degrees (defaults to Dominica coordinates)
     * @param longitude Longitude in degrees (defaults to Dominica coordinates)
     * @return Pair of moonrise and moonset times as strings
     */
    fun calculateMoonTimes(
        date: LocalDate = LocalDate.now(),
        latitude: Double = DOMINICA_LAT,
        longitude: Double = DOMINICA_LON
    ): Pair<String, String> {
        return try {
            // Create a PlainDate for the given date
            val plainDate = PlainDate.of(date.year, date.monthValue, date.dayOfMonth)
            
            // Create timezone for Dominica - get the TZID from the Timezone
            val tz = Timezone.of("America/Dominica")
            val tzid: TZID = tz.id
            
            // Create lunar calculator using correct Time4A API
            val moonlight = LunarTime.ofLocation(tzid, latitude, longitude, 0)
                .on(plainDate)
            
            // Get moonrise and moonset times - check for next day occurrences
            val moonrise = try {
                val riseTime = moonlight.moonrise(tzid)
                if (riseTime != null) {
                    val riseDate = riseTime.toDate()
                    val formattedTime = formatTime4ATimestamp(riseTime)
                    
                    if (riseDate.isAfter(plainDate)) {
                        "$formattedTime (tomorrow)"
                    } else {
                        formattedTime
                    }
                } else {
                    // If moonrise is null for today, check tomorrow
                    val tomorrow = plainDate.plus(1, net.time4j.CalendarUnit.DAYS)
                    val tomorrowMoonlight = LunarTime.ofLocation(tzid, latitude, longitude, 0)
                        .on(tomorrow)
                    val tomorrowRiseTime = tomorrowMoonlight.moonrise(tzid)
                    
                    if (tomorrowRiseTime != null) {
                        val formattedTime = formatTime4ATimestamp(tomorrowRiseTime)
                        "$formattedTime (tomorrow)"
                    } else {
                        "N/A"
                    }
                }
            } catch (e: Exception) {
                "N/A"
            }
            
            val moonset = try {
                val setTime = moonlight.moonset(tzid)
                if (setTime != null) {
                    val setDate = setTime.toDate()
                    val formattedTime = formatTime4ATimestamp(setTime)
                    
                    if (setDate.isAfter(plainDate)) {
                        "$formattedTime (tomorrow)"
                    } else {
                        formattedTime
                    }
                } else {
                    "N/A"
                }
            } catch (e: Exception) {
                "N/A"
            }
            
            Pair(moonrise, moonset)
        } catch (e: Exception) {
            Pair("N/A", "N/A")
        }
    }
    
    /**
     * Calculate moon illumination percentage using Time4A library
     * Uses Sun-Moon angular separation (elongation) for accurate illumination calculation
     * @return Illumination as a percentage (0.0 to 1.0)
     */
    fun calculateIllumination(): Double {
        return try {
            val ill = computeIllumination(DOMINICA_LAT, DOMINICA_LON, 0.0)
            ill.fraction
        } catch (e: Exception) {
            0.12 // Fallback to 12% (Waning Crescent)
        }
    }
    
    /**
     * Calculate moon phase using accurate Sun-Moon elongation
     * @return Moon phase as a string
     */
    fun calculateMoonPhaseAccurate(): String {
        return try {
            val ill = computeIllumination(DOMINICA_LAT, DOMINICA_LON, 0.0)
            ill.phaseLabel.uppercase().replace(" ", "_")
        } catch (e: Exception) {
            "WANING_CRESCENT"
        }
    }
    
    // --- drop-in: robust illumination that tolerates API differences ---
    private fun computeIllumination(
        lat: Double,
        lon: Double,
        elevationMeters: Double = 0.0,
        moment: Moment = Moment.nowInSystemTime()
    ): Illumination {
        // Create GeoLocation using SolarTime.ofLocation (the correct Time4A pattern)
        val loc: GeoLocation = SolarTime.ofLocation(lat, lon, elevationMeters.toInt(), "America/Dominica")

        // 1) Obtain Sun/Moon position objects using whichever factory exists
        val sun = getBodyPosition(
            className = "net.time4j.calendar.astro.SunPosition",
            loc = loc,
            moment = moment
        )
        val moon = getBodyPosition(
            className = "net.time4j.calendar.astro.MoonPosition",
            loc = loc,
            moment = moment
        )

        // 2) Try ecliptic first (best), else fall back to RA/Dec
        val lamS = tryGetAngle(sun, "eclipticLongitude")
        val betS = tryGetAngle(sun, "eclipticLatitude")
        val lamM = tryGetAngle(moon, "eclipticLongitude")
        val betM = tryGetAngle(moon, "eclipticLatitude")

        val (psiRad, waxing) =
            if (lamS != null && betS != null && lamM != null && betM != null) {
                // --- Ecliptic branch ---
                val cosPsi = kotlin.math.sin(betS) * kotlin.math.sin(betM) +
                             kotlin.math.cos(betS) * kotlin.math.cos(betM) * kotlin.math.cos(lamS - lamM)
                val psi = kotlin.math.acos(cosPsi.coerceIn(-1.0, 1.0))

                // waxing if Moon is east of Sun along ecliptic
                val dLam = normPi(lamM - lamS)
                psi to (dLam > 0.0)
            } else {
                // --- Equatorial fallback (RA/Dec) ---
                val raS  = requireAngle(sun, "rightAscension")
                val decS = requireAngle(sun, "declination")
                val raM  = requireAngle(moon, "rightAscension")
                val decM = requireAngle(moon, "declination")

                val cosPsi = kotlin.math.sin(decS) * kotlin.math.sin(decM) +
                             kotlin.math.cos(decS) * kotlin.math.cos(decM) * kotlin.math.cos(raS - raM)
                val psi = kotlin.math.acos(cosPsi.coerceIn(-1.0, 1.0))

                val dRA = normPi(raM - raS)
                psi to (dRA > 0.0)
            }

        val k = ((1.0 + kotlin.math.cos(psiRad)) / 2.0).coerceIn(0.0, 1.0)
        return Illumination(
            fraction = k,
            elongationDeg = Math.toDegrees(psiRad),
            waxing = waxing,
            phaseLabel = phaseLabel(k, waxing)
        )
    }

    private fun normPi(a: Double): Double {
        var x = a % (2 * Math.PI)
        if (x <= -Math.PI) x += 2 * Math.PI
        if (x > Math.PI) x -= 2 * Math.PI
        return x
    }

    // --- helpers -----------------------------------------------------------------

    /** Try known factory shapes for Sun/Moon position objects without hard linking. */
    private fun getBodyPosition(
        className: String,
        loc: Any,               // remains your SolarTime/LunarTime instance
        moment: Moment
    ): Any {
        val cls = Class.forName(className)
        val geoIface = GeoLocation::class.java

        // 1) at(GeoLocation, Moment)
        runCatching {
            val m = cls.getMethod("at", geoIface, Moment::class.java)
            return m.invoke(null, loc, moment)!!
        }

        // 2) at(Moment, GeoLocation)
        runCatching {
            val m = cls.getMethod("at", Moment::class.java, geoIface)
            return m.invoke(null, moment, loc)!!
        }

        // 3) ofLocation(GeoLocation).at(Moment) — some builds expose this pattern
        runCatching {
            val ofLoc = cls.getMethod("ofLocation", geoIface).invoke(null, loc)
            val at = ofLoc.javaClass.getMethod("at", Moment::class.java)
            return at.invoke(ofLoc, moment)!!
        }

        error("$className: no known factory method matched (API mismatch).")
    }

    /** Read an angle (in RADIANS) from a property or getter; returns null if not present. */
    private fun tryGetAngle(obj: Any, base: String): Double? {
        return runCatching {
            val prop = obj.javaClass.getDeclaredField(base)
            prop.isAccessible = true
            val deg = prop.get(obj) as Double
            Math.toRadians(deg)
        }.getOrNull() ?: runCatching {
            val getter = obj.javaClass.getMethod("get${base.replaceFirstChar { it.uppercase() }}")
            val deg = getter.invoke(obj) as Double
            Math.toRadians(deg)
        }.getOrNull()
    }

    /** Read an angle (in RADIANS) from a property or getter; throws if not present. */
    private fun requireAngle(obj: Any, base: String): Double {
        return tryGetAngle(obj, base) ?: error("$base not found on ${obj.javaClass.simpleName}")
    }

    private fun phaseLabel(k: Double, waxing: Boolean): String = when {
        k < 0.03 -> "New Moon"
        k < 0.25 -> if (waxing) "Waxing Crescent" else "Waning Crescent"
        k < 0.55 -> if (waxing) "First Quarter"   else "Last Quarter"
        k < 0.97 -> if (waxing) "Waxing Gibbous"  else "Waning Gibbous"
        else -> "Full Moon"
    }
    
    /**
     * Format Time4A PlainTimestamp to 12-hour time string
     */
    private fun formatTime4ATimestamp(timestamp: net.time4j.PlainTimestamp): String {
        return try {
            val timeString = timestamp.toString()
            val formatted = formatTimeTo12Hour(timeString)
            formatted
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    /**
     * Format time to 12-hour format
     */
    fun formatTimeTo12Hour(timeString: String): String {
        if (timeString.isBlank() || timeString == "N/A") {
            return "N/A"
        }
        
        // If it's already in 12-hour format (contains AM/PM), return as is
        if (timeString.contains("AM") || timeString.contains("PM")) {
            return timeString
        }
        
        // Handle Time4A timestamp format: 2025-09-14T12:43:06
        if (timeString.contains("T") && timeString.length >= 19) {
            return try {
                val timePart = timeString.substringAfter("T").substringBeforeLast(":")
                val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
                val outputFormat = DateTimeFormatter.ofPattern("h:mm a")
                val localTime = LocalTime.parse(timePart, inputFormat)
                val result = localTime.format(outputFormat)
                result
            } catch (e: Exception) {
                "N/A"
            }
        }
        
        // If it's in 24-hour format, convert to 12-hour
        if (timeString.matches(Regex("\\d{1,2}:\\d{2}"))) {
            return try {
                val inputFormat = DateTimeFormatter.ofPattern("H:mm")
                val outputFormat = DateTimeFormatter.ofPattern("h:mm a")
                val localTime = LocalTime.parse(timeString, inputFormat)
                localTime.format(outputFormat)
            } catch (e: Exception) {
                "N/A"
            }
        }
        
        return "N/A"
    }
}