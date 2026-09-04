package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.repository.FlashcardRepository
import com.example.viewmodel.DeckViewModel
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
class DeckViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FlashcardRepository
    private lateinit var viewModel: DeckViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FlashcardRepository.createInMemory()
        viewModel = DeckViewModel(repository = repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state contains pre-seeded decks`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.decks.isNotEmpty())
    }

    @Test
    fun `create deck adds deck to state and repository`() = runTest {
        advanceUntilIdle()
        val initialCount = viewModel.uiState.value.decks.size

        viewModel.createDeck(
            title = "Test Physiology",
            description = "Cardio system",
            topic = "Cardiology",
            categoryColor = Color(0xFF8B5CF6),
            customCards = listOf("Heart chambers?" to "4 chambers")
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(initialCount + 1, state.decks.size)
        val created = state.decks.firstOrNull { it.title == "Test Physiology" }
        assertNotNull(created)
        assertEquals("Test Physiology", created?.title)
        assertEquals(1, created?.cards?.size)
    }

    @Test
    fun `delete deck removes deck from state and repository`() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.decks.first()
        viewModel.deleteDeck(target)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.decks.any { it.id == target.id })
    }

    @Test
    fun `toggle star updates star status in state and repository`() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.decks.first()
        val originalStarred = target.isStarred

        viewModel.toggleStarDeck(target.id)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.decks.first { it.id == target.id }
        assertEquals(!originalStarred, updated.isStarred)
    }

    @Test
    fun `update deck progress persists updated progress`() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.decks.first()

        viewModel.updateDeckProgress(target.id, 0.85f)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.decks.first { it.id == target.id }
        assertEquals(0.85f, updated.progress, 0.01f)
    }
}
