package com.example.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 60/120 FPS Ultra-Smooth Glassmorphic Card
 *
 * Optimized for butter-smooth scrolling and zero frame drops:
 * - Direct static layer rendering without recomposition overhead
 * - Clean hardware-accelerated drop shadows
 * - Specular border gradient
 * - Low-latency touch ripple response
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f),
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

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

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val defaultBorder = remember(outlineColor) {
        Brush.verticalGradient(
            listOf(
                outlineColor.copy(alpha = 0.65f),
                outlineColor.copy(alpha = 0.30f),
                Color.Transparent
            )
        )
    }

    val finalBorderModifier = when {
        borderBrush != null -> Modifier.border(borderWidth, borderBrush, shape)
        borderColor != null -> Modifier.border(borderWidth, borderColor, shape)
        else -> Modifier.border(borderWidth, defaultBorder, shape)
    }

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(elevation = elevation, shape = shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(backgroundColor)
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
    val interactionSource = remember { MutableInteractionSource() }

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
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val defaultBorder = remember(primaryColor, outlineVariant) {
        Brush.linearGradient(
            listOf(
                outlineVariant.copy(alpha = 0.6f),
                primaryColor.copy(alpha = 0.25f),
                Color.Transparent
            )
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
            colors = listOf(
                surfaceColor.copy(alpha = 0.75f),
                surfaceVariant.copy(alpha = 0.50f)
            )
        )
    }

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(elevation = elevation, shape = shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(defaultBackground)
            .then(borderModifier)
            .then(clickModifier),
        content = content
    )
}
