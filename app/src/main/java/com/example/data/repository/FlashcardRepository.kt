package com.example.data.repository

import com.example.data.local.dao.FlashcardDao
import com.example.data.local.entity.DeckWithCards
import com.example.data.local.entity.FlashcardEntity
import com.example.model.Flashcard
import com.example.model.FlashcardDeck
import com.example.model.MockDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FlashcardRepository(
    private val dao: FlashcardDao? = null
) {
    private val inMemoryDecks = MutableStateFlow(MockDataSource.decks)

    val allDecks: Flow<List<FlashcardDeck>> = if (dao != null) {
        dao.getAllDecksWithCards().map { list -> list.map { it.toDomain() } }
    } else {
        inMemoryDecks.asStateFlow()
    }

    suspend fun insertDeckWithCards(deck: FlashcardDeck) {
        if (dao != null) {
            val (deckEntity, cardEntities) = DeckWithCards.fromDomain(deck)
            dao.insertDeckWithCards(deckEntity, cardEntities)
        } else {
            inMemoryDecks.value = listOf(deck) + inMemoryDecks.value.filter { it.id != deck.id }
        }
    }

    suspend fun insertDecksWithCards(decks: List<FlashcardDeck>) {
        if (dao != null) {
            val pairs = decks.map { DeckWithCards.fromDomain(it) }
            dao.insertDecksWithCards(pairs)
        } else {
            val newIds = decks.map { it.id }.toSet()
            inMemoryDecks.value = decks + inMemoryDecks.value.filter { it.id !in newIds }
        }
    }

    suspend fun renameDeck(deckId: String, newTitle: String) {
        if (dao != null) {
            dao.renameDeck(deckId, newTitle)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map {
                if (it.id == deckId) it.copy(title = newTitle) else it
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

    suspend fun updateDeckProgress(deckId: String, progress: Float, lastReviewed: String) {
        if (dao != null) {
            dao.updateDeckProgress(deckId, progress, lastReviewed)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map {
                if (it.id == deckId) it.copy(progress = progress, lastReviewed = lastReviewed) else it
            }
        }
    }

    suspend fun toggleCardMastery(deckId: String, cardId: String, isMastered: Boolean) {
        if (dao != null) {
            dao.updateCardMastery(cardId, isMastered)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map { deck ->
                if (deck.id == deckId) {
                    val updatedCards = deck.cards.map { card ->
                        if (card.id == cardId) card.copy(isMastered = isMastered) else card
                    }
                    val masteredCount = updatedCards.count { it.isMastered }
                    val newProgress = if (updatedCards.isNotEmpty()) masteredCount.toFloat() / updatedCards.size else 0f
                    deck.copy(cards = updatedCards, progress = newProgress)
                } else deck
            }
        }
    }

    suspend fun addCardToDeck(deckId: String, card: Flashcard) {
        if (dao != null) {
            val cardEntity = FlashcardEntity(
                id = card.id,
                deckId = deckId,
                front = card.front,
                back = card.back,
                topic = card.topic,
                tagsCsv = card.tags.joinToString(","),
                isMastered = card.isMastered,
                orderIndex = 9999
            )
            dao.insertCard(cardEntity)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map { deck ->
                if (deck.id == deckId) {
                    val updatedCards = deck.cards + card
                    deck.copy(cards = updatedCards, cardCount = updatedCards.size)
                } else deck
            }
        }
    }

    suspend fun editCard(deckId: String, cardId: String, front: String, back: String) {
        if (dao != null) {
            dao.updateCardFrontBack(cardId, front, back)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map { deck ->
                if (deck.id == deckId) {
                    val updatedCards = deck.cards.map { card ->
                        if (card.id == cardId) card.copy(front = front, back = back) else card
                    }
                    deck.copy(cards = updatedCards)
                } else deck
            }
        }
    }

    suspend fun deleteCard(cardId: String) {
        if (dao != null) {
            dao.deleteCardById(cardId)
        } else {
            inMemoryDecks.value = inMemoryDecks.value.map { deck ->
                val updatedCards = deck.cards.filter { it.id != cardId }
                deck.copy(cards = updatedCards, cardCount = updatedCards.size)
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

    suspend fun seedInitialData(decks: List<FlashcardDeck>) {
        if (dao != null) {
            val pairs = decks.map { DeckWithCards.fromDomain(it) }
            dao.insertDecksWithCards(pairs)
        } else {
            inMemoryDecks.value = decks
        }
    }

    suspend fun getDeckCount(): Int {
        return dao?.getDeckCount() ?: inMemoryDecks.value.size
    }

    companion object {
        fun createInMemory(): FlashcardRepository = FlashcardRepository(null)
    }
}
