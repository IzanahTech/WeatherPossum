package com.weatherpossum.app.widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weatherpossum.app.data.model.Result as DataResult
import com.weatherpossum.app.data.repository.HurricaneRepository
import com.weatherpossum.app.data.repository.WeatherRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class WeatherWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: WeatherRepository by inject()
    private val hurricaneRepository: HurricaneRepository by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            hurricaneRepository.getActiveHurricanes(forceRefresh = true)
            when (repository.getWeatherForecast(forceRefresh = true)) {
                is DataResult.Success -> {
                    WeatherWidgetUpdateManager.updateAllWidgets(applicationContext)
                    ListenableWorker.Result.success()
                }
                is DataResult.Error -> {
                    WeatherWidgetUpdateManager.updateAllWidgets(applicationContext)
                    ListenableWorker.Result.retry()
                }
                is DataResult.Loading -> ListenableWorker.Result.retry()
            }
        } catch (_: Exception) {
            WeatherWidgetUpdateManager.updateAllWidgets(applicationContext)
            ListenableWorker.Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "weather_widget_refresh"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<WeatherWidgetWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
