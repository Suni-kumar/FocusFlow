package com.sepfol.app.ui.folder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC,
    SIZE_DESC
}

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

data class Breadcrumb(
    val id: String?,
    val name: String
)

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

class FolderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialItems = listOf(
            // Root Folders
            FolderItem(
                id = "folder_university",
                name = "University",
                isDirectory = true,
                parentId = null,
                lastModified = System.currentTimeMillis() - 3600000 * 2,
                itemCount = 3
            ),
            FolderItem(
                id = "folder_work",
                name = "Work",
                isDirectory = true,
                parentId = null,
                lastModified = System.currentTimeMillis() - 3600000 * 5,
                itemCount = 2
            ),
            FolderItem(
                id = "folder_personal",
                name = "Personal",
                isDirectory = true,
                parentId = null,
                lastModified = System.currentTimeMillis() - 86400000 * 2,
                itemCount = 1
            ),
            FolderItem(
                id = "folder_flashcards",
                name = "Flashcards & Decks",
                isDirectory = true,
                parentId = null,
                lastModified = System.currentTimeMillis() - 86400000 * 3,
                itemCount = 2
            ),

            // Inside University
            FolderItem(
                id = "folder_neuro",
                name = "Neurobiology",
                isDirectory = true,
                parentId = "folder_university",
                lastModified = System.currentTimeMillis() - 3600000,
                itemCount = 2
            ),
            FolderItem(
                id = "file_bio10",
                name = "Bio Lecture 10.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_university",
                contentData = "# Bio Lecture 10: Cellular Energetics\n\n- Key concepts of membrane potential and ATP synthesis.\n- Electron transport chain in the inner mitochondrial membrane.\n- Chemiosmosis and electrochemical proton gradients.",
                sizeBytes = 2450L,
                lastModified = System.currentTimeMillis() - 7200000
            ),
            FolderItem(
                id = "file_assign",
                name = "Assignment Checklist.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_university",
                contentData = "# University Tasks\n- [x] Submit Neurobiology Lab Report\n- [ ] Literature review for Cognitive Psychology\n- [ ] Practice JLPT deck for 30 minutes",
                sizeBytes = 320L,
                lastModified = System.currentTimeMillis() - 14400000
            ),

            // Inside Neurobiology subfolder
            FolderItem(
                id = "file_synaptic",
                name = "Synaptic Pruning Notes.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_neuro",
                contentData = "# Synaptic Pruning\n\nThe brain eliminates extra synapses to increase cognitive efficiency.\nOccurs predominantly during late childhood and early adolescence.\n\n### Key Mechanisms:\n1. Microglia engulfment of inactive dendritic spines.\n2. Complement cascade (C1q, C3) tagging.",
                sizeBytes = 1420L,
                lastModified = System.currentTimeMillis() - 120000
            ),
            FolderItem(
                id = "file_hebbian",
                name = "Hebbian Learning.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_neuro",
                contentData = "# Hebbian Theory\n\n*\"Neurons that fire together, wire together.\"*\n\nDescribes how the adaptation of neurons occurs in the brain during the learning process.",
                sizeBytes = 890L,
                lastModified = System.currentTimeMillis() - 3600000
            ),

            // Inside Work
            FolderItem(
                id = "file_q3",
                name = "Q3 Product Architecture.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_work",
                contentData = "# Q3 Client System Architecture\n\n- Offline-first local SQLite cache with Room.\n- Reactive Flow streams for instant UI re-render.\n- Liquid glass UI components with zero latency.",
                sizeBytes = 3100L,
                lastModified = System.currentTimeMillis() - 3600000 * 4
            ),
            FolderItem(
                id = "file_retro",
                name = "Sprint Retrospective.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_work",
                contentData = "# Sprint Retro\n- Faster compose state reconciliation.\n- Clean breadcrumb hierarchy navigation.\n- Validated modal inputs.",
                sizeBytes = 1200L,
                lastModified = System.currentTimeMillis() - 86400000
            ),

            // Inside Personal
            FolderItem(
                id = "file_books",
                name = "Reading List 2026.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_personal",
                contentData = "# 2026 Reading List\n1. Gödel, Escher, Bach - Douglas Hofstadter\n2. The Master and His Emissary - Iain McGilchrist\n3. Structure and Interpretation of Computer Programs",
                sizeBytes = 410L,
                lastModified = System.currentTimeMillis() - 86400000 * 2
            ),

            // Inside Flashcards folder
            FolderItem(
                id = "file_qm",
                name = "Quantum Mechanics Summary.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_flashcards",
                contentData = "# Quantum Mechanics Postulates\n\n1. State vector |ψ⟩ in Hilbert space.\n2. Observables represented by Hermitian operators.\n3. Probability given by Born rule P = |⟨φ|ψ⟩|².",
                sizeBytes = 1850L,
                lastModified = System.currentTimeMillis() - 86400000 * 3
            ),
            FolderItem(
                id = "file_jlpt",
                name = "JLPT N2 Vocabulary List.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = "folder_flashcards",
                contentData = "# JLPT N2 Target Kanji\n- 考慮 (こうりょ) : Consideration\n- 把握 (はあく) : Grasp / comprehension\n- 促進 (そくしん) : Promotion / acceleration",
                sizeBytes = 5600L,
                lastModified = System.currentTimeMillis() - 86400000 * 4
            ),

            // Root Files (PDFs, Images & Notes)
            FolderItem(
                id = "file_maths_pdf",
                name = "Maths_SecP1X_2026_27.pdf",
                isDirectory = false,
                extension = "pdf",
                mimeType = "application/pdf",
                parentId = null,
                contentData = "Mathematics Subject Code 041 & 241 Class X (2026-27) Comprehensive Syllabus & Course Structure with 10 Pages",
                sizeBytes = 2450000L,
                lastModified = System.currentTimeMillis() - 60000
            ),
            FolderItem(
                id = "file_physics_pdf",
                name = "Physics_Mechanics_Handbook.pdf",
                isDirectory = false,
                extension = "pdf",
                mimeType = "application/pdf",
                parentId = null,
                contentData = "Complete mechanics formulas, diagrams, vectors, and Newtonian physics questions",
                sizeBytes = 3800000L,
                lastModified = System.currentTimeMillis() - 3600000
            ),
            FolderItem(
                id = "file_cell_img",
                name = "Biology_Cell_Diagram.png",
                isDirectory = false,
                extension = "png",
                mimeType = "image/png",
                parentId = null,
                contentData = "High-resolution diagram of plant & animal cell organelles and mitochondria",
                sizeBytes = 1950000L,
                lastModified = System.currentTimeMillis() - 7200000
            ),
            FolderItem(
                id = "file_arch_img",
                name = "System_Architecture_Diagram.png",
                isDirectory = false,
                extension = "png",
                mimeType = "image/png",
                parentId = "folder_work",
                contentData = "High level client architecture diagram with offline-first Room cache",
                sizeBytes = 2800000L,
                lastModified = System.currentTimeMillis() - 14400000
            ),
            FolderItem(
                id = "file_root_bio",
                name = "Biology Notes.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = null,
                contentData = "# High Level Biology Notes\nComprehensive overview of neurobiology, synaptic plasticity, and biological systems.",
                sizeBytes = 1600L,
                lastModified = System.currentTimeMillis() - 120000
            ),
            FolderItem(
                id = "file_root_report",
                name = "Q3 Report.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = null,
                contentData = "# Q3 Executive Overview\nSummary of quarterly performance and study metrics.",
                sizeBytes = 2800L,
                lastModified = System.currentTimeMillis() - 3600000
            ),
            FolderItem(
                id = "file_root_neuro",
                name = "Neuro Systems.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                parentId = null,
                contentData = "# Central vs Peripheral Nervous System\nStructural mappings and synaptic pathway diagrams.",
                sizeBytes = 2100L,
                lastModified = System.currentTimeMillis() - 86400000 * 3
            )
        )

        val rootItems = initialItems.filter { it.parentId == null }
        val recentNotes = initialItems.filter { !it.isDirectory }.sortedByDescending { it.lastModified }.take(6)

        _uiState.value = FolderUiState(
            currentFolderId = null,
            folderStack = listOf(Breadcrumb(id = null, name = "Root")),
            allItems = initialItems,
            currentItems = rootItems,
            recentItems = recentNotes,
            gridColumns = 2
        )
    }

    fun openFolder(folder: FolderItem) {
        if (!folder.isDirectory) return

        _uiState.update { state ->
            val newStack = state.folderStack + Breadcrumb(id = folder.id, name = folder.name)
            val filtered = state.allItems.filter { it.parentId == folder.id }
            state.copy(
                currentFolderId = folder.id,
                folderStack = newStack,
                currentItems = filterBySearch(filtered, state.searchQuery)
            )
        }
    }

    fun navigateToBreadcrumb(index: Int) {
        _uiState.update { state ->
            if (index < 0 || index >= state.folderStack.size) return@update state
            val targetCrumb = state.folderStack[index]
            val newStack = state.folderStack.take(index + 1)
            val filtered = state.allItems.filter { it.parentId == targetCrumb.id }
            state.copy(
                currentFolderId = targetCrumb.id,
                folderStack = newStack,
                currentItems = filterBySearch(filtered, state.searchQuery)
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
                val filtered = state.allItems.filter { it.parentId == targetCrumb.id }
                state.copy(
                    currentFolderId = targetCrumb.id,
                    folderStack = newStack,
                    currentItems = filterBySearch(filtered, state.searchQuery)
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
            val updatedCurrent = updatedAll.filter { it.parentId == state.currentFolderId }
            state.copy(
                allItems = updatedAll,
                currentItems = filterBySearch(updatedCurrent, state.searchQuery),
                isCreateFolderDialogOpen = false,
                statusMessage = "Created folder \"$trimmed\""
            )
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
            val updatedCurrent = updatedAll.filter { it.parentId == state.currentFolderId }
            val updatedRecent = (listOf(newNote) + state.recentItems.filter { it.id != newNote.id }).take(8)
            state.copy(
                allItems = updatedAll,
                currentItems = filterBySearch(updatedCurrent, state.searchQuery),
                recentItems = updatedRecent,
                isCreateNoteDialogOpen = false,
                statusMessage = "Saved note \"$finalTitle\""
            )
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
            val updatedCurrent = updatedAll.filter { it.parentId == state.currentFolderId }
            val updatedRecent = (listOf(newItem) + state.recentItems.filter { it.id != newItem.id }).take(8)
            state.copy(
                allItems = updatedAll,
                currentItems = filterBySearch(updatedCurrent, state.searchQuery),
                recentItems = updatedRecent,
                statusMessage = "Imported \"$fileName\" successfully"
            )
        }
    }

    fun deleteItem(item: FolderItem) {
        _uiState.update { state ->
            // Recursively collect ids to delete if folder
            val idsToDelete = mutableSetOf(item.id)
            if (item.isDirectory) {
                fun collectChildIds(parentId: String) {
                    val children = state.allItems.filter { it.parentId == parentId }
                    for (child in children) {
                        idsToDelete.add(child.id)
                        if (child.isDirectory) {
                            collectChildIds(child.id)
                        }
                    }
                }
                collectChildIds(item.id)
            }

            val updatedAll = state.allItems.filter { it.id !in idsToDelete }
            val updatedCurrent = updatedAll.filter { it.parentId == state.currentFolderId }
            val updatedRecent = state.recentItems.filter { it.id !in idsToDelete }

            state.copy(
                allItems = updatedAll,
                currentItems = filterBySearch(updatedCurrent, state.searchQuery),
                recentItems = updatedRecent,
                selectedNote = if (state.selectedNote?.id in idsToDelete) null else state.selectedNote,
                statusMessage = "Deleted \"${item.name}\""
            )
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

        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) it.copy(name = trimmed, extension = extension, lastModified = System.currentTimeMillis())
                else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) it.copy(name = trimmed, extension = extension, lastModified = System.currentTimeMillis())
                else it
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
    }

    fun togglePin(item: FolderItem) {
        _uiState.update { state ->
            val newPin = !item.isPinned
            val updatedAll = state.allItems.map {
                if (it.id == item.id) it.copy(isPinned = newPin) else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) it.copy(isPinned = newPin) else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                actionMenuItem = null,
                statusMessage = if (newPin) "Pinned \"${item.name}\"" else "Unpinned \"${item.name}\""
            )
        }
    }

    fun toggleFavorite(item: FolderItem) {
        _uiState.update { state ->
            val newFav = !item.isFavorite
            val updatedAll = state.allItems.map {
                if (it.id == item.id) it.copy(isFavorite = newFav) else it
            }
            val updatedRecent = state.recentItems.map {
                if (it.id == item.id) it.copy(isFavorite = newFav) else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                recentItems = updatedRecent,
                actionMenuItem = null,
                statusMessage = if (newFav) "Added to Starred" else "Removed from Starred"
            )
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
    }

    fun moveItem(item: FolderItem, targetFolderId: String?) {
        if (item.parentId == targetFolderId) {
            _uiState.update { it.copy(moveTargetItem = null) }
            return
        }

        _uiState.update { state ->
            val updatedAll = state.allItems.map {
                if (it.id == item.id) it.copy(parentId = targetFolderId, lastModified = System.currentTimeMillis())
                else it
            }
            state.copy(
                allItems = updatedAll,
                currentItems = computeCurrentItems(updatedAll, state.currentFolderId, state.searchQuery, state.activeFilterTab, state.sortOption),
                moveTargetItem = null,
                actionMenuItem = null,
                statusMessage = "Moved \"${item.name}\""
            )
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
        val baseItems = when (filterTab) {
            "PINNED" -> allItems.filter { it.isPinned }
            "STARRED" -> allItems.filter { it.isFavorite }
            "DOCS" -> allItems.filter { !it.isDirectory && (it.extension in listOf("md", "txt", "pdf", "doc", "json", "csv")) }
            "IMAGES" -> allItems.filter { !it.isDirectory && (it.extension in listOf("png", "jpg", "jpeg", "webp", "gif", "svg")) }
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
