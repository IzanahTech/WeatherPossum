package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

    // Sizes
    val tabWidth: Dp = 148.dp
    val pillWidth = tabWidth * tabs.size
    val pillHeight = 64.dp
    val cornerShape = RoundedCornerShape(28.dp)

    // Theme-aware colors
    val dark = isSystemInDarkTheme()
    val glassBg = if (dark) Color(0x4DFFFFFF) else Color(0x99FFFFFF)     // more opaque glass
    val onGlass = if (dark) Color(0xFFECEFF4) else Color(0xFF1F2937)

    val borderBrush = if (dark) {
        Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF)))
    } else {
        Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x33FFFFFF)))
    }

    val indicatorBrush = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)))
    }

    // Indicator animation
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val highlightOffset by animateDpAsState(
        targetValue = tabWidth * selectedIndex,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "highlightOffset"
    )
    val indicatorElevation by animateDpAsState(
        targetValue = if (selectedIndex == 0) 8.dp else 10.dp,
        label = "indicatorElevation"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "labelAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glass pill
        Surface(
            modifier = Modifier
                .width(pillWidth)
                .height(pillHeight)
                .shadow(elevation = 14.dp, shape = cornerShape, clip = false),
            shape = cornerShape,
            color = Color.Transparent
        ) {
            // glass background + subtle gradient tint to match app
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                glassBg,
                                glassBg.copy(alpha = if (dark) 0.4f else 0.5f)
                            )
                        )
                    )
                    .border(width = 1.dp, brush = borderBrush, shape = cornerShape)
            ) {
                // Indicator (animated) – sits behind content but above glass bg
                Box(
                    modifier = Modifier
                        .offset(x = highlightOffset)
                        .width(tabWidth)
                        .height(pillHeight)
                        .padding(4.dp)
                        .zIndex(1f)
                ) {
                    // Rounded gradient chip
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(cornerShape)
                            .background(indicatorBrush)
                            .shadow(indicatorElevation, cornerShape, clip = false)
                    )
                }

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { _, tab ->
                        val isSelected = tab == selectedTab
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(lottieRes[tab]!!)
                        )
                        val progress by animateLottieCompositionAsState(
                            composition,
                            isPlaying = isSelected,
                            speed = if (isSelected) 1.2f else 0.6f
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            modifier = Modifier
                                .width(tabWidth)
                                .fillMaxHeight()
                                .clip(cornerShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onTabSelected(tab) }
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            // Label
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else onGlass.copy(alpha = 0.85f),
                                letterSpacing = 0.2.sp,
                                modifier = Modifier.alpha(labelAlpha)
                            )
                        }
                    }
                }
            }
        }
    }
}
