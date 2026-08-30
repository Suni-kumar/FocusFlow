package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.AccentTheme

/**
 * Ultra-Vibrant Liquid Glass Ambient Background (Hardware-Accelerated 120 FPS Optimized)
 *
 * Provides deep rich atmospheric depth with:
 * - Multi-stop cosmic base gradient
 * - Lightweight drawBehind atmospheric glowing auras
 * - Zero continuous composition CPU/GPU drain for stutter-free scrolling & swiping
 */
@Composable
fun AmbientLiquidOrbsBackground(
    accentTheme: AccentTheme,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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

    val baseGradient = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF0B1326),
                    Color(0xFF0D162B),
                    Color(0xFF080F22),
                    Color(0xFF0A1224),
                    Color(0xFF060D20)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFEDE9FE),
                    Color(0xFFE2E8F0)
                )
            )
        }
    }

    val orbAlphaMultiplier = if (isDarkTheme) 0.25f else 0.12f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
            .drawBehind {
                val canvasW = size.width
                val canvasH = size.height

                // Top-Center Atmospheric Beam
                val topBeamRadius = canvasW * 0.80f
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

                // Orb 1: Top-Left floating aura
                val orb1Radius = canvasW * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb1Color.copy(alpha = orbAlphaMultiplier * 0.80f),
                            orb1Color.copy(alpha = orbAlphaMultiplier * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.15f, canvasH * 0.14f),
                        radius = orb1Radius
                    ),
                    radius = orb1Radius,
                    center = Offset(canvasW * 0.15f, canvasH * 0.14f)
                )

                // Orb 2: Top-Right fluid aura
                val orb2Radius = canvasW * 0.70f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb2Color.copy(alpha = orbAlphaMultiplier * 0.75f),
                            orb3Color.copy(alpha = orbAlphaMultiplier * 0.30f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.88f, canvasH * 0.32f),
                        radius = orb2Radius
                    ),
                    radius = orb2Radius,
                    center = Offset(canvasW * 0.88f, canvasH * 0.32f)
                )

                // Orb 3: Bottom-Left fluid aura
                val orb3Radius = canvasW * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb3Color.copy(alpha = orbAlphaMultiplier * 0.70f),
                            orb1Color.copy(alpha = orbAlphaMultiplier * 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.18f, canvasH * 0.72f),
                        radius = orb3Radius
                    ),
                    radius = orb3Radius,
                    center = Offset(canvasW * 0.18f, canvasH * 0.72f)
                )

                // Orb 4: Bottom-Right auxiliary aura
                val orb4Radius = canvasW * 0.60f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb4Color.copy(alpha = orbAlphaMultiplier * 0.65f),
                            orb2Color.copy(alpha = orbAlphaMultiplier * 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.82f, canvasH * 0.86f),
                        radius = orb4Radius
                    ),
                    radius = orb4Radius,
                    center = Offset(canvasW * 0.82f, canvasH * 0.86f)
                )
            }
    ) {
        content()
    }
}
