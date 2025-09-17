package com.weatherpossum.app.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface HurricaneApi {
    @GET("index-at.xml")
    suspend fun getHurricaneRssFeed(): ResponseBody
}
