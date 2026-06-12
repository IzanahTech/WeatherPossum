package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.Hurricane
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

@Composable
fun ActiveStormsCard(
    storms: List<Hurricane>,
    modifier: Modifier = Modifier
) {
    if (storms.isEmpty()) return

    val context = LocalContext.current

    ExpressiveCard(
        modifier = modifier,
        style = CardGradientStyle.HurricaneWarm,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_active_storms_title),
                subtitle = stringResource(R.string.card_active_storms_subtitle, storms.size),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = WeatherIconType.HURRICANE,
                            modifier = Modifier.fillMaxSize(),
                            color = onColor
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            storms.forEachIndexed { index, storm ->
                if (index > 0) {
                    HorizontalDivider(color = onColor.copy(alpha = 0.12f))
                }
                ActiveStormRow(storm = storm, onColor = onColor, context = context)
            }
        }
    }
}

@Composable
private fun ActiveStormRow(
    storm: Hurricane,
    onColor: Color,
    context: android.content.Context
) {
    val isDarkMode = isSystemInDarkTheme()
    val name = HurricaneLabels.stormName(context, storm.name)
    val status = HurricaneLabels.stormStatus(context, storm.classification, storm.category)
    val location = HurricaneLabels.location(context, storm.location)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassInset(onColor, isDarkMode, cornerRadius = 16.dp)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(
                    label = stringResource(R.string.card_active_storms_status),
                    value = status,
                    onColor = onColor,
                    modifier = Modifier.weight(1f)
                )
                if (storm.windSpeed > 0) {
                    DetailChip(
                        label = stringResource(R.string.card_active_storms_wind),
                        value = stringResource(
                            R.string.card_active_storms_wind_value,
                            storm.windSpeed
                        ),
                        onColor = onColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            DetailChip(
                label = stringResource(R.string.card_active_storms_location),
                value = location,
                onColor = onColor
            )

            if (storm.category > 0) {
                Text(
                    text = HurricaneLabels.categoryDescription(context, storm.category),
                    color = onColor.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
