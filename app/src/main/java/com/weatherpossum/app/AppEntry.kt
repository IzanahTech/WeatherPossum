package com.weatherpossum.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.weatherpossum.app.BuildConfig
import com.weatherpossum.app.di.appModule
import com.weatherpossum.app.presentation.WeatherScreen
import com.weatherpossum.app.ui.theme.WeatherPossumTheme
import com.weatherpossum.app.widget.WeatherWidgetWorker
import net.time4j.android.ApplicationStarter
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.context.startKoin

class WeatherPossumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApplicationStarter.initialize(this, true)
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@WeatherPossumApp)
            modules(appModule)
        }
        WeatherWidgetWorker.schedule(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoinAndroidContext {
                WeatherPossumTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WeatherScreen()
                    }
                }
            }
        }
    }
}
