package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.weatherpossum.app.R
import com.weatherpossum.app.util.SunCalculator
import kotlin.math.pow

// sRGB -> linear
private fun srgbToLinear(c: Float): Double =
    if (c <= 0.04045f) (c / 12.92f).toDouble()
    else (((c + 0.055f) / 1.055f).toDouble()).pow(2.4)

// Relative luminance per WCAG (0..1)
private fun Color.relativeLuminance(): Double {
    val r = srgbToLinear(red)
    val g = srgbToLinear(green)
    val b = srgbToLinear(blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

// Contrast ratio between two colors (>= 1.0). Higher is better.
private fun contrastRatio(a: Color, b: Color): Double {
    val la = a.relativeLuminance()
    val lb = b.relativeLuminance()
    val (l1, l2) = if (la >= lb) la to lb else lb to la
    return (l1 + 0.05) / (l2 + 0.05)
}

// Choose an on-color (foreground) that maximizes contrast vs the background.
// You can tweak candidates to match your design system.
private fun pickOnColorFor(background: Color): Color {
    val dark = Color(0xFF1F2937) // slate-ish "ink"
    val light = Color.White
    return if (contrastRatio(light, background) >= contrastRatio(dark, background)) light else dark
}

// If your background is a gradient, approximate by averaging the two ends.
// You can also weight toward the center if needed.
private fun pickOnColorForGradient(top: Color, bottom: Color): Color {
    val avg = Color(
        red = (top.red + bottom.red) / 2f,
        green = (top.green + bottom.green) / 2f,
        blue = (top.blue + bottom.blue) / 2f,
        alpha = 1f
    )
    return pickOnColorFor(avg)
}

// Enforce minimum contrast (AA)
private fun ensureAA(on: Color, bg: Color): Color {
    val ratio = contrastRatio(on, bg)
    return if (ratio >= 4.5) on else {
        // If chosen on-color fails, flip to the other candidate.
        val other = if (on == Color.White) Color(0xFF1F2937) else Color.White
        if (contrastRatio(other, bg) > ratio) other else on
    }
}

@Composable
fun GreetingCard(
    userName: String?,
    synopsis: String?,
    modifier: Modifier = Modifier
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_good_morning)
        in 12..17 -> stringResource(R.string.greeting_good_afternoon)
        else -> stringResource(R.string.greeting_good_night)
    }

    val displayName = userName?.replaceFirstChar { it.uppercase() } ?: ""
    val personalizedGreeting = if (displayName.isNotBlank()) "$greeting, $displayName" else greeting

    // Daylight progress (0..100) using your calculator
    val sunProgressPercent = SunCalculator.calculateSunProgress().coerceIn(0, 100)
    val sunFrac = sunProgressPercent / 100f

    // Select greeting animation based on time of day (unchanged)
    val greetingAnimation = when (hour) {
        in 5..11 -> R.raw.gmorning
        in 12..17 -> R.raw.afternoon
        else -> R.raw.night
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(greetingAnimation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    // -------- Dynamic gradient driven by daylight fraction ----------
    val dawnTop = Color(0xFF0B1D3A); val dawnBottom = Color(0xFF27446E)     // pre-dawn
    val morningTop = Color(0xFF67B8F7); val morningBottom = Color(0xFF9CD3FF) // morning
    val noonTop = Color(0xFFFFE082); val noonBottom = Color(0xFFFFB74D)    // noon
    val sunsetTop = Color(0xFF7F7FD5); val sunsetBottom = Color(0xFF86A8E7) // dusk
    val nightTop = Color(0xFF0F172A); val nightBottom = Color(0xFF1E293B)   // night

    fun lerpColor(a: Color, b: Color, t: Float): Color =
        Color(
            red = a.red + (b.red - a.red) * t.coerceIn(0f, 1f),
            green = a.green + (b.green - a.green) * t.coerceIn(0f, 1f),
            blue = a.blue + (b.blue - a.blue) * t.coerceIn(0f, 1f),
            alpha = a.alpha + (b.alpha - a.alpha) * t.coerceIn(0f, 1f)
        )

    val (targetTop, targetBottom) = remember(sunFrac) {
        if (sunFrac <= 0.5f) {
            // 0..0.25 Dawn→Morning, 0.25..0.5 Morning→Noon
            if (sunFrac <= 0.25f) {
                val t = sunFrac / 0.25f
                lerpColor(dawnTop, morningTop, t) to lerpColor(dawnBottom, morningBottom, t)
            } else {
                val t = (sunFrac - 0.25f) / 0.25f
                lerpColor(morningTop, noonTop, t) to lerpColor(morningBottom, noonBottom, t)
            }
        } else {
            // 0.5..0.75 Noon→Sunset, 0.75..1 Sunset→Night
            if (sunFrac <= 0.75f) {
                val t = (sunFrac - 0.5f) / 0.25f
                lerpColor(noonTop, sunsetTop, t) to lerpColor(noonBottom, sunsetBottom, t)
            } else {
                val t = (sunFrac - 0.75f) / 0.25f
                lerpColor(sunsetTop, nightTop, t) to lerpColor(sunsetBottom, nightBottom, t)
            }
        }
    }

    val top by animateColorAsState(targetTop, label = "gradTop")
    val bottom by animateColorAsState(targetBottom, label = "gradBottom")
    val gradient = remember(top, bottom) { Brush.verticalGradient(listOf(top, bottom)) }

    // Contrast-aware foreground with WCAG compliance
    val onColor = remember(top, bottom) {
        val avg = pickOnColorForGradient(top, bottom)
        val avgBg = Color(
            red = (top.red + bottom.red) / 2f,
            green = (top.green + bottom.green) / 2f,
            blue = (top.blue + bottom.blue) / 2f,
            alpha = 1f
        )
        ensureAA(avg, avgBg)
    }

    // --- texture overlay kept from your version
    fun Modifier.addTextureOverlay(): Modifier = this.drawWithContent {
        drawContent()
        val noiseColor = onColor.copy(alpha = 0.03f)
        val size = size
        for (i in 0..50) {
            val x = (i * 7.3f) % size.width
            val y = (i * 11.7f) % size.height
            drawCircle(color = noiseColor, radius = 1.5f, center = Offset(x, y))
        }
        val overlayBrush = Brush.radialGradient(
            colors = listOf(Color.Transparent, onColor.copy(alpha = 0.05f)),
            radius = size.width * 0.8f,
            center = Offset(size.width * 0.3f, size.height * 0.3f)
        )
        drawRect(overlayBrush)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(22.dp))
                .addTextureOverlay()
                .padding(20.dp)
        ) {
            GradientNoiseOverlay() // your existing overlay

            Column {
                // Header with greeting and animation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = personalizedGreeting.uppercase(),
                            color = onColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day progress chip uses the same onColor for readability
                        DayProgressChip(percent = sunProgressPercent, on = onColor)
                    }

                    // Time-based Lottie animation
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Synopsis section (if available)
                if (!synopsis.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(
                        color = onColor.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SYNOPSIS",
                        color = onColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = synopsis,
                        color = onColor.copy(alpha = 0.95f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
