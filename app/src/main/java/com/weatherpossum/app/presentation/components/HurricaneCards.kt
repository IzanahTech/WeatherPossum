package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.domain.hurricane.FormationChance
import com.weatherpossum.app.domain.hurricane.HurricaneFormationChanceParser
import com.weatherpossum.app.ui.theme.WeatherPossumColors
import com.weatherpossum.app.ui.theme.WeatherPossumDimens

data class HurricaneSystem(
    val title: String,
    val content: String,
    val formationChances: List<FormationChance>
)

private data class HurricaneOutlookParsed(
    val activeSystems: String,
    val easternTropical: String,
    val formationChances: List<FormationChance>,
    val individualSystems: List<HurricaneSystem>
)

@Composable
fun HurricaneOutlookCard(
    outlookText: String,
    forecaster: String? = null,
    issued: String? = null
) {
    val parsed = remember(outlookText) {
        HurricaneOutlookParsed(
            activeSystems = parseActiveSystems(outlookText),
            easternTropical = parseEasternTropical(outlookText),
            formationChances = HurricaneFormationChanceParser.parse(outlookText),
            individualSystems = parseIndividualSystems(outlookText)
        )
    }

    ExpressiveCard(
        style = CardGradientStyle.HurricaneOutlook,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_hurricane_outlook_title),
                endContent = {
                    Box(
                        modifier = Modifier.size(WeatherPossumDimens.iconMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = WeatherIconType.HURRICANE,
                            modifier = Modifier.fillMaxSize(),
                            color = onColor
                        )
                    }
                },
                onColor = onColor
            )
        }
    ) { onColor ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            parsed.individualSystems.forEach { system ->
                HurricaneSystemContainer(system = system, onColor = onColor)
            }

            if (parsed.individualSystems.isEmpty()) {
                if (parsed.activeSystems.isNotBlank()) {
                    HurricaneSectionBlock(
                        title = stringResource(R.string.card_hurricane_active_systems),
                        content = parsed.activeSystems,
                        onColor = onColor
                    )
                }

                if (parsed.easternTropical.isNotBlank()) {
                    HurricaneSectionBlock(
                        title = stringResource(R.string.card_hurricane_eastern_tropical),
                        content = parsed.easternTropical,
                        onColor = onColor,
                        formationChances = parsed.formationChances
                    )
                }
            }
        }

        if (forecaster != null || issued != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (issued != null) {
                    HurricaneMetaChip(
                        label = stringResource(R.string.card_hurricane_issued),
                        emoji = "📅",
                        value = issued,
                        onColor = onColor
                    )
                }

                if (forecaster != null) {
                    HurricaneMetaChip(
                        label = stringResource(R.string.card_hurricane_forecaster),
                        emoji = "👨‍💼",
                        value = forecaster.removePrefix(
                            stringResource(R.string.hurricane_forecaster_prefix)
                        ),
                        onColor = onColor
                    )
                }
            }
        }
    }
}

@Composable
fun HurricaneNeutralCard() {
    ExpressiveCard(
        style = CardGradientStyle.HurricaneNeutral,
        header = { onColor ->
            CardHeader(
                title = stringResource(R.string.card_hurricane_updates_title),
                onColor = onColor
            )
        }
    ) { onColor ->
        Text(
            stringResource(R.string.card_hurricane_no_active_storms),
            color = onColor.copy(alpha = 0.9f),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HurricaneMetaChip(
    label: String,
    emoji: String,
    value: String,
    onColor: Color
) {
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassInset(onColor, isDarkMode, cornerRadius = 12.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = onColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = emoji,
                    fontSize = 16.sp,
                    color = onColor.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                color = onColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun HurricaneSectionBlock(
    title: String,
    content: String,
    onColor: Color,
    formationChances: List<FormationChance> = emptyList()
) {
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .glassInset(onColor, isDarkMode, cornerRadius = 20.dp)
            .padding(16.dp)
    ) {
        Column {
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

            val cleanContent = if (formationChances.isNotEmpty()) {
                HurricaneFormationChanceParser.stripFromText(content)
            } else {
                content
            }
            Text(
                text = cleanContent,
                color = onColor.copy(alpha = 0.98f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            if (formationChances.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    formationChances.forEach { chance ->
                        FormationChanceChip(chance = chance, onColor = onColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun HurricaneSystemContainer(
    system: HurricaneSystem,
    onColor: Color
) {
    val isDarkMode = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .glassInset(onColor, isDarkMode, cornerRadius = 20.dp)
            .padding(16.dp)
    ) {
        Column {
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

            val cleanContent = if (system.formationChances.isNotEmpty()) {
                HurricaneFormationChanceParser.stripFromText(system.content)
            } else {
                system.content
            }
            Text(
                text = cleanContent,
                color = onColor.copy(alpha = 0.98f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            if (system.formationChances.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    system.formationChances.forEach { chance ->
                        FormationChanceChip(chance = chance, onColor = onColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormationChanceChip(chance: FormationChance, onColor: Color) {
    val capitalizedLevel = chance.level.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
    DetailChip(
        label = stringResource(
            R.string.hurricane_formation_chance_label,
            chance.timeframe,
            chance.unit
        ),
        value = stringResource(
            R.string.hurricane_formation_chance_value,
            capitalizedLevel,
            chance.percentage
        ),
        onColor = onColor
    )
}

// ── Outlook text parsing ─────────────────────────────────────────────────────

private fun parseIndividualSystems(text: String): List<HurricaneSystem> {
    val systems = mutableListOf<HurricaneSystem>()

    val activeSystemsRegex = Regex(
        "Active Systems:(.*?)(?=Central and Western Tropical Atlantic|Eastern Caribbean Sea|Eastern Tropical Atlantic|\\$\\$|$)",
        RegexOption.DOT_MATCHES_ALL
    )
    activeSystemsRegex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }?.let { content ->
        systems.add(HurricaneSystem("Active Systems", content, emptyList()))
    }

    val centralWesternRegex = Regex(
        "Central and Western Tropical Atlantic \\(AL93\\):(.*?)(?=Eastern Caribbean Sea|Eastern Tropical Atlantic|\\$\\$|$)",
        RegexOption.DOT_MATCHES_ALL
    )
    centralWesternRegex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }?.let { content ->
        systems.add(
            HurricaneSystem(
                title = "Central and Western Tropical Atlantic (AL93)",
                content = content,
                formationChances = HurricaneFormationChanceParser.parse(content)
            )
        )
    }

    val easternCaribbeanRegex = Regex(
        "Eastern Caribbean Sea \\(AL94\\):(.*?)(?=Eastern Tropical Atlantic|\\$\\$|$)",
        RegexOption.DOT_MATCHES_ALL
    )
    easternCaribbeanRegex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }?.let { content ->
        systems.add(
            HurricaneSystem(
                title = "Eastern Caribbean Sea (AL94)",
                content = content,
                formationChances = HurricaneFormationChanceParser.parse(content)
            )
        )
    }

    val easternTropicalRegex = Regex(
        "Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)",
        RegexOption.DOT_MATCHES_ALL
    )
    easternTropicalRegex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }?.let { content ->
        if (systems.none { it.title.contains("Eastern Tropical Atlantic") }) {
            systems.add(
                HurricaneSystem(
                    title = "Eastern Tropical Atlantic",
                    content = content,
                    formationChances = HurricaneFormationChanceParser.parse(content)
                )
            )
        }
    }

    return systems
}

private fun parseActiveSystems(text: String): String {
    val activeSystemsRegex = Regex("Active Systems:(.*?)(?=Eastern Tropical Atlantic|$)", RegexOption.DOT_MATCHES_ALL)
    return activeSystemsRegex.find(text)?.groupValues?.get(1)?.trim().orEmpty()
}

private fun parseEasternTropical(text: String): String {
    val easternRegex = Regex("Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
    return easternRegex.find(text)?.groupValues?.get(1)?.trim().orEmpty()
}

