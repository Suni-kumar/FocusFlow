package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryContainerDark
import com.example.ui.theme.SurfaceContainerDark

enum class MainTab {
    FILES,
    STUDIO
}

@Composable
fun SepFolBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(SurfaceContainerDark)
            .navigationBarsPadding()
            .padding(horizontal = 40.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Files Tab
            val isFilesSelected = selectedTab == MainTab.FILES
            val filesBgColor by animateColorAsState(
                targetValue = if (isFilesSelected) PrimaryContainerDark else Color.Transparent,
                label = "filesBg"
            )
            val filesContentColor by animateColorAsState(
                targetValue = if (isFilesSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                label = "filesColor"
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(filesBgColor)
                    .clickable { onTabSelected(MainTab.FILES) }
                    .padding(horizontal = if (isFilesSelected) 20.dp else 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isFilesSelected) Icons.Filled.FolderOpen else Icons.Default.FolderOpen,
                        contentDescription = "Files Vault",
                        tint = filesContentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Files",
                        color = filesContentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = if (isFilesSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Studio Tab
            val isStudioSelected = selectedTab == MainTab.STUDIO
            val studioBgColor by animateColorAsState(
                targetValue = if (isStudioSelected) PrimaryContainerDark else Color.Transparent,
                label = "studioBg"
            )
            val studioContentColor by animateColorAsState(
                targetValue = if (isStudioSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                label = "studioColor"
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(studioBgColor)
                    .clickable { onTabSelected(MainTab.STUDIO) }
                    .padding(horizontal = if (isStudioSelected) 20.dp else 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isStudioSelected) Icons.Filled.Style else Icons.Outlined.Style,
                        contentDescription = "Flashcard Studio",
                        tint = studioContentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Studio",
                        color = studioContentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = if (isStudioSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
