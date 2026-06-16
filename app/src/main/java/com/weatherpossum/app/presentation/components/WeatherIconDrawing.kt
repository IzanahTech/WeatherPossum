package com.weatherpossum.app.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class CloudShades(
    val highlight: Color,
    val body: Color,
    val shadow: Color,
    val underside: Color
)

internal data class SunPalette(
    val core: Color,
    val mid: Color,
    val edge: Color,
    val ray: Color,
    val glow: Color
)

internal data class LeafPalette(
    val fill: Color,
    val outline: Color,
    val vein: Color
)

internal fun Color.relativeLuminance(): Float {
    fun channel(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    val r = channel(red)
    val g = channel(green)
    val b = channel(blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

internal fun Color.isNeutralTint(): Boolean {
    if (this == Color.Unspecified) return true
    val lum = relativeLuminance()
    return lum > 0.82f || lum < 0.12f
}

/** Keeps card text colors readable while preserving explicit accent tints (e.g. fact icon gold). */
internal fun iconAccentColor(tint: Color, semantic: Color): Color = when {
    tint == Color.Unspecified -> semantic
    tint.isNeutralTint() -> semantic
    else -> tint
}

internal fun iconAccentColor(
    tint: Color,
    lightSemantic: Color,
    darkSemantic: Color,
    onDarkSurface: Boolean
): Color = iconAccentColor(tint, if (onDarkSurface) darkSemantic else lightSemantic)

internal fun tintSuggestsDarkSurface(tint: Color): Boolean =
    tint != Color.Unspecified && tint.isNeutralTint() && tint.relativeLuminance() > 0.5f

internal fun cloudShadesFromTint(tint: Color, onDarkSurface: Boolean = false): CloudShades {
    if (tint == Color.Unspecified || tint.isNeutralTint()) {
        return if (onDarkSurface) {
            CloudShades(
                highlight = Color(0xFFCFD8DC),
                body = Color(0xFF90A4AE),
                shadow = Color(0xFF546E7A),
                underside = Color(0xFF37474F)
            )
        } else {
            CloudShades(
                highlight = Color(0xFFECEFF1),
                body = Color(0xFF90A4AE),
                shadow = Color(0xFF607D8B),
                underside = Color(0xFF455A64)
            )
        }
    }
    return CloudShades(
        highlight = blendColor(tint, Color.White, 0.35f),
        body = tint,
        shadow = darkenColor(tint, 0.72f),
        underside = darkenColor(tint, 0.55f)
    )
}

internal fun sunPaletteFromTint(tint: Color, warm: Boolean = false): SunPalette {
    if (tint == Color.Unspecified) {
        return if (warm) {
            SunPalette(
                core = Color(0xFFFFF3E0),
                mid = Color(0xFFFF9800),
                edge = Color(0xFFE65100),
                ray = Color(0xFFFFCC80),
                glow = Color(0xFFFFAB40)
            )
        } else {
            SunPalette(
                core = Color(0xFFFFFDE7),
                mid = Color(0xFFFFCA28),
                edge = Color(0xFFFF8F00),
                ray = Color(0xFFFFE082),
                glow = Color(0xFFFFB300)
            )
        }
    }
    return SunPalette(
        core = blendColor(tint, Color.White, 0.45f),
        mid = tint,
        edge = darkenColor(tint, 0.82f),
        ray = blendColor(tint, Color.White, 0.2f),
        glow = tint
    )
}

internal fun DrawScope.drawRealisticSun(
    center: Offset,
    radius: Float,
    rotationDegrees: Float,
    palette: SunPalette,
    rayCount: Int = 12
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.glow.copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.65f
        ),
        radius = radius * 1.65f,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.glow.copy(alpha = 0.28f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.25f
        ),
        radius = radius * 1.25f,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.core, palette.mid, palette.edge),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.42f),
        radius = radius * 0.34f,
        center = Offset(center.x - radius * 0.22f, center.y - radius * 0.24f)
    )

    rotate(rotationDegrees, center) {
        for (i in 0 until rayCount) {
            val angle = (i * 360f / rayCount) * PI.toFloat() / 180f
            val rayScale = 0.18f + (i % 3) * 0.035f
            val inner = radius * 1.05f
            val outer = radius * (1f + rayScale)
            drawLine(
                color = palette.ray.copy(alpha = 0.35f + (i % 2) * 0.15f),
                start = Offset(center.x + inner * cos(angle), center.y + inner * sin(angle)),
                end = Offset(center.x + outer * cos(angle), center.y + outer * sin(angle)),
                strokeWidth = 2.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

internal fun DrawScope.drawLayeredCloud(
    center: Offset,
    width: Float,
    height: Float,
    offsetX: Float = 0f,
    shades: CloudShades
) {
    val cx = center.x + offsetX
    val cy = center.y

    drawOval(
        color = shades.underside.copy(alpha = 0.35f),
        topLeft = Offset(cx - width * 0.4f, cy + height * 0.12f),
        size = Size(width * 0.8f, height * 0.24f)
    )

    val puffs = listOf(
        Triple(-0.22f, -0.06f, 0.24f to shades.shadow),
        Triple(-0.08f, -0.18f, 0.28f to shades.body),
        Triple(0.12f, -0.14f, 0.26f to shades.body),
        Triple(0.24f, -0.04f, 0.22f to shades.highlight)
    )

    puffs.forEach { (xFactor, yFactor, radiusAndColor) ->
        val (radiusFactor, color) = radiusAndColor
        val puffCenter = Offset(cx + width * xFactor, cy + height * yFactor)
        val puffRadius = width * radiusFactor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColor(color, Color.White, 0.28f),
                    color,
                    darkenColor(color, 0.78f)
                ),
                center = puffCenter,
                radius = puffRadius
            ),
            radius = puffRadius,
            center = puffCenter
        )
    }

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                shades.shadow.copy(alpha = 0.45f),
                shades.underside.copy(alpha = 0.55f),
                shades.shadow.copy(alpha = 0.45f)
            ),
            startX = cx - width * 0.38f,
            endX = cx + width * 0.38f
        ),
        topLeft = Offset(cx - width * 0.38f, cy + height * 0.02f),
        size = Size(width * 0.76f, height * 0.18f)
    )
}

internal fun DrawScope.drawRainStreaks(
    center: Offset,
    cloudWidth: Float,
    progress: Float,
    dropColor: Color
) {
    val startY = center.y + cloudWidth * 0.08f
    val dropLength = size.height * 0.14f
    val columns = listOf(-0.28f, -0.1f, 0.08f, 0.26f)

    columns.forEachIndexed { index, xFactor ->
        val phase = (progress + index * 0.22f) % 1f
        val x = center.x + cloudWidth * xFactor
        val y = startY + phase * dropLength * 2.2f
        val alpha = (1f - phase).coerceIn(0.25f, 1f)
        drawLine(
            color = dropColor.copy(alpha = alpha),
            start = Offset(x, y),
            end = Offset(x - cloudWidth * 0.04f, y + dropLength),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = dropColor.copy(alpha = alpha * 0.85f),
            radius = 1.4.dp.toPx(),
            center = Offset(x - cloudWidth * 0.04f, y + dropLength)
        )
    }
}

internal fun DrawScope.drawLightningBolt(
    center: Offset,
    height: Float,
    boltColor: Color,
    alpha: Float
) {
    val top = center.y - height * 0.15f
    val path = Path().apply {
        moveTo(center.x + height * 0.02f, top)
        lineTo(center.x - height * 0.14f, center.y + height * 0.02f)
        lineTo(center.x - height * 0.02f, center.y + height * 0.02f)
        lineTo(center.x - height * 0.08f, center.y + height * 0.28f)
        lineTo(center.x + height * 0.16f, center.y + height * 0.04f)
        lineTo(center.x + height * 0.04f, center.y + height * 0.04f)
        close()
    }

    drawPath(path, color = boltColor.copy(alpha = alpha * 0.35f))
    drawPath(
        path,
        color = boltColor.copy(alpha = alpha),
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    )
    drawPath(path, color = Color.White.copy(alpha = alpha * 0.55f))
}

internal fun DrawScope.drawRealisticMoon(
    center: Offset,
    radius: Float,
    tint: Color,
    glowAlpha: Float = 0.25f
) {
    val moonTint = if (tint == Color.Unspecified) Color(0xFFE8EAF6) else tint
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(moonTint.copy(alpha = glowAlpha), Color.Transparent),
            center = center,
            radius = radius * 1.5f
        ),
        radius = radius * 1.5f,
        center = center
    )
    drawMoonLitSurface(center, radius, moonTint)
}

internal fun isWaxingMoonPhase(phase: String): Boolean = when (phase.uppercase()) {
    "WAXING_CRESCENT", "FIRST_QUARTER", "WAXING_GIBBOUS", "NEW_MOON" -> true
    "WANING_CRESCENT", "LAST_QUARTER", "WANING_GIBBOUS", "FULL_MOON" -> false
    else -> phase.uppercase().contains("WAXING")
}

/**
 * Draws an astronomically accurate moon disk for the given illumination (0–1) and waxing flag.
 * Uses the classic equal-radius circle overlap method for the terminator curve.
 */
internal fun DrawScope.drawRealisticMoonPhase(
    center: Offset,
    radius: Float,
    tint: Color,
    illumination: Double,
    waxing: Boolean,
    glowAlpha: Float = 0.22f
) {
    val moonTint = if (tint == Color.Unspecified) Color(0xFFE8EAF6) else tint
    val illum = illumination.coerceIn(0.0, 1.0)

    if (illum > 0.92) {
        drawRealisticMoon(center, radius, moonTint, glowAlpha)
        return
    }

    if (illum < 0.03) {
        drawNewMoonDisk(center, radius, moonTint)
        return
    }

    if (illum > 0.65) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(moonTint.copy(alpha = glowAlpha * 0.85f), Color.Transparent),
                center = center,
                radius = radius * 1.45f
            ),
            radius = radius * 1.45f,
            center = center
        )
    }

    drawMoonDarkDisk(center, radius, moonTint)

    if (illum < 0.18) {
        drawEarthshine(center, radius, moonTint, strength = 0.14f)
    }

    val litPath = moonLitPath(center, radius, illum.toFloat(), waxing)
    clipPath(litPath) {
        drawMoonLitSurface(center, radius, moonTint)
    }

    drawCircle(
        color = blendColor(moonTint, Color.White, 0.35f).copy(alpha = 0.22f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawNewMoonDisk(center: Offset, radius: Float, tint: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                darkenColor(tint, 0.25f).copy(alpha = 0.55f),
                darkenColor(tint, 0.12f).copy(alpha = 0.25f)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawEarthshine(center, radius, tint, strength = 0.22f)
    drawCircle(
        color = tint.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.2.dp.toPx())
    )
}

private fun DrawScope.drawMoonDarkDisk(center: Offset, radius: Float, tint: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                darkenColor(tint, 0.42f).copy(alpha = 0.72f),
                darkenColor(tint, 0.18f).copy(alpha = 0.38f)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawEarthshine(center: Offset, radius: Float, tint: Color, strength: Float) {
    val glowCenter = Offset(center.x + radius * 0.08f, center.y + radius * 0.05f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                blendColor(tint, Color(0xFF90CAF9), 0.25f).copy(alpha = strength),
                Color.Transparent
            ),
            center = glowCenter,
            radius = radius * 0.92f
        ),
        radius = radius * 0.92f,
        center = glowCenter
    )
}

private fun moonLitPath(
    center: Offset,
    radius: Float,
    illumination: Float,
    waxing: Boolean
): Path {
    val shadowOffset = radius * (2f * illumination)
    val shadowCenterX = if (waxing) center.x - shadowOffset else center.x + shadowOffset

    return Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(moonOval(center, radius))
        addOval(moonOval(Offset(shadowCenterX, center.y), radius))
    }
}

private fun moonOval(center: Offset, radius: Float): Rect =
    Rect(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius
    )

private fun DrawScope.drawMoonLitSurface(center: Offset, radius: Float, moonTint: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                blendColor(moonTint, Color.White, 0.42f),
                moonTint,
                darkenColor(moonTint, 0.78f)
            ),
            center = Offset(center.x - radius * 0.12f, center.y - radius * 0.14f),
            radius = radius * 1.08f
        ),
        radius = radius,
        center = center
    )
    listOf(
        Offset(-0.2f, -0.1f) to 0.13f,
        Offset(0.06f, 0.16f) to 0.09f,
        Offset(0.22f, -0.06f) to 0.07f,
        Offset(-0.05f, 0.28f) to 0.05f
    ).forEach { (offset, craterScale) ->
        drawCircle(
            color = darkenColor(moonTint, 0.62f).copy(alpha = 0.32f),
            radius = radius * craterScale,
            center = Offset(center.x + radius * offset.x, center.y + radius * offset.y)
        )
        drawCircle(
            color = blendColor(moonTint, Color.White, 0.18f).copy(alpha = 0.18f),
            radius = radius * craterScale * 0.35f,
            center = Offset(
                center.x + radius * offset.x - radius * craterScale * 0.18f,
                center.y + radius * offset.y - radius * craterScale * 0.18f
            )
        )
    }
}

internal fun DrawScope.drawStarField(
    center: Offset,
    radius: Float,
    tint: Color,
    twinkle: Float
) {
    val starColor = if (tint == Color.Unspecified) Color(0xFFFFF9C4) else tint
    val stars = listOf(
        Offset(0.95f, -0.35f) to 1f,
        Offset(-0.85f, -0.55f) to 0.75f,
        Offset(0.55f, 0.75f) to 0.85f,
        Offset(-0.35f, 0.45f) to 0.65f
    )
    stars.forEachIndexed { index, (offset, scale) ->
        val alpha = (0.45f + twinkle * 0.45f + index * 0.05f).coerceAtMost(1f)
        val starCenter = Offset(center.x + radius * offset.x, center.y + radius * offset.y)
        drawCircle(
            color = starColor.copy(alpha = alpha),
            radius = 1.8.dp.toPx() * scale,
            center = starCenter
        )
        if (index % 2 == 0) {
            drawLine(
                color = starColor.copy(alpha = alpha * 0.8f),
                start = Offset(starCenter.x - 3.dp.toPx(), starCenter.y),
                end = Offset(starCenter.x + 3.dp.toPx(), starCenter.y),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = starColor.copy(alpha = alpha * 0.8f),
                start = Offset(starCenter.x, starCenter.y - 3.dp.toPx()),
                end = Offset(starCenter.x, starCenter.y + 3.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

internal fun DrawScope.drawOceanWaveLayer(
    baseY: Float,
    waveWidth: Float,
    waveHeight: Float,
    phase: Float,
    surfaceColor: Color,
    depthColor: Color
) {
    val path = Path().apply {
        moveTo(0f, baseY)
        var x = 0f
        while (x <= size.width) {
            val y = baseY + waveHeight * sin((x / waveWidth) * 2f * PI.toFloat() + phase)
            lineTo(x, y)
            x += 6f
        }
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(surfaceColor, depthColor),
            startY = baseY - waveHeight,
            endY = size.height
        )
    )
}

internal fun darkenColor(color: Color, factor: Float): Color = Color(
    red = color.red * factor,
    green = color.green * factor,
    blue = color.blue * factor,
    alpha = color.alpha
)

internal fun blendColor(a: Color, b: Color, bWeight: Float): Color {
    val aw = 1f - bWeight
    return Color(
        red = a.red * aw + b.red * bWeight,
        green = a.green * aw + b.green * bWeight,
        blue = a.blue * aw + b.blue * bWeight,
        alpha = a.alpha * aw + b.alpha * bWeight
    )
}

internal fun DrawScope.drawWindStreamLines(
    progress: Float,
    streamColor: Color,
    lineCount: Int = 4
) {
    val waveLength = size.width * 0.55f
    val waveHeight = size.height * 0.07f
    val offset = waveLength * progress
    val centerY = size.height / 2f

    repeat(lineCount) { index ->
        val baseY = centerY - size.height * 0.22f + index * (size.height * 0.44f / (lineCount - 1).coerceAtLeast(1))
        val path = Path().apply {
            var x = 0f
            while (x <= size.width) {
                val waveY = baseY + waveHeight * sin((x + offset) * 2f * PI.toFloat() / waveLength)
                if (x == 0f) moveTo(x, waveY) else lineTo(x, waveY)
                x += 5f
            }
        }
        val alpha = 0.55f + index * 0.12f
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    streamColor.copy(alpha = 0f),
                    streamColor.copy(alpha = alpha),
                    streamColor.copy(alpha = alpha * 0.85f),
                    streamColor.copy(alpha = 0f)
                ),
                start = Offset(0f, baseY),
                end = Offset(size.width, baseY)
            ),
            style = Stroke(width = (3.5f - index * 0.35f).dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

internal fun DrawScope.drawWindSwirl(
    center: Offset,
    maxRadius: Float,
    rotation: Float,
    swirlColor: Color
) {
    val path = Path().apply {
        var angle = 0f
        var first = true
        while (angle <= 300f) {
            val angleRad = (rotation + angle) * PI.toFloat() / 180f
            val radius = maxRadius * (1f - angle / 360f).coerceAtLeast(0.18f)
            val x = center.x + radius * cos(angleRad)
            val y = center.y + radius * sin(angleRad)
            if (first) {
                moveTo(x, y)
                first = false
            } else {
                lineTo(x, y)
            }
            angle += 7f
        }
    }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(
                swirlColor.copy(alpha = 0.85f),
                swirlColor.copy(alpha = 0.35f)
            ),
            start = center,
            end = Offset(center.x + maxRadius, center.y)
        ),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

internal fun DrawScope.drawTumblingLeaf(
    center: Offset,
    sizeScale: Float,
    rotationDegrees: Float,
    palette: LeafPalette
) {
    rotate(rotationDegrees, center) {
        val leafSize = size.minDimension * sizeScale
        val leafPath = Path().apply {
            moveTo(center.x, center.y - leafSize * 0.52f)
            cubicTo(
                center.x + leafSize * 0.34f, center.y - leafSize * 0.18f,
                center.x + leafSize * 0.42f, center.y + leafSize * 0.12f,
                center.x, center.y + leafSize * 0.52f
            )
            cubicTo(
                center.x - leafSize * 0.42f, center.y + leafSize * 0.12f,
                center.x - leafSize * 0.34f, center.y - leafSize * 0.18f,
                center.x, center.y - leafSize * 0.52f
            )
            close()
        }
        drawPath(
            path = leafPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColor(palette.fill, Color.White, 0.25f),
                    palette.fill,
                    darkenColor(palette.fill, 0.72f)
                ),
                center = Offset(center.x - leafSize * 0.08f, center.y - leafSize * 0.12f),
                radius = leafSize
            )
        )
        drawPath(
            path = leafPath,
            color = palette.outline.copy(alpha = 0.9f),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )
        drawLine(
            color = palette.vein.copy(alpha = 0.75f),
            start = Offset(center.x, center.y - leafSize * 0.38f),
            end = Offset(center.x, center.y + leafSize * 0.38f),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

internal fun DrawScope.drawWarningTriangle(
    center: Offset,
    size: Float,
    rotationDegrees: Float,
    accent: Color,
    pulse: Float,
    markAlpha: Float
) {
    rotate(rotationDegrees, center) {
        val trianglePath = Path().apply {
            moveTo(center.x, center.y - size)
            lineTo(center.x + size * 0.866f, center.y + size * 0.5f)
            lineTo(center.x - size * 0.866f, center.y + size * 0.5f)
            close()
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = (pulse - 1f) * 0.35f), Color.Transparent),
                center = center,
                radius = size * 1.45f
            ),
            radius = size * 1.45f,
            center = center
        )

        drawPath(
            path = trianglePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    blendColor(accent, Color.White, 0.35f),
                    accent,
                    darkenColor(accent, 0.78f)
                ),
                startY = center.y - size,
                endY = center.y + size * 0.5f
            )
        )
        drawPath(
            path = trianglePath,
            color = darkenColor(accent, 0.55f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val markHeight = size * 0.42f
        val markWidth = size * 0.14f
        val markY = center.y - size * 0.08f
        drawCircle(
            color = darkenColor(accent, 0.45f).copy(alpha = markAlpha),
            radius = markWidth * 0.55f,
            center = Offset(center.x, markY - markHeight * 0.28f)
        )
        drawLine(
            color = darkenColor(accent, 0.45f).copy(alpha = markAlpha),
            start = Offset(center.x, markY + markHeight * 0.05f),
            end = Offset(center.x, markY + markHeight * 0.38f),
            strokeWidth = markWidth,
            cap = StrokeCap.Round
        )
    }
}

internal fun DrawScope.drawLightBulb(
    center: Offset,
    bulbRadius: Float,
    rotationDegrees: Float,
    glow: Float,
    accent: Color
) {
    val bulbCenter = Offset(center.x, center.y - bulbRadius * 0.08f)
    val glassTop = bulbCenter.y - bulbRadius
    val glassBottom = bulbCenter.y + bulbRadius * 0.72f
    val baseTop = glassBottom + bulbRadius * 0.08f
    val baseWidth = bulbRadius * 0.62f
    val baseHeight = bulbRadius * 0.38f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = glow * 0.35f), Color.Transparent),
            center = bulbCenter,
            radius = bulbRadius * 2.1f
        ),
        radius = bulbRadius * 2.1f,
        center = bulbCenter
    )

    rotate(rotationDegrees, center) {
        val bulbPath = Path().apply {
            moveTo(bulbCenter.x - bulbRadius * 0.72f, glassBottom)
            cubicTo(
                bulbCenter.x - bulbRadius * 0.95f, bulbCenter.y - bulbRadius * 0.15f,
                bulbCenter.x - bulbRadius * 0.55f, glassTop + bulbRadius * 0.15f,
                bulbCenter.x, glassTop
            )
            cubicTo(
                bulbCenter.x + bulbRadius * 0.55f, glassTop + bulbRadius * 0.15f,
                bulbCenter.x + bulbRadius * 0.95f, bulbCenter.y - bulbRadius * 0.15f,
                bulbCenter.x + bulbRadius * 0.72f, glassBottom
            )
            close()
        }

        drawPath(
            path = bulbPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColor(accent, Color.White, 0.55f).copy(alpha = 0.95f),
                    accent.copy(alpha = 0.82f + glow * 0.12f),
                    darkenColor(accent, 0.75f).copy(alpha = 0.9f)
                ),
                center = Offset(bulbCenter.x - bulbRadius * 0.18f, bulbCenter.y - bulbRadius * 0.22f),
                radius = bulbRadius * 1.35f
            )
        )
        drawPath(
            path = bulbPath,
            color = darkenColor(accent, 0.65f).copy(alpha = 0.55f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.35f + glow * 0.2f),
            topLeft = Offset(bulbCenter.x - bulbRadius * 0.34f, glassTop + bulbRadius * 0.12f),
            size = Size(bulbRadius * 0.42f, bulbRadius * 0.55f)
        )

        val filamentPath = Path().apply {
            moveTo(bulbCenter.x - bulbRadius * 0.22f, bulbCenter.y - bulbRadius * 0.12f)
            cubicTo(
                bulbCenter.x - bulbRadius * 0.08f, bulbCenter.y + bulbRadius * 0.08f,
                bulbCenter.x + bulbRadius * 0.08f, bulbCenter.y - bulbRadius * 0.08f,
                bulbCenter.x + bulbRadius * 0.22f, bulbCenter.y + bulbRadius * 0.12f
            )
        }
        drawPath(
            path = filamentPath,
            color = Color(0xFFFFF59D).copy(alpha = 0.55f + glow * 0.35f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        val basePath = Path().apply {
            val topWidth = baseWidth * 0.72f
            moveTo(bulbCenter.x - topWidth / 2f, baseTop)
            lineTo(bulbCenter.x + topWidth / 2f, baseTop)
            lineTo(bulbCenter.x + baseWidth / 2f, baseTop + baseHeight)
            lineTo(bulbCenter.x - baseWidth / 2f, baseTop + baseHeight)
            close()
        }
        drawPath(
            path = basePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    blendColor(accent, Color.White, 0.2f),
                    darkenColor(accent, 0.62f)
                ),
                startY = baseTop,
                endY = baseTop + baseHeight
            )
        )
        repeat(3) { i ->
            val y = baseTop + (baseHeight / 4f) * (i + 1)
            drawLine(
                color = darkenColor(accent, 0.45f).copy(alpha = 0.55f),
                start = Offset(bulbCenter.x - baseWidth / 2f, y),
                end = Offset(bulbCenter.x + baseWidth / 2f, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
