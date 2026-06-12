package com.weatherpossum.app.data.api

import com.weatherpossum.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClients {

    const val USER_AGENT = "WeatherPossum/${BuildConfig.VERSION_NAME} (Android)"

    private val userAgentInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/json,*/*")
            .build()
        chain.proceed(request)
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    fun weatherForecast(extraInterceptors: List<Interceptor> = emptyList()): OkHttpClient =
        baseBuilder()
            .apply { extraInterceptors.forEach(::addInterceptor) }
            .build()

    fun default(): OkHttpClient = baseBuilder().build()

    private fun baseBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
}
