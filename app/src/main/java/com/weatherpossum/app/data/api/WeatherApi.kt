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
import org.jsoup.Jsoup
import okhttp3.ConnectionPool
import okhttp3.Protocol
import java.net.SocketTimeoutException
import java.util.Arrays

interface WeatherForecastApi {
    @GET("forecast")
    suspend fun getWeatherForecast(): String

    companion object {
        private const val TIMEOUT_SECONDS = 30L // Increased timeout
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY = 2000L

        private class RetryInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                var attempt = 0
                var response: Response? = null
                var exception: IOException? = null

                while (attempt < MAX_RETRIES) {
                    try {
                        // Create a new request with increased timeouts for each retry
                        val newRequest = chain.request().newBuilder()
                            .header("Connection", "close") // Disable keep-alive
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
                        val delayMs = INITIAL_RETRY_DELAY * (1 shl attempt) // Exponential backoff
                        Thread.sleep(delayMs)
                    }
                }

                throw exception ?: IOException("Request failed after $MAX_RETRIES attempts")
            }
        }

        fun create(context: Context): WeatherForecastApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val client = OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1)) // Force HTTP/1.1
                .connectionPool(ConnectionPool(0, 1, TimeUnit.MILLISECONDS)) // Disable connection pooling
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