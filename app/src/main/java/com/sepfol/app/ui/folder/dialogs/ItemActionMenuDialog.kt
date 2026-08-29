package com.sepfol.app.ui.folder.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SurfaceCharcoalDark
import com.example.ui.theme.SurfaceContainerDark
import com.sepfol.app.ui.folder.FolderItem

@Composable
fun ItemActionMenuDialog(
    item: FolderItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                // Header item info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.DriveFileRenameOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (item.isDirectory) "Folder" else "${item.extension.uppercase()} File",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                // Action Menu Items
                ActionMenuItemRow(
                    icon = Icons.Default.DriveFileRenameOutline,
                    label = "Rename",
                    subtitle = "Change file/folder name",
                    iconColor = Color(0xFF3B82F6),
                    onClick = {
                        onDismiss()
                        onRename()
                    },
                    testTag = "action_rename"
                )

                ActionMenuItemRow(
                    icon = if (item.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    label = if (item.isPinned) "Unpin from Top" else "Pin to Top",
                    subtitle = if (item.isPinned) "Remove priority pin" else "Keep item at top of list",
                    iconColor = if (item.isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        onDismiss()
                        onTogglePin()
                    },
                    testTag = "action_pin"
                )

                ActionMenuItemRow(
                    icon = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = if (item.isFavorite) "Remove from Starred" else "Add to Starred",
                    subtitle = if (item.isFavorite) "Starred item" else "Quick bookmark",
                    iconColor = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        onDismiss()
                        onToggleFavorite()
                    },
                    testTag = "action_favorite"
                )

                if (!item.isDirectory) {
                    ActionMenuItemRow(
                        icon = Icons.Default.ContentCopy,
                        label = "Duplicate",
                        subtitle = "Create an identical copy",
                        iconColor = Color(0xFF10B981),
                        onClick = {
                            onDismiss()
                            onDuplicate()
                        },
                        testTag = "action_duplicate"
                    )
                }

                ActionMenuItemRow(
                    icon = Icons.Default.DriveFileMove,
                    label = "Move to...",
                    subtitle = "Change parent folder",
                    iconColor = Color(0xFFA855F7),
                    onClick = {
                        onDismiss()
                        onMove()
                    },
                    testTag = "action_move"
                )

                ActionMenuItemRow(
                    icon = Icons.Default.Share,
                    label = "Share",
                    subtitle = "Send or export content",
                    iconColor = Color(0xFF0284C7),
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    testTag = "action_share"
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                ActionMenuItemRow(
                    icon = Icons.Outlined.Delete,
                    label = "Delete",
                    subtitle = "Permanently remove item",
                    iconColor = Color(0xFFEF4444),
                    textColor = Color(0xFFEF4444),
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    testTag = "action_delete"
                )
            }
        }
    }
}

@Composable
private fun ActionMenuItemRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
