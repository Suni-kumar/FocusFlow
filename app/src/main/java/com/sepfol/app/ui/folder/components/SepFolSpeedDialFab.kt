package com.sepfol.app.ui.folder.components

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.theme.SurfaceSlateDark

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

    // Morphing shape animation: Circle (30dp radius) -> Rounded Square (16dp radius)
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 30.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "fabCornerRadius"
    )

    // Main FAB Colorful Gradients
    val closedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8B5CF6), // Neon Purple
            Color(0xFFEC4899), // Neon Pink
            Color(0xFF06B6D4)  // Neon Cyan
        )
    )
    val expandedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF3366), // Hot Crimson
            Color(0xFFFF5E3A)  // Sunset Coral
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
                    .background(Color.Black.copy(alpha = 0.65f))
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
                enter = fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
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
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Option 1: Import File (Vibrant Cyan / Sky Blue)
                    ColorfulSpeedDialItem(
                        icon = Icons.Default.UploadFile,
                        label = "Import File",
                        subtitle = "Select from local storage",
                        gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA)),
                        glowColor = Color(0xFF00E5FF),
                        testTag = "speed_dial_import_file",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onImportFileClick()
                        }
                    )

                    // Option 2: Make Notes (Vibrant Emerald / Mint Green)
                    ColorfulSpeedDialItem(
                        icon = Icons.Default.NoteAdd,
                        label = "Make Notes",
                        subtitle = "Create Markdown document",
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853)),
                        glowColor = Color(0xFF00E676),
                        testTag = "speed_dial_make_notes",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMakeNotesClick()
                        }
                    )

                    // Option 3: Create Folder (Vibrant Fuchsia / Purple)
                    ColorfulSpeedDialItem(
                        icon = Icons.Default.CreateNewFolder,
                        label = "Create Folder",
                        subtitle = "New directory category",
                        gradientColors = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF)),
                        glowColor = Color(0xFFE040FB),
                        testTag = "speed_dial_create_folder",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCreateFolderClick()
                        }
                    )
                }
            }

            // Main Action FAB Button (Morphs from Circle '+' to Rounded Square '✕')
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = if (isExpanded) 18.dp else 12.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = if (isExpanded) Color(0xFFFF3366) else Color(0xFF8B5CF6),
                        spotColor = if (isExpanded) Color(0xFFFF5E3A) else Color(0xFFEC4899)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(if (isExpanded) expandedGradient else closedGradient)
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.35f),
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
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun ColorfulSpeedDialItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    gradientColors: List<Color>,
    glowColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = glowColor.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Option Text Pill Card (Theme-aware with glow border)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.2.dp,
                color = glowColor.copy(alpha = 0.5f)
            ),
            shadowElevation = 8.dp,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Vibrant Gradient Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = gradientColors))
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}
