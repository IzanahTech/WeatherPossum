package com.weatherpossum.app.data

import com.weatherpossum.app.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class MoonData(
    val phase: String,
    val moonrise: String,
    val moonset: String,
    val illumination: Double,
    val iconResId: Int = getMoonPhaseIcon(phase)
) {
    companion object {
        fun formatMoonPhase(phase: String): String {
            return phase
                .split('_')
                .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
        }

        fun formatTimeTo12Hour(time24: String): String {
            val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
            val outputFormat = DateTimeFormatter.ofPattern("h:mm a")
            val localTime = LocalTime.parse(time24, inputFormat)
            return localTime.format(outputFormat)
        }

        fun getMoonPhaseIcon(phase: String): Int {
            return when (phase.uppercase()) {
                "NEW_MOON" -> R.drawable.ic_moon_new
                "WAXING_CRESCENT" -> R.drawable.ic_moon_waxing_crescent
                "FIRST_QUARTER" -> R.drawable.ic_moon_first_quarter
                "WAXING_GIBBOUS" -> R.drawable.ic_moon_waxing_gibbous
                "FULL_MOON" -> R.drawable.ic_moon_full
                "WANING_GIBBOUS" -> R.drawable.ic_moon_waning_gibbous
                "LAST_QUARTER" -> R.drawable.ic_moon_last_quarter
                "WANING_CRESCENT" -> R.drawable.ic_moon_waning_crescent
                else -> R.drawable.ic_moon_new // Default to new moon if phase is unknown
            }
        }
    }
} 