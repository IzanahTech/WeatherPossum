package com.weatherpossum.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.weatherpossum.app.data.api.DmoUrls

// ── Weather ──────────────────────────────────────────────────────────────────

sealed class ForecastSection {
    data object TODAY : ForecastSection()
    data object TONIGHT : ForecastSection()
    data object TODAY_TONIGHT : ForecastSection()
    data object TOMORROW : ForecastSection()
    data object TWENTY_FOUR_HOURS : ForecastSection()
    data class UNKNOWN(val rawTitle: String) : ForecastSection()
}

data class DMOForecastResult(
    val section: ForecastSection,
    val titleRaw: String,
    val body: String,
    val sourceUrl: String = DmoUrls.DAILY_FORECAST
)

data class WeatherCard(
    val title: String,
    val value: String,
    val forecastSection: ForecastSection? = null,
    val validFrom: String? = null
) {
    fun isForecastCard(): Boolean = forecastSection != null
}

sealed class Result<out T> {
    data class Success<T>(val data: T, val isStale: Boolean = false) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

data class ForecastDay(
    val date: String,
    val maxTemp: String,
    val minTemp: String,
    val weather: String,
    val wind: String,
    val seas: String,
    val waves: String
)

data class DominicaParsedForecast(
    val validFrom: String?,
    val synopsis: String?,
    val shortTermForecast: DMOForecastResult?,
    val wind: String?,
    val seaConditions: String?,
    val waves: String?,
    val advisory: String?,
    val sunrise: String?,
    val sunset: String?,
    val lowTide: String?,
    val highTide: String?,
    val outlookTitle: String?,
    val outlookValidFrom: String?,
    val outlookText: String?
)

// ── Moon ─────────────────────────────────────────────────────────────────────

data class MoonData(
    val phase: String,
    val moonrise: String,
    val moonset: String,
    val illumination: Double
) {
    companion object {
        fun formatMoonPhase(phase: String): String {
            return phase
                .split('_')
                .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
        }
    }
}

// ── Hurricane ────────────────────────────────────────────────────────────────

data class HurricaneData(
    val activeStorms: List<Hurricane>,
    val tropicalOutlook: String? = null,
    val forecaster: String? = null,
    val issued: String? = null,
    val lastUpdated: Long,
    val isFromCache: Boolean = false
)

data class Hurricane(
    val id: String,
    val name: String,
    val category: Int,
    val classification: String?,
    val location: String,
    val windSpeed: Int,
    val pressure: Int,
    val lastUpdated: String
)

@JsonClass(generateAdapter = true)
data class CurrentStormsDto(
    @Json(name = "activeStorms") val activeStorms: List<StormDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StormDto(
    val id: String?,
    val name: String?,
    val basin: String?,
    val adv: String?,
    val stormType: String?,
    val productLink: String?,
    val classification: String?,
    val intensity: String?,
    val pressure: String?,
    val latitude: String?,
    val longitude: String?,
    val latitudeNumeric: Double?,
    val longitudeNumeric: Double?,
    val movementDir: Int?,
    val movementSpeed: Int?,
    val lastUpdate: String?,
    val publicAdvisory: AdvisoryDto?
)

@JsonClass(generateAdapter = true)
data class AdvisoryDto(
    val advNum: String?,
    val issuance: String?,
    val url: String?
)
