package com.weatherpossum.app.data.cache

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.WeatherCard

@JsonClass(generateAdapter = true)
data class CachedWeatherCardDto(
    val title: String,
    val value: String,
    val forecastSectionKey: String? = null,
    val validFrom: String? = null
)

@JsonClass(generateAdapter = true)
data class CachedWeatherCardsPayload(
    val cards: List<CachedWeatherCardDto> = emptyList()
)

object WeatherCardCache {
    private val adapter = Moshi.Builder().build().adapter(CachedWeatherCardsPayload::class.java)

    fun encode(cards: List<WeatherCard>): String =
        adapter.toJson(CachedWeatherCardsPayload(cards.map { it.toDto() }))

    fun decode(json: String): List<WeatherCard>? = runCatching {
        adapter.fromJson(json)?.cards?.map { it.toWeatherCard() }
    }.getOrNull()?.takeIf { it.isNotEmpty() }

    private fun WeatherCard.toDto(): CachedWeatherCardDto = CachedWeatherCardDto(
        title = title,
        value = value,
        forecastSectionKey = forecastSection?.toCacheKey(),
        validFrom = validFrom
    )

    private fun CachedWeatherCardDto.toWeatherCard(): WeatherCard = WeatherCard(
        title = title,
        value = value,
        forecastSection = forecastSectionKey?.toForecastSection(),
        validFrom = validFrom
    )

    private fun ForecastSection.toCacheKey(): String = when (this) {
        ForecastSection.TODAY -> "TODAY"
        ForecastSection.TONIGHT -> "TONIGHT"
        ForecastSection.TODAY_TONIGHT -> "TODAY_TONIGHT"
        ForecastSection.TOMORROW -> "TOMORROW"
        ForecastSection.TWENTY_FOUR_HOURS -> "TWENTY_FOUR_HOURS"
        is ForecastSection.UNKNOWN -> "UNKNOWN|$rawTitle"
    }

    private fun String.toForecastSection(): ForecastSection? = when {
        this == "TODAY" -> ForecastSection.TODAY
        this == "TONIGHT" -> ForecastSection.TONIGHT
        this == "TODAY_TONIGHT" -> ForecastSection.TODAY_TONIGHT
        this == "TOMORROW" -> ForecastSection.TOMORROW
        this == "TWENTY_FOUR_HOURS" -> ForecastSection.TWENTY_FOUR_HOURS
        startsWith("UNKNOWN|") -> ForecastSection.UNKNOWN(removePrefix("UNKNOWN|"))
        else -> null
    }
}
