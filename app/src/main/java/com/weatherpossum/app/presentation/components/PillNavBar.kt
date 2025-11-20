package com.weatherpossum.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.weatherpossum.app.presentation.components.AnimatedWeatherIcon
import com.weatherpossum.app.presentation.components.WeatherIconType
import com.weatherpossum.app.ui.theme.Shapes

@Composable
fun PillNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Now", "Extras")
    val iconTypes = mapOf("Now" to WeatherIconType.PARTLY_CLOUDY, "Extras" to WeatherIconType.EXTRAS)

    // Material You Expressive theming
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    
    // Sizes
    val tabWidth: Dp = 160.dp
    val pillWidth = tabWidth * tabs.size
    val pillHeight = 72.dp
    val cornerShape = shapes.extraLarge // M3 extraLarge shape (e.g., 28.dp)

    // Expressive colors
    // Use an elevated surface color for better contrast/depth
    val baseSurfaceColor = colorScheme.surfaceColorAtElevation(3.dp) 
    val onSurfaceColor = colorScheme.onSurface
    val activeTabColor = colorScheme.primary
    val onActiveTabColor = colorScheme.onPrimary

    // 1. EXPRESSIVE MOTION: Bouncy, highly reactive indicator movement
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val highlightOffset by animateDpAsState(
        targetValue = tabWidth * selectedIndex,
        animationSpec = spring(
            // Low damping ratio for exaggerated oscillation (more bounce)
            dampingRatio = Spring.DampingRatioLowBouncy, 
            // Low stiffness for a slower, more dramatic movement
            stiffness = Spring.StiffnessLow 
        ),
        label = "highlightOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Material You Expressive navigation surface
        Surface(
            modifier = Modifier
                .width(pillWidth)
                .height(pillHeight)
                // Use higher manual shadow for expressive lift
                .shadow(
                    elevation = 12.dp, 
                    shape = cornerShape
                ),
            shape = cornerShape,
            color = baseSurfaceColor, // Use elevated surface color
            tonalElevation = 6.dp // High tonal elevation for visual depth
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .background(baseSurfaceColor)
                    .border( // Maintain a light border for definition
                        width = 1.dp,
                        color = colorScheme.outline.copy(alpha = 0.12f),
                        shape = cornerShape
                    )
            ) {
                // Material You Expressive indicator (animated)
                Box(
                    modifier = Modifier
                        .offset(x = highlightOffset)
                        .width(tabWidth)
                        .height(pillHeight)
                        .padding(8.dp) // Increased padding for visual lift and shape contrast
                        .zIndex(1f)
                ) {
                    // Indicator Surface (Primary color with expressive shape)
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shapes.large), // Contrastive shape to container
                        color = activeTabColor,
                        tonalElevation = 10.dp // Very high elevation for a 'popping' feel
                    ) {
                        // Gradient overlay for depth and vibrancy (Expressive touch)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            activeTabColor.copy(alpha = 0.9f),
                                            activeTabColor,
                                            activeTabColor.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )
                    }
                }

                // Tabs with Material You Expressive motion and typography
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val localInteractionSource = remember { MutableInteractionSource() }
                        val isTabPressed by localInteractionSource.collectIsPressedAsState()
                        
                        // 2. EXPRESSIVE MOTION for Pressed/Selected Tab Element
                        val targetScale = when {
                            isSelected -> 1.05f // Subtle lift on selected item
                            isTabPressed -> 0.95f // Gentle squash on press
                            else -> 1f
                        }

                        val elementScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy, // More reactive element movement
                                stiffness = Spring.StiffnessLow 
                            ),
                            label = "elementScale_$tab"
                        )

                        Column(
                            modifier = Modifier
                                .width(tabWidth)
                                .fillMaxHeight()
                                .graphicsLayer { // Apply the scale animation here
                                    scaleX = elementScale
                                    scaleY = elementScale
                                }
                                .clip(cornerShape)
                                .clickable(
                                    interactionSource = localInteractionSource,
                                    indication = null,
                                    onClick = { onTabSelected(tab) }
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon with expressive color shift
                            Box(
                                modifier = Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedWeatherIcon(
                                    type = iconTypes[tab]!!,
                                    modifier = Modifier.size(28.dp),
                                    // Use primary color for unselected tabs to increase vibrancy
                                    color = if (isSelected) onActiveTabColor else activeTabColor.copy(alpha = 0.7f) 
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            
                            // Label with Material You Expressive typography
                            Text(
                                text = tab.uppercase(),
                                style = if (isSelected) {
                                    // Use a heavier weight and slightly larger size when selected
                                    typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black, 
                                        letterSpacing = 1.0.sp 
                                    )
                                } else {
                                    typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                },
                                color = if (isSelected) {
                                    onActiveTabColor
                                } else {
                                    onSurfaceColor.copy(alpha = 0.7f)
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}