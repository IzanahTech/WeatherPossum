package com.weatherpossum.app.data.api

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import android.content.Context
import com.weatherpossum.app.BuildConfig
import java.net.SocketTimeoutException

interface WeatherForecastApi {
    @GET("forecast") // Using the endpoint from WeatherApi.kt (Source A)
    suspend fun getWeatherForecast(): String

    companion object {
        private const val TIMEOUT_SECONDS = 30L
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY = 2000L

        private class RetryInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var attempt = 0
                var response: Response? = null
                var exception: IOException? = null

                while (attempt < MAX_RETRIES) {
                    try {
                        val newRequest = chain.request().newBuilder()
                            // .header("Connection", "close") // MODIFICATION: Removed this line
                            .build()

                        response = chain.proceed(newRequest)

                        if (response.isSuccessful) {
                            val contentType = response.header("Content-Type")
                            if (contentType?.contains("text/html") == true) {
                                return response
                            }
                            response.close()
                            throw IOException("Invalid content type: $contentType")
                        } else {
                            response.close()
                        }
                    } catch (e: Exception) {
                        exception = when (e) {
                            is SocketTimeoutException -> e
                            is IOException -> e
                            else -> IOException(e.message, e)
                        }
                        response?.close()
                    }

                    attempt++
                    if (attempt < MAX_RETRIES) {
                        val delayMs = INITIAL_RETRY_DELAY * (1 shl attempt)
                        try {
                            Thread.sleep(delayMs)
                        } catch (ie: InterruptedException) {
                            Thread.currentThread().interrupt()
                            throw IOException("Thread interrupted during retry delay", ie)
                        }
                    }
                }
                throw exception ?: IOException("Request failed after $MAX_RETRIES attempts without a specific exception")
            }
        }

        fun create(): WeatherForecastApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val client = OkHttpClient.Builder()
                // .protocols(Arrays.asList(Protocol.HTTP_1_1)) // MODIFICATION: Removed
                // .connectionPool(ConnectionPool(0, 1, TimeUnit.MILLISECONDS)) // MODIFICATION: Removed
                .addInterceptor(RetryInterceptor())
                .addInterceptor(loggingInterceptor)
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://weather.gov.dm/")
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(WeatherForecastApi::class.java)
        }
    }
}