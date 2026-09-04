package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AccentTheme
import com.example.ui.theme.LocalAccentTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native Jetpack Compose Audio-Reactive Radar Visualizer.
 *
 * Replaces the heavy molten orb with a lightweight, high-performance futuristic HUD radar
 * inspired by the Uiverse CSS radar loader.
 *
 * Features:
 * - Concentric circular rings (including dashed middle radar ring)
 * - Continuous 360-degree rotating radar sweep with a soft glowing sector
 * - Real microphone reactivity derived from DictationVoiceCommander RMS levels
 * - Smooth attack/decay audio interpolation (no jitter or random pulsing)
 * - Automatic adaptation to AccentTheme (BIO_MATRIX, CYBER_CORE, DEEP_VELVET, NEON_ELECTRIC, etc.)
 * - Native Dark and Light mode contrast optimization
 * - Strict bounding box containment to eliminate full-screen color bleed
 */
@Composable
fun AudioRadarVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    isActive: Boolean = true,
    isListening: Boolean = true,
    audioLevel: Float = 0f,
    accentTheme: AccentTheme = LocalAccentTheme.current,
    isDarkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    onClick: () -> Unit = {}
) {
    // 1. Radar Sweep Angle Rotation (Smooth 3.2s continuous clockwise sweep)
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep_transition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Subtle ambient breathing for idle state
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_idle_pulse"
    )

    // 2. Audio Smoothing Filter with Asymmetric Attack / Decay
    // Attack is rapid (60ms) to catch speech onset immediately;
    // Decay is gentle (240ms) so silence settles smoothly without jitter.
    val targetAudio = if (isActive && isListening) audioLevel.coerceIn(0f, 1f) else 0f
    val smoothedAudio by animateFloatAsState(
        targetValue = targetAudio,
        animationSpec = tween(
            durationMillis = if (targetAudio > 0.1f) 70 else 250,
            easing = FastOutSlowInEasing
        ),
        label = "radar_audio_smooth"
    )

    // Theme Color Palette Extraction
    val primary = accentTheme.primaryColor
    val secondary = accentTheme.secondaryColor
    val glowColor = accentTheme.accentGlowColor

    // Colors tailored for Dark vs Light mode
    val backgroundColor = if (isDarkTheme) {
        Color(0xFF0C0D14)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f)
    }

    val lightBgSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val lightBgSurfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val lightBgSurface = MaterialTheme.colorScheme.surfaceContainer

    val radarRingBaseColor = if (isDarkTheme) {
        primary.copy(alpha = 0.30f)
    } else {
        secondary.copy(alpha = 0.45f)
    }

    val radarDashedRingColor = if (isDarkTheme) {
        primary.copy(alpha = 0.40f + (smoothedAudio * 0.35f))
    } else {
        secondary.copy(alpha = 0.55f + (smoothedAudio * 0.30f))
    }

    val radarOuterBorderColor = if (isDarkTheme) {
        primary.copy(alpha = 0.22f + (smoothedAudio * 0.20f))
    } else {
        secondary.copy(alpha = 0.30f + (smoothedAudio * 0.15f))
    }

    val sweepSectorLeadingColor = if (isDarkTheme) {
        glowColor.copy(alpha = (0.28f + (smoothedAudio * 0.35f)).coerceIn(0.20f, 0.75f))
    } else {
        secondary.copy(alpha = (0.32f + (smoothedAudio * 0.30f)).coerceIn(0.25f, 0.70f))
    }

    val coreColor = if (isDarkTheme) primary else secondary
    val coreGlowColor = glowColor

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2f, color = primary),
                onClick = onClick
            )
            .testTag("dictation_radar_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val maxRadius = (canvasWidth.coerceAtMost(canvasHeight) / 2f) * 0.94f

            // A. Dark HUD Radar Background Disk (Restrained, Localized)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            Color(0xFF161524),
                            Color(0xFF0F0E18),
                            Color(0xFF0A0A10)
                        )
                    } else {
                        listOf(
                            lightBgSurfaceHighest,
                            lightBgSurfaceHigh,
                            lightBgSurface
                        )
                    },
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // B. Concentric Radar Rings (Matching Uiverse Reference)
            val outerRadius = maxRadius
            val dashedRadius = maxRadius * 0.68f
            val innerRadius = maxRadius * 0.40f
            val coreRingRadius = maxRadius * 0.22f

            // 1. Outer Solid Boundary Ring
            drawCircle(
                color = radarOuterBorderColor,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. Middle Dashed Radar Ring (Key reference characteristic)
            val dashInterval = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
            drawCircle(
                color = radarDashedRingColor,
                radius = dashedRadius,
                center = center,
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(dashInterval, 0f)
                )
            )

            // 3. Inner Concentric Ring
            drawCircle(
                color = radarRingBaseColor.copy(alpha = (radarRingBaseColor.alpha + smoothedAudio * 0.2f).coerceIn(0f, 1f)),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Core Boundary Orbit Ring
            drawCircle(
                color = radarRingBaseColor.copy(alpha = (radarRingBaseColor.alpha * 0.75f)),
                radius = coreRingRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 5. Subtle Azimuth Axis Marks (HUD Ticks at 0°, 90°, 180°, 270°)
            val tickLength = 7.dp.toPx()
            for (angleDeg in listOf(0f, 90f, 180f, 270f)) {
                val rad = Math.toRadians(angleDeg.toDouble())
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()
                val start = Offset(center.x + (outerRadius - tickLength) * cosA, center.y + (outerRadius - tickLength) * sinA)
                val end = Offset(center.x + outerRadius * cosA, center.y + outerRadius * sinA)
                drawLine(
                    color = radarRingBaseColor,
                    start = start,
                    end = end,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // C. Rotating Radar Sweep (Trailing Glow Sector + Leading Radial Line)
            rotate(degrees = sweepAngle, pivot = center) {
                // Sweep sector: 75-degree gradient trailing arc behind the leading sweep line (from -75° to 0°)
                val sectorAngle = 75f

                // Render trailing sector using a localized arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            ((360f - sectorAngle) / 360f) to Color.Transparent,
                            ((360f - (sectorAngle * 0.5f)) / 360f) to sweepSectorLeadingColor.copy(alpha = sweepSectorLeadingColor.alpha * 0.25f),
                            1f to sweepSectorLeadingColor
                        ),
                        center = center
                    ),
                    startAngle = 360f - sectorAngle,
                    sweepAngle = sectorAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2f, outerRadius * 2f)
                )

                // Leading crisp sweep line from center to outer rim
                val sweepLineEnd = Offset(center.x + outerRadius, center.y)
                val lineStrokePx = (1.5f + (smoothedAudio * 0.8f)).dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            coreColor.copy(alpha = 0.5f),
                            coreColor,
                            Color.White.copy(alpha = if (isDarkTheme) 0.9f else 0.7f)
                        ),
                        startX = center.x,
                        endX = center.x + outerRadius
                    ),
                    start = center,
                    end = sweepLineEnd,
                    strokeWidth = lineStrokePx,
                    cap = StrokeCap.Round
                )

                // Leading Tip Glow Ping on the outer border
                val tipRadiusPx = (2.2f + (smoothedAudio * 1.5f)).dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = if (isDarkTheme) 0.85f else 0.65f),
                    radius = tipRadiusPx,
                    center = sweepLineEnd
                )
            }

            // D. Central Audio-Reactive Core
            // Base radius 10dp, expanding smoothly up to 18dp depending on speech amplitude
            val audioCoreScale = 1.0f + (smoothedAudio * 0.65f)
            val baseCoreRadius = 10.dp.toPx() * audioCoreScale * idlePulse
            val coreHaloRadius = baseCoreRadius * (1.8f + (smoothedAudio * 0.6f))

            // 1. Ambient Radial Halo around core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor.copy(alpha = if (isDarkTheme) 0.45f * (0.5f + smoothedAudio * 0.5f) else 0.35f * (0.5f + smoothedAudio * 0.5f)),
                        coreGlowColor.copy(alpha = 0.15f * (0.5f + smoothedAudio * 0.5f)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreHaloRadius
                ),
                radius = coreHaloRadius,
                center = center
            )

            // 2. Central Core Solid Disk
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            coreColor,
                            coreColor.copy(alpha = 0.8f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            coreColor,
                            coreColor.copy(alpha = 0.9f)
                        )
                    },
                    center = center,
                    radius = baseCoreRadius
                ),
                radius = baseCoreRadius,
                center = center
            )

            // 3. Central Core Center Micro-Dot
            drawCircle(
                color = Color.White,
                radius = (baseCoreRadius * 0.32f).coerceAtLeast(1.5.dp.toPx()),
                center = center
            )
        }
    }
}
