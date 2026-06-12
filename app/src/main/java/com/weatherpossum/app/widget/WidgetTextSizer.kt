package com.weatherpossum.app.widget

import android.graphics.Paint
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object WidgetTextSizer {

    fun fitSingleLineTextSize(
        text: String,
        availableWidthPx: Float,
        maxSp: Float,
        minSp: Float = 12f,
        density: Float,
        isBold: Boolean = true
    ): TextUnit {
        if (text.isEmpty() || availableWidthPx <= 0f) {
            return maxSp.coerceAtLeast(minSp).sp
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFakeBoldText = isBold
        }

        var sizeSp = maxSp
        while (sizeSp >= minSp) {
            paint.textSize = sizeSp * density
            if (paint.measureText(text) <= availableWidthPx) {
                return sizeSp.sp
            }
            sizeSp -= 0.5f
        }

        return minSp.sp
    }
}
