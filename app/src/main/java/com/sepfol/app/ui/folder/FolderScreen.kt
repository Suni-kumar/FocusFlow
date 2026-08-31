package com.sepfol.app.ui.folder

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryContainerDark
import com.example.ui.util.AppHaptic
import com.sepfol.app.ui.folder.components.FilterSortToolbar
import com.sepfol.app.ui.folder.components.SepFolSpeedDialFab
import com.sepfol.app.ui.folder.dialogs.CreateFolderDialog
import com.sepfol.app.ui.folder.dialogs.CreateMarkdownNoteDialog
import com.sepfol.app.ui.folder.dialogs.ItemActionMenuDialog
import com.sepfol.app.ui.folder.dialogs.MoveItemDialog
import com.sepfol.app.ui.folder.dialogs.NotePreviewDialog
import com.sepfol.app.ui.folder.dialogs.RenameItemDialog
import com.sepfol.app.ui.viewer.PdfImageViewerScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

private data class FileBadgeInfo(
    val badgeText: String,
    val badgeBg: Color,
    val iconColor: Color,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun FolderScreen(
    modifier: Modifier = Modifier,
    viewModel: FolderViewModel = viewModel(),
    onImportClick: () -> Unit = {},
    onSwipeUpFab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Storage Document Picker for importing local files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                var fileName = "imported_document"
                var fileSize = 0L

                context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            cursor.getString(nameIndex)?.let { fileName = it }
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }

                val mimeType = context.contentResolver.getType(fileUri)

                // Read file contents (for text/markdown/code/notes)
                val contentString: String? = try {
                    context.contentResolver.openInputStream(fileUri)?.use { stream ->
                        val bytes = stream.readBytes()
                        if (fileSize == 0L) fileSize = bytes.size.toLong()
                        if (bytes.size <= 1024 * 1024) {
                            String(bytes, Charsets.UTF_8)
                        } else {
                            "Preview unavailable for binary/large file (${formatFileSize(fileSize)})."
                        }
                    }
                } catch (e: Exception) {
                    null
                }

                viewModel.importFile(
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = fileSize,
                    contentData = contentString
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val hapticView = LocalView.current

    // Intercept hardware/gesture back press when viewer, speed dial, dialogs, preview, search, selection or subfolder is active
    val isFolderBackActive = uiState.selectedViewerItem != null ||
            uiState.isSelectionMode ||
            uiState.isSpeedDialOpen ||
            uiState.selectedNote != null ||
            uiState.actionMenuItem != null ||
            uiState.renameTargetItem != null ||
            uiState.moveTargetItem != null ||
            uiState.isCreateFolderDialogOpen ||
            uiState.isCreateNoteDialogOpen ||
            uiState.searchQuery.isNotBlank() ||
            uiState.activeFilterTab != "ALL" ||
            uiState.currentFolderId != null ||
            uiState.folderStack.size > 1

    BackHandler(enabled = isFolderBackActive) {
        AppHaptic.vibrateClick(context, hapticView)
        when {
            uiState.isSelectionMode -> viewModel.clearSelection()
            uiState.selectedViewerItem != null -> viewModel.dismissViewer()
            uiState.actionMenuItem != null -> viewModel.dismissActionMenu()
            uiState.renameTargetItem != null -> viewModel.dismissRenameDialog()
            uiState.moveTargetItem != null -> viewModel.dismissMoveDialog()
            uiState.isSpeedDialOpen -> viewModel.closeSpeedDial()
            uiState.selectedNote != null -> viewModel.dismissNotePreview()
            uiState.isCreateFolderDialogOpen -> viewModel.dismissCreateFolderDialog()
            uiState.isCreateNoteDialogOpen -> viewModel.dismissCreateNoteDialog()
            uiState.searchQuery.isNotBlank() -> viewModel.setSearchQuery("")
            uiState.activeFilterTab != "ALL" -> viewModel.setActiveFilterTab("ALL")
            uiState.folderStack.size > 1 || uiState.currentFolderId != null -> viewModel.navigateUp()
        }
    }

    val folders = remember(uiState.currentItems) { uiState.currentItems.filter { it.isDirectory } }
    val files = remember(uiState.currentItems) { uiState.currentItems.filter { !it.isDirectory } }
    val folderRows = remember(folders, uiState.gridColumns) { folders.chunked(uiState.gridColumns.coerceIn(1, 4)) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (uiState.folderStack.size > 1 && uiState.selectedViewerItem == null) Modifier.statusBarsPadding()
                    else Modifier
                ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Breadcrumb Navigation Trail
            item(key = "breadcrumb_bar") {
                BreadcrumbBar(
                    breadcrumbs = uiState.folderStack,
                    onBreadcrumbClick = { index ->
                        AppHaptic.vibrateClick(context, hapticView)
                        viewModel.navigateToBreadcrumb(index)
                    },
                    onBackClick = {
                        AppHaptic.vibrateClick(context, hapticView)
                        viewModel.navigateUp()
                    }
                )
            }

            // Filter & Sort Toolbar
            item(key = "filter_sort_toolbar") {
                FilterSortToolbar(
                    activeFilterTab = uiState.activeFilterTab,
                    onFilterTabSelected = { tab -> viewModel.setActiveFilterTab(tab) },
                    currentSortOption = uiState.sortOption,
                    onSortOptionSelected = { sort -> viewModel.setSortOption(sort) }
                )
            }

            // Search Active Filter Status Chip
            if (uiState.searchQuery.isNotBlank()) {
                item(key = "search_filter_chip") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Filtered by: \"${uiState.searchQuery}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.setSearchQuery("") }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Recent Files Section (Only at Root and when on ALL filter and no search query)
            if (uiState.currentFolderId == null && uiState.activeFilterTab == "ALL" && uiState.searchQuery.isBlank() && uiState.recentItems.isNotEmpty()) {
                item(key = "recent_files_section") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "RECENT FILES & NOTES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.08.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 8.dp)
                        ) {
                            items(uiState.recentItems, key = { "recent_${it.id}" }) { item ->
                                RecentNoteCard(
                                    item = item,
                                    onClick = { viewModel.selectNote(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Active Folder Content
            if (uiState.currentItems.isEmpty()) {
                item(key = "empty_folder_state") {
                    EmptyFolderState(
                        isSearch = uiState.searchQuery.isNotBlank() || uiState.activeFilterTab != "ALL",
                        onCreateFolderClick = { viewModel.openCreateFolderDialog() },
                        onCreateNoteClick = { viewModel.openCreateNoteDialog() },
                        onImportFileClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                }
            } else {
                // Folders Section
                if (folders.isNotEmpty()) {
                    item(key = "folders_header") {
                        Text(
                            text = "FOLDERS (${folders.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.08.sp
                        )
                    }

                    itemsIndexed(folderRows, key = { rowIndex, _ -> "folder_row_$rowIndex" }) { _, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (folder in row) {
                                val isSelected = folder.id in uiState.selectedItemIds
                                Box(modifier = Modifier.weight(1f)) {
                                    FolderCard(
                                        folder = folder,
                                        isSelected = isSelected,
                                        isSelectionMode = uiState.isSelectionMode,
                                        onClick = {
                                            AppHaptic.vibrateClick(context, hapticView)
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleItemSelection(folder.id)
                                            } else {
                                                viewModel.openFolder(folder)
                                            }
                                        },
                                        onLongClick = {
                                            AppHaptic.vibrateHeavy(context, hapticView)
                                            viewModel.toggleItemSelection(folder.id)
                                        },
                                        onMoreClick = {
                                            AppHaptic.vibrateClick(context, hapticView)
                                            viewModel.openActionMenu(folder)
                                        }
                                    )
                                }
                            }
                            if (row.size < uiState.gridColumns) {
                                repeat(uiState.gridColumns - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Documents & Markdown Notes Section
                if (files.isNotEmpty()) {
                    item(key = "documents_header") {
                        Text(
                            text = "DOCUMENTS & FILES (${files.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.08.sp
                        )
                    }

                    items(files, key = { "file_${it.id}" }) { file ->
                        val isSelected = file.id in uiState.selectedItemIds
                        NoteListItemCard(
                            item = file,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleItemSelection(file.id)
                                } else {
                                    viewModel.selectNote(file)
                                }
                            },
                            onLongClick = {
                                AppHaptic.vibrateHeavy(context, hapticView)
                                viewModel.toggleItemSelection(file.id)
                            },
                            onMoreClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                viewModel.openActionMenu(file)
                            }
                        )
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Floating Animated Speed Dial FAB at Bottom Right
        SepFolSpeedDialFab(
            isExpanded = uiState.isSpeedDialOpen,
            onToggle = { viewModel.toggleSpeedDial() },
            onDismiss = { viewModel.closeSpeedDial() },
            onCreateFolderClick = { viewModel.openCreateFolderDialog() },
            onMakeNotesClick = { viewModel.openCreateNoteDialog() },
            onImportFileClick = {
                viewModel.closeSpeedDial()
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            onSwipeUp = onSwipeUpFab,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Modal Dialogs
    if (uiState.isCreateFolderDialogOpen) {
        CreateFolderDialog(
            onDismiss = { viewModel.dismissCreateFolderDialog() },
            onCreateFolder = { name -> viewModel.createFolder(name) }
        )
    }

    if (uiState.isCreateNoteDialogOpen) {
        CreateMarkdownNoteDialog(
            onDismiss = { viewModel.dismissCreateNoteDialog() },
            onCreateNote = { title, content -> viewModel.createMarkdownNote(title, content) }
        )
    }

    // Item Action Context Menu Dialog (Rename, Pin, Star, Duplicate, Move, Share, Delete)
    uiState.actionMenuItem?.let { actionItem ->
        ItemActionMenuDialog(
            item = actionItem,
            onDismiss = { viewModel.dismissActionMenu() },
            onRename = { viewModel.openRenameDialog(actionItem) },
            onTogglePin = { viewModel.togglePin(actionItem) },
            onToggleFavorite = { viewModel.toggleFavorite(actionItem) },
            onDuplicate = { viewModel.duplicateItem(actionItem) },
            onMove = { viewModel.openMoveDialog(actionItem) },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, actionItem.name)
                    putExtra(Intent.EXTRA_TEXT, "Content from ${actionItem.name}:\n${actionItem.contentData ?: ""}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share ${actionItem.name}")
                context.startActivity(shareIntent)
            },
            onDelete = { viewModel.deleteItem(actionItem) }
        )
    }

    // Rename Dialog
    uiState.renameTargetItem?.let { renameItem ->
        RenameItemDialog(
            item = renameItem,
            onConfirm = { newName -> viewModel.renameItem(renameItem, newName) },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }

    // Move Dialog
    uiState.moveTargetItem?.let { moveItem ->
        MoveItemDialog(
            item = moveItem,
            availableFolders = uiState.allItems.filter { it.isDirectory },
            onConfirmMove = { targetFolderId -> viewModel.moveItem(moveItem, targetFolderId) },
            onDismiss = { viewModel.dismissMoveDialog() }
        )
    }

    uiState.selectedNote?.let { selected ->
        NotePreviewDialog(
            item = selected,
            onDismiss = { viewModel.dismissNotePreview() },
            onDelete = { item -> viewModel.deleteItem(item) },
            onOpenViewer = { item -> viewModel.openViewer(item) }
        )
    }

    // Full Screen PDF & Image Viewer (matching Screenshots)
    AnimatedVisibility(
        visible = uiState.selectedViewerItem != null,
        enter = slideInVertically(animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeIn(tween(200)),
        exit = slideOutVertically(animationSpec = tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeOut(tween(160))
    ) {
        uiState.selectedViewerItem?.let { viewerItem ->
            PdfImageViewerScreen(
                item = viewerItem,
                onBackClick = { viewModel.dismissViewer() }
            )
        }
    }
}

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<Breadcrumb>,
    onBreadcrumbClick: (index: Int) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = null,
        elevation = 2.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = breadcrumbs.size > 1,
                enter = slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it } + fadeIn(),
                exit = slideOutHorizontally { -it } + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Up",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            LazyRow(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = breadcrumbs,
                    key = { _, crumb -> crumb.id ?: "root_folder" }
                ) { index, crumb ->
                    val isLast = index == breadcrumbs.lastIndex
                    val isFirst = index == 0

                    val textColor by animateColorAsState(
                        targetValue = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "crumbTextColor"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(enabled = !isLast) { onBreadcrumbClick(index) }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isFirst) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = crumb.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor
                            )
                        }

                        if (!isLast) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentNoteCard(
    item: FolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .width(150.dp)
            .height(135.dp),
        backgroundColor = null,
        elevation = 2.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isPdf = item.extension.equals("pdf", ignoreCase = true) || item.mimeType.contains("pdf")
                val isImg = item.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp") || item.mimeType.startsWith("image/")

                val badgeInfo = when {
                    isPdf -> FileBadgeInfo("PDF", Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFEF4444), Icons.Default.PictureAsPdf)
                    isImg -> FileBadgeInfo("IMG", Color(0xFF06B6D4).copy(alpha = 0.2f), Color(0xFF0891B2), Icons.Default.Image)
                    else -> FileBadgeInfo("MD", MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.primary, Icons.Default.Description)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeInfo.badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeInfo.iconVector,
                        contentDescription = null,
                        tint = badgeInfo.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeInfo.badgeBg)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeInfo.badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeInfo.iconColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Text(
                    text = formatRelativeTime(item.lastModified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun FolderCard(
    folder: FolderItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else null
    val borderCol = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        folder.isPinned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        backgroundColor = bg,
        elevation = 2.dp,
        borderColor = borderCol,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (folder.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        if (folder.isFavorite) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Folder options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteListItemCard(
    item: FolderItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else null
    val borderCol = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        item.isPinned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
        backgroundColor = bg,
        elevation = 2.dp,
        borderColor = borderCol,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    val isPdf = item.extension.equals("pdf", ignoreCase = true) || item.mimeType.contains("pdf")
                    val isImg = item.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp") || item.mimeType.startsWith("image/")

                    val (badgeBg, iconColor, iconVector) = when {
                        isPdf -> Triple(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFEF4444), Icons.Default.PictureAsPdf)
                        isImg -> Triple(Color(0xFF06B6D4).copy(alpha = 0.2f), Color(0xFF0891B2), Icons.Default.Image)
                        else -> Triple(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), MaterialTheme.colorScheme.primary, Icons.Default.Description)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (item.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        if (item.isFavorite) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatFileSize(item.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = formatRelativeTime(item.lastModified),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFolderState(
    isSearch: Boolean,
    onCreateFolderClick: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onImportFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        backgroundColor = null,
        elevation = 2.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isSearch) "No matching items found" else "This directory is empty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isSearch) "Try searching with a different keyword" else "Start organizing your study vault or import files directly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            if (!isSearch) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onImportFileClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Import", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onCreateFolderClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+ Folder", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onCreateNoteClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+ Note", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60000
    val hours = mins / 60
    val days = hours / 24

    return when {
        mins < 2 -> "Just now"
        mins < 60 -> "$mins mins ago"
        hours < 24 -> "$hours hrs ago"
        days < 7 -> "$days days ago"
        else -> dateFormat.format(Date(timestamp))
    }
}
