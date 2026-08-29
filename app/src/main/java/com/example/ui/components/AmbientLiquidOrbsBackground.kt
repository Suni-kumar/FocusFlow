package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-Vibrant 8K Atmospheric Ambient Liquid Orbs Background
 *
 * Implements an ultra-high-fidelity dynamic atmospheric background featuring:
 * - Multi-stop cosmic base gradient (avoiding flat solid blacks or greys)
 * - 4 fluidly orbiting, multi-chromatic glowing plasma orbs with smooth easing
 * - Top-center atmospheric specular aura that reflects the active accent theme
 * - Silky smooth 144Hz-grade continuous animations
 */
@Composable
fun AmbientLiquidOrbsBackground(
    accentTheme: AccentTheme,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Animate color transitions with fluid spring response
    val orb1TargetColor = accentTheme.orbColors.getOrElse(0) { accentTheme.primaryColor }
    val orb2TargetColor = accentTheme.orbColors.getOrElse(1) { accentTheme.secondaryColor }
    val orb3TargetColor = accentTheme.orbColors.getOrElse(2) { accentTheme.accentGlowColor }
    val orb4TargetColor = accentTheme.orbColors.getOrElse(3) { accentTheme.primaryColor }

    val orb1Color by animateColorAsState(
        targetValue = orb1TargetColor,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "orb1Color"
    )
    val orb2Color by animateColorAsState(
        targetValue = orb2TargetColor,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "orb2Color"
    )
    val orb3Color by animateColorAsState(
        targetValue = orb3TargetColor,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "orb3Color"
    )
    val orb4Color by animateColorAsState(
        targetValue = orb4TargetColor,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "orb4Color"
    )

    // Infinite gentle, fluid orbiting motions
    val infiniteTransition = rememberInfiniteTransition(label = "ambientOrbsMotion")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    // Dynamic Multi-Stop Cosmic Base Gradient (Eliminates flat single-color look)
    val baseGradient = if (isDarkTheme) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0C0A1A), // Deep Cosmic Indigo
                Color(0xFF140F2E), // Luminous Twilight Violet
                Color(0xFF080712), // Obsidian Shadow
                Color(0xFF0E0A22), // Midnight Amethyst
                Color(0xFF06050C)  // Pure Void Base
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF8FAFC), // Crisp Slate
                Color(0xFFEEF2FF), // Soft Indigo Mist
                Color(0xFFFAF5FF), // Soft Lavender Mist
                Color(0xFFF1F5F9), // Crisp Canvas
                Color(0xFFE2E8F0)  // Subtle Foundation
            )
        )
    }

    val orbAlphaMultiplier = if (isDarkTheme) 0.38f else 0.16f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
    ) {
        // Draw fluid ambient radial orbs and chromatic lighting
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Top-Center Atmospheric Accent Beam (Gives rich 8K depth highlight)
            val topBeamRadius = canvasW * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb1Color.copy(alpha = orbAlphaMultiplier * 0.65f),
                        orb2Color.copy(alpha = orbAlphaMultiplier * 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.5f, 0f),
                    radius = topBeamRadius
                ),
                radius = topBeamRadius,
                center = Offset(canvasW * 0.5f, 0f)
            )

            // Orb 1: Top-Left floating anchor
            val orb1X = canvasW * 0.18f + (canvasW * 0.12f * cos(phase1.toDouble())).toFloat()
            val orb1Y = canvasH * 0.16f + (canvasH * 0.10f * sin(phase1.toDouble())).toFloat()
            val orb1Radius = canvasW * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb1Color.copy(alpha = orbAlphaMultiplier * 1.1f),
                        orb1Color.copy(alpha = orbAlphaMultiplier * 0.55f),
                        orb2Color.copy(alpha = orbAlphaMultiplier * 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = Offset(orb1X, orb1Y)
            )

            // Orb 2: Top-Right / Center-Right fluid anchor
            val orb2X = canvasW * 0.85f + (canvasW * 0.14f * sin(phase2.toDouble())).toFloat()
            val orb2Y = canvasH * 0.36f + (canvasH * 0.12f * cos(phase2.toDouble())).toFloat()
            val orb2Radius = canvasW * 0.70f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb2Color.copy(alpha = orbAlphaMultiplier * 1.0f),
                        orb3Color.copy(alpha = orbAlphaMultiplier * 0.50f),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = Offset(orb2X, orb2Y)
            )

            // Orb 3: Bottom-Left / Center-Bottom fluid anchor
            val orb3X = canvasW * 0.22f + (canvasW * 0.15f * cos(phase3.toDouble())).toFloat()
            val orb3Y = canvasH * 0.75f + (canvasH * 0.11f * sin(phase3.toDouble())).toFloat()
            val orb3Radius = canvasW * 0.72f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb3Color.copy(alpha = orbAlphaMultiplier * 0.95f),
                        orb1Color.copy(alpha = orbAlphaMultiplier * 0.40f),
                        Color.Transparent
                    ),
                    center = Offset(orb3X, orb3Y),
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = Offset(orb3X, orb3Y)
            )

            // Orb 4: Bottom-Right auxiliary glow
            val orb4X = canvasW * 0.82f + (canvasW * 0.10f * sin(phase1.toDouble() + 1.5f)).toFloat()
            val orb4Y = canvasH * 0.88f + (canvasH * 0.09f * cos(phase2.toDouble() + 1.2f)).toFloat()
            val orb4Radius = canvasW * 0.60f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb4Color.copy(alpha = orbAlphaMultiplier * 0.85f),
                        orb2Color.copy(alpha = orbAlphaMultiplier * 0.35f),
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

