package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.util.AppHaptic

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
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val view = LocalView.current

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 10f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 10f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

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

    val shimmerAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
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
            .onSizeChanged { componentSize = it }
            .pointerInput(Unit) {
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
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val view = LocalView.current

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 10f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 10f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

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
        targetValue = if (isPressed) 0.22f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
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
            .onSizeChanged { componentSize = it }
            .pointerInput(Unit) {
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
