package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.AccentTheme
import com.example.ui.theme.CanvasLight
import com.example.ui.theme.CanvasVoidDark
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic Ambient Liquid Orbs Background
 *
 * Implements the dark luxury glassmorphic visual system with 4 slowly floating,
 * fluid radial color orbs that dynamically shift their hue and position when
 * the active accent theme changes.
 */
@Composable
fun AmbientLiquidOrbsBackground(
    accentTheme: AccentTheme,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Animate color transitions smoothly (300-450ms)
    val orb1TargetColor = accentTheme.orbColors.getOrElse(0) { accentTheme.primaryColor }
    val orb2TargetColor = accentTheme.orbColors.getOrElse(1) { accentTheme.secondaryColor }
    val orb3TargetColor = accentTheme.orbColors.getOrElse(2) { accentTheme.accentGlowColor }
    val orb4TargetColor = accentTheme.orbColors.getOrElse(3) { accentTheme.primaryColor }

    val orb1Color by animateColorAsState(
        targetValue = orb1TargetColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb1Color"
    )
    val orb2Color by animateColorAsState(
        targetValue = orb2TargetColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb2Color"
    )
    val orb3Color by animateColorAsState(
        targetValue = orb3TargetColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb3Color"
    )
    val orb4Color by animateColorAsState(
        targetValue = orb4TargetColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb4Color"
    )

    // Infinite gentle floating animations
    val infiniteTransition = rememberInfiniteTransition(label = "ambientOrbsMotion")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    val baseBackground = if (isDarkTheme) CanvasVoidDark else CanvasLight
    val orbAlphaMultiplier = if (isDarkTheme) 0.24f else 0.10f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
    ) {
        // Draw fluid ambient radial orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Orb 1: Top-Left floating anchor
            val orb1X = canvasW * 0.20f + (canvasW * 0.10f * cos(phase1.toDouble())).toFloat()
            val orb1Y = canvasH * 0.15f + (canvasH * 0.08f * sin(phase1.toDouble())).toFloat()
            val orb1Radius = canvasW * 0.55f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb1Color.copy(alpha = orbAlphaMultiplier),
                        orb1Color.copy(alpha = orbAlphaMultiplier * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = Offset(orb1X, orb1Y)
            )

            // Orb 2: Top-Right / Center-Right fluid anchor
            val orb2X = canvasW * 0.85f + (canvasW * 0.12f * sin(phase2.toDouble())).toFloat()
            val orb2Y = canvasH * 0.38f + (canvasH * 0.10f * cos(phase2.toDouble())).toFloat()
            val orb2Radius = canvasW * 0.60f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb2Color.copy(alpha = orbAlphaMultiplier * 0.9f),
                        orb2Color.copy(alpha = orbAlphaMultiplier * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = Offset(orb2X, orb2Y)
            )

            // Orb 3: Bottom-Left / Center-Bottom fluid anchor
            val orb3X = canvasW * 0.25f + (canvasW * 0.14f * cos(phase3.toDouble())).toFloat()
            val orb3Y = canvasH * 0.78f + (canvasH * 0.09f * sin(phase3.toDouble())).toFloat()
            val orb3Radius = canvasW * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb3Color.copy(alpha = orbAlphaMultiplier * 0.85f),
                        orb3Color.copy(alpha = orbAlphaMultiplier * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(orb3X, orb3Y),
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = Offset(orb3X, orb3Y)
            )

            // Orb 4: Bottom-Right auxiliary glow
            val orb4X = canvasW * 0.80f + (canvasW * 0.08f * sin(phase1.toDouble() + 1.5f)).toFloat()
            val orb4Y = canvasH * 0.90f + (canvasH * 0.07f * cos(phase2.toDouble() + 1.2f)).toFloat()
            val orb4Radius = canvasW * 0.50f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb4Color.copy(alpha = orbAlphaMultiplier * 0.7f),
                        orb4Color.copy(alpha = orbAlphaMultiplier * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(orb4X, orb4Y),
                    radius = orb4Radius
                ),
                radius = orb4Radius,
                center = Offset(orb4X, orb4Y)
            )
        }

        // Foreground content with transparent/frosted glass layers
        content()
    }
}
