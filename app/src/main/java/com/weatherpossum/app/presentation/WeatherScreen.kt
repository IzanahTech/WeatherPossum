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
// M2 PullRefreshIndicator, pullRefresh, rememberPullRefreshState removed
import androidx.compose.foundation.layout.Arrangement
// LazyColumn import was duplicated, ensure it's present once.
// import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
// ExperimentalMaterialApi removed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.res.stringResource // Added import
import com.weatherpossum.app.R // Added import


@OptIn(ExperimentalMaterial3Api::class) // ExperimentalMaterialApi removed
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val synopsis by viewModel.synopsis.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value

    val pullToRefreshState = rememberPullToRefreshState()
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            if (!pullToRefreshState.isRefreshing) pullToRefreshState.startRefresh()
        } else {
            if (pullToRefreshState.isRefreshing) pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name_full)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            state = pullToRefreshState,
            onRefresh = { viewModel.loadWeather(forceRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // The Box that had .pullRefresh modifier is replaced by PullToRefreshContainer
            // The content (when block) is now a direct child of PullToRefreshContainer
            when (uiState) {
                is WeatherUiState.Loading -> {
                    SplashScreen() // SplashScreen typically fills the screen, suitable as direct child
                }
                is WeatherUiState.Success -> {
                    // LazyColumn is the primary scrollable content for success state
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(), // LazyColumn should fill the container
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            GreetingCard(
                                userName = userName,
                                synopsis = synopsis ?: stringResource(R.string.screen_greeting_loading_synopsis),
                                onNameSubmit = { name -> viewModel.saveUserName(name) }
                            )
                        }
                        items(uiState.weatherCards, key = { it.title + it.value }) { card ->
                            WeatherCard(card = card)
                        }
                    }
                    // PullRefreshIndicator is now implicitly handled by PullToRefreshContainer
                }
                is WeatherUiState.Error -> {
                    // ErrorContent can also be a direct child
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
                    text = stringResource(R.string.screen_splash_title_weather),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = stringResource(R.string.screen_splash_title_possum),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black,
                    letterSpacing = 2.sp
                )
            }
            
            Text(
                text = stringResource(R.string.screen_splash_subtitle),
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
            Text(stringResource(R.string.button_retry))
        }
    }
} 