package com.weatherpossum.app.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R
import com.weatherpossum.app.presentation.components.AnimatedWeatherIcon
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.presentation.components.WeatherIconType
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import com.weatherpossum.app.presentation.components.PillNavBar
import com.weatherpossum.app.widget.WeatherWidgetUpdateManager
import android.content.Context
import com.weatherpossum.app.presentation.components.ParallaxCard
import com.weatherpossum.app.presentation.components.StaggeredReveal
import com.weatherpossum.app.presentation.components.keepTabComposed
import com.weatherpossum.app.presentation.components.GreetingCard
import androidx.compose.ui.platform.LocalContext
import com.weatherpossum.app.presentation.components.UpdateSheetWithContext
import com.weatherpossum.app.presentation.components.ExpressiveLoadingIndicator
import com.weatherpossum.app.presentation.components.ExtrasErrorCard
import com.weatherpossum.app.presentation.components.WeatherInfoCard
import com.weatherpossum.app.presentation.components.WeatherCardStyle
import com.weatherpossum.app.presentation.components.WeatherCardRouter
import com.weatherpossum.app.ui.theme.LocalWeatherHazeState
import com.weatherpossum.app.ui.theme.WeatherPossumGradients
import com.weatherpossum.app.ui.theme.glassAmbientLayer
import com.weatherpossum.app.ui.theme.rememberWeatherHazeState
import com.weatherpossum.app.ui.theme.weatherHazeSource
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel(),
    updateViewModel: UpdateViewModel = koinViewModel(),
    extendedForecastViewModel: ExtendedForecastViewModel = koinViewModel(),
    moonViewModel: MoonViewModel = koinViewModel(),
    hurricaneViewModel: HurricaneViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf("Now") }
    var isRefreshing by remember { mutableStateOf(false) }
    val synopsis by viewModel.synopsis.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isDarkMode = isSystemInDarkTheme()
    val bgColors = WeatherPossumGradients.screenBackground(isDarkMode, synopsis)
    val hazeState = rememberWeatherHazeState()

    val listState = rememberLazyListState()
    val extrasScrollState = rememberScrollState()
    CompositionLocalProvider(LocalWeatherHazeState provides hazeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weatherHazeSource(hazeState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColors)
                    .glassAmbientLayer(isDarkMode)
            ) {
                Scaffold(
                    containerColor = Color.Transparent
                ) { padding ->
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                try {
                                    when (selectedTab) {
                                        "Now" -> viewModel.refreshWeather(forceRefresh = true)
                                        "Extras" -> refreshExtrasContent(
                                            context = context.applicationContext,
                                            weatherViewModel = viewModel,
                                            extendedForecastViewModel = extendedForecastViewModel,
                                            moonViewModel = moonViewModel,
                                            hurricaneViewModel = hurricaneViewModel
                                        )
                                    }
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                // Reset scroll position when switching tabs
                LaunchedEffect(selectedTab) {
                    when (selectedTab) {
                        "Now" -> {
                            listState.animateScrollToItem(0)
                        }
                        "Extras" -> {
                            extrasScrollState.animateScrollTo(0)
                        }
                    }
                }
                
                // Keep both tabs composed so scroll position and card state survive tab switches.
                Box(modifier = Modifier.fillMaxSize()) {
                    NowTabContent(
                        uiState = uiState,
                        synopsis = synopsis,
                        userName = userName,
                        viewModel = viewModel,
                        listState = listState,
                        scope = scope,
                        modifier = Modifier
                            .fillMaxSize()
                            .keepTabComposed(selectedTab == "Now")
                    )
                    ExtrasScreenContent(
                        scrollState = extrasScrollState,
                        weatherViewModel = viewModel,
                        extendedForecastViewModel = extendedForecastViewModel,
                        moonViewModel = moonViewModel,
                        hurricaneViewModel = hurricaneViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .keepTabComposed(selectedTab == "Extras")
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    PillNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
                    }
                    }
                }
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                updateViewModel.checkForUpdates(context)
            }

            UpdateSheetWithContext(
                vm = updateViewModel,
                context = context,
                onDismiss = { updateViewModel.dismissUpdate() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowTabContent(
    uiState: WeatherUiState,
    synopsis: String?,
    userName: String?,
    viewModel: WeatherViewModel,
    listState: LazyListState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = uiState,
        animationSpec = tween(420, easing = WeatherPossumMotion.EmphasizedDecelerate),
        label = "nowTabState",
        modifier = modifier.fillMaxSize()
    ) { state ->
    when (state) {
        is WeatherUiState.Loading -> SplashScreen()
        is WeatherUiState.Error -> ErrorContent(
            errorMessage = state.message,
            onRetry = {
                scope.launch { viewModel.loadWeather(forceRefresh = true) }
            }
        )
        is WeatherUiState.Success -> {
            var nameInput by remember { mutableStateOf("") }
            var nameSavePending by remember { mutableStateOf(false) }
            val showNameDialog = userName.isNullOrBlank() && !nameSavePending

            LaunchedEffect(userName) {
                if (!userName.isNullOrBlank()) {
                    nameSavePending = false
                }
            }

            if (showNameDialog) {
                // 3. EXPRESSIVE DIALOGUE STYLING
                AlertDialog(
                    onDismissRequest = { },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.saveUserName(nameInput.trim())
                                    nameSavePending = true
                                }
                            },
                            // Exaggerated corner shape
                            shape = MaterialTheme.shapes.medium, 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                stringResource(R.string.welcome_confirm),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Bolder Welcome text
                            Text(
                                text = stringResource(R.string.welcome_title),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                            )
                            // Larger icon
                            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                                AnimatedWeatherIcon(
                                    type = WeatherIconType.SUNNY,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.welcome_prompt),
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text(stringResource(R.string.welcome_name_placeholder)) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.large // Expressive rounding
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge, // Exaggerated outer shape
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, // Higher elevation surface
                    tonalElevation = 6.dp,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            val uniqueCards = state.weatherCards
                .distinctBy { "${it.title.trim().lowercase()}|${it.value.trim().lowercase()}" }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                // Tighter padding to give cards more space to breathe and parallax
                contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing
            ) {
                if (state.isStale) {
                    item {
                        Text(
                            text = stringResource(R.string.weather_stale_cache_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                item {
                    StaggeredReveal(key = "now_greeting", index = 0) {
                        GreetingCard(userName = userName, synopsis = synopsis)
                    }
                }

                val warningCard = uniqueCards.find { it.title.equals("Warning/Advisory", ignoreCase = true) }
                if (warningCard != null) {
                    item {
                        StaggeredReveal(key = "now_warning", index = 1) {
                            WeatherInfoCard(
                                title = warningCard.title,
                                description = warningCard.value,
                                iconType = WeatherIconType.ADVISORY,
                                style = WeatherCardStyle.Advisory
                            )
                        }
                    }
                }

                itemsIndexed(
                    uniqueCards.filterNot { it.title.equals("Warning/Advisory", ignoreCase = true) },
                    key = { _, card -> card.title }
                ) { index, card ->
                    StaggeredReveal(key = "now_${card.title}", index = index + 2) {
                        ParallaxCard(index = index, listState = listState) {
                            WeatherCardRouter(card = card)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StaggeredReveal(key = "splash_loading", index = 0) {
            ExpressiveLoadingIndicator()
        }
    }
}

@Composable
fun ErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExtrasErrorCard(
            title = stringResource(R.string.error_weather_unavailable),
            message = errorMessage
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.button_retry))
        }
    }
}

private suspend fun refreshExtrasContent(
    context: Context,
    weatherViewModel: WeatherViewModel,
    extendedForecastViewModel: ExtendedForecastViewModel,
    moonViewModel: MoonViewModel,
    hurricaneViewModel: HurricaneViewModel
) {
    coroutineScope {
        val weather = async { weatherViewModel.refreshWeather(forceRefresh = true) }
        val forecast = async { extendedForecastViewModel.refreshForecast(forceRefresh = true) }
        val moon = async { moonViewModel.forceRefresh() }
        val hurricane = async { hurricaneViewModel.refreshHurricaneDataAwait() }
        weather.await()
        forecast.await()
        moon.await()
        hurricane.await()
    }
    WeatherWidgetUpdateManager.updateAllWidgets(context)
}
