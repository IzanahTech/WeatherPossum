package com.weatherpossum.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.weatherpossum.app.presentation.caribbeanWeatherFacts
import com.weatherpossum.app.presentation.components.FunFactCard
import com.weatherpossum.app.presentation.components.GreetingCard
import com.weatherpossum.app.presentation.components.ExtendedForecastCard
import com.weatherpossum.app.presentation.components.MoonPhaseCard
import com.weatherpossum.app.ui.viewmodel.MoonViewModel
import com.weatherpossum.app.ui.viewmodel.MoonUiState
import com.weatherpossum.app.data.MoonData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

// --- Updated HurricaneViewModel with StateFlow ---
class HurricaneViewModel : ViewModel() {
    private val _stormCount = MutableStateFlow(0)
    val stormCount: StateFlow<Int> = _stormCount

    val message: StateFlow<String> = stormCount
        .map { count ->
            if (count == 0) "No active storms in the Atlantic"
            else "$count active storms in the Atlantic"
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "No active storms in the Atlantic")
}

@Composable
fun ExtrasScreenContent2(
    onNavigateToWeather: () -> Unit,
    onNavigateToSettings: () -> Unit,
    extrasViewModel: ExtrasViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = koinViewModel(),
    extendedForecastViewModel: ExtendedForecastViewModel = viewModel(),
    moonViewModel: MoonViewModel = koinViewModel(),
    hurricaneViewModel: HurricaneViewModel = viewModel(),
    userName: String = "Guest"
) {
    val synopsis by weatherViewModel.synopsis.collectAsState()
    val randomFact = remember { caribbeanWeatherFacts.random() }
    val extendedForecast by extendedForecastViewModel.forecast.collectAsState()
    val isLoading by extendedForecastViewModel.isLoading.collectAsState()
    val error by extendedForecastViewModel.error.collectAsState()
    val moonState by moonViewModel.uiState.collectAsState()
    val hurricaneMessage by hurricaneViewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        if (extendedForecastViewModel.shouldRefreshForecast() || extendedForecast.isEmpty()) {
            extendedForecastViewModel.loadForecast()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GreetingCard(userName = userName, synopsis = synopsis)
        FunFactCard(facts = caribbeanWeatherFacts)

        // Moon Phase card using MoonViewModel
        when (val state = moonState) {
            is MoonUiState.Success -> {
                MoonPhaseCard(moonData = state.moonData)
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

        // Hurricane Card (now uses ViewModel and dynamic message)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Hurricane Updates", fontWeight = FontWeight.SemiBold)
                Text(hurricaneMessage, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
} 