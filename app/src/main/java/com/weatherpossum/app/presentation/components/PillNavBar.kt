package com.weatherpossum.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.weatherpossum.app.R
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.weatherpossum.app.ui.theme.CardGradientStyle
import com.weatherpossum.app.ui.theme.WeatherPossumDimens
import com.weatherpossum.app.ui.theme.WeatherPossumGlass
import com.weatherpossum.app.ui.theme.WeatherPossumMotion

@Immutable
private data class NavTab(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: WeatherIconType,
    val style: CardGradientStyle
)

@Composable
fun PillNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val tabs = listOf(
        NavTab(
            id = "Now",
            title = stringResource(R.string.nav_tab_now),
            subtitle = stringResource(R.string.nav_tab_now_subtitle),
            icon = WeatherIconType.PARTLY_CLOUDY,
            style = CardGradientStyle.Outlook
        ),
        NavTab(
            id = "Extras",
            title = stringResource(R.string.nav_tab_extras),
            subtitle = stringResource(R.string.nav_tab_extras_subtitle),
            icon = WeatherIconType.EXTRAS,
            style = CardGradientStyle.Fact
        )
    )
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTab }.coerceAtLeast(0)
    val dockShape = RoundedCornerShape(WeatherPossumDimens.navBarCornerRadius)
    val indicatorShape = RoundedCornerShape(26.dp)
    val motionSpec = WeatherPossumMotion.fluidSpring<Float>()
    val colorMotionSpec = WeatherPossumMotion.gentleSpring<androidx.compose.ui.graphics.Color>()

    val (dockTintTop, dockTintBottom) = WeatherPossumGlass.colorsForStyle(
        tabs[selectedIndex].style,
        isDarkMode
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WeatherPossumDimens.navBarHorizontalPadding,
                vertical = WeatherPossumDimens.navBarBottomPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(WeatherPossumDimens.navBarHeight)
                .shadow(
                    elevation = if (isDarkMode) 18.dp else 14.dp,
                    shape = dockShape,
                    ambientColor = Color(0xFF0277BD).copy(alpha = if (isDarkMode) 0.35f else 0.18f),
                    spotColor = Color(0xFF5E35B1).copy(alpha = if (isDarkMode) 0.28f else 0.14f)
                )
                .liquidGlassNavSurface(
                    tintTop = dockTintTop,
                    tintBottom = dockTintBottom,
                    isDarkMode = isDarkMode,
                    cornerRadius = WeatherPossumDimens.navBarCornerRadius
                )
        ) {
            val tabWidth = maxWidth / tabs.size
            val highlightOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = WeatherPossumMotion.fluidSpring(),
                label = "navHighlight"
            )

            val (activeTop, activeBottom) = WeatherPossumGlass.colorsForStyle(
                tabs[selectedIndex].style,
                isDarkMode
            )
            val activeOnColor = WeatherPossumGlass.onColorForTint(activeTop, activeBottom, isDarkMode)

            Box(
                modifier = Modifier
                    .offset { IntOffset(highlightOffset.roundToPx(), 0) }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(5.dp)
                    .zIndex(0f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navSelectedSurface(activeTop, activeBottom, isDarkMode, cornerRadius = 26.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    val interactionSource = remember(tab.id) { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val scale by animateFloatAsState(
                        targetValue = when {
                            isPressed -> 0.94f
                            isSelected -> 1.02f
                            else -> 1f
                        },
                        animationSpec = motionSpec,
                        label = "navScale_${tab.id}"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            activeOnColor
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (isDarkMode) 0.94f else 0.92f
                            )
                        },
                        animationSpec = colorMotionSpec,
                        label = "navLabel_${tab.id}"
                    )

                    val subtitleColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            activeOnColor.copy(alpha = 0.88f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (isDarkMode) 0.72f else 0.78f
                            )
                        },
                        animationSpec = colorMotionSpec,
                        label = "navSubtitle_${tab.id}"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(indicatorShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onTabSelected(tab.id) }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedWeatherIcon(
                            type = tab.icon,
                            modifier = Modifier.size(26.dp),
                            color = labelColor
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            color = labelColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            letterSpacing = 0.2.sp,
                            maxLines = 1
                        )
                        Text(
                            text = tab.subtitle,
                            color = subtitleColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.6.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
