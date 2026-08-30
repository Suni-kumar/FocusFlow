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

    // Main FAB Cohesive Gradients
    val closedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFA855F7), // Royal Violet
            Color(0xFF6366F1)  // Indigo
        )
    )
    val expandedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A2238), // Obsidian Plum
            Color(0xFF1E172A)
        )
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop: Only consumes outside clicks when speed dial is open
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF09080D).copy(alpha = 0.72f))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Speed Dial Vertical Options with clean animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) +
                        scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(120)
                        ) +
                        scaleOut(
                            targetScale = 0.85f,
                            animationSpec = tween(120)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Option 1: Import File (Azure / Sky)
                    PremiumSpeedDialItem(
                        icon = Icons.Default.UploadFile,
                        label = "Import File",
                        subtitle = "Select from local storage",
                        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        glowColor = Color(0xFF38BDF8),
                        testTag = "speed_dial_import_file",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onImportFileClick()
                        }
                    )

                    // Option 2: Make Notes (Mint Emerald)
                    PremiumSpeedDialItem(
                        icon = Icons.Default.NoteAdd,
                        label = "Make Notes",
                        subtitle = "Create Markdown document",
                        gradientColors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                        glowColor = Color(0xFF34D399),
                        testTag = "speed_dial_make_notes",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMakeNotesClick()
                        }
                    )

                    // Option 3: Create Folder (Royal Violet)
                    PremiumSpeedDialItem(
                        icon = Icons.Default.CreateNewFolder,
                        label = "Create Folder",
                        subtitle = "New directory category",
                        gradientColors = listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)),
                        glowColor = Color(0xFFA78BFA),
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
                    .size(58.dp)
                    .shadow(
                        elevation = if (isExpanded) 14.dp else 16.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = if (isExpanded) Color(0xFF8B5CF6).copy(alpha = 0.4f) else Color(0xFFA855F7).copy(alpha = 0.5f),
                        spotColor = if (isExpanded) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFF6366F1).copy(alpha = 0.6f)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(if (isExpanded) expandedGradient else closedGradient)
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                if (isExpanded) Color(0xFFA78BFA).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.45f),
                                if (isExpanded) Color(0xFF6366F1).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f)
                            )
                        ),
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
                    tint = if (isExpanded) Color(0xFFD0BCFF) else Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun PremiumSpeedDialItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    gradientColors: List<Color>,
    glowColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "itemScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // Option Text Pill Card (Dark frosted glass with subtle glowing border)
        Surface(
            modifier = Modifier
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        glowColor.copy(alpha = 0.45f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF3F0F7),
                    fontSize = 13.5.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    fontSize = 10.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Luminous Layered Gradient Icon Button
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = glowColor.copy(alpha = 0.45f),
                    spotColor = glowColor.copy(alpha = 0.65f)
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors))
                .border(
                    width = 1.2.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

