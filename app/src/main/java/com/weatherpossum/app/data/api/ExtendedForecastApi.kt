package com.weatherpossum.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

interface ExtendedForecastApi {
    @GET(DmoUrls.EXTENDED_FORECAST_PATH)
    suspend fun getExtendedForecastHtml(): String

    companion object {
        fun create(): ExtendedForecastApi {
            val client = HttpClients.weatherForecast()

            return Retrofit.Builder()
                .baseUrl(DmoUrls.BASE)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(ExtendedForecastApi::class.java)
        }
    }
}
