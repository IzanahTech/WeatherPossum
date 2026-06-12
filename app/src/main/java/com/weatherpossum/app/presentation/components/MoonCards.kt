package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import androidx.compose.ui.unit.dp
import com.weatherpossum.app.data.model.MoonData
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumColors
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

@Composable
fun MoonPhaseCard(data: MoonData) {
    ExpressiveCard(
        style = CardGradientStyle.Moon,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_moon_phase_title),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedMoonPhaseIcon(
                            phase = data.phase,
                            illumination = data.illumination,
                            modifier = Modifier.fillMaxSize(),
                            color = WeatherPossumColors.moonAccent
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailChip(
                    label = stringResource(R.string.card_moon_illumination),
                    value = "${(data.illumination * 100).toInt()}%",
                    onColor = onColor
                )
                DetailChip(
                    label = stringResource(R.string.card_moon_phase_label),
                    value = formatMoonPhaseLabel(data.phase),
                    onColor = onColor
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailChip(
                    label = stringResource(R.string.card_moon_moonrise),
                    value = data.moonrise,
                    onColor = onColor
                )
                DetailChip(
                    label = stringResource(R.string.card_moon_moonset),
                    value = data.moonset,
                    onColor = onColor
                )
            }
        }
    }
}
