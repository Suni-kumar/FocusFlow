package com.example

import com.sepfol.app.ui.folder.FolderItem
import com.sepfol.app.ui.folder.FolderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FolderViewModelTest {

    private lateinit var viewModel: FolderViewModel

    @Before
    fun setUp() {
        viewModel = FolderViewModel()
    }

    @Test
    fun `initial state contains root folders and notes`() {
        val state = viewModel.uiState.value
        assertEquals(null, state.currentFolderId)
        assertEquals(1, state.folderStack.size)
        assertEquals("Root", state.folderStack[0].name)
        assertTrue(state.currentItems.isNotEmpty())
        assertTrue(state.currentItems.any { it.isDirectory && it.name == "University" })
    }

    @Test
    fun `open folder updates stack and filters children`() {
        val state = viewModel.uiState.value
        val universityFolder = state.currentItems.first { it.name == "University" }

        viewModel.openFolder(universityFolder)

        val updatedState = viewModel.uiState.value
        assertEquals(universityFolder.id, updatedState.currentFolderId)
        assertEquals(2, updatedState.folderStack.size)
        assertEquals("University", updatedState.folderStack.last().name)
        assertTrue(updatedState.currentItems.all { it.parentId == universityFolder.id })
    }

    @Test
    fun `navigate up returns to parent folder`() {
        val universityFolder = viewModel.uiState.value.currentItems.first { it.name == "University" }
        viewModel.openFolder(universityFolder)
        assertEquals(2, viewModel.uiState.value.folderStack.size)

        val handled = viewModel.navigateUp()
        assertTrue(handled)

        val rootState = viewModel.uiState.value
        assertEquals(null, rootState.currentFolderId)
        assertEquals(1, rootState.folderStack.size)
        assertEquals("Root", rootState.folderStack[0].name)
    }

    @Test
    fun `create folder adds item to active directory`() {
        val folderName = "Cellular Biology"
        viewModel.createFolder(folderName)

        val state = viewModel.uiState.value
        val created = state.currentItems.firstOrNull { it.name == folderName }
        assertNotNull(created)
        assertTrue(created!!.isDirectory)
        assertEquals(null, created.parentId)
        assertTrue(created.id.startsWith("folder_"))
    }

    @Test
    fun `create markdown note auto-appends md extension and sets size`() {
        val title = "Action Potentials"
        val content = "# Action Potential Stages\n1. Depolarization\n2. Repolarization"
        viewModel.createMarkdownNote(title, content)

        val state = viewModel.uiState.value
        val created = state.currentItems.firstOrNull { it.name == "Action Potentials.md" }
        assertNotNull(created)
        assertFalse(created!!.isDirectory)
        assertEquals("md", created.extension)
        assertEquals("text/markdown", created.mimeType)
        assertEquals(content.toByteArray(Charsets.UTF_8).size.toLong(), created.sizeBytes)
        assertEquals(content, created.contentData)
    }

    @Test
    fun `delete item removes item and reflects in current items`() {
        viewModel.createFolder("Temp Folder")
        val folder = viewModel.uiState.value.currentItems.first { it.name == "Temp Folder" }

        viewModel.deleteItem(folder)

        val state = viewModel.uiState.value
        assertFalse(state.currentItems.any { it.id == folder.id })
    }

    @Test
    fun `import file adds custom file from storage to active folder`() {
        val fileName = "Lecture_01_Introduction.pdf"
        val mimeType = "application/pdf"
        val sizeBytes = 2048576L
        val content = null

        viewModel.importFile(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            contentData = content
        )

        val state = viewModel.uiState.value
        val imported = state.currentItems.firstOrNull { it.name == fileName }
        assertNotNull(imported)
        assertFalse(imported!!.isDirectory)
        assertEquals("pdf", imported.extension)
        assertEquals("application/pdf", imported.mimeType)
        assertEquals(sizeBytes, imported.sizeBytes)
        assertEquals(null, imported.contentData)
        assertTrue(state.statusMessage?.contains(fileName) == true)
    }

    @Test
    fun `toggleSpeedDial toggles isSpeedDialOpen correctly`() {
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.toggleSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.toggleSpeedDial()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
    }

    @Test
    fun `openCreateFolderDialog closes speed dial and opens dialog`() {
        viewModel.openSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.openCreateFolderDialog()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
        assertTrue(viewModel.uiState.value.isCreateFolderDialogOpen)
    }

    @Test
    fun `openCreateNoteDialog closes speed dial and opens dialog`() {
        viewModel.openSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.openCreateNoteDialog()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
        assertTrue(viewModel.uiState.value.isCreateNoteDialogOpen)
    }
}
