package com.weatherpossum.app.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface HurricaneFeedsApi {
    // NHC base: https://www.nhc.noaa.gov/
    @GET("CurrentStorms.json")
    suspend fun currentStorms(): ResponseBody // parse with Moshi/Serialization later

    // TWO text (Atlantic). Either MIATWOAT.shtml or gtwo.php?text= (both yield stable text)
    @GET("text/MIATWOAT.shtml")
    suspend fun atlanticTwoText(): ResponseBody
}
