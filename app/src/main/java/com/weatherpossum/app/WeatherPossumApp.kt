package com.weatherpossum.app

import android.app.Application
import com.weatherpossum.app.di.appModule
import net.time4j.android.ApplicationStarter
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WeatherPossumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Time4A for astronomical calculations
        ApplicationStarter.initialize(this, true)
        
        startKoin {
            androidLogger()
            androidContext(this@WeatherPossumApp)
            modules(appModule)
        }
    }
}