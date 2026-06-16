package com.weatherpossum.app.widget

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R
import com.weatherpossum.app.ui.theme.WeatherPossumColors

@Composable
fun WidgetGreetingPreview(
    userName: String?,
    modifier: Modifier = Modifier,
    showCoastalHint: Boolean = true
) {
    val context = LocalContext.current
    val isDarkMode = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val (topColor, bottomColor) = GreetingWidgetHelper.greetingColors(context)
    val onColor = WeatherPossumColors.onColorForGradient(topColor, bottomColor)
    val synopsisColor = onColor.copy(alpha = 0.88f)
    val hintColor = onColor.copy(alpha = 0.5f)
    val backgroundColor = if (isDarkMode) {
        bottomColor
    } else {
        WeatherPossumColors.lerpColor(topColor, bottomColor, 0.38f)
    }

    val greeting = GreetingWidgetHelper.widgetGreetingLine(context, userName)
    val emoji = GreetingWidgetHelper.widgetGreetingEmoji()
    val synopsis = stringResource(R.string.widget_preview_synopsis)

    val greetingText = "$greeting $emoji"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        BoxWithConstraints {
            val density = LocalDensity.current.density
            val greetingFontSize = WidgetTextSizer.fitSingleLineTextSize(
                text = greetingText,
                availableWidthPx = maxWidth.value * density,
                maxSp = 20f,
                minSp = 12f,
                density = density
            )

            Column {
                if (showCoastalHint) {
                    Text(
                        text = stringResource(R.string.widget_coastal_available),
                        color = hintColor,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = greetingText,
                    color = onColor,
                    fontSize = greetingFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = synopsis,
                    color = synopsisColor,
                    fontSize = 15.sp,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun WidgetPreviewLabel() {
    Text(
        text = stringResource(R.string.widget_configure_preview_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    )
}
