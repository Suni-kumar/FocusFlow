package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusFlowApplication
import com.example.data.ai.GeminiDictationService
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.DictationRepository
import com.example.data.speech.DictationVoiceCommand
import com.example.data.speech.DictationVoiceCommander
import com.example.data.speech.FlashcardAudioPlayer
import com.example.model.DictationDeck
import com.example.model.DictationMockDataSource
import com.example.model.DictationWord
import com.example.model.DictationWordStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@androidx.compose.runtime.Stable
data class DictationUiState(
    val decks: List<DictationDeck> = emptyList(),
    val selectedDeckIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val customApiKey: String = "",
    val statusMessage: String? = null,

    // Active Practice Session State
    val activeDeck: DictationDeck? = null,
    val currentWordIndex: Int = 0,
    val isPracticeActive: Boolean = false,
    val isWordCardVisible: Boolean = false,
    val isMeaningSpeaking: Boolean = false,
    val isSessionAsleep: Boolean = false,
    val lastVoiceCommand: DictationVoiceCommand = DictationVoiceCommand.NONE,
    val lastSpokenText: String = "",
    val wordResults: Map<String, DictationWordStatus> = emptyMap(),

    // Dialogs & Modals
    val isCreateDeckDialogOpen: Boolean = false,
    val editingDeck: DictationDeck? = null,
    val isAiGenerateDialogOpen: Boolean = false,
    val isAiGenerating: Boolean = false,
    val aiProgressMessage: String = "",
    val aiInitialPrompt: String = ""
) {
    val editTargetDeck: DictationDeck? get() = editingDeck
    val aiGenerationProgressMessage: String get() = aiProgressMessage
}

class DictationViewModel @JvmOverloads constructor(
    application: Application,
    repository: DictationRepository? = null
) : AndroidViewModel(application) {

    private val repo: DictationRepository = repository
        ?: try {
            FocusFlowApplication.instance.dictationRepository
        } catch (e: Exception) {
            DictationRepository.createInMemory()
        }

    private val prefsManager = UserPreferencesManager(application)
    private val audioPlayer = FlashcardAudioPlayer.getInstance(application)
    private val aiService = GeminiDictationService()
    val voiceCommander = DictationVoiceCommander(application)

    private val _uiState = MutableStateFlow(
        DictationUiState(
            customApiKey = prefsManager.customApiKey
        )
    )
    val uiState: StateFlow<DictationUiState> = _uiState.asStateFlow()

    private var autoDismissCardJob: Job? = null

    init {
        viewModelScope.launch {
            repo.allDecks.collect { decks ->
                _uiState.update { it.copy(decks = decks) }
            }
        }

        voiceCommander.onCommandRecognized = { command ->
            handleVoiceCommand(command)
        }

        voiceCommander.onInactivityTimeout = {
            _uiState.update { it.copy(isSessionAsleep = true) }
            postStatusMessage("Sleep Mode: Microphone paused after 5 minutes inactivity")
        }

        viewModelScope.launch {
            voiceCommander.lastRecognizedText.collect { text ->
                _uiState.update { it.copy(lastSpokenText = text) }
            }
        }

        viewModelScope.launch {
            voiceCommander.isAsleepDueToInactivity.collect { isAsleep ->
                _uiState.update { it.copy(isSessionAsleep = isAsleep) }
            }
        }
    }

    fun setInitialCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key.trim()) }
    }

    fun updateCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key.trim()) }
    }

    fun setCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key.trim()) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun postStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedTag(tag: String?) {
        _uiState.update { it.copy(selectedTag = if (it.selectedTag == tag) null else tag) }
    }

    fun toggleDeckSelection(deckId: String) {
        _uiState.update { state ->
            val updated = if (deckId in state.selectedDeckIds) {
                state.selectedDeckIds - deckId
            } else {
                state.selectedDeckIds + deckId
            }
            state.copy(selectedDeckIds = updated)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDeckIds = emptySet()) }
    }

    fun toggleStarDeck(deckId: String) {
        _uiState.update { state ->
            state.copy(
                decks = state.decks.map { deck ->
                    if (deck.id == deckId) deck.copy(isStarred = !deck.isStarred) else deck
                }
            )
        }
        viewModelScope.launch {
            repo.toggleStarDeck(deckId)
        }
    }

    // ==========================================
    // DICTATION PRACTICE SESSION FLOW
    // ==========================================

    fun startPracticeSession(deck: DictationDeck) {
        if (deck.words.isEmpty()) return
        val initialMap = deck.words.associate { it.id to DictationWordStatus.UNTESTED }
        _uiState.update {
            it.copy(
                activeDeck = deck,
                currentWordIndex = 0,
                isPracticeActive = true,
                isWordCardVisible = false,
                isMeaningSpeaking = false,
                isSessionAsleep = false,
                lastVoiceCommand = DictationVoiceCommand.NONE,
                wordResults = initialMap
            )
        }
        voiceCommander.startListening()
        playCurrentWord()
    }

    fun wakeUpSession() {
        _uiState.update { it.copy(isSessionAsleep = false) }
        voiceCommander.wakeUp()
        playCurrentWord()
    }

    fun playCurrentWord() {
        val state = _uiState.value
        val deck = state.activeDeck ?: return
        val words = deck.words
        if (words.isEmpty() || state.currentWordIndex !in words.indices) return

        val currentWord = words[state.currentWordIndex]
        voiceCommander.pauseTemporarilyForAudio()
        audioPlayer.speakDictationWord(currentWord.word) {
            voiceCommander.resumeAfterAudio()
        }
    }

    fun repeatCurrentWord() {
        playCurrentWord()
    }

    fun playCurrentWordSlowly() {
        val state = _uiState.value
        val deck = state.activeDeck ?: return
        val words = deck.words
        if (words.isEmpty() || state.currentWordIndex !in words.indices) return

        val currentWord = words[state.currentWordIndex]
        voiceCommander.pauseTemporarilyForAudio()
        audioPlayer.speakDictationWordSlowly(currentWord.word) {
            voiceCommander.resumeAfterAudio()
        }
    }

    fun nextWord() {
        val state = _uiState.value
        val deck = state.activeDeck ?: return
        if (state.currentWordIndex < deck.words.size - 1) {
            _uiState.update {
                it.copy(
                    currentWordIndex = it.currentWordIndex + 1,
                    isWordCardVisible = false
                )
            }
            playCurrentWord()
        } else {
            // Already at last word -> replay last word
            playCurrentWord()
        }
    }

    fun previousWord() {
        val state = _uiState.value
        if (state.currentWordIndex > 0) {
            _uiState.update {
                it.copy(
                    currentWordIndex = it.currentWordIndex - 1,
                    isWordCardVisible = false
                )
            }
            playCurrentWord()
        }
    }

    fun showCurrentWordCard() {
        autoDismissCardJob?.cancel()
        _uiState.update { it.copy(isWordCardVisible = true) }
        autoDismissCardJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(isWordCardVisible = false) }
        }
    }

    fun hideCurrentWordCard() {
        autoDismissCardJob?.cancel()
        _uiState.update { it.copy(isWordCardVisible = false) }
    }

    fun speakCurrentWordMeaning() {
        val state = _uiState.value
        val deck = state.activeDeck ?: return
        val words = deck.words
        if (words.isEmpty() || state.currentWordIndex !in words.indices) return

        val currentWord = words[state.currentWordIndex]
        _uiState.update { it.copy(isMeaningSpeaking = true) }
        voiceCommander.pauseTemporarilyForAudio()
        audioPlayer.speakDictationMeaning(currentWord.word, currentWord.meaning) {
            _uiState.update { it.copy(isMeaningSpeaking = false) }
            voiceCommander.resumeAfterAudio()
        }
    }

    fun handleVoiceCommand(command: DictationVoiceCommand) {
        _uiState.update { it.copy(lastVoiceCommand = command) }
        when (command) {
            DictationVoiceCommand.REPEAT -> repeatCurrentWord()
            DictationVoiceCommand.NEXT -> nextWord()
            DictationVoiceCommand.PREVIOUS -> previousWord()
            DictationVoiceCommand.SHOW_WORD -> showCurrentWordCard()
            DictationVoiceCommand.SAY_MEANING -> speakCurrentWordMeaning()
            DictationVoiceCommand.PAUSE -> {
                audioPlayer.stop()
                voiceCommander.pauseTemporarilyForAudio()
            }
            DictationVoiceCommand.CHECK_TIME -> {
                // UI intercepts
            }
            DictationVoiceCommand.NONE -> {}
            else -> {}
        }
    }

    fun markWordStatus(wordId: String, status: DictationWordStatus) {
        _uiState.update { state ->
            val updated = state.wordResults.toMutableMap()
            updated[wordId] = status
            state.copy(wordResults = updated)
        }
    }

    fun finishPracticeSession(recordAccuracy: Boolean = true) {
        voiceCommander.stopListening()
        audioPlayer.stop()
        val state = _uiState.value
        val active = state.activeDeck
        if (recordAccuracy && active != null) {
            val total = active.words.size
            val correctCount = state.wordResults.values.count { it == DictationWordStatus.CORRECT }
            val accuracyFraction = if (total > 0) (correctCount.toFloat() / total.toFloat()) else 0f

            _uiState.update { s ->
                s.copy(
                    decks = s.decks.map { d ->
                        if (d.id == active.id) {
                            d.copy(
                                accuracy = accuracyFraction,
                                lastPracticed = "Just now"
                            )
                        } else d
                    },
                    isPracticeActive = false,
                    activeDeck = null
                )
            }
            viewModelScope.launch {
                repo.updateDeckPracticeStats(active.id, accuracyFraction, "Just now")
            }
        } else {
            _uiState.update { it.copy(isPracticeActive = false, activeDeck = null) }
        }
    }

    // ==========================================
    // DECK CRUD & AI GENERATION
    // ==========================================

    fun openCreateDeckDialog() {
        _uiState.update { it.copy(isCreateDeckDialogOpen = true) }
    }

    fun dismissCreateDeckDialog() {
        _uiState.update { it.copy(isCreateDeckDialogOpen = false) }
    }

    fun openEditDeckDialog(deck: DictationDeck) {
        _uiState.update { it.copy(editingDeck = deck) }
    }

    fun dismissEditDeckDialog() {
        _uiState.update { it.copy(editingDeck = null) }
    }

    fun openAiGenerateDialog(initialPrompt: String = "") {
        _uiState.update {
            it.copy(
                isAiGenerateDialogOpen = true,
                aiInitialPrompt = initialPrompt,
                aiProgressMessage = ""
            )
        }
    }

    fun dismissAiGenerateDialog() {
        _uiState.update { it.copy(isAiGenerateDialogOpen = false, isAiGenerating = false) }
    }

    fun createDeck(
        title: String,
        description: String,
        categoryColor: androidx.compose.ui.graphics.Color,
        words: List<DictationWord>,
        tags: List<String>
    ) {
        val newDeck = DictationDeck(
            title = title.trim(),
            description = description.trim(),
            categoryColor = categoryColor,
            words = words,
            tags = tags
        )
        _uiState.update { state ->
            state.copy(
                decks = listOf(newDeck) + state.decks,
                isCreateDeckDialogOpen = false,
                statusMessage = "Created \"${newDeck.title}\" with ${newDeck.words.size} words"
            )
        }
        viewModelScope.launch {
            repo.insertDeckWithWords(newDeck)
        }
    }

    fun updateDeck(updatedDeck: DictationDeck) {
        _uiState.update { state ->
            state.copy(
                decks = state.decks.map { if (it.id == updatedDeck.id) updatedDeck else it },
                editingDeck = null,
                activeDeck = if (state.activeDeck?.id == updatedDeck.id) updatedDeck else state.activeDeck,
                statusMessage = "Updated \"${updatedDeck.title}\""
            )
        }
        viewModelScope.launch {
            repo.insertDeckWithWords(updatedDeck)
        }
    }

    fun deleteDeck(deckId: String) {
        _uiState.update { state ->
            state.copy(
                decks = state.decks.filter { it.id != deckId },
                selectedDeckIds = state.selectedDeckIds - deckId,
                statusMessage = "Deck deleted"
            )
        }
        viewModelScope.launch {
            repo.deleteDeck(deckId)
        }
    }

    fun deleteSelectedDecks() {
        val selected = _uiState.value.selectedDeckIds
        if (selected.isEmpty()) return
        _uiState.update { state ->
            state.copy(
                decks = state.decks.filter { it.id !in selected },
                selectedDeckIds = emptySet(),
                statusMessage = "Deleted ${selected.size} deck(s)"
            )
        }
        viewModelScope.launch {
            repo.deleteDecks(selected.toList())
        }
    }

    fun restoreDecks(importedDecks: List<DictationDeck>, merge: Boolean) {
        if (importedDecks.isEmpty()) return
        _uiState.update { state ->
            val finalDecks = if (merge) {
                val map = state.decks.associateBy { it.id }.toMutableMap()
                for (d in importedDecks) map[d.id] = d
                map.values.toList()
            } else {
                importedDecks
            }
            state.copy(
                decks = finalDecks,
                selectedDeckIds = emptySet(),
                statusMessage = if (merge) "Merged ${importedDecks.size} dictation decks" else "Restored ${importedDecks.size} dictation decks"
            )
        }
        viewModelScope.launch {
            if (!merge) {
                repo.clearAll()
            }
            repo.insertDecksWithWords(importedDecks)
        }
    }

    fun addOrUpdateDeck(deck: DictationDeck) {
        _uiState.update { state ->
            val updated = listOf(deck) + state.decks.filter { it.id != deck.id }
            state.copy(decks = updated, statusMessage = "Imported dictation deck \"${deck.title}\"")
        }
        viewModelScope.launch {
            repo.insertDeckWithWords(deck)
        }
    }

    fun generateAiDeck(
        topicOrNotes: String = "",
        inputContent: String = topicOrNotes,
        deckTitle: String? = null,
        targetWordCount: Int = 10,
        targetCount: Int = targetWordCount,
        onComplete: ((DictationDeck) -> Unit)? = null
    ) {
        val finalContent = if (inputContent.isNotBlank()) inputContent else topicOrNotes
        val finalCount = if (targetCount != 10) targetCount else targetWordCount

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiGenerating = true,
                    aiProgressMessage = "Connecting to Gemini AI..."
                )
            }

            try {
                _uiState.update { it.copy(aiProgressMessage = "Extracting words & generating contextual meanings...") }

                val generatedDeck = aiService.generateOrParseDictationDeck(
                    inputContent = finalContent,
                    userCustomApiKey = _uiState.value.customApiKey,
                    deckTitleOverride = deckTitle,
                    targetWordCount = finalCount
                )

                _uiState.update { state ->
                    state.copy(
                        decks = listOf(generatedDeck) + state.decks,
                        isAiGenerating = false,
                        isAiGenerateDialogOpen = false,
                        statusMessage = "Created \"${generatedDeck.title}\" with ${generatedDeck.words.size} words"
                    )
                }
                viewModelScope.launch {
                    repo.insertDeckWithWords(generatedDeck)
                }
                onComplete?.invoke(generatedDeck)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiGenerating = false,
                        aiProgressMessage = "Generation failed: ${e.message}",
                        statusMessage = "AI Generation error: ${e.message}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceCommander.destroy()
        audioPlayer.stop()
    }
}
