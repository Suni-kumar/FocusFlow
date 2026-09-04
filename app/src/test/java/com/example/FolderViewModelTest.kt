package com.example

import com.example.data.repository.FolderRepository
import com.sepfol.app.ui.folder.FolderItem
import com.sepfol.app.ui.folder.FolderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FolderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FolderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FolderViewModel(repository = FolderRepository.createInMemory())
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state contains root folders and notes`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(null, state.currentFolderId)
        assertEquals(1, state.folderStack.size)
        assertEquals("Root", state.folderStack[0].name)
        assertTrue(state.currentItems.isNotEmpty())
        assertTrue(state.currentItems.any { it.isDirectory && it.name == "University" })
    }

    @Test
    fun `open folder updates stack and filters children`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        val universityFolder = state.currentItems.first { it.name == "University" }

        viewModel.openFolder(universityFolder)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(universityFolder.id, updatedState.currentFolderId)
        assertEquals(2, updatedState.folderStack.size)
        assertEquals("University", updatedState.folderStack.last().name)
        assertTrue(updatedState.currentItems.all { it.parentId == universityFolder.id })
    }

    @Test
    fun `navigate up returns to parent folder`() = runTest {
        advanceUntilIdle()
        val universityFolder = viewModel.uiState.value.currentItems.first { it.name == "University" }
        viewModel.openFolder(universityFolder)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.folderStack.size)

        val handled = viewModel.navigateUp()
        advanceUntilIdle()
        assertTrue(handled)

        val rootState = viewModel.uiState.value
        assertEquals(null, rootState.currentFolderId)
        assertEquals(1, rootState.folderStack.size)
        assertEquals("Root", rootState.folderStack[0].name)
    }

    @Test
    fun `create folder adds item to active directory`() = runTest {
        advanceUntilIdle()
        val folderName = "Cellular Biology"
        viewModel.createFolder(folderName)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val created = state.currentItems.firstOrNull { it.name == folderName }
        assertNotNull(created)
        assertTrue(created!!.isDirectory)
        assertEquals(null, created.parentId)
        assertTrue(created.id.startsWith("folder_"))
    }

    @Test
    fun `create markdown note auto-appends md extension and sets size`() = runTest {
        advanceUntilIdle()
        val title = "Action Potentials"
        val content = "# Action Potential Stages\n1. Depolarization\n2. Repolarization"
        viewModel.createMarkdownNote(title, content)
        advanceUntilIdle()

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
    fun `delete item removes item and reflects in current items`() = runTest {
        advanceUntilIdle()
        viewModel.createFolder("Temp Folder")
        advanceUntilIdle()
        val folder = viewModel.uiState.value.currentItems.first { it.name == "Temp Folder" }

        viewModel.deleteItem(folder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.currentItems.any { it.id == folder.id })
    }

    @Test
    fun `import file adds custom file from storage to active folder`() = runTest {
        advanceUntilIdle()
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
        advanceUntilIdle()

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
    fun `toggleSpeedDial toggles isSpeedDialOpen correctly`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.toggleSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.toggleSpeedDial()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
    }

    @Test
    fun `openCreateFolderDialog closes speed dial and opens dialog`() = runTest {
        advanceUntilIdle()
        viewModel.openSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.openCreateFolderDialog()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
        assertTrue(viewModel.uiState.value.isCreateFolderDialogOpen)
    }

    @Test
    fun `openCreateNoteDialog closes speed dial and opens dialog`() = runTest {
        advanceUntilIdle()
        viewModel.openSpeedDial()
        assertTrue(viewModel.uiState.value.isSpeedDialOpen)

        viewModel.openCreateNoteDialog()
        assertFalse(viewModel.uiState.value.isSpeedDialOpen)
        assertTrue(viewModel.uiState.value.isCreateNoteDialogOpen)
    }
}
