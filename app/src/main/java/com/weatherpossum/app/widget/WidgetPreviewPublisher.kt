package com.weatherpossum.app.widget

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Publishes Glance-rendered widget previews for the system widget picker (Android 15+).
 * Static widget_preview.xml remains as the fallback on older devices.
 */
object WidgetPreviewPublisher {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun publishIfNeeded(context: Context) {
        scope.launch {
            runCatching {
                GlanceAppWidgetManager(context).setWidgetPreviews(
                    receiver = WeatherWidgetReceiver::class,
                    widgetCategories = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
                )
            }
        }
    }
}
