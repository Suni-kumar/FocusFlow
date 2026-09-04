package com.example.data.repository

import com.example.data.local.dao.DictationDao
import com.example.data.local.entity.DictationDeckWithWords
import com.example.model.DictationDeck
import com.example.model.DictationMockDataSource
import com.example.model.DictationWordStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class DictationRepository(
    private val dao: DictationDao? = null
) {
    private val inMemoryDecks = MutableStateFlow(DictationMockDataSource.getInitialDictationDecks())

    val allDecks: Flow<List<DictationDeck>> = if (dao != null) {
        dao.getAllDecksWithWords().map { list -> list.map { it.toDomain() } }
    } else {
        inMemoryDecks.asStateFlow()
    }

    suspend fun insertDeckWithWords(deck: DictationDeck) {
        if (dao != null) {
            val (deckEntity, wordEntities) = DictationDeckWithWords.fromDomain(deck)
            dao.insertDeckWithWords(deckEntity, wordEntities)
        } else {
            inMemoryDecks.value = listOf(deck) + inMemoryDecks.value.filter { it.id != deck.id }
        }
    }

    suspend fun insertDecksWithWords(decks: List<DictationDeck>) {
        if (dao != null) {
            val pairs = decks.map { DictationDeckWithWords.fromDomain(it) }
            dao.insertDecksWithWords(pairs)
        } else {
            val newIds = decks.map { it.id }.toSet()
            inMemoryDecks.value = decks + inMemoryDecks.value.filter { it.id !in newIds }
        }
    }

    suspend fun updateDeckInfo(deckId: String, title: String, description: String) {
        if (dao != null) {
            dao.updateDeckInfo(deckId, title, description)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map {
                if (it.id == deckId) it.copy(title = title, description = description) else it
            }
        }
    }

    suspend fun deleteDeck(deckId: String) {
        if (dao != null) {
            dao.deleteDeckById(deckId)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.filter { it.id != deckId }
        }
    }

    suspend fun deleteDecks(deckIds: List<String>) {
        if (dao != null) {
            dao.deleteDecksByIds(deckIds)
        } else {
            val idSet = deckIds.toSet()
            inMemoryDecks.value = inMemoryDecks.value.filter { it.id !in idSet }
        }
    }

    suspend fun toggleStarDeck(deckId: String) {
        if (dao != null) {
            dao.toggleStarDeck(deckId)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map {
                if (it.id == deckId) it.copy(isStarred = !it.isStarred) else it
            }
        }
    }

    suspend fun updateDeckPracticeStats(deckId: String, accuracy: Float, lastPracticed: String) {
        if (dao != null) {
            dao.updateDeckPracticeStats(deckId, accuracy, lastPracticed)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map {
                if (it.id == deckId) it.copy(accuracy = accuracy, lastPracticed = lastPracticed) else it
            }
        }
    }

    suspend fun updateWordStatus(wordId: String, status: DictationWordStatus) {
        if (dao != null) {
            dao.updateWordStatus(wordId, status.name)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map { deck ->
                val updatedWords = deck.words.map { word ->
                    if (word.id == wordId) word.copy(status = status) else word
                }
                deck.copy(words = updatedWords)
            }
        }
    }

    suspend fun clearAll() {
        if (dao != null) {
            dao.clearAll()
        } else {
            inMemoryDecks.value = emptyList()
        }
    }

    suspend fun seedInitialData(decks: List<DictationDeck>) {
        if (dao != null) {
            val pairs = decks.map { DictationDeckWithWords.fromDomain(it) }
            dao.insertDecksWithWords(pairs)
        } else {
            inMemoryDecks.value = decks
        }
    }

    suspend fun getDeckCount(): Int {
        return dao?.getDeckCount() ?: inMemoryDecks.value.size
    }

    companion object {
        fun createInMemory(): DictationRepository = DictationRepository(null)
    }
}
