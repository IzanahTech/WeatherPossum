package com.weatherpossum.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow

object WeatherPossumDimens {
    val cardPadding = 16.dp
    val cardElevation = 10.dp
    val cardCornerRadius = 28.dp
    val sectionSpacing = 16.dp
    val chipBackgroundAlpha = 0.12f
    val detailChipHorizontalPadding = 12.dp
    val detailChipVerticalPadding = 8.dp
    val iconSmall = 32.dp
    val iconMedium = 48.dp
    val iconLarge = 64.dp
    val iconHero = 80.dp
    val navBarHeight = 68.dp
    val navBarCornerRadius = 34.dp
    val navBarHorizontalPadding = 20.dp
    val navBarBottomPadding = 20.dp
}

enum class CardGradientStyle {
    Moon,
    Sun,
    HurricaneOutlook,
    HurricaneNeutral,
    HurricaneWarm,
    Error,
    Fact,
    Info,
    Outlook,
    Advisory,
    Forecast
}

/**
 * Central palette for Weather Possum's expressive gradient cards and screen backgrounds.
 *
 * Each card family has paired light and dark stops. Dark stops are deeper and less saturated
 * so cards sit comfortably on dark screen backgrounds while keeping the same hue families.
 */
object WeatherPossumGradients {

    // ── Card: moon & night ───────────────────────────────────────────────────
    val moonTop = Color(0xFF1A237E)
    val moonBottom = Color(0xFF0D1B3E)
    private val moonDarkTop = Color(0xFF0A1028)
    private val moonDarkBottom = Color(0xFF050814)

    // ── Card: sun & daylight (light: warm golden hour) ───────────────────────
    val sunTop = Color(0xFFFFB74D)
    val sunBottom = Color(0xFFFF6D00)
    private val sunDarkTop = Color(0xFF8B5A14)
    private val sunDarkBottom = Color(0xFF5C3A0A)

    // ── Card: hurricane ──────────────────────────────────────────────────────
    val hurricaneOutlookTop = Color(0xFF1565C0)
    val hurricaneOutlookBottom = Color(0xFF0D47A1)
    private val hurricaneOutlookDarkTop = Color(0xFF0D2847)
    private val hurricaneOutlookDarkBottom = Color(0xFF071A30)

    val hurricaneNeutralTop = Color(0xFF80DEEA)
    val hurricaneNeutralBottom = Color(0xFF00BCD4)
    private val hurricaneNeutralDarkTop = Color(0xFF1A4A5E)
    private val hurricaneNeutralDarkBottom = Color(0xFF0F3447)

    val hurricaneWarmTop = Color(0xFFFFE082)
    val hurricaneWarmBottom = Color(0xFFFFB300)
    private val hurricaneWarmDarkTop = Color(0xFF6B5200)
    private val hurricaneWarmDarkBottom = Color(0xFF4A3800)

    // ── Card: status ─────────────────────────────────────────────────────────
    val errorTop = Color(0xFFFFAB91)
    val errorBottom = Color(0xFFFF5722)
    private val errorDarkTop = Color(0xFF7A3E30)
    private val errorDarkBottom = Color(0xFF5C2E24)

    // ── Card: content types (Now tab) ────────────────────────────────────────
    val factTop = Color(0xFFB388FF)
    val factBottom = Color(0xFF7C4DFF)
    private val factDarkTop = Color(0xFF3D2A5C)
    private val factDarkBottom = Color(0xFF241839)

    val infoTop = Color(0xFF4DD0E1)
    val infoBottom = Color(0xFF0097A7)
    private val infoDarkTop = Color(0xFF263238)
    private val infoDarkBottom = Color(0xFF1A2327)

    val outlookTop = Color(0xFF00B0FF)
    val outlookBottom = Color(0xFF0091EA)
    private val outlookDarkTop = Color(0xFF015A7A)
    private val outlookDarkBottom = Color(0xFF003D52)

    val advisoryTop = Color(0xFFFFD740)
    val advisoryBottom = Color(0xFFFFAB00)
    private val advisoryDarkTop = Color(0xFF6B5200)
    private val advisoryDarkBottom = Color(0xFF4A3800)

    val forecastTop = Color(0xFF26C6DA)
    val forecastBottom = Color(0xFF00ACC1)
    private val forecastDarkTop = Color(0xFF004D45)
    private val forecastDarkBottom = Color(0xFF002822)

    // ── Greeting card: daylight cycle keyframes ──────────────────────────────
    val dawnTop = Color(0xFF1A237E)
    val dawnBottom = Color(0xFF3949AB)
    private val dawnDarkTop = Color(0xFF0A1028)
    private val dawnDarkBottom = Color(0xFF151B40)

    val morningTop = Color(0xFF4FC3F7)
    val morningBottom = Color(0xFF039BE5)
    private val morningDarkTop = Color(0xFF1A4A6B)
    private val morningDarkBottom = Color(0xFF0F3447)

    val noonTop = Color(0xFFFFE082)
    val noonBottom = Color(0xFFFF9100)
    private val noonDarkTop = Color(0xFF6B5200)
    private val noonDarkBottom = Color(0xFF8B6914)

    val sunsetTop = Color(0xFFCE93D8)
    val sunsetBottom = Color(0xFF7986CB)
    private val sunsetDarkTop = Color(0xFF2A3555)
    private val sunsetDarkBottom = Color(0xFF1E2640)

    val nightTop = Color(0xFF3949AB)
    val nightBottom = Color(0xFF283593)
    private val nightDarkTop = Color(0xFF060A12)
    private val nightDarkBottom = Color(0xFF0F172A)

    // ── Screen backgrounds (light: luminous sky washes) ───────────────────────
    private val screenDayLightTop = Color(0xFFB3E5FC)
    private val screenDayLightBottom = Color(0xFF4FC3F7)
    private val screenRainLightTop = Color(0xFF90CAF9)
    private val screenRainLightBottom = Color(0xFF42A5F5)
    private val screenCloudLightTop = Color(0xFFE1BEE7)
    private val screenCloudLightBottom = Color(0xFF90A4AE)
    private val screenWindLightTop = Color(0xFF80DEEA)
    private val screenWindLightBottom = Color(0xFF26A69A)
    private val screenNightLightTop = Color(0xFF9FA8DA)
    private val screenNightLightBottom = Color(0xFF5C6BC0)

    private val screenDayDarkTop = Color(0xFF0F172A)
    private val screenDayDarkBottom = Color(0xFF1E3A5F)
    private val screenRainDarkTop = Color(0xFF1C2833)
    private val screenRainDarkBottom = Color(0xFF0D1B2A)
    private val screenCloudDarkTop = Color(0xFF263238)
    private val screenCloudDarkBottom = Color(0xFF37474F)
    private val screenWindDarkTop = Color(0xFF004D40)
    private val screenWindDarkBottom = Color(0xFF1B5E20)
    private val screenNightDarkTop = Color(0xFF0A0E1A)
    private val screenNightDarkBottom = Color(0xFF151C2C)

    fun vertical(top: Color, bottom: Color): Brush =
        Brush.verticalGradient(listOf(top, bottom))

    fun colors(style: CardGradientStyle, isDarkMode: Boolean): Pair<Color, Color> = when (style) {
        CardGradientStyle.Moon ->
            if (isDarkMode) moonDarkTop to moonDarkBottom else moonTop to moonBottom
        CardGradientStyle.Sun ->
            if (isDarkMode) sunDarkTop to sunDarkBottom else sunTop to sunBottom
        CardGradientStyle.HurricaneOutlook ->
            if (isDarkMode) hurricaneOutlookDarkTop to hurricaneOutlookDarkBottom
            else hurricaneOutlookTop to hurricaneOutlookBottom
        CardGradientStyle.HurricaneNeutral ->
            if (isDarkMode) hurricaneNeutralDarkTop to hurricaneNeutralDarkBottom
            else hurricaneNeutralTop to hurricaneNeutralBottom
        CardGradientStyle.HurricaneWarm ->
            if (isDarkMode) hurricaneWarmDarkTop to hurricaneWarmDarkBottom
            else hurricaneWarmTop to hurricaneWarmBottom
        CardGradientStyle.Error ->
            if (isDarkMode) errorDarkTop to errorDarkBottom else errorTop to errorBottom
        CardGradientStyle.Fact ->
            if (isDarkMode) factDarkTop to factDarkBottom else factTop to factBottom
        CardGradientStyle.Info ->
            if (isDarkMode) infoDarkTop to infoDarkBottom else infoTop to infoBottom
        CardGradientStyle.Outlook ->
            if (isDarkMode) outlookDarkTop to outlookDarkBottom else outlookTop to outlookBottom
        CardGradientStyle.Advisory ->
            if (isDarkMode) advisoryDarkTop to advisoryDarkBottom else advisoryTop to advisoryBottom
        CardGradientStyle.Forecast ->
            if (isDarkMode) forecastDarkTop to forecastDarkBottom else forecastTop to forecastBottom
    }

    fun brush(style: CardGradientStyle, isDarkMode: Boolean): Brush {
        val (top, bottom) = colors(style, isDarkMode)
        return vertical(top, bottom)
    }

    fun greetingGradient(sunFrac: Float, isDarkMode: Boolean): Pair<Color, Color> {
        val dawnT = if (isDarkMode) dawnDarkTop else dawnTop
        val dawnB = if (isDarkMode) dawnDarkBottom else dawnBottom
        val morningT = if (isDarkMode) morningDarkTop else morningTop
        val morningB = if (isDarkMode) morningDarkBottom else morningBottom
        val noonT = if (isDarkMode) noonDarkTop else noonTop
        val noonB = if (isDarkMode) noonDarkBottom else noonBottom
        val sunsetT = if (isDarkMode) sunsetDarkTop else sunsetTop
        val sunsetB = if (isDarkMode) sunsetDarkBottom else sunsetBottom
        val nightT = if (isDarkMode) nightDarkTop else nightTop
        val nightB = if (isDarkMode) nightDarkBottom else nightBottom

        return if (sunFrac <= 0.5f) {
            if (sunFrac <= 0.25f) {
                val t = sunFrac / 0.25f
                WeatherPossumColors.lerpColor(dawnT, morningT, t) to
                    WeatherPossumColors.lerpColor(dawnB, morningB, t)
            } else {
                val t = (sunFrac - 0.25f) / 0.25f
                WeatherPossumColors.lerpColor(morningT, noonT, t) to
                    WeatherPossumColors.lerpColor(morningB, noonB, t)
            }
        } else {
            if (sunFrac <= 0.75f) {
                val t = (sunFrac - 0.5f) / 0.25f
                WeatherPossumColors.lerpColor(noonT, sunsetT, t) to
                    WeatherPossumColors.lerpColor(noonB, sunsetB, t)
            } else {
                val t = (sunFrac - 0.75f) / 0.25f
                WeatherPossumColors.lerpColor(sunsetT, nightT, t) to
                    WeatherPossumColors.lerpColor(sunsetB, nightB, t)
            }
        }
    }

    fun moon(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Moon, isDarkMode)
    fun sun(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Sun, isDarkMode)
    fun hurricaneOutlook(isDarkMode: Boolean = false): Brush =
        brush(CardGradientStyle.HurricaneOutlook, isDarkMode)
    fun hurricaneNeutral(isDarkMode: Boolean = false): Brush =
        brush(CardGradientStyle.HurricaneNeutral, isDarkMode)
    fun hurricaneWarm(isDarkMode: Boolean = false): Brush =
        brush(CardGradientStyle.HurricaneWarm, isDarkMode)
    fun error(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Error, isDarkMode)
    fun fact(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Fact, isDarkMode)
    fun info(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Info, isDarkMode)
    fun outlook(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Outlook, isDarkMode)
    fun advisory(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Advisory, isDarkMode)
    fun forecast(isDarkMode: Boolean = false): Brush = brush(CardGradientStyle.Forecast, isDarkMode)

    fun screenBackground(isDarkMode: Boolean, synopsis: String?): Brush {
        val (top, bottom) = screenBackgroundColors(isDarkMode, synopsis)
        return vertical(top, bottom)
    }

    fun screenBackgroundColors(isDarkMode: Boolean, synopsis: String?): Pair<Color, Color> {
        return if (isDarkMode) {
            when {
                synopsis.containsKeyword("rain") -> screenRainDarkTop to screenRainDarkBottom
                synopsis.containsKeyword("cloud") -> screenCloudDarkTop to screenCloudDarkBottom
                synopsis.containsKeyword("wind") -> screenWindDarkTop to screenWindDarkBottom
                else -> screenNightDarkTop to screenNightDarkBottom
            }
        } else {
            when {
                synopsis.containsKeyword("rain") -> screenRainLightTop to screenRainLightBottom
                synopsis.containsKeyword("cloud") -> screenCloudLightTop to screenCloudLightBottom
                synopsis.containsKeyword("wind") -> screenWindLightTop to screenWindLightBottom
                else -> screenDayLightTop to screenDayLightBottom
            }
        }
    }

    private fun String?.containsKeyword(keyword: String): Boolean =
        this?.contains(keyword, ignoreCase = true) == true
}

object WeatherPossumColors {
    val ink = Color(0xFF0F172A)
    val softInk = Color(0xFF1E293B)
    val hurricaneNeutralOn = Color(0xFF01579B)
    val moonAccent = Color(0xFFFFD54F)
    val factIcon = Color(0xFFFFCA28)
    val sunGlow = Color(0xFFFFF8E1)

    fun onColorForGradient(top: Color, bottom: Color): Color {
        val avgBg = averageColor(top, bottom)
        val candidate = pickOnColorFor(avgBg)
        return ensureAccessible(candidate, avgBg)
    }

    fun onColorForTint(tintTop: Color, tintBottom: Color, isDarkMode: Boolean): Color {
        if (isDarkMode) return onColorForGradient(tintTop, tintBottom)
        val effectiveBg = effectiveLightGlassColor(tintTop, tintBottom)
        val candidate = pickOnColorFor(effectiveBg)
        return ensureAccessible(candidate, effectiveBg)
    }

    fun onColorForStyle(style: CardGradientStyle, isDarkMode: Boolean): Color {
        val (top, bottom) = WeatherPossumGradients.colors(style, isDarkMode)
        if (!isDarkMode) {
            return when (style) {
                CardGradientStyle.Moon,
                CardGradientStyle.HurricaneOutlook -> onColorForGradient(top, bottom)
                else -> onColorForTint(top, bottom, isDarkMode = false)
            }
        }
        return when (style) {
            CardGradientStyle.HurricaneNeutral -> hurricaneNeutralOn
            else -> onColorForGradient(top, bottom)
        }
    }

    private fun averageColor(top: Color, bottom: Color): Color =
        Color(
            red = (top.red + bottom.red) / 2f,
            green = (top.green + bottom.green) / 2f,
            blue = (top.blue + bottom.blue) / 2f,
            alpha = 1f
        )

    /** Approximates the frosted-glass card surface in light mode (frost + tint over a pale screen). */
    private fun effectiveLightGlassColor(tintTop: Color, tintBottom: Color): Color {
        val tintMid = lerpColor(tintTop, tintBottom, 0.5f)
        val frost = Color(0xFFF8FBFF)
        return lerpColor(frost, tintMid, 0.54f)
    }

    private fun srgbToLinear(c: Float): Double =
        if (c <= 0.04045f) (c / 12.92f).toDouble()
        else (((c + 0.055f) / 1.055f).toDouble()).pow(2.4)

    private fun Color.relativeLuminance(): Double {
        val r = srgbToLinear(red)
        val g = srgbToLinear(green)
        val b = srgbToLinear(blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val la = a.relativeLuminance()
        val lb = b.relativeLuminance()
        val (l1, l2) = if (la >= lb) la to lb else lb to la
        return (l1 + 0.05) / (l2 + 0.05)
    }

    private fun pickOnColorFor(background: Color): Color {
        return if (contrastRatio(Color.White, background) >= contrastRatio(ink, background)) {
            Color.White
        } else {
            ink
        }
    }

    private fun ensureAccessible(on: Color, bg: Color): Color {
        if (contrastRatio(on, bg) >= 4.5) return on
        val other = if (on == Color.White) ink else Color.White
        return if (contrastRatio(other, bg) > contrastRatio(on, bg)) other else on
    }

    fun lerpColor(a: Color, b: Color, t: Float): Color =
        Color(
            red = a.red + (b.red - a.red) * t.coerceIn(0f, 1f),
            green = a.green + (b.green - a.green) * t.coerceIn(0f, 1f),
            blue = a.blue + (b.blue - a.blue) * t.coerceIn(0f, 1f),
            alpha = a.alpha + (b.alpha - a.alpha) * t.coerceIn(0f, 1f)
        )
}
