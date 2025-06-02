package com.weatherpossum.app.di

import android.app.Application
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.repository.WeatherRepository
import com.weatherpossum.app.presentation.WeatherViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // API Services
    single { WeatherForecastApi.create(androidContext()) }

    // User Preferences
    single { UserPreferences(androidContext()) }

    // Repository
    // WeatherRepository now takes Context, WeatherForecastApi, UserPreferences
    single { WeatherRepository(androidContext(), get(), get()) }

    // ViewModel
    // WeatherViewModel now takes Application
    viewModel { WeatherViewModel(androidApplication()) }
}