package com.weatherpossum.app.presentation

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

private const val TAG = "ApiClient"

object ApiClient {
    private const val BASE_URL = "https://api.ipgeolocation.io/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val moonPhaseApi: MoonPhaseApi by lazy {
        Log.d(TAG, "Initializing moon phase API client")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoonPhaseApi::class.java)
    }
} 