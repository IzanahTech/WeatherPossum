package com.weatherpossum.app.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.presentation.components.AnimatedWeatherIcon
import com.weatherpossum.app.presentation.components.WeatherIconType
import com.weatherpossum.app.presentation.components.getWeatherIconType
import androidx.compose.foundation.Image
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.WeatherCard
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
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
import com.weatherpossum.app.presentation.components.ExpressiveLoadingIndicator

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
    // EXPRESSIVE: Use higher contrast/more saturated colors based on weather
    val bgColors = when {
        isDarkMode -> when {
            synopsis?.contains("rain", ignoreCase = true) == true -> listOf(Color(0xFF2C3E50), Color(0xFF1C2833)) // Darker, deep blue
            synopsis?.contains("cloud", ignoreCase = true) == true -> listOf(Color(0xFF4A4A4A), Color(0xFF2C3E50)) // Deeper gray
            synopsis?.contains("wind", ignoreCase = true) == true -> listOf(Color(0xFF00695C), Color(0xFF1E8449)) // Rich teal to green
            else -> listOf(Color(0xFF151C2C), Color(0xFF0F172A)) // Deep night
        }
        else -> when {
            synopsis?.contains("rain", ignoreCase = true) == true -> listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)) // Brighter, saturated blue
            synopsis?.contains("cloud", ignoreCase = true) == true -> listOf(Color(0xFFB0BEC5), Color(0xFFCFD8DC)) // Light, distinct gray
            synopsis?.contains("wind", ignoreCase = true) == true -> listOf(Color(0xFF4DB6AC), Color(0xFF80CBC4)) // Brighter aqua
            else -> listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA)) // Vibrant day sky
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
                // 1. EXPRESSIVE MOTION: Use bouncy spring animation for tab switching
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState == "Extras" && initialState == "Now") 1 else -1
                        
                        // Custom spring motion for expressive tab sliding
                        val springSpec = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )

                        (slideInHorizontally(initialOffsetX = { width -> direction * width }, animationSpec = springSpec) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { width -> -direction * width }, animationSpec = springSpec) + fadeOut())
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
                    modifier = Modifier.align(Alignment.TopCenter),
                    // EXPRESSIVE: Use primary color for indicator
                    contentColor = MaterialTheme.colorScheme.primary 
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
                // 3. EXPRESSIVE DIALOGUE STYLING
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.saveUserName(nameInput.trim())
                                    showNameDialog = false
                                }
                            }, 
                            // Exaggerated corner shape
                            shape = RoundedCornerShape(20.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("OK", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Bolder Welcome text
                            Text(
                                text = "WELCOME ABOARD!",
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
                            Text("What shall I call you?", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp))
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("Enter your name") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp) // Expressive rounding
                            )
                        }
                    },
                    shape = RoundedCornerShape(32.dp), // Exaggerated outer shape
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), // Higher elevation surface
                    tonalElevation = 18.dp,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            val uniqueCards = uiState.weatherCards
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
                item {
                    GreetingCard(userName = userName, synopsis = synopsis)
                }

                val warningCard = uniqueCards.find { it.title.equals("Warning/Advisory", ignoreCase = true) }
                if (warningCard != null) {
                    item {
                        WeatherInfoCard(
                            title = warningCard.title,
                            description = warningCard.value,
                            iconType = WeatherIconType.ADVISORY
                        )
                    }
                }

                itemsIndexed(uniqueCards.filterNot { it.title.equals("Warning/Advisory", ignoreCase = true) }) { index, card ->
                    // 4. Parallax Effect Enhancement
                    ParallaxCard(index = index, listState = listState) {
                        AnimatedVisibility(
                            visible = true,
                            // Expressive: Simple entry animation to match the overall feel
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                        ) {
                            if (card.title.contains("Weather Outlook", ignoreCase = true)) {
                                OutlookWeatherCard(card = card, iconType = getWeatherIconType("${card.title} ${card.value}"))
                            } else if (card.title.contains("Forecast", ignoreCase = true)) {
                                ForecastCard(card = card)
                            } else if (card.title.contains("sun", ignoreCase = true) || 
                                    card.value.contains("sunrise", ignoreCase = true) || 
                                    card.value.contains("sunset", ignoreCase = true)) {
                                SunCard()
                            } else {
                                WeatherInfoCard(
                                    title = card.title,
                                    description = card.value,
                                    iconType = getWeatherIconType("${card.title} ${card.value}")
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
        ExpressiveLoadingIndicator()
    }
}

@Composable
fun ErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp), // More generous padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp)) // More space
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp), // Expressive button shape
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp) // Lifted button
        ) { 
            Text("TRY AGAIN!", fontWeight = FontWeight.Bold) // Assertive text
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   NEW/MODIFIED: Styled WeatherInfoCard + helpers
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun WeatherInfoCard(title: String, description: String, iconType: WeatherIconType) {
    val style = chooseInfoStyle(title, description)
    val annotated = remember(description) { boldKnownLabels(description, style.boldLabels) }

    // Use expressive shape everywhere
    val expressiveShape = remember { RoundedCornerShape(28.dp) } 

    Card(
        shape = expressiveShape,
        elevation = CardDefaults.cardElevation(14.dp), // Higher elevation
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .enhancedCardBackground(style.gradient)
                .fillMaxWidth()
                .padding(20.dp) // More padding
        ) {
            GradientNoiseOverlay()
            
            Column {
                // Header (modified for Expressive Typography)
                CardHeader(
                    title = style.headerTitle ?: title.uppercase(),
                    subtitle = style.subtitle,
                    endContent = {
                        Box(Modifier.size(style.iconSize), contentAlignment = Alignment.Center) {
                            AnimatedWeatherIcon(
                                type = iconType,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    onColor = style.onColor
                )

                // Body content logic remains the same, but uses expressive typography below
                when {
                    style.rows.isNotEmpty() -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            // Expressive Body Text: slightly larger and more generous line height
                            fontSize = 16.sp, 
                            lineHeight = 22.sp 
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
    val iconSize: Dp = 64.dp, // Increased default icon size
    val labelWidth: Dp = 120.dp, // Wider label for complex data
    val rows: List<Pair<String, (String) -> String?>> = emptyList(),
    val pills: List<Pair<String, (String) -> String?>> = emptyList(),
    val boldLabels: List<String> = emptyList()
)

@Composable
private fun chooseInfoStyle(title: String, description: String): InfoStyle {
    val t = title.lowercase()
    val text = "$title $description".lowercase()
    val colorScheme = MaterialTheme.colorScheme // Access MaterialTheme colors

    // Warning / Advisory (Using Primary/Error for high urgency)
    if (t.contains("warning") || t.contains("advisory") || text.contains("alert")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(colorScheme.error, colorScheme.primary)),
            onColor = colorScheme.onPrimary,
            headerTitle = "CRITICAL ALERT",
            iconSize = 72.dp, // Even larger
            boldLabels = listOf("area:", "valid until:", "impact:", "advice:")
        )
    }

    // Tides / Sea (Using Secondary/Tertiary for calm water tones)
    if (t.contains("tide") || text.contains("low tide") || text.contains("high tide") || text.contains("sea")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(colorScheme.secondaryContainer, colorScheme.tertiaryContainer)),
            onColor = colorScheme.onSecondaryContainer,
            headerTitle = "MARINE CONDITIONS",
            iconSize = 64.dp,
            labelWidth = 110.dp,
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

    // Sun (sunrise/sunset) (Using Vibrant Yellow/Orange)
    if (t.contains("sun") || text.contains("sunrise") || text.contains("sunset")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFFFFB75E), Color(0xFFFF9800))),
            onColor = Color.White,
            headerTitle = "SOLAR EVENTS",
            iconSize = 64.dp,
            pills = listOf(
                "Sunrise" to { s: String -> findValueAfterLabel(s, "Sunrise:") },
                "Sunset" to { s: String -> findValueAfterLabel(s, "Sunset:") }
            ),
            boldLabels = listOf("Sunrise:", "Sunset:")
        )
    }

    // Wind (Using Green/Cyan for moving air)
    if (t.contains("wind") || text.contains("gust") || text.contains("breeze")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(Color(0xFF80CBC4), Color(0xFF4DB6AC))),
            onColor = Color(0xFF004D40),
            headerTitle = "WIND VELOCITY",
            iconSize = 64.dp,
            rows = listOf("Summary" to { s: String -> s.takeIf { it.isNotBlank() } })
        )
    }

    // Outlook / Synopsis (Using Primary/Secondary for general purpose)
    if (t.contains("outlook") || t.contains("synopsis")) {
        return InfoStyle(
            gradient = Brush.verticalGradient(listOf(colorScheme.primary, colorScheme.secondary)),
            onColor = colorScheme.onPrimary,
            headerTitle = title.uppercase()
        )
    }

    // Generic fallback
    return InfoStyle(
        gradient = Brush.verticalGradient(listOf(colorScheme.surfaceTint, colorScheme.primary)),
        onColor = colorScheme.onPrimary,
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
            fontWeight = FontWeight.ExtraBold, // Bolder label
            fontSize = 15.sp, // Slightly larger
            modifier = Modifier.width(labelWidth)
        )
        Text(
            value, 
            color = onColor.copy(alpha = 0.96f), 
            fontSize = 15.sp, // Slightly larger
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun Pill(label: String, value: String, onColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp)) // More rounded pill
            .background(Color.White.copy(alpha = 0.2f)) // Brighter overlay
            .padding(horizontal = 16.dp, vertical = 10.dp) // More padding
            .fillMaxWidth()
    ) {
        Text(label, color = onColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        Text(value, color = onColor.copy(alpha = 0.95f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun boldKnownLabels(text: String, labels: List<String>): AnnotatedString {
    return buildAnnotatedString {
        text.lines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            val label = labels.firstOrNull { trimmed.startsWith(it, ignoreCase = true) }
            if (label != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append(label) } // Max boldness
                append(" " + trimmed.removePrefix(label).trim())
            } else {
                append(trimmed)
            }
            if (index != text.lines().lastIndex) append("\n")
        }
    }
}

@Deprecated("Use getWeatherIconType instead - this function is kept for reference only")
fun getLottieResForCard(card: WeatherCard): Int {
    // This function is deprecated and no longer used
    // All drawable resources have been removed
    // Use getWeatherIconType("${card.title} ${card.value}") instead
    return 0 // Dummy return - function should not be called
}

@Composable
private fun ForecastCard(card: WeatherCard) {
    val gradient = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    val on = MaterialTheme.colorScheme.onPrimary
    
    val content = card.value
    val title = card.title
    
    val hasToday = title.contains("Today", ignoreCase = true) || 
                   content.contains("Forecast for Today", ignoreCase = true) || 
                   content.contains("Today:", ignoreCase = true)
    val hasTonight = content.contains("Forecast for Tonight", ignoreCase = true) || 
                     content.contains("Tonight:", ignoreCase = true)
    
    val hasBothSections = title.contains("Today", ignoreCase = true) && hasTonight
    
    val displayTitle = if (hasBothSections) "Forecast" else title
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp), // Exaggerated shape
        elevation = CardDefaults.cardElevation(14.dp), // Higher elevation
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .enhancedCardBackground(gradient)
                .fillMaxWidth()
                .padding(20.dp) // More padding
        ) {
            GradientNoiseOverlay()
            
            Column {
                CardHeader(
                    title = displayTitle.uppercase(),
                    endContent = {
                        Box(
                            modifier = Modifier.size(50.dp), // Larger icon
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedWeatherIcon(
                                type = WeatherIconType.FORECAST,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    onColor = on
                )

                if (hasBothSections) {
                    val todayContent = extractSectionContent(content, "Today")
                    val tonightContent = extractSectionContent(content, "Tonight")
                    
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { // More spacing
                        // Today container
                        ForecastSectionContainer(
                            title = "Day Forecast", // More descriptive title
                            content = todayContent,
                            iconType = WeatherIconType.PARTLY_CLOUDY,
                            onColor = on
                        )
                        
                        // Tonight container
                        ForecastSectionContainer(
                            title = "Night Forecast",
                            content = tonightContent,
                            iconType = WeatherIconType.NIGHT,
                            onColor = on
                        )
                    }
                } else {
                    Text(
                        text = content,
                        color = on.copy(alpha = 0.98f),
                        fontSize = 16.sp, // Larger body text
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastSectionContainer(
    title: String,
    content: String,
    iconType: WeatherIconType,
    onColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Larger rounding
        color = onColor.copy(alpha = 0.15f), // Brighter overlay
        border = BorderStroke(2.dp, onColor.copy(alpha = 0.3f)), // Thicker, more prominent border
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp), // More padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AVD animation
            Box(
                modifier = Modifier.size(40.dp), // Larger icon
                contentAlignment = Alignment.Center
            ) {
                AnimatedWeatherIcon(
                    type = iconType,
                    modifier = Modifier.fillMaxSize(),
                    color = onColor // Match color
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp)) // More space
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    color = onColor,
                    fontSize = 14.sp, // Larger label
                    fontWeight = FontWeight.Black, // Max boldness
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = content,
                    color = onColor.copy(alpha = 0.95f),
                    fontSize = 14.sp, 
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun extractSectionContent(fullContent: String, section: String): String {
    val text = fullContent.trim()
    
    return when (section.lowercase()) {
        "today" -> {
            val patterns = listOf(
                Regex("Forecast for Today[:\\.]?\\s*(.*?)(?=Forecast for Tonight|$)", RegexOption.DOT_MATCHES_ALL),
                Regex("Today[:\\.]?\\s*(.*?)(?=Tonight|Forecast for Tonight|$)", RegexOption.DOT_MATCHES_ALL),
                Regex("FORECAST FOR TODAY[:\\.]?\\s*(.*?)(?=FORECAST FOR TONIGHT|TONIGHT|$)", RegexOption.DOT_MATCHES_ALL),
                Regex("^(.*?)(?=Forecast for Tonight|$)", RegexOption.DOT_MATCHES_ALL)
            )
            
            for (pattern in patterns) {
                val match = pattern.find(text)
                if (match != null) {
                    val content = match.groupValues[1].trim()
                    if (content.isNotBlank()) {
                        return content
                    }
                }
            }
            ""
        }
        "tonight" -> {
            val patterns = listOf(
                Regex("Forecast for Tonight[:\\.]?\\s*(.*?)$", RegexOption.DOT_MATCHES_ALL),
                Regex("Tonight[:\\.]?\\s*(.*?)$", RegexOption.DOT_MATCHES_ALL),
                Regex("FORECAST FOR TONIGHT[:\\.]?\\s*(.*?)$", RegexOption.DOT_MATCHES_ALL)
            )
            
            for (pattern in patterns) {
                val match = pattern.find(text)
                if (match != null) {
                    val content = match.groupValues[1].trim()
                    if (content.isNotBlank()) {
                        return content
                    }
                }
            }
            ""
        }
        else -> ""
    }
}

@Composable
private fun OutlookWeatherCard(card: WeatherCard, iconType: WeatherIconType) {
    val gradient = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary))
    val on = MaterialTheme.colorScheme.onSecondary
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp), // Expressive shape
        elevation = CardDefaults.cardElevation(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .enhancedCardBackground(gradient)
                .fillMaxWidth()
                .padding(20.dp) // More padding
        ) {
            GradientNoiseOverlay()
            
            Column {
                CardHeader(
                    title = card.title.uppercase(),
                    endContent = {
                        Box(
                            modifier = Modifier.size(110.dp), // Larger Icon
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedWeatherIcon(
                                type = iconType,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    onColor = on
                )

                Text(
                    text = card.value,
                    color = on.copy(alpha = 0.98f),
                    fontSize = 16.sp, // Larger body text
                    lineHeight = 22.sp
                )
            }
        }
    }
}