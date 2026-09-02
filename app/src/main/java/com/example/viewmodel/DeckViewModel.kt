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

@androidx.compose.runtime.Stable
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
        deckTitle: String = "",
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

                val finalTitle = if (deckTitle.isNotBlank()) deckTitle.trim() else result.title

                val newDeck = FlashcardDeck(
                    id = "ai_deck_" + System.currentTimeMillis(),
                    title = finalTitle,
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

                val finalStatus = if (!result.warningMessage.isNullOrBlank()) {
                    "Deck \"${newDeck.title}\" created. (Note: ${result.warningMessage})"
                } else {
                    "Deck \"${newDeck.title}\" ready ($sourceNotice)"
                }

                _uiState.update { state ->
                    state.copy(
                        decks = listOf(newDeck) + state.decks,
                        isAiGenerating = false,
                        isAiGenerateDialogOpen = false,
                        aiInitialPrompt = "",
                        statusMessage = finalStatus,
                        newlyGeneratedDeckId = newDeck.id
                    )
                }

                onComplete?.invoke(newDeck)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        aiGenerationProgressMessage = "Error: ${e.message ?: "fallback applied"}",
                        statusMessage = "Generation notice: ${e.message ?: "fallback applied"}"
                    )
                }
            }
        }
    }

    fun updateDeckProgress(deckId: String, progress: Float, lastReviewed: String = "Just now") {
        _uiState.update { state ->
            val updatedDecks = state.decks.map { deck ->
                if (deck.id == deckId) {
                    deck.copy(
                        progress = progress.coerceIn(0f, 1f),
                        lastReviewed = lastReviewed
                    )
                } else deck
            }
            state.copy(decks = updatedDecks)
        }
    }

    fun toggleCardMastery(deckId: String, cardId: String, isMastered: Boolean) {
        _uiState.update { state ->
            val updatedDecks = state.decks.map { deck ->
                if (deck.id == deckId) {
                    val updatedCards = deck.cards.map { card ->
                        if (card.id == cardId) card.copy(isMastered = isMastered) else card
                    }
                    val masteredCount = updatedCards.count { it.isMastered }
                    val newProgress = if (updatedCards.isNotEmpty()) masteredCount.toFloat() / updatedCards.size else 0f
                    deck.copy(
                        cards = updatedCards,
                        progress = newProgress.coerceIn(0f, 1f)
                    )
                } else deck
            }
            state.copy(decks = updatedDecks)
        }
    }

    fun createDeck(
        title: String,
        description: String,
        topic: String,
        categoryColor: Color,
        customCards: List<Pair<String, String>> = emptyList()
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val flashcards = if (customCards.isNotEmpty()) {
            customCards.mapIndexed { idx, pair ->
                Flashcard(
                    id = "c_${System.currentTimeMillis()}_$idx",
                    front = pair.first.trim(),
                    back = pair.second.trim(),
                    topic = topic.ifBlank { trimmedTitle },
                    tags = listOf("#Custom", "#HighYield")
                )
            }
        } else {
            listOf(
                Flashcard(
                    id = "c_" + System.currentTimeMillis(),
                    front = "What is the key objective of $trimmedTitle?",
                    back = "Master the core concepts, definitions, and applications.",
                    topic = topic.ifBlank { trimmedTitle },
                    tags = listOf("#HighYield")
                ),
                Flashcard(
                    id = "c_" + System.currentTimeMillis() + "_2",
                    front = "Core principle & fundamentals of $trimmedTitle",
                    back = "Understand key mechanisms and practical problem solving techniques.",
                    topic = topic.ifBlank { trimmedTitle },
                    tags = listOf("#Fundamental")
                )
            )
        }

        val newDeck = FlashcardDeck(
            id = "deck_" + System.currentTimeMillis(),
            title = trimmedTitle,
            description = description.ifBlank { "Custom flashcard study deck." },
            cardCount = flashcards.size,
            lastReviewed = "Just created",
            progress = 0f,
            iconName = when (topic.lowercase()) {
                "math", "physics" -> "calculate"
                "language", "vocabulary" -> "translate"
                "code", "programming", "system" -> "terminal"
                else -> "psychology"
            },
            categoryColor = categoryColor,
            cards = flashcards,
            tags = listOf("#Custom")
        )

        _uiState.update { state ->
            state.copy(
                decks = listOf(newDeck) + state.decks,
                isCreateDeckDialogOpen = false,
                statusMessage = "Created deck \"$trimmedTitle\" with ${flashcards.size} cards"
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

    fun toggleStarDeck(deckId: String) {
        _uiState.update { state ->
            val updated = state.decks.map { deck ->
                if (deck.id == deckId) deck.copy(isStarred = !deck.isStarred) else deck
            }
            state.copy(decks = updated)
        }
    }

    fun setStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
