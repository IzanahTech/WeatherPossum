package com.weatherpossum.app.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherpossum.app.MainActivity
import com.weatherpossum.app.R
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.ui.theme.WeatherPossumColors

class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        private val COMPACT_4X2 = DpSize(250.dp, 110.dp)
        private val MEDIUM_4X3 = DpSize(250.dp, 155.dp)
        private val EXPANDED_4X3 = DpSize(250.dp, 180.dp)
        private val TALL_4X4 = DpSize(250.dp, 220.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(COMPACT_4X2, MEDIUM_4X3, EXPANDED_4X3, TALL_4X4)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val snapshot = UserPreferences(context).readWidgetSnapshot(appWidgetId)

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
    val size = LocalSize.current
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))

    val (topColor, bottomColor) = GreetingWidgetHelper.greetingColors(context)

    val onColor = WeatherPossumColors.onColorForGradient(topColor, bottomColor)
    val onColorProvider = ColorProvider(onColor)
    val synopsisColorProvider = ColorProvider(onColor.copy(alpha = 0.88f))
    val hintColorProvider = ColorProvider(onColor.copy(alpha = 0.5f))

    val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val backgroundColor = widgetBackgroundColor(topColor, bottomColor, isDarkMode)

    val backgroundModifier = GlanceModifier
        .fillMaxSize()
        .cornerRadius(28.dp)
        .background(ColorProvider(backgroundColor))
        .clickable(openApp)
        .padding(20.dp)

    GreetingWidgetLayout(
        snapshot = snapshot,
        backgroundModifier = backgroundModifier,
        onColorProvider = onColorProvider,
        synopsisColorProvider = synopsisColorProvider,
        hintColorProvider = hintColorProvider
    )
}

private fun widgetBackgroundColor(top: Color, bottom: Color, isDarkMode: Boolean): Color =
    if (isDarkMode) bottom else WeatherPossumColors.lerpColor(top, bottom, 0.38f)

@androidx.compose.runtime.Composable
private fun GreetingWidgetLayout(
    snapshot: WidgetSnapshot,
    backgroundModifier: GlanceModifier,
    onColorProvider: ColorProvider,
    synopsisColorProvider: ColorProvider,
    hintColorProvider: ColorProvider
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val density = context.resources.displayMetrics.density
    val greeting = GreetingWidgetHelper.widgetGreetingLine(context, snapshot.userName)
    val emoji = GreetingWidgetHelper.widgetGreetingEmoji()
    val synopsis = snapshot.synopsis?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.screen_greeting_loading_synopsis)
    val unavailable = context.getString(R.string.widget_metric_unavailable)
    val greetingText = "$greeting $emoji"

    val layoutPlan = WidgetLayoutPlanner.plan(
        size = size,
        greetingText = greetingText,
        synopsis = synopsis,
        windLabel = context.getString(R.string.widget_metric_wind),
        seaLabel = context.getString(R.string.widget_metric_sea),
        tideLabel = context.getString(R.string.widget_metric_tide),
        wind = snapshot.windSummary,
        sea = snapshot.seaSummary,
        tide = snapshot.tideSummary,
        hasCoastalDetails = snapshot.hasCoastalDetails,
        unavailable = unavailable
    )

    val greetingFontSize = WidgetTextSizer.fitSingleLineTextSize(
        text = greetingText,
        availableWidthPx = ((size.width - 40.dp).value * density).coerceAtLeast(0f),
        maxSp = layoutPlan.greetingMaxSp,
        minSp = 12f,
        density = density,
        isBold = true
    )

    Box(modifier = backgroundModifier, contentAlignment = Alignment.TopStart) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            if (layoutPlan.showCoastalHint) {
                Text(
                    text = context.getString(R.string.widget_coastal_available),
                    style = TextStyle(
                        color = hintColorProvider,
                        fontSize = layoutPlan.typography.hint,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(layoutPlan.typography.sectionGap))
            }

            Text(
                text = greetingText,
                style = TextStyle(
                    color = onColorProvider,
                    fontSize = greetingFontSize,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(GlanceModifier.height(layoutPlan.typography.lineGap))

            Text(
                text = synopsis,
                style = TextStyle(
                    color = synopsisColorProvider,
                    fontSize = layoutPlan.typography.synopsis,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = layoutPlan.synopsisMaxLines
            )

            if (layoutPlan.metrics.isNotEmpty()) {
                Spacer(GlanceModifier.height(layoutPlan.typography.sectionGap))
                CoastalMetricsSection(
                    metrics = layoutPlan.metrics,
                    onColorProvider = onColorProvider,
                    labelColorProvider = hintColorProvider,
                    typography = layoutPlan.typography
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CoastalMetricsSection(
    metrics: List<WidgetMetricSlot>,
    onColorProvider: ColorProvider,
    labelColorProvider: ColorProvider,
    typography: WidgetTypography
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        metrics.forEachIndexed { index, metric ->
            CoastalMetricRow(
                label = metric.label,
                value = metric.value,
                textColor = onColorProvider,
                labelColor = labelColorProvider,
                typography = typography,
                maxLines = metric.maxLines
            )
            if (index < metrics.lastIndex) {
                Spacer(GlanceModifier.height(typography.metricGap))
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CoastalMetricRow(
    label: String,
    value: String,
    textColor: ColorProvider,
    labelColor: ColorProvider,
    typography: WidgetTypography,
    maxLines: Int = 1
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = labelColor,
                fontSize = typography.metricLabel,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.width(46.dp)
        )

        Text(
            text = value,
            style = TextStyle(
                color = textColor,
                fontSize = typography.metricValue,
                fontWeight = FontWeight.Normal
            ),
            maxLines = maxLines,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}
