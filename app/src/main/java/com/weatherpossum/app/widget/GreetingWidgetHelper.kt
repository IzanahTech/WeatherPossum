package com.weatherpossum.app.widget

import android.content.Context
import android.content.res.Configuration
import com.weatherpossum.app.R
import com.weatherpossum.app.ui.theme.WeatherPossumGradients
import com.weatherpossum.app.util.SunCalculator
import java.time.LocalTime

object GreetingWidgetHelper {

    fun widgetGreetingLine(context: Context, userName: String?, hour: Int = LocalTime.now().hour): String {
        val greeting = when (hour) {
            in 5..11 -> context.getString(R.string.widget_greeting_morning)
            in 12..17 -> context.getString(R.string.widget_greeting_afternoon)
            else -> context.getString(R.string.widget_greeting_night)
        }
        val displayName = userName?.trim()?.replaceFirstChar { it.uppercase() }.orEmpty()
        return if (displayName.isNotBlank()) "$greeting, $displayName!" else "$greeting!"
    }

    fun widgetGreetingEmoji(hour: Int = LocalTime.now().hour): String = when (hour) {
        in 5..11 -> "😊"
        in 12..17 -> "☀️"
        else -> "🌙"
    }

    fun daylightPercent(): Int = SunCalculator.calculateSunProgress().coerceIn(0, 100)

    fun greetingColors(context: Context): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val sunFrac = daylightPercent() / 100f
        return WeatherPossumGradients.greetingGradient(sunFrac, isDarkMode)
    }
}
