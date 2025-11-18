package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.data.MoonData

@Composable
fun MoonPhaseCard(
    moonData: MoonData,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Expressive Asymmetrical Shape
    val expressiveShape = remember {
        RoundedCornerShape(
            topStart = 40.dp,   // Exaggerated
            topEnd = 16.dp,     // Medium
            bottomStart = 16.dp, // Medium
            bottomEnd = 40.dp  // Exaggerated
        )
    }

    // Expressive Colors: Primary container for deep, vibrant background
    val containerColor = colorScheme.primary 
    val contentColor = colorScheme.onPrimary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = expressiveShape,
        // Increased elevation for a dramatic, lifted presence
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp) 
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Increased padding for impact
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing
        ) {
            // Moon Icon with Expressive Size and Color
            Icon(
                painter = painterResource(id = moonData.iconResId),
                contentDescription = "Moon Phase",
                // Use a dynamic, highly saturated color for the moon tint
                tint = Color(0xFFFDD835), 
                modifier = Modifier
                    .size(80.dp) // Larger icon for presence
                    .graphicsLayer { rotationZ = -15f } // Slight rotation for expressive tilt
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Expressive Title Typography
                Text(
                    text = "LUNAR PHASES", // More assertive title
                    style = typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold, 
                        letterSpacing = 1.0.sp // Open letter spacing
                    ),
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Primary Phase Text
                Text(
                    text = MoonData.formatMoonPhase(moonData.phase).uppercase(),
                    style = typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black, // Assertive display of the phase
                        fontSize = 24.sp
                    ),
                    color = Color(0xFFFDD835) // Match moon color
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Moonrise and Moonset details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    MoonDetail(
                        label = "Moonrise",
                        time = moonData.moonrise,
                        color = contentColor,
                        icon = "🌙"
                    )
                    MoonDetail(
                        label = "Moonset",
                        time = moonData.moonset,
                        color = contentColor,
                        icon = "🌘"
                    )
                }
            }
        }
    }
}

// Helper Composable for Expressive Detail Row
@Composable
private fun MoonDetail(label: String, time: String?, color: Color, icon: String) {
    Column {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = color.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$icon $time",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp // Larger size for readability
            ),
            color = color
        )
    }
}