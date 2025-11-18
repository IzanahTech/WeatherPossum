package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.presentation.components.WavyLineProgressIndicator

/**
 * Adds a subtle noise overlay for Expressive texture and vibrancy.
 */
@Composable
fun GradientNoiseOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.10f // Increased default alpha for visibility
) {
    Canvas(modifier.fillMaxSize()) {
        val step = 4 // Smaller steps for denser noise pattern
        val rnd = java.util.Random(42)
        for (y in 0 until size.height.toInt() step step) {
            for (x in 0 until size.width.toInt() step step) {
                // Increased range for a more noticeable noise contrast
                val a = (rnd.nextFloat() * alpha).coerceIn(0f, 0.2f) 
                drawRect(
                    Color.White.copy(alpha = a),
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = Size(step.toFloat(), step.toFloat())
                )
            }
        }
    }
}

/**
 * Creates exaggerated parallax and scale effects for cards based on scroll position.
 */
@Composable
fun ParallaxCard(
    index: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val first = listState.firstVisibleItemIndex
    val offset = listState.firstVisibleItemScrollOffset
    // Exaggerated progress factor for more movement
    val progress = ((index - first) * 1f - offset / 200.0f).coerceIn(-1f, 1f) 
    
    // Increased scale and translation ranges for a dramatic 3D effect
    val scale = 1f - (0.04f * progress.coerceAtLeast(0f)) // Stronger squash/scale
    val translate = (progress * 16f) // Larger vertical translation
    
    Box(
        modifier = modifier.graphicsLayer {
            translationY = translate
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Enhanced card modifier with gradient background, noise overlay, and vibrant border.
 */
fun Modifier.enhancedCardBackground(
    gradient: Brush,
    // Exaggerated corner radius for expressive shapes
    cornerRadius: androidx.compose.ui.unit.Dp = 32.dp, 
    // Increased border alpha for a stronger 'glow' or defining line
    borderAlpha: Float = 0.25f 
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(gradient)
    .border(
        width = 2.dp, // Thicker border
        color = Color.White.copy(alpha = borderAlpha),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Standardized card header pattern with Assertive Typography.
 */
@Composable
fun CardHeader(
    title: String,
    subtitle: String? = null,
    endContent: @Composable (() -> Unit)? = null,
    onColor: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp) // More space
        ) {
            Text(
                title, 
                color = onColor, 
                fontSize = 20.sp, // Larger font size
                fontWeight = FontWeight.Black, // Assertive boldness
                letterSpacing = 1.0.sp, // More open spacing
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle, 
                    color = onColor.copy(0.95f), 
                    fontSize = 15.sp, // Larger subtitle
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Icon gets fixed space on the right (and is visually separated)
        Box(
            modifier = Modifier.padding(start = 16.dp), // More separation
            contentAlignment = Alignment.Center
        ) {
            endContent?.invoke()
        }
    }
    Spacer(Modifier.height(8.dp)) // More space after header block
    HorizontalDivider(color = onColor.copy(0.3f), thickness = 2.dp) // Thicker divider
    Spacer(Modifier.height(6.dp))
}

/**
 * Day progress chip showing percentage of daylight passed
 * Uses Material You Expressive wavy line indicator
 */
@Composable
fun DayProgressChip(percent: Int, on: Color) {
    Column(
        Modifier
            .clip(RoundedCornerShape(20.dp)) // Larger rounding
            .background(Color.White.copy(0.25f)) // Brighter background fill
            .padding(horizontal = 16.dp, vertical = 10.dp), // Increased padding
        verticalArrangement = Arrangement.spacedBy(8.dp) // More space
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$percent% DAYLIGHT PROGRESS", 
                color = on, 
                fontSize = 14.sp, // Larger text
                fontWeight = FontWeight.ExtraBold, // Bolder
                letterSpacing = 1.0.sp
            )
        }
        WavyLineProgressIndicator(
            progress = percent / 100f,
            color = on,
            backgroundColor = on.copy(alpha = 0.3f), // Brighter background
            height = 4.dp, // Thicker line
            modifier = Modifier.fillMaxWidth()
        )
    }
}