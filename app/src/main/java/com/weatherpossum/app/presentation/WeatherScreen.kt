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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val userName by viewModel.userName.collectAsState()
    val synopsis by viewModel.synopsis.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            if (uiState !is WeatherUiState.Loading) {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name_full)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    SplashScreen()
                }
                is WeatherUiState.Success -> {
                    val listState = rememberLazyListState()
                    val snappingFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            scope.launch {
                                viewModel.loadWeather(forceRefresh = true)
                                while (uiState is WeatherUiState.Loading) {
                                    kotlinx.coroutines.delay(100)
                                }
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            flingBehavior = snappingFlingBehavior,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { -it },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                ) {
                                    GreetingCard(
                                        userName = userName,
                                        synopsis = synopsis ?: stringResource(R.string.screen_greeting_loading_synopsis)
                                    )
                                }
                            }
                            itemsIndexed(
                                items = uiState.weatherCards,
                                key = { _, it -> it.title + it.value }
                            ) { index, card ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { -it },
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                ) {
                                    AnimatedWeatherCard(
                                        index = index,
                                        state = listState,
                                        card = card
                                    )
                                }
                            }
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    ErrorContent(
                        errorMessage = uiState.message,
                        onRetry = {
                            isRefreshing = true
                            scope.launch {
                                viewModel.loadWeather(forceRefresh = true)
                                while (uiState is WeatherUiState.Loading) {
                                    kotlinx.coroutines.delay(100)
                                }
                                isRefreshing = false
                            }
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
        // Copyright at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "© 2025 Everton L. Frederick. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 12.dp)
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

@Composable
private fun AnimatedWeatherCard(
    index: Int,
    state: androidx.compose.foundation.lazy.LazyListState,
    card: com.weatherpossum.app.data.model.WeatherCard,
    modifier: Modifier = Modifier
) {
    val itemOffset = remember {
        derivedStateOf {
            val visibleItems = state.layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.find { it.index == index }
            itemInfo?.offset?.toFloat() ?: 0f
        }
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (itemOffset.value in -50f..50f) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card scale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (itemOffset.value in -100f..100f) 1f else 0.7f,
        animationSpec = tween(durationMillis = 200),
        label = "card alpha"
    )

    WeatherCard(
        card = card,
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
    )
} 