package com.weatherpossum.app.di

import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.repository.WeatherRepository
import com.weatherpossum.app.data.repository.MoonRepository
import com.weatherpossum.app.data.repository.HurricaneRepository
import com.weatherpossum.app.presentation.WeatherViewModel
import com.weatherpossum.app.presentation.ExtrasViewModel
import com.weatherpossum.app.ui.viewmodel.MoonViewModel
import com.weatherpossum.app.ui.viewmodel.HurricaneViewModel
import com.weatherpossum.app.ui.viewmodel.UpdateViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // API Services
    single { WeatherForecastApi.create() }

    // User Preferences
    single { UserPreferences(androidContext()) }

    // Repositories
    single { WeatherRepository(get(), get()) }
    single { MoonRepository() }
    single { HurricaneRepository() }

    // ViewModels
    viewModel { WeatherViewModel(androidApplication()) }
    viewModel { ExtrasViewModel() }
    viewModel { MoonViewModel(get(), get()) }
    viewModel { HurricaneViewModel(get()) }
    viewModel { UpdateViewModel() }
}