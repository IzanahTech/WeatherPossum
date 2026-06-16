package com.weatherpossum.app.widget

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WidgetMetricSlot(
    val label: String,
    val value: String,
    val maxLines: Int
)

data class WidgetLayoutPlan(
    val typography: WidgetTypography,
    val greetingMaxSp: Float,
    val showCoastalHint: Boolean,
    val synopsisMaxLines: Int,
    val metrics: List<WidgetMetricSlot>
) {
    val showsCoastalMetrics: Boolean get() = metrics.isNotEmpty()
}

data class WidgetTypography(
    val greeting: androidx.compose.ui.unit.TextUnit,
    val synopsis: androidx.compose.ui.unit.TextUnit,
    val metricLabel: androidx.compose.ui.unit.TextUnit,
    val metricValue: androidx.compose.ui.unit.TextUnit,
    val hint: androidx.compose.ui.unit.TextUnit,
    val sectionGap: Dp,
    val lineGap: Dp,
    val metricGap: Dp
)

object WidgetLayoutPlanner {

    private const val PADDING_DP = 40f
    private const val LABEL_WIDTH_DP = 46f
    private const val LINE_HEIGHT_MULT = 1.22f
    private const val HEIGHT_FUDGE_DP = 8f
    private const val MIN_METRICS_WIDGET_HEIGHT_DP = 148f

    fun plan(
        size: DpSize,
        greetingText: String,
        synopsis: String,
        windLabel: String,
        seaLabel: String,
        tideLabel: String,
        wind: String?,
        sea: String?,
        tide: String?,
        hasCoastalDetails: Boolean,
        unavailable: String
    ): WidgetLayoutPlan {
        val contentHeightDp = (size.height.value - PADDING_DP).coerceAtLeast(0f)
        val contentWidthDp = (size.width.value - PADDING_DP).coerceAtLeast(0f)
        val valueWidthDp = (contentWidthDp - LABEL_WIDTH_DP).coerceAtLeast(48f)
        val canShowMetrics = hasCoastalDetails && size.height.value >= MIN_METRICS_WIDGET_HEIGHT_DP

        val metricCandidates = buildMetricCandidates(
            windLabel = windLabel,
            seaLabel = seaLabel,
            tideLabel = tideLabel,
            wind = wind,
            sea = sea,
            tide = tide,
            unavailable = unavailable,
            includeMetrics = canShowMetrics
        )

        for (scale in generateSequence(2.05f) { it - 0.08f }.takeWhile { it >= 0.82f }) {
            val typography = buildTypography(size, scale)
            val greetingMaxSp = typography.greeting.value

            for (synopsisLines in listOf(2, 1)) {
                for (metricMaxLines in listOf(2, 1)) {
                    for (metrics in metricCandidates) {
                        val estimated = estimateHeightDp(
                            typography = typography,
                            greetingText = greetingText,
                            greetingMaxSp = greetingMaxSp,
                            synopsis = synopsis,
                            synopsisMaxLines = synopsisLines,
                            metrics = metrics,
                            metricMaxLines = metricMaxLines,
                            showCoastalHint = !canShowMetrics && hasCoastalDetails,
                            contentWidthDp = contentWidthDp,
                            valueWidthDp = valueWidthDp
                        )
                        if (estimated <= contentHeightDp + HEIGHT_FUDGE_DP) {
                            return WidgetLayoutPlan(
                                typography = typography,
                                greetingMaxSp = greetingMaxSp,
                                showCoastalHint = !canShowMetrics && hasCoastalDetails,
                                synopsisMaxLines = synopsisLines,
                                metrics = metrics.map { slot ->
                                    slot.copy(maxLines = metricMaxLines.coerceAtMost(slot.maxLines))
                                }
                            )
                        }
                    }
                }
            }
        }

        val fallbackTypography = buildTypography(size, 0.82f)
        return WidgetLayoutPlan(
            typography = fallbackTypography,
            greetingMaxSp = fallbackTypography.greeting.value,
            showCoastalHint = !canShowMetrics && hasCoastalDetails,
            synopsisMaxLines = 1,
            metrics = emptyList()
        )
    }

    private fun buildMetricCandidates(
        windLabel: String,
        seaLabel: String,
        tideLabel: String,
        wind: String?,
        sea: String?,
        tide: String?,
        unavailable: String,
        includeMetrics: Boolean
    ): List<List<WidgetMetricSlot>> {
        if (!includeMetrics) return listOf(emptyList())

        fun slot(label: String, value: String?) = WidgetMetricSlot(
            label = label,
            value = value?.takeIf { it.isNotBlank() } ?: unavailable,
            maxLines = 2
        )

        val windSlot = slot(windLabel, wind)
        val seaSlot = slot(seaLabel, sea)
        val tideSlot = slot(tideLabel, tide)

        return listOf(
            listOf(windSlot, seaSlot, tideSlot),
            listOf(windSlot, seaSlot),
            listOf(windSlot, tideSlot),
            listOf(windSlot)
        )
    }

    private fun estimateHeightDp(
        typography: WidgetTypography,
        greetingText: String,
        greetingMaxSp: Float,
        synopsis: String,
        synopsisMaxLines: Int,
        metrics: List<WidgetMetricSlot>,
        metricMaxLines: Int,
        showCoastalHint: Boolean,
        contentWidthDp: Float,
        valueWidthDp: Float
    ): Float {
        var height = 0f

        if (showCoastalHint) {
            height += lineHeightDp(typography.hint.value, 1)
            height += typography.sectionGap.value
        }

        val greetingSp = fittedSingleLineSp(
            text = greetingText,
            widthDp = contentWidthDp,
            maxSp = greetingMaxSp,
            minSp = 12f,
            bold = true
        )
        height += lineHeightDp(greetingSp, 1)
        height += typography.lineGap.value

        height += textBlockHeightDp(
            text = synopsis,
            widthDp = contentWidthDp,
            fontSp = typography.synopsis.value,
            maxLines = synopsisMaxLines
        )

        if (metrics.isNotEmpty()) {
            height += typography.sectionGap.value
            metrics.forEachIndexed { index, metric ->
                height += textBlockHeightDp(
                    text = metric.value,
                    widthDp = valueWidthDp,
                    fontSp = typography.metricValue.value,
                    maxLines = metricMaxLines.coerceAtMost(metric.maxLines)
                )
                if (index < metrics.lastIndex) {
                    height += typography.metricGap.value
                }
            }
        }

        return height
    }

    private fun fittedSingleLineSp(
        text: String,
        widthDp: Float,
        maxSp: Float,
        minSp: Float,
        bold: Boolean = false
    ): Float {
        if (text.isEmpty() || widthDp <= 0f) return maxSp.coerceAtLeast(minSp)

        val widthFactor = if (bold) 0.54f else 0.48f
        var sizeSp = maxSp
        while (sizeSp >= minSp) {
            if (text.length * sizeSp * widthFactor <= widthDp) {
                return sizeSp
            }
            sizeSp -= 0.5f
        }
        return minSp
    }

    private fun textBlockHeightDp(
        text: String,
        widthDp: Float,
        fontSp: Float,
        maxLines: Int
    ): Float {
        val lines = estimatedLineCount(
            text = text,
            widthDp = widthDp,
            fontSp = fontSp
        ).coerceIn(1, maxLines)
        return lineHeightDp(fontSp, lines)
    }

    private fun estimatedLineCount(
        text: String,
        widthDp: Float,
        fontSp: Float
    ): Int {
        if (text.isEmpty() || widthDp <= 0f) return 0

        val charsPerLine = (widthDp / (fontSp * 0.48f)).toInt().coerceAtLeast(8)
        return text.split('\n').sumOf { paragraph ->
            if (paragraph.isBlank()) {
                1
            } else {
                ((paragraph.length + charsPerLine - 1) / charsPerLine).coerceAtLeast(1)
            }
        }.coerceAtLeast(1)
    }

    private fun lineHeightDp(fontSp: Float, lines: Int): Float =
        fontSp * LINE_HEIGHT_MULT * lines

    private fun buildTypography(size: DpSize, scale: Float): WidgetTypography {
        val sizeScale = ((size.height.value / 110f) * 0.72f + (size.width.value / 250f) * 0.28f)
            .coerceIn(1f, 2.1f)
        val s = sizeScale * scale

        fun sp(base: Float, min: Float, max: Float) =
            (base * s).coerceIn(min, max).sp

        fun gap(base: Float) = (base * s).coerceIn(base, base * 1.35f).dp

        return WidgetTypography(
            greeting = sp(20f, 16f, 28f),
            synopsis = sp(15f, 12f, 20f),
            metricLabel = sp(11f, 9f, 14f),
            metricValue = sp(13.5f, 11f, 17f),
            hint = sp(11f, 9f, 13f),
            sectionGap = gap(8f),
            lineGap = gap(5f),
            metricGap = gap(4f)
        )
    }
}
