package com.weatherpossum.app.data.cache

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.weatherpossum.app.data.model.MoonData

@JsonClass(generateAdapter = true)
data class CachedMoonDataDto(
    val phase: String,
    val moonrise: String,
    val moonset: String,
    val illumination: Double
)

object MoonCache {
    private val adapter = Moshi.Builder().build().adapter(CachedMoonDataDto::class.java)

    fun encode(data: MoonData): String = adapter.toJson(
        CachedMoonDataDto(
            phase = data.phase,
            moonrise = data.moonrise,
            moonset = data.moonset,
            illumination = data.illumination
        )
    )

    fun decode(json: String): MoonData? = runCatching {
        adapter.fromJson(json)?.toMoonData()
    }.getOrNull()

    private fun CachedMoonDataDto.toMoonData(): MoonData = MoonData(
        phase = phase,
        moonrise = moonrise,
        moonset = moonset,
        illumination = illumination
    )
}
