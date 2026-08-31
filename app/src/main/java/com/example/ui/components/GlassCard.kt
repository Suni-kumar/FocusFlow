package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Local3DGlassEnabled
import com.example.ui.theme.LocalAccentTheme
import com.example.ui.util.AppHaptic

/**
 * Standard GlassCard that dynamically switches between 3D Liquid Glassmorphism
 * (with interactive perspective tilt, specular reflections & refraction highlights)
 * and Classic Flat Minimalist Mode based on the user's 3D Glass setting.
 * Supports both Light and Dark themes seamlessly.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val is3DEnabled = Local3DGlassEnabled.current
    val accentTheme = LocalAccentTheme.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    var isPressed by remember { mutableStateOf(false) }
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val view = LocalView.current

    // Dynamic 3D tilt angles on touch/press
    val tiltX by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed && touchPosition != Offset.Unspecified && componentSize.height > 0) {
            val normalizedY = (touchPosition.y / componentSize.height.toFloat()) - 0.5f
            -normalizedY * 16f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glassTiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed && touchPosition != Offset.Unspecified && componentSize.width > 0) {
            val normalizedX = (touchPosition.x / componentSize.width.toFloat()) - 0.5f
            normalizedX * 16f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glassTiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) (if (is3DEnabled) 0.965f else 0.985f) else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassScale"
    )

    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    // 3D Glass vs Flat Border styling
    val defaultBorderBrush = remember(is3DEnabled, isDark, accentTheme, outlineVariant) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f), // Top-left specular glint
                        accentTheme.primaryColor.copy(alpha = 0.65f),
                        accentTheme.secondaryColor.copy(alpha = 0.35f),
                        outlineVariant.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.15f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f), // Luminous top-left crystal highlight
                        accentTheme.primaryColor.copy(alpha = 0.55f),
                        Color(0xFFCBD5E1).copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.85f),
                        accentTheme.secondaryColor.copy(alpha = 0.30f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            }
        } else {
            Brush.verticalGradient(
                listOf(
                    outlineVariant.copy(alpha = 0.45f),
                    outlineVariant.copy(alpha = 0.25f)
                )
            )
        }
    }

    val finalBorderModifier = if (borderColor != null) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier.border(if (is3DEnabled) (borderWidth + 0.5.dp) else borderWidth, defaultBorderBrush, shape)
    }

    // Dynamic 3D refraction shimmer
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed) (if (isDark) 0.25f else 0.45f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassShimmerAlpha"
    )

    // Background calculation
    val finalBackgroundModifier = remember(backgroundColor, is3DEnabled, isDark, surfaceContainerLow, surfaceContainer, accentTheme) {
        if (backgroundColor != null) {
            Modifier.background(backgroundColor)
        } else if (is3DEnabled) {
            if (isDark) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1C38).copy(alpha = 0.82f),
                            Color(0xFF111022).copy(alpha = 0.90f)
                        )
                    )
                )
            } else {
                Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF8FC).copy(alpha = 0.84f), // Soft luminous crystal pink-tint
                            Color(0xFFF7ECF4).copy(alpha = 0.72f), // Semi-translucent core
                            Color(0xFFEFE2EC).copy(alpha = 0.80f)  // Warm platinum frost bottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(600f, 800f)
                    )
                )
            }
        } else {
            Modifier.background(surfaceContainerLow)
        }
    }

    // Depth Shadow
    val actualElevation = if (elevation > 0.dp) elevation else if (is3DEnabled) 6.dp else 2.dp
    val shadowModifier = if (is3DEnabled) {
        Modifier.shadow(
            elevation = actualElevation,
            shape = shape,
            ambientColor = if (isDark) accentTheme.primaryColor.copy(alpha = 0.40f) else accentTheme.primaryColor.copy(alpha = 0.25f),
            spotColor = if (isDark) accentTheme.secondaryColor.copy(alpha = 0.35f) else Color(0x383B2544)
        )
    } else {
        Modifier.shadow(elevation = actualElevation.coerceAtMost(3.dp), shape = shape)
    }

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = accentTheme.primaryColor),
            onClick = {
                AppHaptic.vibrateClick(context, view)
                onClick?.invoke()
            },
            onLongClick = {
                AppHaptic.vibrateHeavy(context, view)
                onLongClick?.invoke()
            }
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(shadowModifier)
            .onSizeChanged { componentSize = it }
            .pointerInput(is3DEnabled) {
                if (is3DEnabled) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        touchPosition = down.position
                        isPressed = true

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                touchPosition = change.position
                            }
                        } while (event.changes.any { it.pressed })

                        isPressed = false
                        touchPosition = Offset.Unspecified
                    }
                }
            }
            .graphicsLayer {
                if (is3DEnabled) {
                    rotationX = tiltX
                    rotationY = tiltY
                    scaleX = scale
                    scaleY = scale
                    cameraDistance = 16f * density
                } else {
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(shape)
            .then(finalBackgroundModifier)
            .drawWithContent {
                drawContent()
                if (is3DEnabled) {
                    // Top-edge specular glint sheen
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.12f else 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height.coerceAtMost(60f)
                        )
                    )

                    // Touch refraction light ray
                    if (shimmerAlpha > 0f) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    (if (isDark) Color.White else accentTheme.primaryColor).copy(alpha = shimmerAlpha),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                    }
                }
            }
            .then(finalBorderModifier)
            .then(clickModifier),
        content = content
    )
}

/**
 * Premium Liquid Glass Card with enhanced multi-layered specular borders
 * and refraction depth for primary hero cards, folders, and flashcards.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    accentBrush: Brush? = null,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val is3DEnabled = Local3DGlassEnabled.current
    val accentTheme = LocalAccentTheme.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    var isPressed by remember { mutableStateOf(false) }
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val view = LocalView.current

    val tiltX by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 14f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 14f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) (if (is3DEnabled) 0.95f else 0.98f) else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    val defaultBorder = remember(is3DEnabled, isDark, primaryColor, outlineVariant, accentTheme) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        accentTheme.primaryColor.copy(alpha = 0.45f),
                        outlineVariant.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.98f),
                        accentTheme.primaryColor.copy(alpha = 0.50f),
                        outlineVariant.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.80f),
                        accentTheme.secondaryColor.copy(alpha = 0.30f)
                    )
                )
            }
        } else {
            Brush.linearGradient(
                listOf(outlineVariant.copy(alpha = 0.6f), primaryColor.copy(alpha = 0.25f), Color.Transparent)
            )
        }
    }

    val borderModifier = if (accentBrush != null) {
        Modifier.border(1.4.dp, accentBrush, shape)
    } else {
        Modifier.border(1.2.dp, defaultBorder, shape)
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceContainerHigh

    val defaultBackground = remember(is3DEnabled, isDark, surfaceColor, surfaceVariant, accentTheme) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = 0.80f),
                        surfaceVariant.copy(alpha = 0.60f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF8FC).copy(alpha = 0.86f),
                        Color(0xFFF6EAF3).copy(alpha = 0.74f),
                        Color(0xFFEEDEEB).copy(alpha = 0.82f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                colors = listOf(surfaceColor, surfaceVariant)
            )
        }
    }

    val shimmerAlpha by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed) 0.35f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    val shadowModifier = if (is3DEnabled && elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) accentTheme.primaryColor.copy(alpha = 0.40f) else accentTheme.primaryColor.copy(alpha = 0.25f),
            spotColor = if (isDark) accentTheme.secondaryColor.copy(alpha = 0.35f) else Color(0x383B2544)
        )
    } else if (elevation > 0.dp) {
        Modifier.shadow(elevation = elevation, shape = shape)
    } else Modifier

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = accentTheme.primaryColor),
            onClick = {
                AppHaptic.vibrateClick(context, view)
                onClick?.invoke()
            },
            onLongClick = {
                AppHaptic.vibrateHeavy(context, view)
                onLongClick?.invoke()
            }
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(shadowModifier)
            .onSizeChanged { componentSize = it }
            .pointerInput(is3DEnabled) {
                if (is3DEnabled) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                if (change.pressed) {
                                    isPressed = true
                                    touchPosition = change.position
                                } else {
                                    isPressed = false
                                    touchPosition = Offset.Unspecified
                                }
                            }
                        }
                    }
                }
            }
            .graphicsLayer {
                if (is3DEnabled) {
                    rotationX = tiltX
                    rotationY = tiltY
                    scaleX = scale
                    scaleY = scale
                    cameraDistance = 14f * density
                } else {
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(shape)
            .background(defaultBackground)
            .drawWithContent {
                drawContent()
                if (is3DEnabled && shimmerAlpha > 0f) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                (if (isDark) Color.White else accentTheme.primaryColor).copy(alpha = shimmerAlpha),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
            }
            .then(borderModifier)
            .then(clickModifier),
        content = content
    )
}
