package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Jetpack Compose realization of the Andrew Manzyk Uiverse Animated Liquid Glow Orb.
 *
 * Replaces the traditional speaker icon with a living, morphing molten fluid core,
 * ambient outer glow, spherical refraction highlights, and audio reactivity.
 */
@Composable
fun LiquidGlowOrb(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    isPlaying: Boolean = false,
    audioLevel: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glow_orb_anim")

    // Rotation timers matching the CSS animation speeds (2s base rotation)
    val rot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot1"
    )

    val rotReverse by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotReverse"
    )

    val rot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot3"
    )

    // Colorize keyframe cycle (6s duration: hue shifts from amber #ffbf48 to ruby #be4a1d and deep crimson)
    val colorShiftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    // Morphing / Organic pulse scale
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Dynamic color calculation interpolated based on CSS colorize keyframes
    val colorOne = remember(colorShiftPhase) {
        interpolateColor(
            start = Color(0xFFFFBF48), // #ffbf48
            end = Color(0xFFFF6D3A),
            fraction = colorShiftPhase
        )
    }

    val colorTwo = remember(colorShiftPhase) {
        interpolateColor(
            start = Color(0xFFBE4A1D), // #be4a1d
            end = Color(0xFF8B1D0E),
            fraction = colorShiftPhase
        )
    }

    val colorThree = colorOne.copy(alpha = 0.50f)
    val colorFour = colorTwo.copy(alpha = 0.50f)
    val colorFive = colorOne.copy(alpha = 0.25f)

    val interactiveScale = if (isPlaying) {
        breathingPulse + (audioLevel * 0.18f)
    } else {
        breathingPulse
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = interactiveScale
                scaleY = interactiveScale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 1.5f, color = colorOne),
                onClick = onClick
            )
            .testTag("dictation_liquid_glow_orb"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Multi-layered Ambient Radial Glow & Deep Inset Shadow Aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // CSS: box-shadow: 0 0 25px 0 var(--color-three), 0 20px 50px 0 var(--color-four);
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorThree,
                        colorFour.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = center.copy(y = center.y + (radius * 0.15f)),
                    radius = radius * 1.55f
                ),
                radius = radius * 1.55f,
                center = center.copy(y = center.y + (radius * 0.15f))
            )

            // Upper ambient halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorThree.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.25f
                ),
                radius = radius * 1.25f,
                center = center
            )
        }

        // 2. Spherical Glass Capsule Container (CSS: .loader::before)
        Box(
            modifier = Modifier
                .fillMaxSize(0.82f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorFive, colorFour)
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(colorOne, colorTwo)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 3. Morphing Organic Molten Liquid Core (Andrew Manzyk metaball / SVG clipping mask representation)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                val w = this.size.width
                val h = this.size.height
                val c = Offset(w / 2f, h / 2f)

                // Background gradient within sphere
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorFive, colorFour.copy(alpha = 0.6f))
                    ),
                    center = c,
                    radius = w / 2f
                )

                // Rotating Blob 1 (Base liquid lobe)
                rotate(degrees = rot1, pivot = c) {
                    drawMorphingBlob(
                        center = c,
                        baseRadius = w * 0.34f,
                        amplitude = w * 0.08f,
                        color1 = colorOne,
                        color2 = colorTwo,
                        phase = rot1
                    )
                }

                // Rotating Blob 2 (Counter-rotation liquid lobe)
                rotate(degrees = rotReverse, pivot = Offset(w * 0.48f, h * 0.52f)) {
                    drawMorphingBlob(
                        center = Offset(w * 0.48f, h * 0.52f),
                        baseRadius = w * 0.28f,
                        amplitude = w * 0.07f,
                        color1 = colorOne.copy(alpha = 0.9f),
                        color2 = colorTwo,
                        phase = rotReverse + 45f
                    )
                }

                // Rotating Blob 3 (Organic center droplet)
                rotate(degrees = rot3, pivot = Offset(w * 0.52f, h * 0.46f)) {
                    drawMorphingBlob(
                        center = Offset(w * 0.52f, h * 0.46f),
                        baseRadius = w * 0.24f,
                        amplitude = w * 0.06f,
                        color1 = colorOne,
                        color2 = colorTwo.copy(alpha = 0.85f),
                        phase = rot3 + 90f
                    )
                }

                // Inner core bright molten highlight (Top light refraction)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorOne.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.45f, h * 0.35f),
                        radius = w * 0.28f
                    ),
                    center = Offset(w * 0.45f, h * 0.35f),
                    radius = w * 0.28f
                )

                // Inset spherical rim highlights (CSS: inset 0 10px 10px var(--color-three), inset 0 -10px 10px var(--color-four))
                drawCircle(
                    brush = Brush.verticalGradient(
                        0.0f to colorThree.copy(alpha = 0.6f),
                        0.25f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1.0f to colorFour.copy(alpha = 0.6f)
                    ),
                    radius = (w / 2f) - 1f,
                    center = c,
                    style = Stroke(width = 10.dp.toPx())
                )

                // Top specular glass arc
                drawArc(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            colorOne.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.06f),
                    size = Size(w * 0.70f, h * 0.40f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Draws a fluid harmonic morphing organic blob inside Compose Canvas.
 */
private fun DrawScope.drawMorphingBlob(
    center: Offset,
    baseRadius: Float,
    amplitude: Float,
    color1: Color,
    color2: Color,
    phase: Float
) {
    val path = Path()
    val points = 36
    val radPhase = Math.toRadians(phase.toDouble()).toFloat()

    for (i in 0..points) {
        val angle = (i.toFloat() / points.toFloat()) * (2f * Math.PI.toFloat())
        // Multi-frequency deformation harmonic
        val r = baseRadius +
                amplitude * sin(3f * angle + radPhase) +
                (amplitude * 0.5f) * cos(2f * angle - radPhase)

        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(color1, color2),
            startY = center.y - baseRadius - amplitude,
            endY = center.y + baseRadius + amplitude
        )
    )
}

/**
 * Color interpolation utility.
 */
private fun interpolateColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}
