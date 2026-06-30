package com.weatherpossum.app.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherpossum.app.MainActivity
import com.weatherpossum.app.R
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.ui.theme.WeatherPossumColors

/**
 * Home-screen widget built with Jetpack Glance (Compose for App Widgets).
 *
 * Runtime content is fully dynamic: greeting, synopsis, and sea conditions are loaded from
 * [UserPreferences] on each update. The XML layouts referenced in [R.xml.weather_widget_info]
 * are system fallbacks only (initial placeholder + picker preview on older Android versions).
 */
class WeatherGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val snapshot = UserPreferences(context).readWidgetSnapshot(appWidgetId)

        provideContent {
            GlanceTheme {
                WeatherWidgetContent(snapshot = snapshot)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val snapshot = WidgetSnapshot(
            userName = "Possum",
            synopsis = context.getString(R.string.widget_preview_synopsis),
            seaConditions = context.getString(R.string.widget_preview_sea)
        )

        provideContent {
            GlanceTheme {
                WeatherWidgetContent(snapshot = snapshot)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WeatherWidgetContent(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))

    val (topColor, bottomColor) = GreetingWidgetHelper.greetingColors(context)
    val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

    val onColor = WeatherPossumColors.onColorForGradient(topColor, bottomColor)
    val onColorProvider = ColorProvider(onColor)
    val synopsisColorProvider = ColorProvider(onColor.copy(alpha = 0.9f))
    val seaLabelColorProvider = ColorProvider(onColor.copy(alpha = 0.62f))
    val dividerColorProvider = ColorProvider(onColor.copy(alpha = 0.22f))
    val backgroundColor = widgetBackgroundColor(topColor, bottomColor, isDarkMode)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(backgroundColor))
            .clickable(openApp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        WidgetBody(
            snapshot = snapshot,
            onColorProvider = onColorProvider,
            synopsisColorProvider = synopsisColorProvider,
            seaLabelColorProvider = seaLabelColorProvider,
            dividerColorProvider = dividerColorProvider
        )
    }
}

@androidx.compose.runtime.Composable
private fun WidgetBody(
    snapshot: WidgetSnapshot,
    onColorProvider: ColorProvider,
    synopsisColorProvider: ColorProvider,
    seaLabelColorProvider: ColorProvider,
    dividerColorProvider: ColorProvider
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val density = context.resources.displayMetrics.density

    val greeting = GreetingWidgetHelper.widgetGreetingLine(context, snapshot.userName)
    val emoji = GreetingWidgetHelper.widgetGreetingEmoji()
    val greetingText = "$greeting $emoji"
    val synopsis = snapshot.synopsis?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.screen_greeting_loading_synopsis)

    val plan = WidgetLayoutPlanner.plan(
        size = size,
        greetingText = greetingText,
        synopsis = synopsis,
        seaConditions = snapshot.seaConditions
    )

    val greetingFontSize = WidgetTextSizer.fitSingleLineTextSize(
        text = plan.greetingText,
        availableWidthPx = ((size.width - 36.dp).value * density).coerceAtLeast(0f),
        maxSp = plan.greetingFontSp,
        minSp = plan.greetingFontSp.coerceAtMost(14f),
        density = density,
        isBold = true
    )

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = plan.greetingText,
            style = TextStyle(
                color = onColorProvider,
                fontSize = greetingFontSize,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            modifier = GlanceModifier.fillMaxWidth()
        )

        Spacer(GlanceModifier.height(plan.greetingLineGap))

        Text(
            text = plan.synopsisText,
            style = TextStyle(
                color = synopsisColorProvider,
                fontSize = plan.synopsisFontSp.sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = plan.synopsisLineCount,
            modifier = GlanceModifier
                .fillMaxWidth()
                .then(
                    if (plan.showSea) GlanceModifier.defaultWeight() else GlanceModifier
                )
        )

        if (plan.showSea && !plan.seaText.isNullOrBlank()) {
            Spacer(GlanceModifier.height(plan.synopsisToSeaGap))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColorProvider)
            ) {}

            Spacer(GlanceModifier.height(plan.seaLabelGap))

            Text(
                text = context.getString(R.string.widget_sea_conditions_label),
                style = TextStyle(
                    color = seaLabelColorProvider,
                    fontSize = plan.seaLabelFontSp.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(GlanceModifier.height(3.dp))

            Text(
                text = plan.seaText,
                style = TextStyle(
                    color = onColorProvider,
                    fontSize = plan.seaValueFontSp.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = WidgetTextWrap.lineCount(
                    text = plan.seaText,
                    widthDp = (size.width - 36.dp).value.coerceAtLeast(0f),
                    fontSp = plan.seaValueFontSp
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}

private fun widgetBackgroundColor(top: Color, bottom: Color, isDarkMode: Boolean): Color =
    if (isDarkMode) {
        WeatherPossumColors.lerpColor(bottom, top, 0.18f)
    } else {
        WeatherPossumColors.lerpColor(top, bottom, 0.42f)
    }
