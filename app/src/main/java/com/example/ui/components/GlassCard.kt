package com.example.ui.components
import androidx.compose.ui.zIndex

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isExpanding by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanding) 12f else if (isPressed) 0.94f else 1f,
        animationSpec = if (isExpanding) androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing) else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val zIndex by animateFloatAsState(targetValue = if (isExpanding || isPressed) 100f else 0f, label = "zIndex")

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            onClick = { 
                if (onClick != null) {
                    coroutineScope.launch {
                        isExpanding = true
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        kotlinx.coroutines.delay(180)
                        onClick.invoke()
                        kotlinx.coroutines.delay(200)
                        isExpanding = false
                    }
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onLongClick?.invoke()
            }
        )
    } else Modifier

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val defaultBorder = remember(outlineColor) {
        Brush.verticalGradient(
            listOf(outlineColor.copy(alpha = 0.6f), outlineColor.copy(alpha = 0.3f), Color.Transparent)
        )
    }

    val finalBorderModifier = if (borderColor != null) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier.border(borderWidth, defaultBorder, shape)
    }

    // Shimmer/Glow calculation based on tilt
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .onSizeChanged { componentSize = it }
            .zIndex(zIndex)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchPosition = down.position
                    isPressed = true
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null) {
                            touchPosition = change.position
                        }
                    } while (event.changes.any { it.pressed })
                    
                    isPressed = false
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    touchPosition = Offset.Unspecified
                }
            }
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
            .clip(shape)
            .background(backgroundColor)
            .drawWithContent {
                drawContent()
                if (shimmerAlpha > 0f) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = shimmerAlpha),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
            }
            .then(finalBorderModifier)
            .then(clickModifier),
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    accentBrush: Brush? = null,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isExpanding by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanding) 12f else if (isPressed) 0.94f else 1f,
        animationSpec = if (isExpanding) androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing) else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val zIndex by animateFloatAsState(targetValue = if (isExpanding || isPressed) 100f else 0f, label = "zIndex")

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            onClick = { 
                if (onClick != null) {
                    coroutineScope.launch {
                        isExpanding = true
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        kotlinx.coroutines.delay(180)
                        onClick.invoke()
                        kotlinx.coroutines.delay(200)
                        isExpanding = false
                    }
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onLongClick?.invoke()
            }
        )
    } else Modifier

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val defaultBorder = remember(primaryColor, outlineVariant) {
        Brush.linearGradient(
            listOf(outlineVariant.copy(alpha = 0.6f), primaryColor.copy(alpha = 0.25f), Color.Transparent)
        )
    }

    val borderModifier = if (accentBrush != null) {
        Modifier.border(1.2.dp, accentBrush, shape)
    } else {
        Modifier.border(1.dp, defaultBorder, shape)
    }
    
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceContainerHigh
    val defaultBackground = remember(surfaceColor, surfaceVariant) {
        Brush.verticalGradient(
            colors = listOf(surfaceColor.copy(alpha = 0.75f), surfaceVariant.copy(alpha = 0.50f))
        )
    }

    val shimmerAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.2f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .onSizeChanged { componentSize = it }
            .zIndex(zIndex)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchPosition = down.position
                    isPressed = true
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null) {
                            touchPosition = change.position
                        }
                    } while (event.changes.any { it.pressed })
                    
                    isPressed = false
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    touchPosition = Offset.Unspecified
                }
            }
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
            .clip(shape)
            .background(defaultBackground)
            .drawWithContent {
                drawContent()
                if (shimmerAlpha > 0f) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = shimmerAlpha), Color.Transparent),
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
