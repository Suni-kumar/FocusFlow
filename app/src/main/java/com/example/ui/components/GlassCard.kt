package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ultra-Responsive 8K Spring Glassmorphic Card
 *
 * Implements:
 * - 144Hz tactile spring physics on touch (subtle elastic compression and recoil)
 * - Multi-stop frosted translucent surface gradient
 * - Specular edge highlight (top rim light catch)
 * - Chromatic ambient drop shadow aligned with the active theme
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 144fps-grade tactile spring physics scale
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.968f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 650f
        ),
        label = "glassCardScale"
    )

    val activeSpotAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.35f else 0.18f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "glassSpotAlpha"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        Modifier
    }

    val finalBorderModifier = if (borderBrush != null) {
        Modifier.border(borderWidth, borderBrush, shape)
    } else {
        Modifier.border(
            borderWidth,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.28f),
                    borderColor,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    Color.Transparent
                )
            ),
            shape
        )
    }

    Box(
        modifier = modifier
            .scale(cardScale)
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = activeSpotAlpha),
                ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                )
            )
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
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.965f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 650f
        ),
        label = "liquidCardScale"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        Modifier
    }

    val borderModifier = if (accentBrush != null) {
        Modifier.border(1.5.dp, accentBrush, shape)
    } else {
        Modifier.border(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    Color.Transparent
                )
            ),
            shape
        )
    }

    Box(
        modifier = modifier
            .scale(cardScale)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                    )
                )
            )
            .then(borderModifier)
            .then(clickModifier),
        content = content
    )
}

