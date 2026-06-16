package com.weatherpossum.app.data.api

import com.weatherpossum.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.time.Duration.Companion.seconds

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

    fun timedBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder().applyStandardTimeouts()

    private fun OkHttpClient.Builder.applyStandardTimeouts(): OkHttpClient.Builder =
        connectTimeout(15.seconds)
            .readTimeout(20.seconds)
            .writeTimeout(15.seconds)
            .callTimeout(45.seconds)
            .retryOnConnectionFailure(true)

    private fun baseBuilder(): OkHttpClient.Builder =
        timedBuilder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor())
}
