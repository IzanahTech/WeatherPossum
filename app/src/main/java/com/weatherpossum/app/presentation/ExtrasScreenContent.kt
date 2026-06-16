package com.weatherpossum.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.weatherpossum.app.R
import org.koin.androidx.compose.koinViewModel
import com.weatherpossum.app.presentation.components.ExtrasErrorCard
import com.weatherpossum.app.presentation.components.ExtrasLoadingCard
import com.weatherpossum.app.presentation.components.FunFactCardExpressive
import com.weatherpossum.app.presentation.components.GreetingCard
import com.weatherpossum.app.presentation.components.ExtendedForecastCard
import com.weatherpossum.app.presentation.components.ActiveStormsCard
import com.weatherpossum.app.presentation.components.CreditsCard
import com.weatherpossum.app.presentation.components.HurricaneNeutralCard
import com.weatherpossum.app.presentation.components.HurricaneOutlookCard
import com.weatherpossum.app.presentation.components.MoonPhaseCard
import com.weatherpossum.app.presentation.components.StaggeredReveal
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExtrasScreenContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    weatherViewModel: WeatherViewModel = koinViewModel(),
    extendedForecastViewModel: ExtendedForecastViewModel = koinViewModel(),
    moonViewModel: MoonViewModel = koinViewModel(),
    hurricaneViewModel: HurricaneViewModel = koinViewModel()
) {
    val userName by weatherViewModel.userName.collectAsStateWithLifecycle()
    val synopsis by weatherViewModel.synopsis.collectAsStateWithLifecycle()
    val extendedForecast by extendedForecastViewModel.forecast.collectAsStateWithLifecycle()
    val isLoading by extendedForecastViewModel.isLoading.collectAsStateWithLifecycle()
    val error by extendedForecastViewModel.error.collectAsStateWithLifecycle()
    val extendedForecastStale by extendedForecastViewModel.isStale.collectAsStateWithLifecycle()
    val moonState by moonViewModel.uiState.collectAsStateWithLifecycle()
    val hurricaneState by hurricaneViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (extendedForecastViewModel.shouldRefreshForecast() || extendedForecast.isEmpty()) {
            extendedForecastViewModel.loadForecast()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 24.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(WeatherPossumDimens.sectionSpacing)
    ) {
        StaggeredReveal(key = "extras_greeting", index = 0) {
            GreetingCard(userName = userName, synopsis = synopsis)
        }

        StaggeredReveal(key = "extras_fun_fact", index = 1) {
            FunFactCardExpressive()
        }

        StaggeredReveal(key = "extras_moon", index = 2) {
            when (val state = moonState) {
                is MoonUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(WeatherPossumDimens.sectionSpacing)) {
                        if (state.isStale) {
                            Text(
                                text = stringResource(R.string.moon_stale_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        MoonPhaseCard(state.moonData)
                    }
                }
                is MoonUiState.Loading -> ExtrasLoadingCard(
                    title = stringResource(R.string.extras_card_moon_phase),
                    style = CardGradientStyle.Moon
                )
                is MoonUiState.Error -> ExtrasErrorCard(
                    title = stringResource(R.string.extras_card_moon_phase),
                    message = state.message
                )
            }
        }

        StaggeredReveal(key = "extras_forecast", index = 3) {
            when {
                isLoading -> ExtrasLoadingCard(
                    title = stringResource(R.string.extras_card_seven_day),
                    style = CardGradientStyle.Forecast
                )
                error != null -> ExtrasErrorCard(
                    title = stringResource(R.string.extras_card_seven_day),
                    message = error ?: stringResource(R.string.extended_forecast_error_load)
                )
                extendedForecast.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(WeatherPossumDimens.sectionSpacing)) {
                        if (extendedForecastStale) {
                            Text(
                                text = stringResource(R.string.extended_forecast_stale_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        ExtendedForecastCard(forecast = extendedForecast)
                    }
                }
            }
        }

        StaggeredReveal(key = "extras_hurricane", index = 4) {
            when (val state = hurricaneState) {
                is HurricaneUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(WeatherPossumDimens.sectionSpacing)) {
                        if (state.hurricaneData.isFromCache) {
                            Text(
                                text = stringResource(R.string.hurricane_stale_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        if (state.hurricaneData.activeStorms.isNotEmpty()) {
                            ActiveStormsCard(storms = state.hurricaneData.activeStorms)
                        }

                        val tropicalOutlook = state.hurricaneData.tropicalOutlook
                        if (!tropicalOutlook.isNullOrBlank()) {
                            HurricaneOutlookCard(
                                outlookText = tropicalOutlook,
                                forecaster = state.hurricaneData.forecaster,
                                issued = state.hurricaneData.issued
                            )
                        } else {
                            HurricaneNeutralCard()
                        }
                    }
                }
                is HurricaneUiState.Loading -> ExtrasLoadingCard(
                    title = stringResource(R.string.extras_card_hurricane),
                    style = CardGradientStyle.HurricaneWarm
                )
                is HurricaneUiState.Error -> ExtrasErrorCard(
                    title = stringResource(R.string.extras_card_hurricane),
                    message = stringResource(R.string.extras_error_hurricane)
                )
            }
        }

        StaggeredReveal(key = "extras_credits", index = 5) {
            CreditsCard()
        }
    }
}
