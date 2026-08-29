package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiFlashcardService
import com.example.data.ai.GenerationSource
import com.example.model.Flashcard
import com.example.model.FlashcardDeck
import com.example.model.MockDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeckUiState(
    val decks: List<FlashcardDeck> = MockDataSource.decks,
    val selectedDeckIds: Set<String> = emptySet(),
    val isCreateDeckDialogOpen: Boolean = false,
    val isAiGenerateDialogOpen: Boolean = false,
    val aiInitialPrompt: String = "",
    val isAiGenerating: Boolean = false,
    val aiGenerationProgressMessage: String = "",
    val customApiKey: String = "",
    val renameTargetDeck: FlashcardDeck? = null,
    val statusMessage: String? = null,
    val newlyGeneratedDeckId: String? = null
) {
    val isSelectionMode: Boolean
        get() = selectedDeckIds.isNotEmpty()
}

class DeckViewModel(
    private val aiService: GeminiFlashcardService = GeminiFlashcardService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckUiState())
    val uiState: StateFlow<DeckUiState> = _uiState.asStateFlow()

    private val categoryColors = listOf(
        Color(0xFF8B5CF6), // Neon Purple
        Color(0xFF3B82F6), // Focus Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Cyber Amber
        Color(0xFFEC4899), // Hot Pink
        Color(0xFF06B6D4), // Cyan
        Color(0xFF6366F1)  // Indigo
    )

    fun setInitialCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key) }
    }

    fun updateCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key.trim()) }
    }

    fun toggleDeckSelection(deckId: String) {
        _uiState.update { state ->
            val updatedSelection = if (deckId in state.selectedDeckIds) {
                state.selectedDeckIds - deckId
            } else {
                state.selectedDeckIds + deckId
            }
            state.copy(selectedDeckIds = updatedSelection)
        }
    }

    fun selectDeck(deckId: String) {
        _uiState.update { state ->
            state.copy(selectedDeckIds = state.selectedDeckIds + deckId)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDeckIds = emptySet()) }
    }

    fun openCreateDeckDialog() {
        _uiState.update { it.copy(isCreateDeckDialogOpen = true) }
    }

    fun dismissCreateDeckDialog() {
        _uiState.update { it.copy(isCreateDeckDialogOpen = false) }
    }

    fun openAiGenerateDialog(initialPrompt: String = "") {
        _uiState.update {
            it.copy(
                isAiGenerateDialogOpen = true,
                aiInitialPrompt = initialPrompt
            )
        }
    }

    fun dismissAiGenerateDialog() {
        if (_uiState.value.isAiGenerating) return
        _uiState.update { it.copy(isAiGenerateDialogOpen = false, aiInitialPrompt = "") }
    }

    fun generateAiDeck(
        topicOrNotes: String,
        targetCardCount: Int,
        onComplete: ((FlashcardDeck) -> Unit)? = null
    ) {
        val prompt = topicOrNotes.trim()
        if (prompt.isEmpty() || _uiState.value.isAiGenerating) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiGenerating = true,
                    aiGenerationProgressMessage = "Connecting with Gemini AI..."
                )
            }

            try {
                _uiState.update {
                    it.copy(aiGenerationProgressMessage = "Generating high-yield cards...")
                }

                val result = aiService.generateFlashcards(
                    topicOrPrompt = prompt,
                    targetCardCount = targetCardCount,
                    userCustomApiKey = _uiState.value.customApiKey
                )

                val colorIndex = (_uiState.value.decks.size) % categoryColors.size
                val assignedColor = categoryColors[colorIndex]

                val newDeck = FlashcardDeck(
                    id = "ai_deck_" + System.currentTimeMillis(),
                    title = result.title,
                    description = result.description,
                    cardCount = result.cards.size,
                    lastReviewed = "Just created",
                    progress = 0f,
                    iconName = when {
                        result.tags.any { it.contains("Formula", true) || it.contains("Math", true) } -> "calculate"
                        result.tags.any { it.contains("Science", true) || it.contains("Med", true) } -> "biotech"
                        result.tags.any { it.contains("Tech", true) || it.contains("Code", true) } -> "terminal"
                        else -> "auto_awesome"
                    },
                    categoryColor = assignedColor,
                    cards = result.cards,
                    tags = result.tags,
                    isAiGenerated = true
                )

                val sourceNotice = when (result.source) {
                    GenerationSource.BYOK_CLIENT -> "Generated with personal Gemini API key"
                    GenerationSource.SERVER_PROXY_FALLBACK -> "Generated with Gemini AI Cloud"
                    GenerationSource.OFFLINE_HEURISTIC -> "Generated with Smart Taxonomy Engine"
                }

                _uiState.update { state ->
                    state.copy(
                        decks = listOf(newDeck) + state.decks,
                        isAiGenerating = false,
                        isAiGenerateDialogOpen = false,
                        aiInitialPrompt = "",
                        statusMessage = "Deck \"${newDeck.title}\" ready ($sourceNotice)",
                        newlyGeneratedDeckId = newDeck.id
                    )
                }

                onComplete?.invoke(newDeck)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        statusMessage = "Generation notice: ${e.message ?: "fallback applied"}"
                    )
                }
            }
        }
    }

    fun createDeck(
        title: String,
        description: String,
        topic: String,
        categoryColor: Color
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val newDeck = FlashcardDeck(
            id = "deck_" + System.currentTimeMillis(),
            title = trimmedTitle,
            description = description.ifBlank { "Custom flashcard study deck." },
            cardCount = 1,
            lastReviewed = "Just now",
            progress = 0f,
            iconName = when (topic.lowercase()) {
                "math", "physics" -> "calculate"
                "language", "vocabulary" -> "translate"
                "code", "programming", "system" -> "terminal"
                else -> "psychology"
            },
            categoryColor = categoryColor,
            cards = listOf(
                Flashcard(
                    id = "c_" + System.currentTimeMillis(),
                    front = "What is the key objective of $trimmedTitle?",
                    back = "Master the core concepts, definitions, and applications.",
                    topic = topic.ifBlank { trimmedTitle },
                    tags = listOf("#HighYield")
                )
            ),
            tags = listOf("#Custom")
        )

        _uiState.update { state ->
            state.copy(
                decks = listOf(newDeck) + state.decks,
                isCreateDeckDialogOpen = false,
                statusMessage = "Created deck \"$trimmedTitle\""
            )
        }
    }

    fun openRenameDeckDialog(deck: FlashcardDeck) {
        _uiState.update { it.copy(renameTargetDeck = deck) }
    }

    fun openRenameSelected() {
        val state = _uiState.value
        if (state.selectedDeckIds.size == 1) {
            val selectedId = state.selectedDeckIds.first()
            val targetDeck = state.decks.find { it.id == selectedId }
            if (targetDeck != null) {
                _uiState.update { it.copy(renameTargetDeck = targetDeck) }
            }
        }
    }

    fun dismissRenameDeckDialog() {
        _uiState.update { it.copy(renameTargetDeck = null) }
    }

    fun renameDeck(deckId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return

        _uiState.update { state ->
            val updatedDecks = state.decks.map { deck ->
                if (deck.id == deckId) deck.copy(title = trimmed) else deck
            }
            state.copy(
                decks = updatedDecks,
                renameTargetDeck = null,
                selectedDeckIds = emptySet(),
                statusMessage = "Renamed deck to \"$trimmed\""
            )
        }
    }

    fun deleteDeck(deck: FlashcardDeck) {
        _uiState.update { state ->
            val updatedDecks = state.decks.filter { it.id != deck.id }
            state.copy(
                decks = updatedDecks,
                selectedDeckIds = state.selectedDeckIds - deck.id,
                statusMessage = "Deleted deck \"${deck.title}\""
            )
        }
    }

    fun deleteSelectedDecks() {
        val state = _uiState.value
        val toDelete = state.selectedDeckIds
        if (toDelete.isEmpty()) return

        val count = toDelete.size
        _uiState.update { s ->
            val updatedDecks = s.decks.filter { it.id !in toDelete }
            s.copy(
                decks = updatedDecks,
                selectedDeckIds = emptySet(),
                statusMessage = if (count == 1) "Deleted 1 deck" else "Deleted $count decks"
            )
        }
    }

    fun restoreDecks(importedDecks: List<FlashcardDeck>, merge: Boolean) {
        if (importedDecks.isEmpty()) return

        _uiState.update { state ->
            val finalDecks = if (merge) {
                val existingMap = state.decks.associateBy { it.id }.toMutableMap()
                for (deck in importedDecks) {
                    existingMap[deck.id] = deck
                }
                existingMap.values.toList()
            } else {
                importedDecks
            }

            val totalCards = importedDecks.sumOf { it.cards.size }
            state.copy(
                decks = finalDecks,
                selectedDeckIds = emptySet(),
                statusMessage = if (merge) "Merged ${importedDecks.size} decks ($totalCards cards)" else "Restored ${importedDecks.size} decks ($totalCards cards)"
            )
        }
    }

    fun addOrUpdateDeck(deck: FlashcardDeck) {
        _uiState.update { state ->
            val updated = listOf(deck) + state.decks.filter { it.id != deck.id }
            state.copy(
                decks = updated,
                statusMessage = "Imported deck \"${deck.title}\""
            )
        }
    }

    fun setStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
