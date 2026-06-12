package com.weatherpossum.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.R

object WeatherPossumFonts {
    val greeting = FontFamily(
        Font(R.font.greeting_display, FontWeight.Bold)
    )

    val greetingTextStyle = TextStyle(
        fontFamily = greeting,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.15).sp
    )
}
