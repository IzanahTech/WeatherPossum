package com.weatherpossum.app.data.cache

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.weatherpossum.app.data.model.Hurricane
import com.weatherpossum.app.data.model.HurricaneData

@JsonClass(generateAdapter = true)
data class CachedHurricaneDto(
    val id: String,
    val name: String,
    val category: Int,
    val classification: String? = null,
    val location: String,
    val windSpeed: Int,
    val pressure: Int,
    val lastUpdated: String
)

@JsonClass(generateAdapter = true)
data class CachedHurricaneDataPayload(
    val activeStorms: List<CachedHurricaneDto> = emptyList(),
    val tropicalOutlook: String? = null,
    val forecaster: String? = null,
    val issued: String? = null,
    val lastUpdated: Long = 0L
)

object HurricaneCache {
    private val adapter = Moshi.Builder().build().adapter(CachedHurricaneDataPayload::class.java)

    fun encode(data: HurricaneData): String =
        adapter.toJson(
            CachedHurricaneDataPayload(
                activeStorms = data.activeStorms.map { it.toDto() },
                tropicalOutlook = data.tropicalOutlook,
                forecaster = data.forecaster,
                issued = data.issued,
                lastUpdated = data.lastUpdated
            )
        )

    fun decode(json: String): HurricaneData? = runCatching {
        adapter.fromJson(json)?.toHurricaneData()
    }.getOrNull()

    private fun Hurricane.toDto(): CachedHurricaneDto = CachedHurricaneDto(
        id = id,
        name = name,
        category = category,
        classification = classification,
        location = location,
        windSpeed = windSpeed,
        pressure = pressure,
        lastUpdated = lastUpdated
    )

    private fun CachedHurricaneDataPayload.toHurricaneData(): HurricaneData = HurricaneData(
        activeStorms = activeStorms.map { it.toHurricane() },
        tropicalOutlook = tropicalOutlook,
        forecaster = forecaster,
        issued = issued,
        lastUpdated = lastUpdated,
        isFromCache = true
    )

    private fun CachedHurricaneDto.toHurricane(): Hurricane = Hurricane(
        id = id,
        name = name,
        category = category,
        classification = classification,
        location = location,
        windSpeed = windSpeed,
        pressure = pressure,
        lastUpdated = lastUpdated
    )
}
