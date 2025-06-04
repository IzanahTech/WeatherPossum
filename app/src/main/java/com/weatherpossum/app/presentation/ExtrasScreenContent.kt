package com.weatherpossum.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R
import com.weatherpossum.app.presentation.caribbeanWeatherFacts
import com.weatherpossum.app.presentation.components.FunFactCard
import com.weatherpossum.app.presentation.components.GreetingCard
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.weatherpossum.app.presentation.components.ExtendedForecastCard
import com.weatherpossum.app.ui.viewmodel.MoonViewModel
import com.weatherpossum.app.ui.viewmodel.MoonUiState
import com.weatherpossum.app.data.MoonData

@Composable
fun ExtrasScreenContent(
    extrasViewModel: ExtrasViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = koinViewModel(),
    extendedForecastViewModel: ExtendedForecastViewModel = viewModel(),
    moonViewModel: MoonViewModel = koinViewModel()
) {
    val userName by weatherViewModel.userName.collectAsState()
    val synopsis by weatherViewModel.synopsis.collectAsState()
    var randomFact by remember { mutableStateOf(caribbeanWeatherFacts.random()) }
    val extendedForecast by extendedForecastViewModel.forecast.collectAsState()
    val isLoading by extendedForecastViewModel.isLoading.collectAsState()
    val error by extendedForecastViewModel.error.collectAsState()
    val moonState by moonViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        extendedForecastViewModel.loadForecast()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GreetingCard(userName = userName, synopsis = synopsis)

        FunFactCard(fact = randomFact)

        // Moon Phase card using MoonViewModel
        when (val state = moonState) {
            is MoonUiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = state.moonData.iconResId),
                            contentDescription = "Moon Phase",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(64.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Moon Phase",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = MoonData.formatMoonPhase(state.moonData.phase),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🌙 Moonrise: ${state.moonData.moonrise}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "🌘 Moonset: ${state.moonData.moonset}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is MoonUiState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is MoonUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            else -> {}
        }

        // Extended Forecast Card
        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text("Error loading extended forecast", color = Color.Red)
            }
            extendedForecast.isNotEmpty() -> {
                ExtendedForecastCard(forecast = extendedForecast)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Hurricane Updates", fontWeight = FontWeight.SemiBold)
                Text("No active storms in the Atlantic", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun ExtrasCard(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
