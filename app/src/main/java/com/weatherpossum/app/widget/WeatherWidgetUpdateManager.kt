package com.weatherpossum.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

object WeatherWidgetUpdateManager {

    suspend fun updateAllWidgets(context: Context) {
        WeatherGlanceWidget().updateAll(context)
    }

    suspend fun updateWidget(context: Context, appWidgetId: Int) {
        val manager = GlanceAppWidgetManager(context)
        val glanceId = manager.getGlanceIdBy(appWidgetId)
        WeatherGlanceWidget().update(context, glanceId)
    }
}
