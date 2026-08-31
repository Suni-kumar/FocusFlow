package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.GlassRefractionTop
import com.example.ui.theme.Local3DGlassEnabled
import com.example.ui.theme.LocalAccentTheme
import com.example.ui.theme.SurfaceContainerDark

enum class MainTab {
    FILES,
    STUDIO,
    DICTATION
}

@Composable
fun SepFolBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val is3DEnabled = Local3DGlassEnabled.current
    val accentTheme = LocalAccentTheme.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val navBarBackground = remember(is3DEnabled, isDark, surfaceContainerHigh) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF18172F).copy(alpha = 0.88f),
                        Color(0xFF0F0E1E).copy(alpha = 0.94f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF8FD).copy(alpha = 0.90f),
                        Color(0xFFF1E0EC).copy(alpha = 0.84f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                listOf(
                    surfaceContainerHigh,
                    surfaceContainerHigh
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (is3DEnabled) 16.dp else 4.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = if (is3DEnabled) (if (isDark) accentTheme.primaryColor.copy(alpha = 0.35f) else accentTheme.primaryColor.copy(alpha = 0.20f)) else Color.Black.copy(alpha = 0.15f),
                spotColor = if (is3DEnabled) (if (isDark) accentTheme.secondaryColor.copy(alpha = 0.30f) else Color(0x353B2544)) else Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(navBarBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (is3DEnabled) (if (isDark) Color.White.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.90f)) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. VAULT Tab (Files)
            BottomNavItem(
                label = "Vault",
                activeIcon = Icons.Filled.Folder,
                inactiveIcon = Icons.Outlined.Folder,
                isSelected = selectedTab == MainTab.FILES,
                testTag = "bottom_nav_vault",
                onClick = { onTabSelected(MainTab.FILES) }
            )

            // 2. STUDIO Tab (Flashcards)
            BottomNavItem(
                label = "Flashcards",
                activeIcon = Icons.Filled.BubbleChart,
                inactiveIcon = Icons.Outlined.BubbleChart,
                isSelected = selectedTab == MainTab.STUDIO,
                testTag = "bottom_nav_studio",
                onClick = { onTabSelected(MainTab.STUDIO) }
            )

            // 3. DICTATION Tab (Dictation Practice Workspace)
            BottomNavItem(
                label = "Dictation",
                activeIcon = Icons.Filled.BubbleChart,
                inactiveIcon = Icons.Outlined.BubbleChart,
                isSelected = selectedTab == MainTab.DICTATION,
                testTag = "bottom_nav_dictation",
                onClick = { onTabSelected(MainTab.DICTATION) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val haptic = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250),
        label = "navContentColor"
    )

    val backgroundPillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "navPillColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.15f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navIconScale"
    )

    val containerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navContainerScale"
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = containerScale
                scaleY = containerScale
            }
            .clip(RoundedCornerShape(9999.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                }
            )
            .background(backgroundPillColor)
            .testTag(testTag)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )

        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

