package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ultra-Sleek, High-End Workspace Switcher Modal
 * Triggered seamlessly by swiping up on the Plus FAB.
 */
@Composable
fun WorkspaceSwitcherModal(
    isOpen: Boolean,
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    if (isOpen) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Translucent Scrim Backdrop (Dismisses on tap)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )

            // Animated Bottom Sheet Panel
            AnimatedVisibility(
                visible = isOpen,
                enter = fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                exit = fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(120)
                        ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = primaryColor.copy(alpha = 0.2f),
                            spotColor = primaryColor.copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Drag Indicator Bar
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f))
                        )

                        // Modal Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Switch Workspace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close switcher",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Workspace Option 1: Vault Workspace
                        EliteWorkspaceOptionCard(
                            title = "Vault",
                            icon = Icons.Default.Folder,
                            accentColor = primaryColor,
                            isSelected = currentTab == MainTab.FILES,
                            testTag = "workspace_option_vault",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onTabSelected(MainTab.FILES)
                                onDismiss()
                            }
                        )

                        // Workspace Option 2: Flashcards Studio
                        EliteWorkspaceOptionCard(
                            title = "Flashcards",
                            icon = Icons.Default.Style,
                            accentColor = secondaryColor,
                            isSelected = currentTab == MainTab.STUDIO,
                            testTag = "workspace_option_studio",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onTabSelected(MainTab.STUDIO)
                                onDismiss()
                            }
                        )

                        // Workspace Option 3: Dictation Practice Studio
                        EliteWorkspaceOptionCard(
                            title = "Dictation Studio",
                            icon = Icons.Default.RecordVoiceOver,
                            accentColor = tertiaryColor,
                            isSelected = currentTab == MainTab.DICTATION,
                            testTag = "workspace_option_dictation",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onTabSelected(MainTab.DICTATION)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Concise, theme-adaptive workspace option card
 */
@Composable
private fun EliteWorkspaceOptionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.01f else 1f,
        label = "cardScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        label = "borderColor"
    )
    val containerBg by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.60f),
        label = "containerBg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = accentColor.copy(alpha = 0.20f)),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accent Icon Container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    listOf(accentColor, accentColor.copy(alpha = 0.75f))
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.10f))
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }

            // Active Badge & Checkmark Pill
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(0.8.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(9999.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
