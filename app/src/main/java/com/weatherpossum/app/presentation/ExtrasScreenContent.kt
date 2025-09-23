package com.weatherpossum.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R
import com.weatherpossum.app.presentation.components.FunFactCard
import com.weatherpossum.app.presentation.components.GreetingCard
import com.weatherpossum.app.presentation.components.ExtendedForecastCard
import com.weatherpossum.app.ui.viewmodel.MoonViewModel
import com.weatherpossum.app.ui.viewmodel.MoonUiState
import com.weatherpossum.app.ui.viewmodel.HurricaneViewModel
import com.weatherpossum.app.ui.viewmodel.HurricaneUiState
import com.weatherpossum.app.data.MoonData
import com.weatherpossum.app.presentation.components.enhancedCardBackground
import com.weatherpossum.app.presentation.components.GradientNoiseOverlay
import com.weatherpossum.app.presentation.components.CardHeader

@Composable
fun ExtrasScreenContent(
    weatherViewModel: WeatherViewModel = koinViewModel(),
    extendedForecastViewModel: ExtendedForecastViewModel = viewModel(),
    moonViewModel: MoonViewModel = koinViewModel(),
    hurricaneViewModel: HurricaneViewModel = koinViewModel()
) {
    val userName by weatherViewModel.userName.collectAsState()
    val synopsis by weatherViewModel.synopsis.collectAsState()
    val extendedForecast by extendedForecastViewModel.forecast.collectAsState()
    val isLoading by extendedForecastViewModel.isLoading.collectAsState()
    val error by extendedForecastViewModel.error.collectAsState()
    val moonState by moonViewModel.uiState.collectAsState()
    val hurricaneState by hurricaneViewModel.uiState.collectAsState()

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

        // ───────────────────────────────────────────────────────────────────
        // MOON PHASE – styled card
        // ───────────────────────────────────────────────────────────────────
        when (val state = moonState) {
            is MoonUiState.Success -> {
                MoonPhaseCard(state.moonData)
            }
            is MoonUiState.Loading -> {
                LoadingGradientCard(
                    title = "Moon Phase",
                    gradient = Brush.verticalGradient(listOf(Color(0xFF0E1E3A), Color(0xFF1F3A6B)))
                )
            }
            is MoonUiState.Error -> {
                ErrorGradientCard(
                    title = "Moon Phase",
                    message = state.message
                )
            }
        }

        // ───────────────────────────────────────────────────────────────────
        // EXTENDED FORECAST
        // ───────────────────────────────────────────────────────────────────
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

        // ───────────────────────────────────────────────────────────────────
        // HURRICANE UPDATES – styled card
        // ───────────────────────────────────────────────────────────────────
        when (val state = hurricaneState) {
            is HurricaneUiState.Success -> {
                val tropicalOutlook = state.hurricaneData.tropicalOutlook

                when {
                    !tropicalOutlook.isNullOrBlank() -> {
                        // Show only the outlook card (which includes active systems)
                        HurricaneOutlookCard(
                            outlook = parseMarkdownText(tropicalOutlook)
                        )
                    }
                    else -> {
                        HurricaneNeutralCard()
                    }
                }
            }
            is HurricaneUiState.Loading -> {
                LoadingGradientCard(
                    title = "Hurricane Updates",
                    gradient = Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFFFC168)))
                )
            }
            is HurricaneUiState.Error -> {
                ErrorGradientCard(
                    title = "Hurricane Updates",
                    message = "Unable to load hurricane data"
                )
            }
        }
    }
}

/* ========================================================================== */
/*  MOON PHASE CARD                                                           */
/* ========================================================================== */

@Composable
private fun MoonPhaseCard(data: MoonData) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF0E1E3A), Color(0xFF1F3A6B))) // deep night blue
    val on = Color.White

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
                // Header
                CardHeader(
                    title = "MOON PHASE",
                    endContent = {
                        Icon(
                            painter = painterResource(id = data.iconResId),
                            contentDescription = "Moon",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(44.dp)
                        )
                    },
                    onColor = on
                )

                // Two-column details
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoonDetail(label = "Illumination", value = "${(data.illumination * 100).toInt()}%", on)
                        MoonDetail(label = "Phase", value = MoonData.formatMoonPhase(data.phase), on)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Pretty chips for times
                        Pill(label = "Moonrise", value = data.moonrise, onColor = on)
                        Pill(label = "Moonset", value = data.moonset, onColor = on)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoonDetail(label: String, value: String, onColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            label,
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = onColor.copy(alpha = 0.95f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Pill(label: String, value: String, onColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            label,
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = onColor.copy(alpha = 0.95f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

/* ========================================================================== */
/*  HURRICANE CARDS                                                           */
/* ========================================================================== */

@Composable
private fun HurricaneActiveCard(
    count: Int,
    storms: List<Pair<String, String>> // name to category
) {
    // Warning gradient (amber -> red)
    val gradient = Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFFF7A59)))
    val on = Color(0xFF2B2B2B)
    
    // Hurricane Lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.hurricane))
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
                    title = "HURRICANE UPDATES",
                    subtitle = "$count active storm${if (count > 1) "s" else ""} in the Atlantic",
                    endContent = {
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
                    },
                    onColor = on
                )

                storms.forEachIndexed { i, (name, cat) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7A1B1B).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (i + 1).toString(),
                                color = on,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, color = on, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(cat, color = on.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HurricaneOutlookCard(outlook: AnnotatedString) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A90E2), Color(0xFF1E3A8A))) // darker info blue
    val on = Color.White
    
    // Hurricane Lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.hurricane))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    
    // Parse the outlook text into sections
    val outlookText = outlook.toString()
    val activeSystems = parseActiveSystems(outlookText)
    val easternTropical = parseEasternTropical(outlookText)
    val formationChances = parseFormationChances(outlookText)
    
    // Parse individual systems from the outlook text
    val individualSystems = parseIndividualSystems(outlookText)
    
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
                    title = "HURRICANE OUTLOOK",
                    endContent = {
                        Box(
                            modifier = Modifier.size(40.dp),
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

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Individual Systems - each in its own container
                    individualSystems.forEach { system ->
                        HurricaneSystemContainer(
                            system = system,
                            onColor = on
                        )
                    }
                    
                    // Fallback: Show original sections if no individual systems found
                    if (individualSystems.isEmpty()) {
                        if (activeSystems.isNotBlank()) {
                            HurricaneSectionBlock(
                                title = "Active Systems",
                                content = activeSystems,
                                onColor = on
                            )
                        }
                        
                        if (easternTropical.isNotBlank()) {
                            HurricaneSectionBlock(
                                title = "Eastern Tropical Atlantic",
                                content = easternTropical,
                                onColor = on,
                                formationChances = formationChances
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/*  HURRICANE SECTION BLOCK - Square container for each section              */
/* ========================================================================== */

@Composable
private fun HurricaneSectionBlock(
    title: String,
    content: String,
    onColor: Color,
    formationChances: List<Pair<String, String>> = emptyList()
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.10f),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            // Section title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = onColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(8.dp))

                    // Section content (remove formation chances since they're shown in pills)
                    val cleanContent = if (formationChances.isNotEmpty()) {
                        removeFormationChancesFromText(content)
                    } else {
                        content
                    }
            Text(
                text = cleanContent,
                color = onColor.copy(alpha = 0.98f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            // Formation chances in pill containers (only for Eastern Tropical Atlantic)
            if (formationChances.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    formationChances.forEach { (label, value) ->
                        Pill(label = label, value = value, onColor = onColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun HurricaneNeutralCard() {
    val gradient = Brush.verticalGradient(listOf(Color(0xFFE1F6FF), Color(0xFFCBE8FF)))
    val on = Color(0xFF1E3A5F)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "HURRICANE UPDATES",
                    color = on,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "No active storms in the Atlantic",
                    color = on.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/* ========================================================================== */
/*  HURRICANE SYSTEM CONTAINER - Individual system with formation chances    */
/* ========================================================================== */

data class HurricaneSystem(
    val title: String,
    val content: String,
    val formationChances: List<Pair<String, String>>
)

@Composable
private fun HurricaneSystemContainer(
    system: HurricaneSystem,
    onColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.10f),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            // System title
            Text(
                text = system.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = onColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(8.dp))

            // System content (remove formation chances since they're shown in pills)
            val cleanContent = if (system.formationChances.isNotEmpty()) {
                removeFormationChancesFromText(system.content)
            } else {
                system.content
            }
            Text(
                text = cleanContent,
                color = onColor.copy(alpha = 0.98f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            // Formation chances in pill containers
            if (system.formationChances.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    system.formationChances.forEach { (label, value) ->
                        Pill(label = label, value = value, onColor = onColor)
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/*  HELPER FUNCTIONS                                                          */
/* ========================================================================== */

private fun parseIndividualSystems(text: String): List<HurricaneSystem> {
    val systems = mutableListOf<HurricaneSystem>()
    
    // Parse Active Systems section
    val activeSystemsRegex = Regex("Active Systems:(.*?)(?=Central and Western Tropical Atlantic|Eastern Tropical Atlantic|East of the Leeward Islands|\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    val activeMatch = activeSystemsRegex.find(text)
    if (activeMatch != null) {
        val activeContent = activeMatch.groupValues[1].trim()
        if (activeContent.isNotBlank()) {
            systems.add(HurricaneSystem(
                title = "Active Systems",
                content = activeContent,
                formationChances = emptyList()
            ))
        }
    }
    
    // Parse Central and Western Tropical Atlantic (AL93)
    val centralWesternRegex = Regex("Central and Western Tropical Atlantic \\(AL93\\):(.*?)(?=East of the Leeward Islands|\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    val centralMatch = centralWesternRegex.find(text)
    if (centralMatch != null) {
        val centralContent = centralMatch.groupValues[1].trim()
        if (centralContent.isNotBlank()) {
            val formationChances = parseFormationChancesForSystem(centralContent)
            systems.add(HurricaneSystem(
                title = "Central and Western Tropical Atlantic (AL93)",
                content = centralContent,
                formationChances = formationChances
            ))
        }
    }
    
    // Parse East of the Leeward Islands (AL94)
    val eastLeewardRegex = Regex("East of the Leeward Islands \\(AL94\\):(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    val eastMatch = eastLeewardRegex.find(text)
    if (eastMatch != null) {
        val eastContent = eastMatch.groupValues[1].trim()
        if (eastContent.isNotBlank()) {
            val formationChances = parseFormationChancesForSystem(eastContent)
            systems.add(HurricaneSystem(
                title = "East of the Leeward Islands (AL94)",
                content = eastContent,
                formationChances = formationChances
            ))
        }
    }
    
    // Parse Eastern Tropical Atlantic (fallback for other systems)
    val easternTropicalRegex = Regex("Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    val easternMatch = easternTropicalRegex.find(text)
    if (easternMatch != null) {
        val easternContent = easternMatch.groupValues[1].trim()
        if (easternContent.isNotBlank() && systems.none { it.title.contains("Eastern Tropical Atlantic") }) {
            val formationChances = parseFormationChancesForSystem(easternContent)
            systems.add(HurricaneSystem(
                title = "Eastern Tropical Atlantic",
                content = easternContent,
                formationChances = formationChances
            ))
        }
    }
    
    return systems
}

private fun parseFormationChancesForSystem(systemText: String): List<Pair<String, String>> {
    val formationChances = mutableListOf<Pair<String, String>>()
    val seenLabels = mutableSetOf<String>()
    
    // Look for formation chance patterns - handle various formats
    val patterns = listOf(
        // Pattern with asterisk: "* Formation chance through 48 hours...low...10 percent."
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        // Pattern without asterisk (fallback)
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        // Pattern with % instead of "percent"
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE)
    )
    
    patterns.forEach { pattern ->
        val matches = pattern.findAll(systemText)
        matches.forEach { matchResult ->
            val timeframe = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2] // hours or days
            val level = matchResult.groupValues[3]
            val percentage = matchResult.groupValues[4]
            
            // Use the actual unit from the source text
            val label = "Formation chance through $timeframe $unit"
            
            // Only add if we haven't seen this label before
            if (!seenLabels.contains(label)) {
                seenLabels.add(label)
                
                // Capitalize the first letter of the level
                val capitalizedLevel = level.replaceFirstChar { it.uppercase() }
                val value = "$capitalizedLevel ($percentage%)"
                formationChances.add(Pair(label, value))
            }
        }
    }
    
    return formationChances
}

private fun parseActiveSystems(text: String): String {
    val activeSystemsRegex = Regex("Active Systems:(.*?)(?=Eastern Tropical Atlantic|$)", RegexOption.DOT_MATCHES_ALL)
    val match = activeSystemsRegex.find(text)
    return match?.groupValues?.get(1)?.trim() ?: ""
}

private fun parseEasternTropical(text: String): String {
    val easternRegex = Regex("Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    val match = easternRegex.find(text)
    return match?.groupValues?.get(1)?.trim() ?: ""
}

private fun parseFormationChances(text: String): List<Pair<String, String>> {
    val formationChances = mutableListOf<Pair<String, String>>()
    val seenLabels = mutableSetOf<String>() // Track seen labels to avoid duplicates
    
    // Look for formation chance patterns - handle various formats
    val patterns = listOf(
        // Pattern with asterisk: "* Formation chance through 48 hours...low...10 percent."
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        // Pattern without asterisk (fallback)
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+) percent", RegexOption.IGNORE_CASE),
        // Pattern with % instead of "percent"
        Regex("\\*\\s*Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (hours?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE),
        Regex("Formation chance through (\\d+) (days?)\\.\\.\\.(\\w+)\\.\\.\\.(\\d+)%", RegexOption.IGNORE_CASE)
    )
    
    patterns.forEach { pattern ->
        val matches = pattern.findAll(text)
        matches.forEach { matchResult ->
            val timeframe = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2] // hours or days
            val level = matchResult.groupValues[3]
            val percentage = matchResult.groupValues[4]
            
            // Use the actual unit from the source text
            val label = "Formation chance through $timeframe $unit"
            
            // Only add if we haven't seen this label before
            if (!seenLabels.contains(label)) {
                seenLabels.add(label)
                
                // Capitalize the first letter of the level
                val capitalizedLevel = level.replaceFirstChar { it.uppercase() }
                val value = "$capitalizedLevel ($percentage%)"
                formationChances.add(Pair(label, value))
            }
        }
    }
    
    return formationChances
}

private fun removeFormationChancesFromText(text: String): String {
    var cleanText = text
    
    // Remove formation chance patterns - handle both asterisk and non-asterisk formats
    val patterns = listOf(
        // Pattern with asterisk and ellipsis: "* Formation chance through 48 hours...low...10 percent."
        Regex("\\*\\s*Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+ percent\\.?", RegexOption.IGNORE_CASE),
        Regex("\\*\\s*Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+%\\.?", RegexOption.IGNORE_CASE),
        // Pattern without asterisk (fallback)
        Regex("Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+ percent\\.?", RegexOption.IGNORE_CASE),
        Regex("Formation chance through \\d+ (hours?|days?)\\.\\.\\.\\w+\\.\\.\\.\\d+%\\.?", RegexOption.IGNORE_CASE)
    )
    
    patterns.forEach { pattern ->
        cleanText = cleanText.replace(pattern, "")
    }
    
    // Clean up extra whitespace and line breaks
    cleanText = cleanText.replace(Regex("\\n\\s*\\n"), "\n\n")
    cleanText = cleanText.replace(Regex("\\s+"), " ")
    
    // Remove duplicate periods and trailing periods
    cleanText = cleanText.replace(Regex("\\.\\s*\\.+"), ".")
    cleanText = cleanText.replace(Regex("\\.\\s*$"), "") // Remove trailing period
    
    cleanText = cleanText.trim()
    
    return cleanText
}

private fun parseTimestampAndText(text: String): Pair<String?, String> {
    // Look for timestamp patterns like "800 PM EDT Sun Sep 14 2025"
    val timestampPattern = Regex("(\\d+\\s+(AM|PM)\\s+EDT\\s+\\w+\\s+\\w+\\s+\\d+\\s+\\d+)", RegexOption.IGNORE_CASE)
    val timestampMatch = timestampPattern.find(text)
    
    val timestamp = timestampMatch?.groupValues?.get(1)
    val mainText = if (timestamp != null) {
        text.replace(timestampPattern, "").trim()
    } else {
        text
    }
    
    // Clean up duplicate periods in the main text
    val cleanedMainText = mainText.replace(Regex("\\.\\s*\\.+"), ".")
    
    return Pair(timestamp, cleanedMainText)
}

/* ========================================================================== */
/*  GENERIC LOADING / ERROR CARDS (gradient for consistency)                  */
/* ========================================================================== */

@Composable
private fun LoadingGradientCard(title: String, gradient: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorGradientCard(title: String, message: String) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFFFFC1C1), Color(0xFFFF8C8C)))
    val on = Color(0xFF2B2B2B)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(message, color = on.copy(alpha = 0.9f), fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}


/* ========================================================================== */
/*  MARKDOWN-ish helper (unchanged)                                           */
/* ========================================================================== */

@Composable
private fun parseMarkdownText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split(Regex("BOLD_START:(.*?)BOLD_END"))
        for (i in parts.indices) {
            if (i % 2 == 0) {
                append(parts[i])
            } else {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(parts[i])
                }
            }
        }
    }
}
