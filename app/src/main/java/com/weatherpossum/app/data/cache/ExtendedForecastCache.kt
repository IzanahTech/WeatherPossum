package com.weatherpossum.app.data.cache

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.weatherpossum.app.data.model.ForecastDay

@JsonClass(generateAdapter = true)
data class CachedForecastDayDto(
    val date: String,
    val maxTemp: String,
    val minTemp: String,
    val weather: String,
    val wind: String,
    val seas: String,
    val waves: String
)

@JsonClass(generateAdapter = true)
data class CachedExtendedForecastPayload(
    val days: List<CachedForecastDayDto> = emptyList()
)

object ExtendedForecastCache {
    private val adapter = Moshi.Builder().build().adapter(CachedExtendedForecastPayload::class.java)

    fun encode(days: List<ForecastDay>): String =
        adapter.toJson(CachedExtendedForecastPayload(days.map { it.toDto() }))

    fun decode(json: String): List<ForecastDay>? = runCatching {
        adapter.fromJson(json)?.days?.map { it.toForecastDay() }
    }.getOrNull()?.takeIf { it.isNotEmpty() }

    private fun ForecastDay.toDto(): CachedForecastDayDto = CachedForecastDayDto(
        date = date,
        maxTemp = maxTemp,
        minTemp = minTemp,
        weather = weather,
        wind = wind,
        seas = seas,
        waves = waves
    )

    private fun CachedForecastDayDto.toForecastDay(): ForecastDay = ForecastDay(
        date = date,
        maxTemp = maxTemp,
        minTemp = minTemp,
        weather = weather,
        wind = wind,
        seas = seas,
        waves = waves
    )
}
