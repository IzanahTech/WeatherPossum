package com.weatherpossum.app.widget

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

data class WidgetLayoutPlan(
  val greetingFontSp: Float,
  val synopsisFontSp: Float,
  val seaLabelFontSp: Float,
  val seaValueFontSp: Float,
  val greetingLineGap: Dp,
  val synopsisToSeaGap: Dp,
  val seaLabelGap: Dp,
  val greetingText: String,
  val synopsisText: String,
  val synopsisLineCount: Int,
  val showSea: Boolean,
  val seaText: String?
)

private data class WidgetTypeScale(
  val greetingMaxSp: Float,
  val greetingMinSp: Float,
  val synopsisSp: Float,
  val seaLabelSp: Float,
  val seaValueSp: Float,
  val greetingLineGap: Float,
  val synopsisToSeaGap: Float,
  val seaLabelGap: Float,
  val dividerHeight: Float
)

object WidgetLayoutPlanner {

  private const val PADDING_DP = 36f
  private const val HEIGHT_FUDGE_DP = 2f
  private const val MIN_SEA_WIDGET_HEIGHT_DP = 150f

  private val typeScales = listOf(
    WidgetTypeScale(
      greetingMaxSp = 22f,
      greetingMinSp = 16f,
      synopsisSp = 15.5f,
      seaLabelSp = 10f,
      seaValueSp = 13f,
      greetingLineGap = 6f,
      synopsisToSeaGap = 10f,
      seaLabelGap = 4f,
      dividerHeight = 1f
    ),
    WidgetTypeScale(
      greetingMaxSp = 20f,
      greetingMinSp = 15f,
      synopsisSp = 14.5f,
      seaLabelSp = 10f,
      seaValueSp = 12.5f,
      greetingLineGap = 5f,
      synopsisToSeaGap = 8f,
      seaLabelGap = 4f,
      dividerHeight = 1f
    ),
    WidgetTypeScale(
      greetingMaxSp = 18f,
      greetingMinSp = 14f,
      synopsisSp = 13.5f,
      seaLabelSp = 9.5f,
      seaValueSp = 12f,
      greetingLineGap = 4f,
      synopsisToSeaGap = 7f,
      seaLabelGap = 3f,
      dividerHeight = 1f
    )
  )

  fun plan(
    size: DpSize,
    greetingText: String,
    synopsis: String,
    seaConditions: String?
  ): WidgetLayoutPlan {
    val contentHeightDp = (size.height.value - PADDING_DP).coerceAtLeast(0f)
    val contentWidthDp = (size.width.value - PADDING_DP).coerceAtLeast(0f)
    val sentences = WidgetTextWrap.splitSentences(synopsis)
    val seaText = seaConditions?.trim()?.takeIf { it.isNotBlank() }
    val canConsiderSea = seaText != null && size.height.value >= MIN_SEA_WIDGET_HEIGHT_DP

    for (scale in typeScales) {
      val greetingSp = WidgetTextWrap.fittedSingleLineSp(
        text = greetingText,
        widthDp = contentWidthDp,
        maxSp = scale.greetingMaxSp,
        minSp = scale.greetingMinSp,
        bold = true
      )

      val greetingHeight = WidgetTextWrap.lineHeightDp(greetingSp, 1) + scale.greetingLineGap
      var remainingHeight = contentHeightDp - greetingHeight

      if (remainingHeight <= 0f) continue

      val synopsisHeightBudget = if (canConsiderSea) {
        val seaBlockHeight = estimateSeaBlockHeight(
          seaText = seaText,
          scale = scale,
          contentWidthDp = contentWidthDp
        )
        if (seaBlockHeight <= remainingHeight) {
          remainingHeight - seaBlockHeight
        } else {
          remainingHeight
        }
      } else {
        remainingHeight
      }

      val fittedSynopsis = WidgetTextWrap.fitSentences(
        sentences = sentences,
        widthDp = contentWidthDp,
        fontSp = scale.synopsisSp,
        maxHeightDp = synopsisHeightBudget
      )

      if (fittedSynopsis.isBlank()) continue

      val synopsisLineCount = WidgetTextWrap.lineCount(
        text = fittedSynopsis,
        widthDp = contentWidthDp,
        fontSp = scale.synopsisSp
      )

      val synopsisHeight = WidgetTextWrap.blockHeightDp(
        text = fittedSynopsis,
        widthDp = contentWidthDp,
        fontSp = scale.synopsisSp
      )

      val usedHeight = greetingHeight + synopsisHeight
      val showSea = canConsiderSea && run {
        val seaBlockHeight = estimateSeaBlockHeight(seaText, scale, contentWidthDp)
        usedHeight + scale.synopsisToSeaGap + seaBlockHeight <= contentHeightDp + HEIGHT_FUDGE_DP
      }

      val totalHeight = usedHeight +
        if (showSea) {
          scale.synopsisToSeaGap + estimateSeaBlockHeight(seaText, scale, contentWidthDp)
        } else {
          0f
        }

      if (totalHeight <= contentHeightDp + HEIGHT_FUDGE_DP) {
        return WidgetLayoutPlan(
          greetingFontSp = greetingSp,
          synopsisFontSp = scale.synopsisSp,
          seaLabelFontSp = scale.seaLabelSp,
          seaValueFontSp = scale.seaValueSp,
          greetingLineGap = scale.greetingLineGap.dp,
          synopsisToSeaGap = scale.synopsisToSeaGap.dp,
          seaLabelGap = scale.seaLabelGap.dp,
          greetingText = greetingText,
          synopsisText = fittedSynopsis,
          synopsisLineCount = synopsisLineCount,
          showSea = showSea,
          seaText = if (showSea) seaText else null
        )
      }
    }

    return fallbackPlan(
      greetingText = greetingText,
      synopsis = synopsis,
      sentences = sentences,
      contentWidthDp = contentWidthDp,
      contentHeightDp = contentHeightDp
    )
  }

  private fun estimateSeaBlockHeight(
    seaText: String,
    scale: WidgetTypeScale,
    contentWidthDp: Float
  ): Float {
    val dividerAndGaps = scale.dividerHeight + scale.seaLabelGap
    val labelHeight = WidgetTextWrap.lineHeightDp(scale.seaLabelSp, 1)
    val valueHeight = WidgetTextWrap.blockHeightDp(
      text = seaText,
      widthDp = contentWidthDp,
      fontSp = scale.seaValueSp
    )
    return dividerAndGaps + labelHeight + valueHeight
  }

  private fun fallbackPlan(
    greetingText: String,
    synopsis: String,
    sentences: List<String>,
    contentWidthDp: Float,
    contentHeightDp: Float
  ): WidgetLayoutPlan {
    val scale = typeScales.last()
    val greetingSp = WidgetTextWrap.fittedSingleLineSp(
      text = greetingText,
      widthDp = contentWidthDp,
      maxSp = scale.greetingMaxSp,
      minSp = scale.greetingMinSp,
      bold = true
    )
    val greetingHeight = WidgetTextWrap.lineHeightDp(greetingSp, 1) + scale.greetingLineGap
    val synopsisBudget = (contentHeightDp - greetingHeight).coerceAtLeast(0f)
    val fittedSynopsis = WidgetTextWrap.fitSentences(
      sentences = sentences,
      widthDp = contentWidthDp,
      fontSp = scale.synopsisSp,
      maxHeightDp = synopsisBudget
    ).ifBlank {
      sentences.firstOrNull().orEmpty()
    }

    val synopsisLineCount = WidgetTextWrap.lineCount(
      text = fittedSynopsis,
      widthDp = contentWidthDp,
      fontSp = scale.synopsisSp
    ).coerceAtLeast(1)

    return WidgetLayoutPlan(
      greetingFontSp = greetingSp,
      synopsisFontSp = scale.synopsisSp,
      seaLabelFontSp = scale.seaLabelSp,
      seaValueFontSp = scale.seaValueSp,
      greetingLineGap = scale.greetingLineGap.dp,
      synopsisToSeaGap = scale.synopsisToSeaGap.dp,
      seaLabelGap = scale.seaLabelGap.dp,
      greetingText = greetingText,
      synopsisText = fittedSynopsis,
      synopsisLineCount = synopsisLineCount,
      showSea = false,
      seaText = null
    )
  }
}
