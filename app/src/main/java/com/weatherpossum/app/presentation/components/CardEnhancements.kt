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

/**
 * Adds a subtle noise overlay to kill banding on large gradient cards
 */
@Composable
fun GradientNoiseOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.06f
) {
    Canvas(modifier.fillMaxSize()) {
        val step = 6
        val rnd = java.util.Random(42)
        for (y in 0 until size.height.toInt() step step) {
            for (x in 0 until size.width.toInt() step step) {
                val a = (rnd.nextFloat() * alpha).coerceIn(0f, 0.12f)
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
 * Creates parallax and scale effects for cards based on scroll position
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
    val progress = ((index - first) * 1f - offset / 300.0f).coerceIn(-1f, 1f)
    val scale = 1f - (0.02f * progress.coerceAtLeast(0f))
    val translate = (progress * 8f)
    
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
 * Enhanced card modifier with gradient background, noise overlay, and translucent border
 */
fun Modifier.enhancedCardBackground(
    gradient: Brush,
    cornerRadius: androidx.compose.ui.unit.Dp = 22.dp,
    borderAlpha: Float = 0.10f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(gradient)
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = borderAlpha),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Standardized card header pattern for consistent styling across all cards
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title, 
                color = onColor, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle, 
                    color = onColor.copy(0.92f), 
                    fontSize = 14.sp
                )
            }
        }
        
        // Icon gets fixed space on the right
        Box(
            modifier = Modifier.padding(start = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            endContent?.invoke()
        }
    }
    Spacer(Modifier.height(6.dp))
    HorizontalDivider(color = onColor.copy(0.18f))
    Spacer(Modifier.height(4.dp))
}

/**
 * Day progress chip showing percentage of daylight passed
 */
@Composable
fun DayProgressChip(percent: Int, on: Color) {
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(on, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text("$percent% of daylight", color = on, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}