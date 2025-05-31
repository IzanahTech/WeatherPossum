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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val maxFontSize = with(LocalDensity.current) { (maxWidth * 0.12f).toSp() }
                    Text(
                        text = if (userName == null) greeting + "!" else "$greeting $userName!",
                        fontFamily = ArialBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = maxFontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (maxFontSize.value * 1.2f).sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "SYNOPSIS: $synopsis",
                    fontFamily = ArialBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                
                if (showNameInput) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Enter your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                onNameSubmit(tempName)
                                showNameInput = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Name")
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Lottie animation
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animations/$animationFile"))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = if (isSystemInDarkTheme()) {
                    Modifier
                        .size(100.dp)
                        .alpha(0.7f)
                } else {
                    Modifier.size(100.dp)
                }
            )
        }
    }
} 