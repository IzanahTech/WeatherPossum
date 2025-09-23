package com.weatherpossum.app.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ArcGisApi {
    // Example: Forecast Cone (summary)
    @GET("tropical/rest/services/tropical/NHC_tropical_weather_summary/MapServer/7/query")
    suspend fun forecastConeGeoJson(
        @Query("f") f: String = "geojson",
        @Query("where") where: String = "1=1",
        @Query("outFields") outFields: String = "*"
    ): ResponseBody
}
