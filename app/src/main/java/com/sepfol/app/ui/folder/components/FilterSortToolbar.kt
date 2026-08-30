package com.sepfol.app.ui.folder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sepfol.app.ui.folder.SortOption

@Composable
fun FilterSortToolbar(
    activeFilterTab: String,
    onFilterTabSelected: (String) -> Unit,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Horizontal Scrollable Filter Chips (All, Pinned, Starred, Docs, Images)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterTabPill(
                label = "All",
                icon = Icons.Default.Folder,
                isSelected = activeFilterTab == "ALL",
                onClick = { onFilterTabSelected("ALL") },
                testTag = "filter_all"
            )

            FilterTabPill(
                label = "Pinned",
                icon = Icons.Default.PushPin,
                isSelected = activeFilterTab == "PINNED",
                onClick = { onFilterTabSelected("PINNED") },
                testTag = "filter_pinned"
            )

            FilterTabPill(
                label = "Starred",
                icon = Icons.Default.Favorite,
                isSelected = activeFilterTab == "STARRED",
                onClick = { onFilterTabSelected("STARRED") },
                testTag = "filter_starred"
            )

            FilterTabPill(
                label = "Docs",
                icon = Icons.Default.Description,
                isSelected = activeFilterTab == "DOCS",
                onClick = { onFilterTabSelected("DOCS") },
                testTag = "filter_docs"
            )

            FilterTabPill(
                label = "Images",
                icon = Icons.Default.Image,
                isSelected = activeFilterTab == "IMAGES",
                onClick = { onFilterTabSelected("IMAGES") },
                testTag = "filter_images"
            )
        }

        // Sort Selector Button with Popup Menu
        Box(modifier = Modifier.padding(start = 8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isSortMenuOpen = true }
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .testTag("sort_menu_button")
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (currentSortOption) {
                            SortOption.NAME_ASC -> "Name A-Z"
                            SortOption.NAME_DESC -> "Name Z-A"
                            SortOption.DATE_DESC -> "Newest"
                            SortOption.DATE_ASC -> "Oldest"
                            SortOption.SIZE_DESC -> "Size"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = isSortMenuOpen,
                onDismissRequest = { isSortMenuOpen = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Newest first (Date ↓)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        onSortOptionSelected(SortOption.DATE_DESC)
                        isSortMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Oldest first (Date ↑)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        onSortOptionSelected(SortOption.DATE_ASC)
                        isSortMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Name (A to Z)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                    onClick = {
                        onSortOptionSelected(SortOption.NAME_ASC)
                        isSortMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Name (Z to A)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                    onClick = {
                        onSortOptionSelected(SortOption.NAME_DESC)
                        isSortMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Size (Largest first)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                    onClick = {
                        onSortOptionSelected(SortOption.SIZE_DESC)
                        isSortMenuOpen = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterTabPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .testTag(testTag)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
