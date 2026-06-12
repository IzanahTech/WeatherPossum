package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.ui.theme.WeatherPossumFonts
import com.weatherpossum.app.ui.theme.WeatherPossumGradients
import com.weatherpossum.app.util.SunCalculator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GreetingCard(
    userName: String?,
    synopsis: String?,
    modifier: Modifier = Modifier
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_good_morning)
        in 12..17 -> stringResource(R.string.greeting_good_afternoon)
        else -> stringResource(R.string.greeting_good_night)
    }

    val displayName = userName?.replaceFirstChar { it.uppercase() } ?: ""
    val personalizedGreeting = if (displayName.isNotBlank()) "$greeting, $displayName" else greeting
    val sunProgressPercent = SunCalculator.calculateSunProgress().coerceIn(0, 100)
    val sunFrac = sunProgressPercent / 100f
    val animatedSunFrac by animateFloatAsState(
        targetValue = sunFrac,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "daylightProgress"
    )

    val greetingIconType = when (hour) {
        in 5..11 -> WeatherIconType.MORNING
        in 12..17 -> WeatherIconType.AFTERNOON
        else -> WeatherIconType.NIGHT
    }

    val isDarkMode = isSystemInDarkTheme()
    val (targetTop, targetBottom) = remember(animatedSunFrac, isDarkMode) {
        WeatherPossumGradients.greetingGradient(animatedSunFrac, isDarkMode)
    }

    val top by animateColorAsState(
        targetTop,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "gradTop"
    )
    val bottom by animateColorAsState(
        targetBottom,
        animationSpec = WeatherPossumMotion.gentleSpring(),
        label = "gradBottom"
    )

    ExpressiveCard(
        modifier = modifier,
        gradientTop = top,
        gradientBottom = bottom
    ) { onColor ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = personalizedGreeting,
                style = WeatherPossumFonts.greetingTextStyle,
                color = onColor,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier.size(WeatherPossumDimens.iconHero),
                contentAlignment = Alignment.Center
            ) {
                AnimatedWeatherIcon(
                    type = greetingIconType,
                    modifier = Modifier.fillMaxSize(),
                    color = onColor
                )
            }
        }

        if (!synopsis.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyLarge,
                color = onColor.copy(alpha = 0.9f),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.widget_daylight_label, sunProgressPercent),
                color = onColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            LinearWavyProgressIndicator(
                progress = { animatedSunFrac },
                modifier = Modifier.fillMaxWidth(),
                color = onColor,
                trackColor = onColor.copy(alpha = 0.3f),
            )
        }
    }
}
