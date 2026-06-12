package com.weatherpossum.app.data.api

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

interface WeatherForecastApi {
    @GET(DmoUrls.DAILY_FORECAST_PATH)
    suspend fun getWeatherForecast(): String

    companion object {
        private const val MAX_RETRIES = 2

        private class RetryInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var lastError: IOException? = null
                repeat(MAX_RETRIES) { attempt ->
                    try {
                        val response = chain.proceed(chain.request())
                        if (!response.isSuccessful) {
                            response.close()
                            throw IOException("HTTP ${response.code}")
                        }
                        val contentType = response.header("Content-Type").orEmpty()
                        if (!contentType.contains("text/html", ignoreCase = true)) {
                            response.close()
                            throw IOException("Invalid content type: $contentType")
                        }
                        return response
                    } catch (e: IOException) {
                        lastError = e
                        if (attempt == MAX_RETRIES - 1) throw e
                    }
                }
                throw lastError ?: IOException("Request failed")
            }
        }

        fun create(): WeatherForecastApi {
            val client = HttpClients.weatherForecast(
                extraInterceptors = listOf(RetryInterceptor())
            )

            return Retrofit.Builder()
                .baseUrl(DmoUrls.BASE)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(WeatherForecastApi::class.java)
        }
    }
}