package com.weatherpossum.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import com.weatherpossum.app.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

private val ArialBlack = FontFamily(
    Font(resId = R.font.arial_bold, weight = FontWeight.Bold)
)

@Composable
fun GreetingCard(
    userName: String?,
    synopsis: String,
    modifier: Modifier = Modifier,
    onNameSubmit: (String) -> Unit
) {
    var showNameInput by remember { mutableStateOf(userName == null) }
    var tempName by remember { mutableStateOf("") }
    val now = remember { java.time.LocalTime.now() }
    val (greeting, animationFile) = remember(now.hour) {
        when (now.hour) {
            in 5..11 -> "GOOD MORNING" to "gmorning.json"
            in 12..17 -> "GOOD AFTERNOON" to "afternoon.json"
            else -> "GOOD NIGHT" to "night.json"
        }
    }

    val textColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF003826)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = if (isSystemInDarkTheme()) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color(0xFFB2DFDB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered greeting and user name at the top
            Text(
                text = if (userName == null) greeting else "$greeting\n${userName}",
                fontFamily = ArialBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Row with synopsis and animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AnnotatedString for bold 'SYNOPSIS:' and normal synopsis
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("SYNOPSIS: ")
                        }
                        append(synopsis)
                    },
                    fontFamily = ArialBlack,
                    fontSize = 18.sp,
                    color = textColor,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(24.dp))
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animations/$animationFile"))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(if (isSystemInDarkTheme()) 0.7f else 1f)
                )
            }
        }
    }
} 