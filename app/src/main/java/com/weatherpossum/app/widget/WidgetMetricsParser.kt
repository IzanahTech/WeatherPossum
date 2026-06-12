package com.weatherpossum.app.widget

import com.weatherpossum.app.data.model.WeatherCard

data class CoastalMetrics(
    val wind: String?,
    val sea: String?,
    val tide: String?
) {
    val hasAny: Boolean
        get() = !wind.isNullOrBlank() || !sea.isNullOrBlank() || !tide.isNullOrBlank()
}

object WidgetMetricsParser {

    fun fromCards(cards: List<WeatherCard>): CoastalMetrics {
        val wind = cards.firstOrNull { it.title.equals("Wind", ignoreCase = true) }
            ?.value
            ?.lineSequence()
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val seaTidesCard = cards.firstOrNull {
            it.title.contains("Sea", ignoreCase = true) && it.title.contains("Tide", ignoreCase = true)
        }
        val (sea, tide) = seaTidesCard?.value?.let(::parseSeaTides) ?: (null to null)

        return CoastalMetrics(wind = wind, sea = sea, tide = tide)
    }

    private fun parseSeaTides(value: String): Pair<String?, String?> {
        val lines = value.lines().map { it.trim() }.filter { it.isNotBlank() }
        val seaParts = lines.mapNotNull { line ->
            when {
                line.startsWith("Sea Conditions:", ignoreCase = true) ->
                    line.substringAfter(":").trim()
                line.startsWith("Waves:", ignoreCase = true) ->
                    line.substringAfter(":").trim()
                else -> null
            }
        }
        val tideParts = lines.mapNotNull { line ->
            when {
                line.startsWith("Low Tide:", ignoreCase = true) ->
                    "Low ${line.substringAfter(":").trim()}"
                line.startsWith("High Tide:", ignoreCase = true) ->
                    "High ${line.substringAfter(":").trim()}"
                else -> null
            }
        }
        return seaParts.joinToString(" • ").takeIf { it.isNotBlank() } to
            tideParts.joinToString(" | ").takeIf { it.isNotBlank() }
    }
}
