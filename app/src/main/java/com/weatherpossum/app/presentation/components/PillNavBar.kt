package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R

@Composable
fun PillNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Now", "Extras")
    val lottieRes = mapOf(
        "Now" to R.raw.sunny,
        "Extras" to R.raw.extras
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            tonalElevation = 12.dp,
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .width(280.dp) // Explicit pill width
                .height(64.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes[tab]!!))
                    val progress by animateLottieCompositionAsState(
                        composition,
                        isPlaying = isSelected,
                        speed = if (isSelected) 1.2f else 0.6f
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(tab) }
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0077BE) else Color.Gray
                        )
                    }
                }
            }
        }
    }
} 