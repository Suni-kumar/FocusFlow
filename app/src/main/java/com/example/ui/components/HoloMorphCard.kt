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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HoloMorphCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
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
        targetValue = if (isPressed) (if (is3DEnabled) 0.96f else 0.98f) else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

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

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    val defaultBorder = remember(is3DEnabled, isDark, outlineColor, accentTheme) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        accentTheme.primaryColor.copy(alpha = 0.35f),
                        outlineColor.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.90f),
                        accentTheme.secondaryColor.copy(alpha = 0.25f),
                        outlineColor.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.50f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                listOf(outlineColor.copy(alpha = 0.5f), outlineColor.copy(alpha = 0.25f))
            )
        }
    }

    val finalBorderModifier = if (borderColor != null) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier.border(borderWidth, defaultBorder, shape)
    }

    val shimmerAlpha by animateFloatAsState(
        targetValue = if (is3DEnabled && isPressed) 0.20f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "shimmerAlpha"
    )

    val finalBackgroundModifier = remember(backgroundColor, is3DEnabled, isDark, surfaceContainerLow, surfaceContainer) {
        if (backgroundColor != null) {
            Modifier.background(backgroundColor)
        } else if (is3DEnabled) {
            if (isDark) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            surfaceContainerLow.copy(alpha = 0.75f),
                            surfaceContainer.copy(alpha = 0.55f)
                        )
                    )
                )
            } else {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            surfaceContainerLow.copy(alpha = 0.72f)
                        )
                    )
                )
            }
        } else {
            Modifier.background(surfaceContainerLow)
        }
    }

    val shadowModifier = if (is3DEnabled && elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) accentTheme.primaryColor.copy(alpha = 0.30f) else Color(0x20000000),
            spotColor = if (isDark) accentTheme.secondaryColor.copy(alpha = 0.25f) else Color(0x30000000)
        )
    } else if (elevation > 0.dp) {
        Modifier.shadow(elevation = elevation, shape = shape)
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
                    cameraDistance = 14f * density
                } else {
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(shape)
            .then(finalBackgroundModifier)
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
            .then(finalBorderModifier)
            .then(clickModifier),
        content = content
    )
}
