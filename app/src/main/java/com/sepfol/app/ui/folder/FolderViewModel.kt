package com.sepfol.app.ui.folder

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusFlowApplication
import com.example.data.repository.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC,
    SIZE_DESC
}

@androidx.compose.runtime.Stable
data class FolderItem(
    val id: String,
    val name: String,
    val isDirectory: Boolean,
    val extension: String = if (isDirectory) "" else "md",
    val mimeType: String = if (isDirectory) "vnd.android.document/directory" else "text/markdown",
    val parentId: String? = null,
    val contentData: String? = null,
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false
)

@androidx.compose.runtime.Stable
data class Breadcrumb(
    val id: String?,
    val name: String
)

@androidx.compose.runtime.Stable
data class FolderUiState(
    val currentFolderId: String? = null,
    val folderStack: List<Breadcrumb> = listOf(Breadcrumb(id = null, name = "Root")),
    val allItems: List<FolderItem> = emptyList(),
    val currentItems: List<FolderItem> = emptyList(),
    val recentItems: List<FolderItem> = emptyList(),
    val isSpeedDialOpen: Boolean = false,
    val isCreateFolderDialogOpen: Boolean = false,
    val isCreateNoteDialogOpen: Boolean = false,
    val selectedNote: FolderItem? = null,
    val selectedViewerItem: FolderItem? = null,
    val actionMenuItem: FolderItem? = null,
    val renameTargetItem: FolderItem? = null,
    val moveTargetItem: FolderItem? = null,
    val searchQuery: String = "",
    val gridColumns: Int = 2,
    val activeFilterTab: String = "ALL", // ALL, PINNED, STARRED, DOCS, IMAGES
    val sortOption: SortOption = SortOption.DATE_DESC,
    val selectedItemIds: Set<String> = emptySet(),
    val statusMessage: String? = null
) {
    val isSelectionMode: Boolean
        get() = selectedItemIds.isNotEmpty()
}

class FolderViewModel(
    application: Application? = null,
    repository: FolderRepository? = null
) : ViewModel() {

    private val repo: FolderRepository = repository
        ?: try {
            FocusFlowApplication.instance.folderRepository
        } catch (e: Exception) {
            FolderRepository.createInMemory()
        }

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repo.allItems.collect { items ->
                _uiState.update { state ->
                    val isCurrentFolderValid = state.currentFolderId == null || items.any { it.id == state.currentFolderId }
                    val effectiveFolderId = if (isCurrentFolderValid) state.currentFolderId else null
                    val effectiveFolderStack = if (isCurrentFolderValid) state.folderStack else listOf(Breadcrumb(id = null, name = "Root"))

                    state.copy(
                        currentFolderId = effectiveFolderId,
                        folderStack = effectiveFolderStack,
                        allItems = items,
                        currentItems = computeCurrentItems(
                            items,
                            effectiveFolderId,
                            state.searchQuery,
                            state.activeFilterTab,
                            state.sortOption
                        ),
                        recentItems = items.filter { !it.isDirectory }.sortedByDescending { it.lastModified }.take(8),
                        selectedNote = if (state.selectedNote != null && items.none { it.id == state.selectedNote.id }) null else state.selectedNote,
                        selectedViewerItem = if (state.selectedViewerItem != null && items.none { it.id == state.selectedViewerItem.id }) null else state.selectedViewerItem
                    )
                }
            }
        }
    }

    fun openFolder(folder: FolderItem) {
        if (!folder.isDirectory) return

        _uiState.update { state ->
            val newStack = state.folderStack + Breadcrumb(id = folder.id, name = folder.name)
            state.copy(
                currentFolderId = folder.id,
                folderStack = newStack,
                currentItems = computeCurrentItems(state.allItems, folder.id, state.searchQuery, state.activeFilterTab, state.sortOption)
            )
        }
    }

    fun navigateToBreadcrumb(index: Int) {
        _uiState.update { state ->
            if (index < 0 || index >= state.folderStack.size) return@update state
            val targetCrumb = state.folderStack[index]
            val newStack = state.folderStack.take(index + 1)
            state.copy(
                currentFolderId = targetCrumb.id,
                folderStack = newStack,
                currentItems = computeCurrentItems(state.allItems, targetCrumb.id, state.searchQuery, state.activeFilterTab, state.sortOption)
            )
        }
    }

    fun navigateUp(): Boolean {
        var handled = false
        _uiState.update { state ->
            if (state.folderStack.size > 1) {
                handled = true
                val newStack = state.folderStack.dropLast(1)
                val targetCrumb = newStack.last()
                state.copy(
                    currentFolderId = targetCrumb.id,
                    folderStack = newStack,
                    currentItems = computeCurrentItems(state.allItems, targetCrumb.id, state.searchQuery, state.activeFilterTab, state.sortOption)
                )
            } else {
                state
            }
        }
        return handled
    }

    fun openSpeedDial() {
        _uiState.update { it.copy(isSpeedDialOpen = true) }
    }

    fun closeSpeedDial() {
        _uiState.update { it.copy(isSpeedDialOpen = false) }
    }

    fun toggleSpeedDial() {
        _uiState.update { it.copy(isSpeedDialOpen = !it.isSpeedDialOpen) }
    }

    fun openCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = true, isSpeedDialOpen = false) }
    }

    fun dismissCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = false) }
    }

    fun openCreateNoteDialog() {
        _uiState.update { it.copy(isCreateNoteDialogOpen = true, isSpeedDialOpen = false) }
    }

    fun dismissCreateNoteDialog() {
        _uiState.update { it.copy(isCreateNoteDialogOpen = false) }
    }

    fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return

        val newFolder = FolderItem(
            id = "folder_" + System.currentTimeMillis(),
            name = trimmed,
            isDirectory = true,
            parentId = _uiState.value.currentFolderId,
            lastModified = System.currentTimeMillis(),
            itemCount = 0
        )

        _uiState.update { state ->
            val updatedAll = listOf(newFolder) + state.allItems
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                isCreateFolderDialogOpen = false,
                statusMessage = "Created folder \"$trimmed\""
            )
        }
        viewModelScope.launch {
            repo.insertItem(newFolder)
        }
    }

    fun createMarkdownNote(title: String, content: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val finalTitle = if (trimmedTitle.endsWith(".md", ignoreCase = true) ||
            trimmedTitle.endsWith(".markdown", ignoreCase = true)
        ) {
            trimmedTitle
        } else {
            "$trimmedTitle.md"
        }

        val sizeBytes = content.toByteArray(Charsets.UTF_8).size.toLong()
        val newNote = FolderItem(
            id = "file_" + System.currentTimeMillis(),
            name = finalTitle,
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = _uiState.value.currentFolderId,
            contentData = content,
            sizeBytes = sizeBytes,
            lastModified = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val updatedAll = listOf(newNote) + state.allItems
            val updatedRecent = (listOf(newNote) + state.recentItems.filter { it.id != newNote.id }).take(8)
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                isCreateNoteDialogOpen = false,
                statusMessage = "Saved note \"$finalTitle\""
            )
        }
        viewModelScope.launch {
            repo.insertItem(newNote)
        }
    }

    fun importFile(
        fileName: String,
        mimeType: String?,
        sizeBytes: Long,
        contentData: String?
    ) {
        val extension = if (fileName.contains('.')) fileName.substringAfterLast('.', "") else ""
        val effectiveMime = mimeType ?: when (extension.lowercase()) {
            "md", "markdown" -> "text/markdown"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }

        val newItem = FolderItem(
            id = "import_" + System.currentTimeMillis(),
            name = fileName,
            isDirectory = false,
            extension = extension,
            mimeType = effectiveMime,
            parentId = _uiState.value.currentFolderId,
            contentData = contentData,
            sizeBytes = sizeBytes,
            lastModified = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val updatedAll = listOf(newItem) + state.allItems
            val updatedRecent = (listOf(newItem) + state.recentItems.filter { it.id != newItem.id }).take(8)
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                statusMessage = "Imported \"$fileName\" successfully"
            )
        }
        viewModelScope.launch {
            repo.insertItem(newItem)
        }
    }

    fun deleteItem(item: FolderItem) {
        val idsToDelete = mutableSetOf(item.id)
        val currentAll = _uiState.value.allItems
        if (item.isDirectory) {
            fun collectChildIds(parentId: String) {
                val children = currentAll.filter { it.parentId == parentId }
                for (child in children) {
                    idsToDelete.add(child.id)
                    if (child.isDirectory) {
                        collectChildIds(child.id)
                    }
                }
            }
            collectChildIds(item.id)
        }

        _uiState.update { state ->
            val updatedAll = state.allItems.filter { it.id !in idsToDelete }
            val updatedRecent = state.recentItems.filter { it.id !in idsToDelete }

            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                selectedNote = if (state.selectedNote?.id in idsToDelete) null else state.selectedNote,
                statusMessage = "Deleted \"${item.name}\""
            )
        }
        viewModelScope.launch {
            repo.deleteItems(idsToDelete.toList())
        }
    }

    fun selectNote(note: FolderItem) {
        val isPdf = note.extension.equals("pdf", ignoreCase = true) || note.mimeType.contains("pdf")
        val isImage = note.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp", "gif", "svg") || note.mimeType.startsWith("image/")
        if (isPdf || isImage) {
            _uiState.update { it.copy(selectedViewerItem = note, selectedNote = null) }
        } else {
            _uiState.update { it.copy(selectedNote = note) }
        }
    }

    fun openViewer(item: FolderItem) {
        _uiState.update { it.copy(selectedViewerItem = item, selectedNote = null) }
    }

    fun dismissViewer() {
        _uiState.update { it.copy(selectedViewerItem = null) }
    }

    fun dismissNotePreview() {
        _uiState.update { it.copy(selectedNote = null) }
    }

    fun openActionMenu(item: FolderItem) {
        _uiState.update { it.copy(actionMenuItem = item) }
    }

    fun dismissActionMenu() {
        _uiState.update { it.copy(actionMenuItem = null) }
    }

    fun openRenameDialog(item: FolderItem) {
        _uiState.update { it.copy(renameTargetItem = item, actionMenuItem = null) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(renameTargetItem = null) }
    }

    fun openMoveDialog(item: FolderItem) {
        _uiState.update { it.copy(moveTargetItem = item, actionMenuItem = null) }
    }

    fun dismissMoveDialog() {
        _uiState.update { it.copy(moveTargetItem = null) }
    }

    fun renameItem(item: FolderItem, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == item.name) {
            _uiState.update { it.copy(renameTargetItem = null) }
            return
        }

        val extension = if (!item.isDirectory && trimmed.contains('.')) trimmed.substringAfterLast('.', "") else item.extension
        val updatedItem = item.copy(name = trimmed, extension = extension, lastModified = System.currentTimeMillis())

        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) updatedItem else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) updatedItem else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                selectedNote = if (state.selectedNote?.id == item.id) state.selectedNote?.copy(name = trimmed) else state.selectedNote,
                selectedViewerItem = if (state.selectedViewerItem?.id == item.id) state.selectedViewerItem?.copy(name = trimmed) else state.selectedViewerItem,
                renameTargetItem = null,
                statusMessage = "Renamed to \"$trimmed\""
            )
        }
        viewModelScope.launch {
            repo.updateItem(updatedItem)
        }
    }

    fun togglePin(item: FolderItem) {
        val newPin = !item.isPinned
        val updatedItem = item.copy(isPinned = newPin)
        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) updatedItem else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) updatedItem else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                actionMenuItem = null,
                statusMessage = if (newPin) "Pinned \"${item.name}\"" else "Unpinned \"${item.name}\""
            )
        }
        viewModelScope.launch {
            repo.updateItem(updatedItem)
        }
    }

    fun toggleFavorite(item: FolderItem) {
        val newFav = !item.isFavorite
        val updatedItem = item.copy(isFavorite = newFav)
        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) updatedItem else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) updatedItem else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                actionMenuItem = null,
                statusMessage = if (newFav) "Added to Starred" else "Removed from Starred"
            )
        }
        viewModelScope.launch {
            repo.updateItem(updatedItem)
        }
    }

    fun duplicateItem(item: FolderItem) {
        if (item.isDirectory) return
        val nameWithoutExt = if (item.name.contains('.')) item.name.substringBeforeLast('.') else item.name
        val ext = if (item.name.contains('.')) "." + item.name.substringAfterLast('.') else ""
        val copyName = "$nameWithoutExt (Copy)$ext"

        val duplicated = item.copy(
            id = "copy_" + System.currentTimeMillis(),
            name = copyName,
            lastModified = System.currentTimeMillis(),
            isPinned = false
        )

        _uiState.update { state ->
            val updatedAll = listOf(duplicated) + state.allItems
            val updatedRecent = (listOf(duplicated) + state.recentItems).take(8)
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                actionMenuItem = null,
                statusMessage = "Created \"$copyName\""
            )
        }
        viewModelScope.launch {
            repo.insertItem(duplicated)
        }
    }

    fun moveItem(item: FolderItem, targetFolderId: String?) {
        if (item.parentId == targetFolderId) {
            _uiState.update { it.copy(moveTargetItem = null) }
            return
        }

        val updatedItem = item.copy(parentId = targetFolderId, lastModified = System.currentTimeMillis())

        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) updatedItem else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                moveTargetItem = null,
                actionMenuItem = null,
                statusMessage = "Moved \"${item.name}\""
            )
        }
        viewModelScope.launch {
            repo.updateItem(updatedItem)
        }
    }

    fun setActiveFilterTab(tab: String) {
        _uiState.update { state ->
            state.copy(
                activeFilterTab = tab,
                currentItems = computeCurrentItems(state.allItems, state.currentFolderId, state.searchQuery, tab, state.sortOption)
            )
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = sort,
                currentItems = computeCurrentItems(state.allItems, state.currentFolderId, state.searchQuery, state.activeFilterTab, sort)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                currentItems = computeCurrentItems(state.allItems, state.currentFolderId, query, state.activeFilterTab, state.sortOption)
            )
        }
    }

    fun setGridColumns(columns: Int) {
        _uiState.update { it.copy(gridColumns = columns.coerceIn(1, 4)) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun computeCurrentItems(
        allItems: List<FolderItem>,
        currentFolderId: String?,
        searchQuery: String,
        filterTab: String,
        sortOption: SortOption
    ): List<FolderItem> {
        val baseItems = when {
            searchQuery.isNotBlank() && currentFolderId == null && (filterTab == "ALL" || filterTab.isEmpty()) -> allItems
            filterTab == "PINNED" -> allItems.filter { it.isPinned }
            filterTab == "STARRED" -> allItems.filter { it.isFavorite }
            filterTab == "DOCS" -> allItems.filter { !it.isDirectory && (it.extension in listOf("md", "txt", "pdf", "doc", "json", "csv")) }
            filterTab == "IMAGES" -> allItems.filter { !it.isDirectory && (it.extension in listOf("png", "jpg", "jpeg", "webp", "gif", "svg")) }
            else -> allItems.filter { it.parentId == currentFolderId }
        }

        val filtered = if (searchQuery.isBlank()) {
            baseItems
        } else {
            baseItems.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                (it.contentData?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        // Sort: Pinned items always stay on top, followed by sort choice (folders then files)
        return filtered.sortedWith(
            compareByDescending<FolderItem> { it.isPinned }
                .thenByDescending { it.isDirectory }
                .thenComparing { a, b ->
                    when (sortOption) {
                        SortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                        SortOption.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                        SortOption.DATE_DESC -> b.lastModified.compareTo(a.lastModified)
                        SortOption.DATE_ASC -> a.lastModified.compareTo(b.lastModified)
                        SortOption.SIZE_DESC -> b.sizeBytes.compareTo(a.sizeBytes)
                    }
                }
        )
    }

    fun toggleItemSelection(id: String) {
        _uiState.update { state ->
            val updated = if (id in state.selectedItemIds) {
                state.selectedItemIds - id
            } else {
                state.selectedItemIds + id
            }
            state.copy(selectedItemIds = updated)
        }
    }

    fun selectItem(id: String) {
        _uiState.update { state ->
            state.copy(selectedItemIds = state.selectedItemIds + id)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItemIds = emptySet()) }
    }

    fun openRenameForSelected() {
        val state = _uiState.value
        if (state.selectedItemIds.size == 1) {
            val selectedId = state.selectedItemIds.first()
            val target = state.allItems.find { it.id == selectedId }
            if (target != null) {
                _uiState.update { it.copy(renameTargetItem = target) }
            }
        }
    }

    fun deleteSelectedItems() {
        val state = _uiState.value
        val toDelete = state.selectedItemIds
        if (toDelete.isEmpty()) return

        val count = toDelete.size
        // Recursively collect ids to delete for any selected folders
        val allIdsToDelete = mutableSetOf<String>()
        allIdsToDelete.addAll(toDelete)

        fun collectChildIds(parentId: String) {
            val children = state.allItems.filter { it.parentId == parentId }
            for (child in children) {
                allIdsToDelete.add(child.id)
                if (child.isDirectory) {
                    collectChildIds(child.id)
                }
            }
        }

        for (id in toDelete) {
            val item = state.allItems.find { it.id == id }
            if (item != null && item.isDirectory) {
                collectChildIds(item.id)
            }
        }

        _uiState.update { s ->
            val updatedAll = s.allItems.filter { it.id !in allIdsToDelete }
            val updatedCurrent = updatedAll.filter { it.parentId == s.currentFolderId }
            val updatedRecent = s.recentItems.filter { it.id !in allIdsToDelete }

            s.copy(
                allItems = updatedAll,
                currentItems = filterBySearch(updatedCurrent, s.searchQuery),
                recentItems = updatedRecent,
                selectedItemIds = emptySet(),
                selectedNote = if (s.selectedNote?.id in allIdsToDelete) null else s.selectedNote,
                statusMessage = if (count == 1) "Deleted 1 item" else "Deleted $count items"
            )
        }
        viewModelScope.launch {
            repo.deleteItems(allIdsToDelete.toList())
        }
    }

    fun restoreItems(importedItems: List<FolderItem>, merge: Boolean) {
        if (importedItems.isEmpty()) return

        _uiState.update { state ->
            val finalItems = if (merge) {
                val existingMap = state.allItems.associateBy { it.id }.toMutableMap()
                for (item in importedItems) {
                    existingMap[item.id] = item
                }
                existingMap.values.toList()
            } else {
                importedItems
            }

            val resetFolderId = if (merge) state.currentFolderId else null
            val resetStack = if (merge) state.folderStack else listOf(Breadcrumb(id = null, name = "Root"))
            val computedCurrent = computeCurrentItems(
                finalItems,
                resetFolderId,
                state.searchQuery,
                state.activeFilterTab,
                state.sortOption
            )
            val computedRecent = finalItems
                .filter { !it.isDirectory }
                .sortedByDescending { it.lastModified }
                .take(8)

            state.copy(
                currentFolderId = resetFolderId,
                folderStack = resetStack,
                allItems = finalItems,
                currentItems = computedCurrent,
                recentItems = computedRecent,
                selectedItemIds = emptySet(),
                statusMessage = if (merge) "Merged ${importedItems.size} items from backup" else "Restored ${importedItems.size} items from backup"
            )
        }
        viewModelScope.launch {
            if (!merge) {
                repo.clearAll()
            }
            repo.insertItems(importedItems)
        }
    }

    fun addOrUpdateItem(item: FolderItem) {
        _uiState.update { state ->
            val updated = state.allItems.filter { it.id != item.id } + item
            val computedCurrent = computeCurrentItems(
                updated,
                state.currentFolderId,
                state.searchQuery,
                state.activeFilterTab,
                state.sortOption
            )
            val computedRecent = (listOf(item) + state.recentItems.filter { it.id != item.id }).take(8)
            state.copy(
                allItems = updated,
                currentItems = computedCurrent,
                recentItems = computedRecent
            )
        }
        viewModelScope.launch {
            repo.insertItem(item)
        }
    }

    fun setStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    private fun filterBySearch(items: List<FolderItem>, query: String): List<FolderItem> {
        if (query.isBlank()) return items
        return items.filter {
            it.name.contains(query, ignoreCase = true) ||
            (it.contentData?.contains(query, ignoreCase = true) == true)
        }
    }
}
