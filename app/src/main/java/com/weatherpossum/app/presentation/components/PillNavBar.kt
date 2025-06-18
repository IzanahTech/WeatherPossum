package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R

@Composable
fun PillNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Now", "Extras")
    val lottieRes = mapOf("Now" to R.raw.sunny, "Extras" to R.raw.extras)
    val tabWidth = 140.dp
    val pillWidth = tabWidth * tabs.size
    val pillHeight = 64.dp
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val highlightOffset by animateDpAsState(
        targetValue = tabWidth * selectedIndex,
        label = "highlightOffset"
    )

    val animatedCornerPercent by animateFloatAsState(
        targetValue = if (selectedTab == "Now") 50f else 50f,
        label = "cornerRadiusAnimation"
    )

    val cornerShape = RoundedCornerShape(percent = animatedCornerPercent.toInt())

    val animatedShadow by animateDpAsState(
        targetValue = if (selectedTab == "Now") 12.dp else 16.dp,
        label = "shadowElevationAnimation"
    )

    val isDark = isSystemInDarkTheme()
    val outlineColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(cornerShape)
                .background(MaterialTheme.colorScheme.surface)
                .width(pillWidth)
                .height(pillHeight)
                .shadow(animatedShadow, cornerShape, clip = false)
        ) {
            // Highlight background
            Box(
                modifier = Modifier
                    .offset(x = highlightOffset)
                    .width(tabWidth)
                    .height(pillHeight)
                    .clip(cornerShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = cornerShape
                    )
                    .zIndex(1f)
            )

            Surface(
                shape = cornerShape,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                color = Color.Transparent,
                modifier = Modifier
                    .width(pillWidth)
                    .height(pillHeight)
                    .zIndex(0f)
                    .border(2.dp, outlineColor, cornerShape)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxSize()
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes[tab]!!))
                        val progress by animateLottieCompositionAsState(
                            composition,
                            isPlaying = isSelected,
                            speed = if (isSelected) 1.2f else 0.6f
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(tabWidth)
                                .fillMaxHeight()
                                .clip(cornerShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = rememberRipple(
                                        bounded = true,
                                        radius = 36.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ),
                                    onClick = { onTabSelected(tab) }
                                )
                                .zIndex(2f)
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
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
