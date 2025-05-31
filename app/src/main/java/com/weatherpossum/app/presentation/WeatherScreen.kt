package com.weatherpossum.app.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.presentation.components.GreetingCard
import com.weatherpossum.app.presentation.components.WeatherCard
import org.koin.androidx.compose.koinViewModel
import com.airbnb.lottie.compose.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val synopsis by viewModel.synopsis.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadWeather(forceRefresh = true) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Possum") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    SplashScreen()
                }
                is WeatherUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            GreetingCard(
                                userName = userName,
                                synopsis = synopsis ?: "Loading weather information...",
                                onNameSubmit = { name -> viewModel.saveUserName(name) }
                            )
                        }
                        items(uiState.weatherCards, key = { it.title + it.value }) { card ->
                            WeatherCard(card = card)
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
                is WeatherUiState.Error -> {
                    ErrorContent(
                        errorMessage = uiState.message,
                        onRetry = {
                            viewModel.loadWeather(forceRefresh = true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC8E6A0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val splashComposition by rememberLottieComposition(LottieCompositionSpec.Asset("animations/splash.json"))
            LottieAnimation(
                composition = splashComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "WEATHER",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "POSSUM",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black,
                    letterSpacing = 2.sp
                )
            }
            
            Text(
                text = "Consulting the possum's weather balloon…",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            
            val loadingComposition by rememberLottieComposition(LottieCompositionSpec.Asset("animations/loading.json"))
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
} 