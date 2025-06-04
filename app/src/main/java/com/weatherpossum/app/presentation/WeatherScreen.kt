package com.weatherpossum.app.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.WeatherCard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.weatherpossum.app.presentation.components.WeatherCard
import androidx.compose.foundation.layout.statusBarsPadding
import com.weatherpossum.app.presentation.components.PillNavBar
import com.weatherpossum.app.presentation.ExtrasScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf("Now") }
    var isRefreshing by remember { mutableStateOf(false) }
    val synopsis by viewModel.synopsis.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value
    val scope = rememberCoroutineScope()

    val bgColors = when {
        synopsis?.contains("rain", ignoreCase = true) == true -> listOf(Color(0xFF90CAF9), Color(0xFFE3F2FD))
        synopsis?.contains("cloud", ignoreCase = true) == true -> listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1))
        synopsis?.contains("wind", ignoreCase = true) == true -> listOf(Color(0xFF80DEEA), Color(0xFFE0F7FA))
        else -> listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = bgColors))
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(targetState = selectedTab, label = "TabSwitch") { tab ->
                    when (tab) {
                        "Now" -> {
                            when (uiState) {
                                is WeatherUiState.Loading -> SplashScreen()
                                is WeatherUiState.Error -> ErrorContent(
                                    errorMessage = uiState.message,
                                    onRetry = {
                                        isRefreshing = true
                                        scope.launch {
                                            viewModel.loadWeather(forceRefresh = true)
                                            isRefreshing = false
                                        }
                                    }
                                )
                                is WeatherUiState.Success -> {
                                    val listState = rememberLazyListState()
                                    val snappingFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

                                    var showNameDialog by remember { mutableStateOf(userName.isNullOrBlank()) }
                                    var nameInput by remember { mutableStateOf("") }

                                    if (showNameDialog) {
                                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sunny))
                                        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

                                        AlertDialog(
                                            onDismissRequest = { /* Prevent dismiss */ },
                                            confirmButton = {
                                                Button(
                                                    onClick = {
                                                        if (nameInput.isNotBlank()) {
                                                            viewModel.saveUserName(nameInput.trim())
                                                            showNameDialog = false
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Text("OK", style = MaterialTheme.typography.labelLarge)
                                                }
                                            },
                                            title = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Welcome",
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Box(
                                                        modifier = Modifier.size(48.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        LottieAnimation(
                                                            composition = composition,
                                                            progress = { progress },
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            },
                                            text = {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        "What shall I call you?",
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Spacer(Modifier.height(12.dp))
                                                    OutlinedTextField(
                                                        value = nameInput,
                                                        onValueChange = { nameInput = it },
                                                        placeholder = { Text("Enter your name") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(28.dp),
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            tonalElevation = 12.dp,
                                            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Filter out duplicate cards (same title and value)
                                    val uniqueCards = uiState.weatherCards.distinctBy { it.title.trim().lowercase() + "|" + it.value.trim().lowercase() }

                                    PullToRefreshBox(
                                        isRefreshing = isRefreshing,
                                        onRefresh = {
                                            isRefreshing = true
                                            scope.launch {
                                                viewModel.loadWeather(forceRefresh = true)
                                                isRefreshing = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        LazyColumn(
                                            state = listState,
                                            flingBehavior = snappingFlingBehavior,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            contentPadding = PaddingValues(top = 16.dp, bottom = 112.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            item {
                                                val hour = LocalTime.now().hour
                                                val greeting = when (hour) {
                                                    in 5..11 -> stringResource(R.string.greeting_good_morning)
                                                    in 12..17 -> stringResource(R.string.greeting_good_afternoon)
                                                    else -> stringResource(R.string.greeting_good_night)
                                                }

                                                val displayName = userName?.replaceFirstChar { it.uppercase() } ?: ""
                                                val personalizedGreeting = if (displayName.isNotBlank()) "$greeting, $displayName" else greeting

                                                // Select greeting animation based on time of day
                                                val greetingAnimation = when (hour) {
                                                    in 5..11 -> R.raw.gmorning
                                                    in 12..17 -> R.raw.afternoon
                                                    else -> R.raw.night
                                                }

                                                val composition by rememberLottieComposition(
                                                    LottieCompositionSpec.RawRes(greetingAnimation)
                                                )
                                                val progress by animateLottieCompositionAsState(
                                                    composition = composition,
                                                    iterations = LottieConstants.IterateForever,
                                                    isPlaying = true
                                                )

                                                Card(
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .statusBarsPadding()
                                                        .padding(bottom = 8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(20.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = personalizedGreeting.uppercase(),
                                                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            if (!synopsis.isNullOrBlank()) {
                                                                Text(
                                                                    text = "SYNOPSIS: ${synopsis!!}",
                                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .size(96.dp)
                                                                .background(Color.Transparent),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            LottieAnimation(
                                                                composition = composition,
                                                                progress = { progress },
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            // Insert warning card below greeting card if present
                                            val warningCard = uniqueCards.find { it.title.equals("Warning/Advisory", ignoreCase = true) }
                                            if (warningCard != null) {
                                                item {
                                                    WeatherInfoCard(
                                                        title = warningCard.title,
                                                        description = warningCard.value,
                                                        lottieRes = R.raw.warning // Use warning.json animation
                                                    )
                                                }
                                            }
                                            itemsIndexed(uniqueCards) { _, card ->
                                                AnimatedVisibility(
                                                    visible = true,
                                                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                                                ) {
                                                    if (card.title.contains("Weather Outlook for Dominica", ignoreCase = true)) {
                                                        WeatherCard(card = card, lottieRes = R.raw.outlook)
                                                    } else {
                                                        WeatherInfoCard(
                                                            title = card.title,
                                                            description = card.value,
                                                            lottieRes = getLottieResForCard(card)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "Extras" -> ExtrasScreenContent()
                    }
                }
                // Overlay pill nav bar at bottom center
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

@Composable
fun WeatherInfoCard(title: String, description: String, lottieRes: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

fun getLottieResForCard(card: WeatherCard): Int {
    val title = card.title.lowercase()
    val value = card.value.lowercase()
    val text = "$title $value"
    // Always use wind.json for Wind Conditions
    if (title.contains("wind conditions")) {
        return R.raw.wind
    }
    // Use partlycloudy for any card mentioning partly cloudy
    if (text.contains("partly cloudy")) {
        return R.raw.partlycloudy
    }
    return when {
        text.contains("thunderstorm") || text.contains("thunder") -> R.raw.thunder
        text.contains("rain") || text.contains("shower") -> R.raw.rain
        text.contains("cloud") || text.contains("overcast") -> R.raw.cloudy
        text.contains("wind") || text.contains("breeze") || text.contains("gust") -> R.raw.wind
        text.contains("sun") || text.contains("clear") || text.contains("fair") -> R.raw.sunny
        text.contains("sea") -> {
            val hour = java.time.LocalTime.now().hour
            if (hour in 6..18) R.raw.seaconday else R.raw.seaconnight
        }
        text.contains("outlook") -> R.raw.outlook
        text.contains("synopsis") -> R.raw.sunny
        else -> R.raw.neutral
    }
}
