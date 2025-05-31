package com.weatherpossum.app.di

import com.weatherpossum.app.BuildConfig
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.OpenAIApi
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.repository.WeatherRepository
import com.weatherpossum.app.presentation.WeatherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // API Services
    single { WeatherForecastApi.create(androidContext()) }
    single { OpenAIApi.create() }
    
    // User Preferences
    single { UserPreferences(androidContext()) }
    
    // Repository
    single { WeatherRepository(get(), get(), BuildConfig.OPENAI_API_KEY, get()) }
    
    // ViewModel
    viewModel { WeatherViewModel() }
} 