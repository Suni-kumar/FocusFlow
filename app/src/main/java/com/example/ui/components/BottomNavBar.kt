package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.GlassRefractionTop
import com.example.ui.theme.SurfaceContainerDark

enum class MainTab {
    FILES,
    STUDIO
}

@Composable
fun SepFolBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GlassRefractionTop,
                        BorderGlass,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
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

            // 2. STUDY Tab (Flashcards)
            BottomNavItem(
                label = "Study",
                activeIcon = Icons.Filled.MenuBook,
                inactiveIcon = Icons.Outlined.MenuBook,
                isSelected = selectedTab == MainTab.STUDIO,
                testTag = "bottom_nav_study",
                onClick = { onTabSelected(MainTab.STUDIO) }
            )

            // 3. STATS Tab (Recall Analytics / Quick switch)
            BottomNavItem(
                label = "Stats",
                activeIcon = Icons.Filled.BubbleChart,
                inactiveIcon = Icons.Outlined.BubbleChart,
                isSelected = false,
                testTag = "bottom_nav_stats",
                onClick = { onTabSelected(MainTab.STUDIO) }
            )

            // 4. PROFILE / SETTINGS Tab
            BottomNavItem(
                label = "Settings",
                activeIcon = Icons.Filled.Settings,
                inactiveIcon = Icons.Filled.Settings,
                isSelected = false,
                testTag = "bottom_nav_settings",
                onClick = onSettingsClick
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
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        label = "navContentColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "navIconScale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Active glowing top accent indicator
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (isSelected) 24.dp else 0.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            )
                        )
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                )
        )

        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale)
        )

        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

