package com.weatherpossum.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherpossum.app.R
import com.weatherpossum.app.presentation.components.HurricaneLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherWidgetInstrumentedTest {

    @Test
    fun widgetProviderIsRegistered() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val provider = ComponentName(context, WeatherWidgetReceiver::class.java)
        val manager = AppWidgetManager.getInstance(context)

        assertNotNull(manager.getAppWidgetIds(provider))
    }

    @Test
    fun hurricaneLabelsFormatStormStatus() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals(
            context.getString(R.string.hurricane_status_tropical_storm),
            HurricaneLabels.stormStatus(context, "TS", 0)
        )
    }
}
