package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.ui.theme.WeatherPossumGlass
import com.weatherpossum.app.ui.theme.WeatherPossumMotion
import com.weatherpossum.app.ui.theme.backdropGlassBlur
import com.weatherpossum.app.ui.theme.backdropNavGlassBlur
import kotlinx.coroutines.delay

private object StaggeredRevealState {
    val revealedKeys = mutableSetOf<Any>()
}

/**
 * Staggered fade + lift entrance for cards and sections.
 * Each [key] animates at most once per app session so scroll/tab changes do not replay it.
 */
@Composable
fun StaggeredReveal(
    key: Any,
    modifier: Modifier = Modifier,
    index: Int = 0,
    content: @Composable () -> Unit
) {
    val alreadyRevealed = key in StaggeredRevealState.revealedKeys
    var visible by remember(key) { mutableStateOf(alreadyRevealed) }

    LaunchedEffect(key) {
        if (alreadyRevealed) {
            visible = true
            return@LaunchedEffect
        }
        delay(WeatherPossumMotion.staggerDelay(index).toLong())
        visible = true
        StaggeredRevealState.revealedKeys.add(key)
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(WeatherPossumMotion.enterTween(index)) +
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 6 },
                animationSpec = WeatherPossumMotion.enterTween(index)
            ) +
            scaleIn(
                initialScale = 0.97f,
                animationSpec = WeatherPossumMotion.fluidSpring()
            ),
        exit = fadeOut(WeatherPossumMotion.exitTween()) +
            scaleOut(targetScale = 0.98f, animationSpec = WeatherPossumMotion.exitTween())
    ) {
        content()
    }
}

/** Keeps tab content composed (and stateful) while hiding it from layout when inactive. */
fun Modifier.keepTabComposed(visible: Boolean): Modifier = zIndex(if (visible) 1f else 0f)
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        if (visible) {
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        } else {
            // Measure to stay composed, but do not place — avoids drawing a ghost overlay.
            layout(0, 0) {}
        }
    }

@Composable
fun GradientNoiseOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.06f
) {
    Canvas(modifier.fillMaxSize()) {
        val step = 4
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

@Composable
fun ParallaxCard(
    index: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scrollPosition by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
    }
    Box(
        modifier = modifier.parallaxScrollEffect(
            index = index,
            firstVisibleIndex = scrollPosition.first,
            scrollOffset = scrollPosition.second
        )
    ) {
        content()
    }
}

fun Modifier.liquidGlassSurface(
    tintTop: Color,
    tintBottom: Color,
    isDarkMode: Boolean,
    cornerRadius: Dp = WeatherPossumGlass.cardCornerRadius
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    val frost = WeatherPossumGlass.frostBrush(isDarkMode)
    val tint = WeatherPossumGlass.tintBrush(tintTop, tintBottom, isDarkMode)
    val rim = WeatherPossumGlass.rimBrush(tintTop, tintBottom, isDarkMode)

    this
        .backdropGlassBlur(shape, tintTop, tintBottom, isDarkMode)
        .background(frost)
        .background(tint)
        .drawBehind {
            WeatherPossumGlass.drawSpecularSheen(this, cornerRadius, isDarkMode)
            WeatherPossumGlass.drawInnerRim(this, cornerRadius, isDarkMode)
        }
        .border(width = 1.dp, brush = rim, shape = shape)
}

/** Higher-opacity glass for the floating pill nav bar (legibility over scroll content). */
fun Modifier.liquidGlassNavSurface(
    tintTop: Color,
    tintBottom: Color,
    isDarkMode: Boolean,
    cornerRadius: Dp = WeatherPossumGlass.cardCornerRadius
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    val rim = WeatherPossumGlass.rimBrush(tintTop, tintBottom, isDarkMode)

    this
        .backdropNavGlassBlur(shape, tintTop, tintBottom, isDarkMode)
        .background(WeatherPossumGlass.navScrimColor(isDarkMode))
        .background(WeatherPossumGlass.navFrostBrush(isDarkMode))
        .background(WeatherPossumGlass.navTintBrush(tintTop, tintBottom, isDarkMode))
        .drawBehind {
            WeatherPossumGlass.drawSpecularSheen(this, cornerRadius, isDarkMode)
            WeatherPossumGlass.drawInnerRim(this, cornerRadius, isDarkMode)
        }
        .border(width = 1.dp, brush = rim, shape = shape)
}

fun Modifier.navSelectedSurface(
    tintTop: Color,
    tintBottom: Color,
    isDarkMode: Boolean,
    cornerRadius: Dp = 26.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val rim = WeatherPossumGlass.rimBrush(tintTop, tintBottom, isDarkMode)

    return this
        .clip(shape)
        .background(WeatherPossumGlass.navSelectedBrush(tintTop, tintBottom, isDarkMode))
        .drawBehind {
            WeatherPossumGlass.drawSpecularSheen(this, cornerRadius, isDarkMode)
            WeatherPossumGlass.drawInnerRim(this, cornerRadius, isDarkMode)
        }
        .border(width = 1.dp, brush = rim, shape = shape)
}

fun Modifier.glassInset(
    onColor: Color,
    isDarkMode: Boolean,
    cornerRadius: Dp = 20.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(WeatherPossumGlass.insetBrush(onColor, isDarkMode))
        .border(
            width = 1.dp,
            color = onColor.copy(alpha = if (isDarkMode) 0.18f else 0.14f),
            shape = shape
        )
}

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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                color = onColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = onColor.copy(0.92f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier.padding(start = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            endContent?.invoke()
        }
    }
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = onColor.copy(0.22f), thickness = 1.dp)
    Spacer(Modifier.height(6.dp))
}

@Composable
fun ExpressiveCard(
    style: CardGradientStyle,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    elevation: Dp = WeatherPossumDimens.cardElevation,
    contentPadding: PaddingValues = PaddingValues(WeatherPossumDimens.cardPadding),
    onColorOverride: Color? = null,
    showNoise: Boolean = true,
    header: @Composable (onColor: Color) -> Unit = {},
    content: @Composable ColumnScope.(onColor: Color) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val (tintTop, tintBottom) = WeatherPossumGlass.colorsForStyle(style, isDarkMode)
    val resolvedOnColor = onColorOverride ?: WeatherPossumGlass.onColorForStyle(style, isDarkMode)

    LiquidGlassCard(
        tintTop = tintTop,
        tintBottom = tintBottom,
        onColor = resolvedOnColor,
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        showNoise = showNoise,
        header = header,
        content = content
    )
}

@Composable
fun ExpressiveCard(
    gradientTop: Color,
    gradientBottom: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    elevation: Dp = WeatherPossumDimens.cardElevation,
    contentPadding: PaddingValues = PaddingValues(WeatherPossumDimens.cardPadding),
    onColorOverride: Color? = null,
    showNoise: Boolean = true,
    header: @Composable (onColor: Color) -> Unit = {},
    content: @Composable ColumnScope.(onColor: Color) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val onColor = onColorOverride ?: WeatherPossumGlass.onColorForTint(gradientTop, gradientBottom, isDarkMode)

    LiquidGlassCard(
        tintTop = gradientTop,
        tintBottom = gradientBottom,
        onColor = onColor,
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        showNoise = showNoise,
        header = header,
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    tintTop: Color,
    tintBottom: Color,
    onColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    elevation: Dp = WeatherPossumDimens.cardElevation,
    contentPadding: PaddingValues = PaddingValues(WeatherPossumDimens.cardPadding),
    showNoise: Boolean = true,
    header: @Composable (onColor: Color) -> Unit = {},
    content: @Composable ColumnScope.(onColor: Color) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val cornerRadius = WeatherPossumDimens.cardCornerRadius
    val glowColor = remember(tintTop, tintBottom) {
        Color(
            red = (tintTop.red + tintBottom.red) / 2f,
            green = (tintTop.green + tintBottom.green) / 2f,
            blue = (tintTop.blue + tintBottom.blue) / 2f
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = glowColor.copy(alpha = if (isDarkMode) 0.35f else 0.2f),
                spotColor = glowColor.copy(alpha = if (isDarkMode) 0.28f else 0.16f)
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .liquidGlassSurface(tintTop, tintBottom, isDarkMode, cornerRadius)
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            if (showNoise) {
                GradientNoiseOverlay()
            }
            Column {
                header(onColor)
                content(onColor)
            }
        }
    }
}

@Composable
fun DetailChip(
    label: String,
    value: String,
    onColor: Color,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .glassInset(onColor, isDarkMode, cornerRadius = 12.dp)
            .padding(
                horizontal = WeatherPossumDimens.detailChipHorizontalPadding,
                vertical = WeatherPossumDimens.detailChipVerticalPadding
            )
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = onColor.copy(alpha = 0.95f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
