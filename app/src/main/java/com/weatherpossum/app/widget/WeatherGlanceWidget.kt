package com.weatherpossum.app.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
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
        private val SMALL_4X2 = DpSize(250.dp, 110.dp)
        private val EXPANDED_4X3 = DpSize(250.dp, 180.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_4X2, EXPANDED_4X3)
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
    val isExpanded = size.height >= 170.dp
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
        isExpanded = isExpanded,
        backgroundModifier = backgroundModifier,
        onColorProvider = onColorProvider,
        synopsisColorProvider = synopsisColorProvider,
        hintColorProvider = hintColorProvider
    )
}

private fun widgetBackgroundColor(top: Color, bottom: Color, isDarkMode: Boolean): Color =
    if (isDarkMode) bottom else WeatherPossumColors.lerpColor(top, bottom, 0.38f)

private data class WidgetTypography(
    val greeting: androidx.compose.ui.unit.TextUnit,
    val synopsis: androidx.compose.ui.unit.TextUnit,
    val metricLabel: androidx.compose.ui.unit.TextUnit,
    val metricValue: androidx.compose.ui.unit.TextUnit,
    val hint: androidx.compose.ui.unit.TextUnit,
    val sectionGap: androidx.compose.ui.unit.Dp,
    val lineGap: androidx.compose.ui.unit.Dp,
    val metricGap: androidx.compose.ui.unit.Dp
)

private fun widgetTypography(size: DpSize, isExpanded: Boolean): WidgetTypography {
    val scale = ((size.height.value / 110f) * 0.72f + (size.width.value / 250f) * 0.28f)
        .coerceIn(1f, 2.1f)
    val expandedBoost = if (isExpanded) 1.08f else 1f
    val s = scale * expandedBoost

    fun sp(base: Float, min: Float, max: Float) =
        (base * s).coerceIn(min, max).sp

    fun gap(base: Float) = (base * s).coerceIn(base, base * 1.35f).dp

    return WidgetTypography(
        greeting = sp(20f, 18f, 28f),
        synopsis = sp(15f, 13f, 21f),
        metricLabel = sp(11f, 10f, 14f),
        metricValue = sp(13.5f, 12f, 18f),
        hint = sp(11f, 10f, 13f),
        sectionGap = gap(if (isExpanded) 10f else 8f),
        lineGap = gap(6f),
        metricGap = gap(5f)
    )
}

@androidx.compose.runtime.Composable
private fun GreetingWidgetLayout(
    snapshot: WidgetSnapshot,
    isExpanded: Boolean,
    backgroundModifier: GlanceModifier,
    onColorProvider: ColorProvider,
    synopsisColorProvider: ColorProvider,
    hintColorProvider: ColorProvider
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val type = widgetTypography(size, isExpanded)
    val greeting = GreetingWidgetHelper.widgetGreetingLine(context, snapshot.userName)
    val emoji = GreetingWidgetHelper.widgetGreetingEmoji()
    val synopsis = snapshot.synopsis?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.screen_greeting_loading_synopsis)
    val unavailable = context.getString(R.string.widget_metric_unavailable)

    val greetingText = "$greeting $emoji"
    val density = context.resources.displayMetrics.density
    val availableGreetingWidthPx = ((size.width - 40.dp).value * density).coerceAtLeast(0f)
    val greetingFontSize = WidgetTextSizer.fitSingleLineTextSize(
        text = greetingText,
        availableWidthPx = availableGreetingWidthPx,
        maxSp = type.greeting.value,
        minSp = 12f,
        density = density,
        isBold = true
    )

    Box(modifier = backgroundModifier, contentAlignment = Alignment.TopStart) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            if (!isExpanded && snapshot.hasCoastalDetails) {
                Text(
                    text = context.getString(R.string.widget_coastal_available),
                    style = TextStyle(
                        color = hintColorProvider,
                        fontSize = type.hint,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(type.sectionGap))
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

            Spacer(GlanceModifier.height(type.lineGap))

            Text(
                text = synopsis,
                style = TextStyle(
                    color = synopsisColorProvider,
                    fontSize = type.synopsis,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = if (isExpanded) 2 else 4
            )

            if (isExpanded) {
                Spacer(GlanceModifier.height(type.sectionGap))
                ExpandedCoastalMetrics(
                    snapshot = snapshot,
                    unavailable = unavailable,
                    onColorProvider = onColorProvider,
                    labelColorProvider = hintColorProvider,
                    typography = type
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ExpandedCoastalMetrics(
    snapshot: WidgetSnapshot,
    unavailable: String,
    onColorProvider: ColorProvider,
    labelColorProvider: ColorProvider,
    typography: WidgetTypography
) {
    val context = LocalContext.current

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        CoastalMetricRow(
            label = context.getString(R.string.widget_metric_wind),
            value = snapshot.windSummary ?: unavailable,
            textColor = onColorProvider,
            labelColor = labelColorProvider,
            typography = typography
        )
        Spacer(GlanceModifier.height(typography.metricGap))
        CoastalMetricRow(
            label = context.getString(R.string.widget_metric_sea),
            value = snapshot.seaSummary ?: unavailable,
            textColor = onColorProvider,
            labelColor = labelColorProvider,
            typography = typography,
            maxLines = 2
        )
        Spacer(GlanceModifier.height(typography.metricGap))
        CoastalMetricRow(
            label = context.getString(R.string.widget_metric_tide),
            value = snapshot.tideSummary ?: unavailable,
            textColor = onColorProvider,
            labelColor = labelColorProvider,
            typography = typography,
            maxLines = 2
        )
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
