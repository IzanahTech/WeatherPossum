package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.ui.theme.WeatherPossumGlass

@Composable
fun ExtrasLoadingCard(
    title: String,
    gradient: Brush? = null,
    style: CardGradientStyle? = null,
    titleColor: androidx.compose.ui.graphics.Color? = null,
    indicatorColor: androidx.compose.ui.graphics.Color? = null,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val resolvedStyle = style ?: CardGradientStyle.Moon
    val (tintTop, tintBottom) = WeatherPossumGlass.colorsForStyle(resolvedStyle, isDarkMode)
    val defaultOnColor = WeatherPossumGlass.onColorForStyle(resolvedStyle, isDarkMode)
    val resolvedTitleColor = titleColor ?: defaultOnColor
    val resolvedIndicatorColor = indicatorColor ?: defaultOnColor

    LiquidGlassCard(
        tintTop = tintTop,
        tintBottom = tintBottom,
        onColor = resolvedTitleColor,
        modifier = modifier
    ) { onColor ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title.uppercase(),
                color = resolvedTitleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            ExpressiveLoadingIndicator(color = resolvedIndicatorColor)
        }
    }
}

@Composable
fun ExtrasErrorCard(
    title: String,
    message: String,
    gradient: Brush? = null,
    contentColor: androidx.compose.ui.graphics.Color? = null,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val (tintTop, tintBottom) = WeatherPossumGlass.colorsForStyle(CardGradientStyle.Error, isDarkMode)
    val resolvedContentColor = contentColor ?: WeatherPossumGlass.onColorForStyle(
        CardGradientStyle.Error,
        isDarkMode
    )

    LiquidGlassCard(
        tintTop = tintTop,
        tintBottom = tintBottom,
        onColor = resolvedContentColor,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                title.uppercase(),
                color = resolvedContentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = resolvedContentColor.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
