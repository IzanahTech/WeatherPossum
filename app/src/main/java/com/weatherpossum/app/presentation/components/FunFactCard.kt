package com.weatherpossum.app.presentation.components

import androidx.compose.animation.*
import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.weatherpossum.app.R
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumColors
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

@Composable
private fun rememberFunFacts(): List<String> =
    stringArrayResource(R.array.fun_facts).toList()

@Composable
fun FunFactCardExpressive(
    modifier: Modifier = Modifier,
    facts: List<String>? = null
) {
    val resolvedFacts = facts ?: rememberFunFacts()
    var currentFact by remember(resolvedFacts) { mutableStateOf(resolvedFacts.random()) }
    val onFactClicked: () -> Unit = {
        val newFact = resolvedFacts.filter { it != currentFact }.randomOrNull() ?: currentFact
        currentFact = newFact
    }

    ExpressiveCard(
        modifier = modifier.clickable(onClick = onFactClicked),
        style = CardGradientStyle.Fact,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_fun_fact_title),
                subtitle = stringResource(R.string.card_fun_fact_subtitle),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = WeatherIconType.FACT,
                            modifier = Modifier.fillMaxSize(),
                            color = WeatherPossumColors.factIcon
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        AnimatedContent(
            targetState = currentFact,
            transitionSpec = WeatherPossumMotion.factTextTransitionSpec(),
            label = "FactTextChangeAnimation"
        ) { targetFact ->
            Text(
                text = targetFact,
                color = onColor.copy(alpha = 0.95f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
