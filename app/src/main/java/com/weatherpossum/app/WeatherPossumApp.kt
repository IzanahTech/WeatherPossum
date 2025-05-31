package com.weatherpossum.app

import android.app.Application
import com.weatherpossum.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WeatherPossumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@WeatherPossumApp)
            modules(appModule)
        }
    }
} 