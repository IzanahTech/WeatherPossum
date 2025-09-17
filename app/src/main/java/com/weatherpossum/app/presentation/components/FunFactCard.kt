package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R

@Composable
fun FunFactCard(facts: List<String>, modifier: Modifier = Modifier) {
    var currentFact by remember { mutableStateOf(facts.random()) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fact))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    // Unique gradient for fun facts - warm, educational theme
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6A4C93), // Purple
            Color(0xFF9A4C95)  // Pink-purple
        )
    )
    val onColor = Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                val newFact = facts.filter { it != currentFact }.randomOrNull() ?: currentFact
                currentFact = newFact
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .enhancedCardBackground(gradient)
                .padding(20.dp)
        ) {
            GradientNoiseOverlay()
            
            Column {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DID YOU KNOW?",
                            color = onColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to discover more!",
                            color = onColor.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Lottie animation icon
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Divider
                HorizontalDivider(
                    color = onColor.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Fact content
                Text(
                    text = currentFact,
                    color = onColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
} 