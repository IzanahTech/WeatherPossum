package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.presentation.components.AnimatedWeatherIcon
import com.weatherpossum.app.presentation.components.WeatherIconType
import com.weatherpossum.app.presentation.components.getWeatherIconType
import com.weatherpossum.app.presentation.ForecastDay

/* ──────────────────────────────────────────────────────────────────────────────
   EXTENDED FORECAST CARD – EXPRESSIVE MAKEOVER
   - Exaggerated asymmetrical shape
   - Saturated, vibrant color scheme
   - Assertive typography for headers and details
   - Subtle pop-up animation on individual forecast blocks
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun ExtendedForecastCard(forecast: List<ForecastDay>) {
    var expanded by remember { mutableStateOf(false) }

    // Use primary/tertiary container for a more vibrant Expressive color scheme
    val colorScheme = MaterialTheme.colorScheme
    val gradientTop = colorScheme.primaryContainer
    val gradientBottom = colorScheme.tertiaryContainer 
    val onGradient = colorScheme.onPrimaryContainer // Text color optimized for primaryContainer

    // Expressive, asymmetrical shape for a bold look
    val expressiveShape = remember {
        RoundedCornerShape(
            topStart = 32.dp,   // Very large roundness
            topEnd = 8.dp,      // Small roundness
            bottomStart = 8.dp, // Small roundness
            bottomEnd = 32.dp   // Very large roundness
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = expressiveShape,
        // Increased elevation for expressive lift
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                // Use a subtle diagonal gradient for visual interest
                .background(Brush.linearGradient(listOf(gradientTop, gradientBottom)))
                .fillMaxWidth()
                .clip(expressiveShape)
                .padding(horizontal = 20.dp, vertical = 18.dp) // Increased padding
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .heightIn(min = 200.dp)
            ) {
                // ── Header ───────────────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-DAY OUTLOOK", // Bolder, more assertive title
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onGradient,
                            letterSpacing = 1.0.sp // More open spacing
                        )
                        val dateLabel = forecast.firstOrNull()?.date.orEmpty()
                        if (dateLabel.isNotBlank()) {
                            Text(
                                text = "Starting: $dateLabel", // More descriptive subtitle
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = onGradient.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Header icon from the first day (larger for presence)
                    if (forecast.isNotEmpty()) {
                        val head = forecast.first()
                        val iconType = getWeatherIconType(head.weather)
                        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            AnimatedWeatherIcon(
                                type = iconType,
                                modifier = Modifier.fillMaxSize(),
                                color = onGradient // Match icon color to text for unified look
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                // Thicker, more expressive divider
                HorizontalDivider(color = onGradient.copy(alpha = 0.25f), thickness = 2.dp) 
                Spacer(Modifier.height(10.dp))

                // ── Collapsed vs Expanded content ───────────────────────────────
                if (forecast.isNotEmpty()) {
                    if (!expanded) {
                        // Collapsed: show the first day only
                        DayForecastBlock(
                            day = forecast.first(),
                            onTextColor = onGradient
                        )

                        Spacer(Modifier.height(14.dp)) // Increased spacing
                        // CENTERED expand chip
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CenterChip(
                                iconRotation = 0f,
                                onClick = { expanded = true },
                                // Use a vibrant secondary color for the chip background
                                background = colorScheme.secondary, 
                                tint = colorScheme.onSecondary // Text color for secondary container
                            )
                        }
                    } else {
                        Column {
                            // Expanded: show every day
                            forecast.forEach { day ->
                                // Each day block has a slight scale effect for an expressive pop
                                DayForecastBlock(
                                    day = day,
                                    onTextColor = onGradient,
                                    isExpanded = true // Flag for expanded state styling
                                )
                            }

                            Spacer(Modifier.height(16.dp)) // Increased spacing
                            // CENTERED collapse chip
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CenterChip(
                                    iconRotation = 180f,
                                    onClick = { expanded = false },
                                    background = colorScheme.secondary,
                                    tint = colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   DAY BLOCK – with Expressive styling and subtle animation
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun DayForecastBlock(
    day: ForecastDay,
    onTextColor: Color,
    isExpanded: Boolean = false
) {
    // Subtle scale animation when the expanded state changes
    val scaleFactor by animateFloatAsState(
        targetValue = if (isExpanded) 1.0f else 0.99f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, 
            stiffness = Spring.StiffnessLow // Expressive, brief pop
        ),
        label = "dayBlockScale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Reduced padding for compactness in expanded view
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            },
        shape = RoundedCornerShape(20.dp), // More rounded corners for sub-card
        color = Color.White.copy(alpha = 0.15f), // Slightly brighter overlay
        tonalElevation = 4.dp // Higher elevation for perceived lift
    ) {
        Column(Modifier.padding(14.dp)) { // Slightly reduced internal padding

            // Large, Expressive day header
            Text(
                text = day.date.uppercase(), 
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold, // Bolder
                letterSpacing = 0.5.sp,
                color = onTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = onTextColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(Modifier.height(6.dp))

            // Details row (no date inside)
            ForecastSummaryRow(
                day = day,
                onTextColor = onTextColor
            )
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────────
   SUMMARY ROW – details
   ────────────────────────────────────────────────────────────────────────────── */

@Composable
fun ForecastSummaryRow(
    day: ForecastDay,
    onTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Weather icon (Slightly smaller for detail density)
        val iconType = getWeatherIconType(day.weather)
        Box(
            modifier = Modifier.size(64.dp), 
            contentAlignment = Alignment.Center
        ) {
            AnimatedWeatherIcon(
                type = iconType,
                modifier = Modifier.fillMaxSize(),
                color = onTextColor // Match color
            )
        }

        Spacer(modifier = Modifier.width(16.dp)) // Slightly more separation

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), // Tighter spacing for density
            modifier = Modifier.weight(1f)
        ) {
            AlignedDetailRow(label = "Weather", value = day.weather, color = onTextColor)
            AlignedDetailRow(label = "Wind", value = day.wind, color = onTextColor)
            AlignedDetailRow(label = "Seas", value = day.seas, color = onTextColor)
            AlignedDetailRow(label = "Waves", value = day.waves, color = onTextColor)
        }
    }
}

/* Label/value with fixed label width for tidy columns */
@Composable
private fun AlignedDetailRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold, // Bolder label
            modifier = Modifier.width(78.dp)
        )
        Text(
            text = value,
            color = color.copy(alpha = 0.95f),
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

/* Centered circular chip for expand/collapse (Expressive Style) */
@Composable
private fun CenterChip(
    iconRotation: Float,
    onClick: () -> Unit,
    background: Color,
    tint: Color
) {
    // Exaggerate size and elevation for expressive focus
    Surface(
        modifier = Modifier
            .size(56.dp) // Larger size
            .clip(CircleShape)
            .clickable { onClick() },
        color = background,
        shadowElevation = 12.dp // Double the original elevation for strong lift
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(32.dp) // Larger icon
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}