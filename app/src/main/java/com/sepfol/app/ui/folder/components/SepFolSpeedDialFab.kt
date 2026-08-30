package com.sepfol.app.ui.folder.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SepFolSpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onCreateFolderClick: () -> Unit,
    onMakeNotesClick: () -> Unit,
    onImportFileClick: () -> Unit,
    onSwipeUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }

    // Rotation animation: 0 deg (Plus '+') -> 135 deg (Cross '✕')
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )

    // Morphing shape animation: Circle (28dp radius) -> Rounded Square (18dp radius)
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 18.dp else 28.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "fabCornerRadius"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    // Dynamic Theme-Aware FAB Gradients
    val closedGradient = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor)
    )
    val expandedBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop: Consumes outside clicks when speed dial is open
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                    }
                    .testTag("speed_dial_scrim")
            )
        }

        // Speed Dial Container (Options Menu + Main FAB) pinned to bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speed Dial Vertical Options with clean animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) +
                        scaleIn(
                            initialScale = 0.90f,
                            animationSpec = tween(160, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(110, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(110)
                        ) +
                        scaleOut(
                            targetScale = 0.90f,
                            animationSpec = tween(110)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Option 1: Import File
                    CompactSpeedDialItem(
                        icon = Icons.Default.UploadFile,
                        label = "Import File",
                        accentColor = tertiaryColor,
                        testTag = "speed_dial_import_file",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onImportFileClick()
                        }
                    )

                    // Option 2: New Note
                    CompactSpeedDialItem(
                        icon = Icons.Default.NoteAdd,
                        label = "New Note",
                        accentColor = Color(0xFF10B981), // Emerald
                        testTag = "speed_dial_make_notes",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMakeNotesClick()
                        }
                    )

                    // Option 3: New Folder
                    CompactSpeedDialItem(
                        icon = Icons.Default.CreateNewFolder,
                        label = "New Folder",
                        accentColor = primaryColor,
                        testTag = "speed_dial_create_folder",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCreateFolderClick()
                        }
                    )
                }
            }

            // Main Action FAB Button (Morphs smoothly between '+' and '✕')
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = if (isExpanded) 12.dp else 16.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = primaryColor.copy(alpha = 0.35f),
                        spotColor = primaryColor.copy(alpha = 0.55f)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .then(
                        if (isExpanded) {
                            Modifier.background(expandedBgColor)
                        } else {
                            Modifier.background(closedGradient)
                        }
                    )
                    .border(
                        width = 1.2.dp,
                        color = if (isExpanded) primaryColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            verticalDragAccumulator += delta
                        },
                        onDragStopped = { velocity ->
                            if (velocity < -120f || verticalDragAccumulator < -25f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isExpanded) onDismiss()
                                onSwipeUp()
                            }
                            verticalDragAccumulator = 0f
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    }
                    .testTag("speed_dial_main_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close creation menu" else "Open creation menu",
                    tint = if (isExpanded) primaryColor else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(26.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

/**
 * Sleek, Elite Rectangular Pill Action Item
 * Displays an icon + concise label in a polished single container.
 */
@Composable
private fun CompactSpeedDialItem(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "itemScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .height(44.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Accent Icon Container
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Concise Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.5.sp
            )
        }
    }
}


