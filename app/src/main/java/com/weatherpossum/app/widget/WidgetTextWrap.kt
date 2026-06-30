package com.weatherpossum.app.widget

object WidgetTextWrap {

  private const val LINE_HEIGHT_MULT = 1.28f
  private const val CHAR_WIDTH_FACTOR = 0.48f
  private const val BOLD_CHAR_WIDTH_FACTOR = 0.54f

  fun splitSentences(text: String): List<String> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return emptyList()

    val parts = trimmed.split(Regex("(?<=[.!?])\\s+"))
      .map { it.trim() }
      .filter { it.isNotBlank() }

    return if (parts.isEmpty()) listOf(trimmed) else parts
  }

  fun wrapToLines(text: String, widthDp: Float, fontSp: Float, bold: Boolean = false): List<String> {
    if (text.isBlank() || widthDp <= 0f || fontSp <= 0f) return emptyList()

    val charsPerLine = charsPerLine(widthDp, fontSp, bold).coerceAtLeast(8)
    val lines = mutableListOf<String>()
    val paragraphs = text.split('\n')

    paragraphs.forEach { paragraph ->
      val words = paragraph.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
      if (words.isEmpty()) return@forEach

      var current = StringBuilder(words.first())
      words.drop(1).forEach { word ->
        val candidate = "${current} $word"
        if (candidate.length <= charsPerLine) {
          current = StringBuilder(candidate)
        } else {
          lines.add(current.toString())
          current = StringBuilder(word)
        }
      }
      lines.add(current.toString())
    }

    return lines
  }

  fun lineCount(text: String, widthDp: Float, fontSp: Float, bold: Boolean = false): Int =
    wrapToLines(text, widthDp, fontSp, bold).size

  fun blockHeightDp(text: String, widthDp: Float, fontSp: Float, bold: Boolean = false): Float {
    val lines = lineCount(text, widthDp, fontSp, bold).coerceAtLeast(if (text.isBlank()) 0 else 1)
    return lineHeightDp(fontSp, lines)
  }

  fun lineHeightDp(fontSp: Float, lines: Int): Float =
    fontSp * LINE_HEIGHT_MULT * lines.coerceAtLeast(0)

  fun fitsWithinHeight(
    text: String,
    widthDp: Float,
    fontSp: Float,
    maxHeightDp: Float,
    bold: Boolean = false
  ): Boolean = blockHeightDp(text, widthDp, fontSp, bold) <= maxHeightDp + 0.5f

  fun fitSentences(
    sentences: List<String>,
    widthDp: Float,
    fontSp: Float,
    maxHeightDp: Float
  ): String {
    if (sentences.isEmpty() || maxHeightDp <= 0f) return ""

    val fitted = mutableListOf<String>()
    for (sentence in sentences) {
      val candidate = (fitted + sentence).joinToString(" ")
      if (fitsWithinHeight(candidate, widthDp, fontSp, maxHeightDp)) {
        fitted.add(sentence)
      } else {
        break
      }
    }

    return fitted.joinToString(" ")
  }

  fun fittedSingleLineSp(
    text: String,
    widthDp: Float,
    maxSp: Float,
    minSp: Float,
    bold: Boolean = true
  ): Float {
    if (text.isEmpty() || widthDp <= 0f) return maxSp.coerceAtLeast(minSp)

    val widthFactor = if (bold) BOLD_CHAR_WIDTH_FACTOR else CHAR_WIDTH_FACTOR
    var sizeSp = maxSp
    while (sizeSp >= minSp) {
      if (text.length * sizeSp * widthFactor <= widthDp) return sizeSp
      sizeSp -= 0.5f
    }
    return minSp
  }

  private fun charsPerLine(widthDp: Float, fontSp: Float, bold: Boolean): Int {
    val widthFactor = if (bold) BOLD_CHAR_WIDTH_FACTOR else CHAR_WIDTH_FACTOR
    return (widthDp / (fontSp * widthFactor)).toInt()
  }
}
