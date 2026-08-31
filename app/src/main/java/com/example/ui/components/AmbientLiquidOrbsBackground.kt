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
import com.example.ui.theme.Local3DGlassEnabled

/**
 * Ultra-Vibrant Liquid Glass Ambient Background (Hardware-Accelerated 120 FPS Optimized)
 *
 * Provides deep rich atmospheric depth with:
 * - Multi-stop cosmic base gradient
 * - Lightweight drawBehind atmospheric glowing auras
 * - Smooth adaptation between 3D Glass mode and Classic flat mode across Light & Dark themes
 */
@Composable
fun AmbientLiquidOrbsBackground(
    accentTheme: AccentTheme,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val is3DEnabled = Local3DGlassEnabled.current
    val orb1Color by animateColorAsState(
        targetValue = accentTheme.orbColors.getOrElse(0) { accentTheme.primaryColor },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "orb1"
    )
    val orb2Color by animateColorAsState(
        targetValue = accentTheme.orbColors.getOrElse(1) { accentTheme.secondaryColor },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "orb2"
    )
    val orb3Color by animateColorAsState(
        targetValue = accentTheme.orbColors.getOrElse(2) { accentTheme.accentGlowColor },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "orb3"
    )
    val orb4Color by animateColorAsState(
        targetValue = accentTheme.orbColors.getOrElse(3) { accentTheme.primaryColor },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "orb4"
    )

    val baseGradient = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF0A0A0C), // Deep Dark Base (0.04, 0.04, 0.05)
                    Color(0xFF0D0D11),
                    Color(0xFF0A0A0C)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFDF7FA), // Soft champagne-rose mist
                    Color(0xFFF3E7F1), // Gentle lilac-quartz tint
                    Color(0xFFECE0EA), // Warm platinum slate
                    Color(0xFFF8EEF5)  // Luminous blush finish
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1800f)
            )
        }
    }

    val orbAlphaMultiplier = if (isDarkTheme) 0.18f else 0.26f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
            .drawBehind {
                if (!is3DEnabled) return@drawBehind

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
