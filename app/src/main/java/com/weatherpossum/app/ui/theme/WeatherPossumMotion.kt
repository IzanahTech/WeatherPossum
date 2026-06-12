package com.weatherpossum.app.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/**
 * Shared motion language: fluid springs and emphasized easing without cartoon bounce.
 */
object WeatherPossumMotion {

    const val FluidDamping = 0.86f
    const val FluidStiffness = 380f

    const val StaggerBaseMs = 48
    const val StaggerStepMs = 56
    const val ContentDurationMs = 400
    const val ExitDurationMs = 280

    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val AmbientDrift = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)

    fun staggerDelay(index: Int): Int = StaggerBaseMs + index * StaggerStepMs

    fun <T> fluidSpring(): SpringSpec<T> = SpringSpec(
        dampingRatio = FluidDamping,
        stiffness = FluidStiffness
    )

    fun <T> gentleSpring(): SpringSpec<T> = SpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> enterTween(index: Int = 0, durationMs: Int = ContentDurationMs): TweenSpec<T> = tween(
        durationMillis = durationMs,
        delayMillis = staggerDelay(index),
        easing = EmphasizedDecelerate
    )

    fun <T> exitTween(durationMs: Int = ExitDurationMs): TweenSpec<T> = tween(
        durationMillis = durationMs,
        easing = EmphasizedAccelerate
    )

    fun factTextTransitionSpec(): AnimatedContentTransitionScope<String>.() -> ContentTransform = {
        (
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 4 },
                animationSpec = fluidSpring()
            ) + fadeIn(tween(360, easing = EmphasizedDecelerate))
            ) togetherWith (
            slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight / 5 },
                animationSpec = fluidSpring()
            ) + fadeOut(tween(280, easing = EmphasizedAccelerate))
            )
    }
}
