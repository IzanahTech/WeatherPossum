package com.weatherpossum.app.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.presentation.components.PillNavBar
import com.weatherpossum.app.presentation.components.SunCard
import com.weatherpossum.app.presentation.components.CardHeader
import com.weatherpossum.app.presentation.components.GradientNoiseOverlay
import com.weatherpossum.app.presentation.components.ParallaxCard
import com.weatherpossum.app.presentation.components.enhancedCardBackground
import com.weatherpossum.app.presentation.components.GreetingCard
import androidx.compose.ui.platform.LocalContext
import com.weatherpossum.app.presentation.components.UpdateSheetWithContext
import com.weatherpossum.app.ui.viewmodel.UpdateViewModel
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel(),
    updateViewModel: UpdateViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf("Now") }
    var isRefreshing by remember { mutableStateOf(false) }
    val synopsis by viewModel.synopsis.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val uiState = viewModel.uiState.collectAsState().value
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isDarkMode = isSystemInDarkTheme()
    val bgColors = when {
        isDarkMode -> when {
            synopsis?.contains("rain", ignoreCase = true) == true -> listOf(Color(0xFF1A237E), Color(0xFF0D1421))
            synopsis?.contains("cloud", ignoreCase = true) == true -> listOf(Color(0xFF263238), Color(0xFF1A1A1A))
            synopsis?.contains("wind", ignoreCase = true) == true -> listOf(Color(0xFF004D40), Color(0xFF0D1B1A))
            else -> listOf(Color(0xFF0D1421), Color(0xFF1A1A1A))
        }
        else -> when {
            synopsis?.contains("rain", ignoreCase = true) == true -> listOf(Color(0xFF90CAF9), Color(0xFFE3F2FD))
            synopsis?.contains("cloud", ignoreCase = true) == true -> listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1))
            synopsis?.contains("wind", ignoreCase = true) == true -> listOf(Color(0xFF80DEEA), Color(0xFFE0F7FA))
            else -> listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE))
        }
    }

    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                viewModel.loadWeather(forceRefresh = true)
                isRefreshing = false
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = bgColors))
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
                    .padding(padding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState == "Extras" && initialState == "Now") {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "TabSwitch"
                ) { tab ->
                    when (tab) {
                        "Now" -> NowTabContent(uiState, synopsis, userName, viewModel, listState, scope)
                        "Extras" -> ExtrasScreenContent()
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
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
    
    // Automatically check for updates in the background on app launch
    // Only shows dialog if update is available - completely silent otherwise
    LaunchedEffect(Unit) {
        // Add a small delay to ensure app is fully loaded before checking
        kotlinx.coroutines.delay(2000)
        updateViewModel.checkForUpdates(context)
    }
    
    // Show update dialog if available
    UpdateSheetWithContext(
        vm = updateViewModel,
        context = context,
        onDismiss = { updateViewModel.dismissUpdate() }
    )
}

@Composable
fun NowTabContent(
    uiState: WeatherUiState,
    synopsis: String?,
    userName: String?,
    viewModel: WeatherViewModel,
    listState: LazyListState,
    scope: CoroutineScope
) {
    when (uiState) {
        is WeatherUiState.Loading -> SplashScreen()
        is WeatherUiState.Error -> ErrorContent(
            errorMessage = uiState.message,
            onRetry = {
                scope.launch { viewModel.loadWeather(forceRefresh = true) }
            }
        )
        is WeatherUiState.Success -> {
            var showNameDialog by remember { mutableStateOf(userName.isNullOrBlank()) }
            var nameInput by remember { mutableStateOf("") }

            if (showNameDialog) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sunny))
                val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        Button(onClick = {
                            if (nameInput.isNotBlank()) {
                                viewModel.saveUserName(nameInput.trim())
                                showNameDialog = false
                            }
                        }, shape = RoundedCornerShape(16.dp)) {
                            Text("OK", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Welcome",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.fillMaxSize())
                            }
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("What shall I call you?", style = MaterialTheme.typography.bodyLarge)
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

            val uniqueCards = uiState.weatherCards
                .distinctBy { "${it.title.trim().lowercase()}|${it.value.trim().lowercase()}" }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GreetingCard(userName = userName, synopsis = synopsis)
                }

                val warningCard = uniqueCards.find { it.title.equals("Warning/Advisory", ignoreCase = true) }
                if (warningCard != null) {
                    item {
                        WeatherInfoCard(
                            title = warningCard.title,
                            description = warningCard.value,
                            lottieRes = R.raw.warning
                        )
                    }
                }

                itemsIndexed(uniqueCards.filterNot { it.title.equals("Warning/Advisory", ignoreCase = true) }) { index, card ->
                    ParallaxCard(index = index, listState = listState) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                        ) {
                            if (card.title.contains("Weather Outlook", ignoreCase = true)) {
                                // Your special Outlook card remains
                                OutlookWeatherCard(card = card, lottieRes = R.raw.outlook)
                            } else if (card.title.contains("sun", ignoreCase = true) || 
                                       card.value.contains("sunrise", ignoreCase = true) || 
                                       card.value.contains("sunset", ignoreCase = true)) {
                                // Use the new SunCard for sun-related content
                                SunCard()
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

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   NEW: Styled WeatherInfoCard + helpers
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun WeatherInfoCard(title: String, description: String, lottieRes: Int) {
    val style = remember(title, description) { chooseInfoStyle(title, description) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    val annotated = remember(description) { boldKnownLabels(description, style.boldLabels) }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .enhancedCardBackground(style.gradient)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GradientNoiseOverlay()
            
            Column {
                // Header
                CardHeader(
                    title = style.headerTitle ?: title.uppercase(),
                    subtitle = style.subtitle,
                    endContent = {
                        Box(Modifier.size(style.iconSize), contentAlignment = Alignment.Center) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    onColor = style.onColor
                )

                // Body
                when {
                    style.rows.isNotEmpty() -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            style.rows.forEach { (label, valueProvider) ->
                                val v = valueProvider(description)
                                if (!v.isNullOrBlank()) {
                                    LabelValueRow(
                                        label = label,
                                        value = v,
                                        onColor = style.onColor,
                                        labelWidth = style.labelWidth
                                    )
                                }
                            }
                        }
                    }
                    style.pills.isNotEmpty() -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            style.pills.forEach { (label, valueProvider) ->
                                val v = valueProvider(description)
                                if (!v.isNullOrBlank()) {
                                    Pill(label = label, value = v, onColor = style.onColor)
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = annotated,
                            color = style.onColor.copy(alpha = 0.98f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

private data class InfoStyle(
    val gradient: Brush,
    val onColor: Color = Color.White,
    val headerTitle: String? = null,
    val subtitle: String? = null,
    val iconSize: Dp = 56.dp,
    val labelWidth: Dp = 110.dp,
    val rows: List<Pair<String, (String) -> String?>> = emptyList(),
    val pills: List<Pair<String, (String) -> String?>> = emptyList(),
    val boldLabels: List<String> = emptyList()
)

private fun chooseInfoStyle(title: String, description: String): InfoStyle {
    val t = title.lowercase()
    val text = "$title $description".lowercase()

    // Warning / Advisory
    if (t.contains("warning") || t.contains("advisory") || text.contains("alert")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFFF7A59))),
            onColor = Color(0xFF2B2B2B),
            headerTitle = "WEATHER ADVISORY",
            boldLabels = listOf("area:", "valid until:", "impact:", "advice:")
        )
    }

    // Tides / Sea
    if (t.contains("tide") || text.contains("low tide") || text.contains("high tide") || text.contains("sea")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1976D2))),
            onColor = Color.White,
            headerTitle = "SEA & TIDES",
            labelWidth = 100.dp,
            pills = listOf(
                "Low Tide" to { s: String -> findValueAfterLabel(s, "Low Tide:") },
                "High Tide" to { s: String -> findValueAfterLabel(s, "High Tide:") }
            ),
            rows = listOf(
                "Sea Conditions" to { s: String -> findValueAfterLabel(s, "Sea Conditions:") },
                "Waves" to { s: String -> findValueAfterLabel(s, "Waves:") }
            ),
            boldLabels = listOf("Sea Conditions:", "Waves:", "Low Tide:", "High Tide:")
        )
    }

    // Sun (sunrise/sunset)
    if (t.contains("sun") || text.contains("sunrise") || text.contains("sunset")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFFFFB75E), Color(0xFFED8F03))),
            onColor = Color.White,
            headerTitle = "SUN",
            pills = listOf(
                "Sunrise" to { s: String -> findValueAfterLabel(s, "Sunrise:") },
                "Sunset"  to { s: String -> findValueAfterLabel(s, "Sunset:") }
            ),
            boldLabels = listOf("Sunrise:", "Sunset:")
        )
    }

    // Wind
    if (t.contains("wind") || text.contains("gust") || text.contains("breeze")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF388E3C))),
            onColor = Color.White,
            headerTitle = "WIND CONDITIONS",
            rows = listOf("Summary" to { s: String -> s.takeIf { it.isNotBlank() } })
        )
    }

    // Outlook / Synopsis
    if (t.contains("outlook") || t.contains("synopsis")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFF86A8E7), Color(0xFF7F7FD5))),
            onColor = Color.White,
            headerTitle = title.uppercase()
        )
    }

    // Generic fallback
    return InfoStyle(
        gradient = Brush.verticalGradient(listOf(Color(0xFF6A4C93), Color(0xFF9A4C95))),
        onColor = Color.White,
        headerTitle = title.uppercase()
    )
}

private fun findValueAfterLabel(text: String, label: String): String? {
    val line = text.lines().firstOrNull { it.trim().startsWith(label, ignoreCase = true) } ?: return null
    return line.substringAfter(label, "").trim().ifEmpty { null }
}

@Composable
private fun LabelValueRow(label: String, value: String, onColor: Color, labelWidth: Dp) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label:",
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.width(labelWidth)
        )
        Text(value, color = onColor.copy(alpha = 0.96f), fontSize = 14.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun Pill(label: String, value: String, onColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(label, color = onColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Text(value, color = onColor.copy(alpha = 0.95f), fontSize = 13.sp)
    }
}

private fun boldKnownLabels(text: String, labels: List<String>): AnnotatedString {
    return buildAnnotatedString {
        text.lines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            val label = labels.firstOrNull { trimmed.startsWith(it, ignoreCase = true) }
            if (label != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(label) }
                append(" " + trimmed.removePrefix(label).trim())
            } else {
                append(trimmed)
            }
            if (index != text.lines().lastIndex) append("\n")
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   Existing helper unchanged: pick Lottie based on content
   ────────────────────────────────────────────────────────────────────────────── */

fun getLottieResForCard(card: WeatherCard): Int {
    val title = card.title.lowercase()
    val value = card.value.lowercase()
    val text = "$title $value"
    if (title.contains("wind conditions")) return R.raw.wind
    if (text.contains("partly cloudy")) return R.raw.partlycloudy
    return when {
        text.contains("thunderstorm") || text.contains("thunder") -> R.raw.thunder
        text.contains("rain") || text.contains("shower") -> R.raw.rain
        text.contains("cloud") || text.contains("overcast") -> R.raw.cloudy
        text.contains("wind") || text.contains("breeze") || text.contains("gust") -> R.raw.wind
        text.contains("sea") -> {
            val hour = java.time.LocalTime.now().hour
            if (hour in 6..18) R.raw.seaconday else R.raw.seaconnight
        }
        text.contains("outlook") -> R.raw.outlook
        text.contains("synopsis") -> R.raw.sunny
        else -> R.raw.neutral
    }
}

@Composable
private fun OutlookWeatherCard(card: WeatherCard, lottieRes: Int) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF86A8E7), Color(0xFF7F7FD5))) // Purple gradient
    val on = Color.White
    
    // Outlook Lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .enhancedCardBackground(gradient)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GradientNoiseOverlay()
            
            Column {
                CardHeader(
                    title = card.title.uppercase(),
                    endContent = {
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    onColor = on
                )

                // Display main forecast text
                Text(
                    text = card.value,
                    color = on.copy(alpha = 0.98f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
