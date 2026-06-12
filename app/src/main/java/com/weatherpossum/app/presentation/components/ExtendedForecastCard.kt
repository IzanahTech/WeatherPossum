package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.data.model.ForecastDay
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

@Composable
fun ExtendedForecastCard(forecast: List<ForecastDay>) {
    var expanded by remember { mutableStateOf(false) }
    val expandRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = WeatherPossumMotion.fluidSpring(),
        label = "forecastExpandRotation"
    )
    val dateLabel = forecast.firstOrNull()?.date.orEmpty()
    val subtitle = if (dateLabel.isNotBlank()) {
        stringResource(R.string.card_extended_forecast_starting, dateLabel)
    } else {
        null
    }

    ExpressiveCard(
        style = CardGradientStyle.Forecast
    ) { onColor ->
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = WeatherPossumMotion.fluidSpring())
                .heightIn(min = 200.dp)
        ) {
            CardHeader(
                title = stringResource(R.string.card_extended_forecast_title),
                subtitle = subtitle,
                endContent = if (forecast.isNotEmpty()) {
                    {
                        val head = forecast.first()
                        val iconType = getWeatherIconType(head.weather)
                        Box(
                            Modifier.size(WeatherPossumDimens.iconLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedWeatherIcon(
                                type = iconType,
                                modifier = Modifier.fillMaxSize(),
                                color = onColor
                            )
                        }
                    }
                } else {
                    null
                },
                onColor = onColor
            )

            if (forecast.isNotEmpty()) {
                if (!expanded) {
                    DayForecastBlock(day = forecast.first(), onTextColor = onColor)
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CenterChip(
                            iconRotation = expandRotation,
                            onClick = { expanded = true },
                            onColor = onColor,
                            contentDescription = stringResource(R.string.card_extended_forecast_expand)
                        )
                    }
                } else {
                    Column {
                        forecast.forEach { day ->
                            DayForecastBlock(day = day, onTextColor = onColor, isExpanded = true)
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CenterChip(
                                iconRotation = expandRotation,
                                onClick = { expanded = false },
                                onColor = onColor,
                                contentDescription = stringResource(R.string.card_extended_forecast_collapse)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   DAY BLOCK – with Expressive styling and subtle animation
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun DayForecastBlock(
    day: ForecastDay,
    onTextColor: Color,
    isExpanded: Boolean = false
) {
    // Subtle scale animation when the expanded state changes
    val scaleFactor by animateFloatAsState(
        targetValue = if (isExpanded) 1.0f else 0.995f,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "dayBlockScale"
    )
    
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .glassInset(onTextColor, isDarkMode, cornerRadius = 20.dp)
            .padding(14.dp)
    ) {
        Column {

            // Large, Expressive day header
            Text(
                text = day.date.uppercase(), 
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold, // Bolder
                letterSpacing = 0.5.sp,
                color = onTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = onTextColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(Modifier.height(6.dp))

            // Details row (no date inside)
            ForecastSummaryRow(
                day = day,
                onTextColor = onTextColor
            )
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   SUMMARY ROW – details
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun ForecastSummaryRow(
    day: ForecastDay,
    onTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Weather icon (Slightly smaller for detail density)
        val iconType = getWeatherIconType(day.weather)
        Box(
            modifier = Modifier.size(WeatherPossumDimens.iconLarge),
            contentAlignment = Alignment.Center
        ) {
            AnimatedWeatherIcon(
                type = iconType,
                modifier = Modifier.fillMaxSize(),
                color = onTextColor // Match color
            )
        }

        Spacer(modifier = Modifier.width(16.dp)) // Slightly more separation

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), // Tighter spacing for density
            modifier = Modifier.weight(1f)
        ) {
            AlignedDetailRow(
                label = stringResource(R.string.card_extended_forecast_weather),
                value = day.weather,
                color = onTextColor
            )
            AlignedDetailRow(
                label = stringResource(R.string.card_extended_forecast_wind),
                value = day.wind,
                color = onTextColor
            )
            AlignedDetailRow(
                label = stringResource(R.string.card_extended_forecast_seas),
                value = day.seas,
                color = onTextColor
            )
            AlignedDetailRow(
                label = stringResource(R.string.card_extended_forecast_waves),
                value = day.waves,
                color = onTextColor
            )
        }
    }
}

/* Label/value with fixed label width for tidy columns */
@Composable
private fun AlignedDetailRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold, // Bolder label
            modifier = Modifier.width(78.dp)
        )
        Text(
            text = value,
            color = color.copy(alpha = 0.95f),
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

/* Centered circular chip for expand/collapse (Expressive Style) */
@Composable
private fun CenterChip(
    iconRotation: Float,
    onClick: () -> Unit,
    onColor: Color,
    contentDescription: String
) {
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable { onClick() }
            .glassInset(onColor, isDarkMode, cornerRadius = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = contentDescription,
                tint = onColor,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}
