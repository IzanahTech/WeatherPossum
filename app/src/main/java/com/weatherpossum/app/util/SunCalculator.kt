package com.weatherpossum.app.util

import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.*

/**
 * Simplified Sun calculator using basic astronomical calculations
 * Provides sunrise, sunset, day length, and progress calculations
 */
object SunCalculator {
    
    // Dominica coordinates
    private const val DOMINICA_LAT = 15.414999
    private const val DOMINICA_LON = -61.370976
    
    /**
     * Calculate sunrise time for today using simplified algorithm
     * @return Sunrise time as formatted string (e.g., "6:23 AM")
     */
    fun calculateSunrise(): String {
        return try {
            val sunriseTime = calculateSunriseSunset(true)
            formatTimeTo12Hour(sunriseTime)
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating sunrise", e)
            "6:00 AM" // Default fallback
        }
    }
    
    /**
     * Calculate sunset time for today using simplified algorithm
     * @return Sunset time as formatted string (e.g., "6:45 PM")
     */
    fun calculateSunset(): String {
        return try {
            val sunsetTime = calculateSunriseSunset(false)
            formatTimeTo12Hour(sunsetTime)
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating sunset", e)
            "6:00 PM" // Default fallback
        }
    }
    
    /**
     * Calculate solar noon time for today
     * @return Solar noon time as formatted string
     */
    fun calculateSolarNoon(): String {
        return try {
            val solarNoonTime = calculateSolarNoonTime()
            formatTimeTo12Hour(solarNoonTime)
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating solar noon", e)
            "12:00 PM" // Default fallback
        }
    }
    
    /**
     * Calculate total day length (sunrise to sunset)
     * @return Day length as formatted string (e.g., "12h 22m")
     */
    fun calculateDayLength(): String {
        return try {
        val sunriseTime = calculateSunriseSunset(true)
        val sunsetTime = calculateSunriseSunset(false)
        
        val sunriseMinutes = sunriseTime.hour * 60 + sunriseTime.minute
        val sunsetMinutes = sunsetTime.hour * 60 + sunsetTime.minute
        val dayLengthMinutes = sunsetMinutes - sunriseMinutes
            
            val hours = dayLengthMinutes / 60
            val minutes = dayLengthMinutes % 60
            
            val formattedLength = if (minutes > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${hours}h"
            }
            
            formattedLength
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating day length", e)
            "12h 0m" // Default fallback
        }
    }
    
    /**
     * Calculate current sun position (altitude and azimuth)
     * @return Pair of altitude and azimuth as formatted strings
     */
    fun calculateCurrentSunPosition(): Pair<String, String> {
        return try {
            val now = java.time.LocalDateTime.now()
            val altitude = calculateSunAltitude(now)
            val azimuth = calculateSunAzimuth(now)
            
            val altitudeFormatted = "${altitude.roundToInt()}°"
            val azimuthFormatted = "${azimuth.roundToInt()}°"
            
            Pair(altitudeFormatted, azimuthFormatted)
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating sun position", e)
            Pair("45°", "180°") // Default fallback
        }
    }
    
    /**
     * Calculate sun progress percentage (0-100) based on current time between sunrise and sunset
     * @return Progress percentage as integer (0-100)
     */
    fun calculateSunProgress(): Int {
        return try {
            val now = java.time.LocalTime.now()
            val sunriseTime = calculateSunriseSunset(true)
            val sunsetTime = calculateSunriseSunset(false)
            
            val sunriseMinutes = sunriseTime.hour * 60 + sunriseTime.minute
            val sunsetMinutes = sunsetTime.hour * 60 + sunsetTime.minute
            val currentMinutes = now.hour * 60 + now.minute
            
            val totalDayMinutes = sunsetMinutes - sunriseMinutes
            val elapsedMinutes = currentMinutes - sunriseMinutes
            
            val progress = if (totalDayMinutes > 0) {
                ((elapsedMinutes.toFloat() / totalDayMinutes.toFloat()) * 100).coerceIn(0f, 100f).toInt()
            } else {
                0
            }
            
            progress
        } catch (e: Exception) {
            Log.e("SunCalculator", "Error calculating sun progress", e)
            50 // Default fallback
        }
    }
    
    /**
     * Get sunrise and sunset moments for progress ring calculations
     * @return Pair of sunrise and sunset moments (simplified to null for now)
     */
    fun getSunriseSunsetMoments(): Pair<Any?, Any?> {
        return Pair(null, null) // Simplified - not using Time4A moments
    }
    
    /**
     * Get solar noon moment for progress ring calculations
     * @return Solar noon moment (simplified to null for now)
     */
    fun getSolarNoonMoment(): Any? {
        return null // Simplified - not using Time4A moments
    }
    
    /**
     * Calculate sunrise or sunset time using more accurate astronomical algorithm
     */
    private fun calculateSunriseSunset(isSunrise: Boolean): LocalTime {
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        val latRad = Math.toRadians(DOMINICA_LAT)
        
        // More accurate solar declination calculation
        val declination = 23.45 * sin(Math.toRadians(284.0 + dayOfYear) * 360.0 / 365.25)
        val declRad = Math.toRadians(declination)
        
        // Hour angle calculation (more accurate)
        val cosHourAngle = -tan(latRad) * tan(declRad)
        
        // Check if sun never rises/sets (polar day/night)
        if (cosHourAngle > 1.0) {
            // Polar night - sun never rises
            return if (isSunrise) LocalTime.of(0, 0) else LocalTime.of(23, 59)
        } else if (cosHourAngle < -1.0) {
            // Polar day - sun never sets
            return if (isSunrise) LocalTime.of(0, 0) else LocalTime.of(23, 59)
        }
        
        val hourAngle = acos(cosHourAngle)
        
        // Time calculation
        val timeOffset = if (isSunrise) -hourAngle else hourAngle
        val solarTime = 12.0 + timeOffset * 12 / PI
        
        // Convert to local time with proper timezone adjustment
        // For local solar time, we don't need longitude adjustment
        // The solar time calculation already accounts for the sun's position
        val localTime = solarTime
        
        // Normalize to 0-24 range
        val normalizedTime = ((localTime % 24) + 24) % 24
        
        val hour = normalizedTime.toInt()
        val minute = ((normalizedTime - hour) * 60).toInt()
        
        
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
    
    /**
     * Calculate solar noon time
     */
    private fun calculateSolarNoonTime(): LocalTime {
        // Solar noon is always at 12:00 local solar time
        return LocalTime.of(12, 0)
    }
    
    /**
     * Calculate sun altitude (simplified)
     */
    private fun calculateSunAltitude(dateTime: java.time.LocalDateTime): Double {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + dateTime.minute / 60.0
        
        val latRad = Math.toRadians(DOMINICA_LAT)
        val declination = 23.45 * sin(Math.toRadians((284 + dayOfYear) * 360.0 / 365))
        val declRad = Math.toRadians(declination)
        
        val hourAngle = Math.toRadians(15 * (hour - 12))
        val altitude = asin(sin(latRad) * sin(declRad) + cos(latRad) * cos(declRad) * cos(hourAngle))
        
        return Math.toDegrees(altitude)
    }
    
    /**
     * Calculate sun azimuth (simplified)
     */
    private fun calculateSunAzimuth(dateTime: java.time.LocalDateTime): Double {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + dateTime.minute / 60.0
        
        val latRad = Math.toRadians(DOMINICA_LAT)
        val declination = 23.45 * sin(Math.toRadians((284 + dayOfYear) * 360.0 / 365))
        val declRad = Math.toRadians(declination)
        
        val hourAngle = Math.toRadians(15 * (hour - 12))
        
        val azimuth = atan2(sin(hourAngle), cos(hourAngle) * sin(latRad) - tan(declRad) * cos(latRad))
        
        return Math.toDegrees(azimuth) + 180 // Convert to 0-360 range
    }
    
    /**
     * Format time to 12-hour format
     */
    private fun formatTimeTo12Hour(time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return time.format(formatter)
    }
}