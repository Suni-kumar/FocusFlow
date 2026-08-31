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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
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

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val view = LocalView.current

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

    val finalBorderModifier = remember(borderColor, borderWidth, defaultBorder, shape) {
        if (borderColor != null) {
            Modifier.border(borderWidth, borderColor, shape)
        } else {
            Modifier.border(borderWidth, defaultBorder, shape)
        }
    }

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

    val shadowModifier = remember(is3DEnabled, elevation, shape, isDark, accentTheme) {
        if (is3DEnabled && elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) accentTheme.primaryColor.copy(alpha = 0.30f) else Color(0x20000000),
                spotColor = if (isDark) accentTheme.secondaryColor.copy(alpha = 0.25f) else Color(0x30000000)
            )
        } else if (elevation > 0.dp) {
            Modifier.shadow(elevation = elevation, shape = shape)
        } else Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .then(finalBackgroundModifier)
            .then(finalBorderModifier)
            .then(clickModifier),
        content = content
    )
}
