package com.weatherpossum.app.di

import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.ExtendedForecastApi
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.repository.ExtendedForecastRepository
import com.weatherpossum.app.data.repository.WeatherRepository
import com.weatherpossum.app.data.repository.MoonRepository
import com.weatherpossum.app.data.repository.HurricaneRepository
import com.weatherpossum.app.presentation.WeatherViewModel
import com.weatherpossum.app.presentation.ExtendedForecastViewModel
import com.weatherpossum.app.presentation.MoonViewModel
import com.weatherpossum.app.presentation.HurricaneViewModel
import com.weatherpossum.app.presentation.UpdateViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // API Services
    single { WeatherForecastApi.create() }
    single { ExtendedForecastApi.create() }

    // User Preferences
    single { UserPreferences(androidContext()) }

    // Repositories
    single { WeatherRepository(get(), get()) }
    single { ExtendedForecastRepository(get(), get()) }
    single { MoonRepository(get()) }
    single { HurricaneRepository(get()) }

    // ViewModels
    viewModel { WeatherViewModel(androidApplication(), get(), get()) }
    viewModel { ExtendedForecastViewModel(androidApplication(), get()) }
    viewModel { MoonViewModel(androidApplication(), get(), get()) }
    viewModel { HurricaneViewModel(androidApplication(), get()) }
    viewModel { UpdateViewModel(androidApplication()) }
}