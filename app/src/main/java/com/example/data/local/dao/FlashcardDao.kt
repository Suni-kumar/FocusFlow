package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.DeckWithCards
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Transaction
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecksWithCards(): Flow<List<DeckWithCards>>

    @Transaction
    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId")
    suspend fun getDeckWithCardsById(deckId: String): DeckWithCards?

    @Query("SELECT COUNT(*) FROM flashcard_decks")
    suspend fun getDeckCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<FlashcardDeckEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Query("UPDATE flashcards SET front = :front, back = :back WHERE id = :cardId")
    suspend fun updateCardFrontBack(cardId: String, front: String, back: String)

    @Query("UPDATE flashcards SET isMastered = :isMastered WHERE id = :cardId")
    suspend fun updateCardMastery(cardId: String, isMastered: Boolean)

    @Query("UPDATE flashcard_decks SET progress = :progress, lastReviewed = :lastReviewed WHERE id = :deckId")
    suspend fun updateDeckProgress(deckId: String, progress: Float, lastReviewed: String)

    @Query("UPDATE flashcard_decks SET title = :title WHERE id = :deckId")
    suspend fun renameDeck(deckId: String, title: String)

    @Query("UPDATE flashcard_decks SET isStarred = NOT isStarred WHERE id = :deckId")
    suspend fun toggleStarDeck(deckId: String)

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: String)

    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: String)

    @Query("DELETE FROM flashcard_decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: String)

    @Query("DELETE FROM flashcard_decks WHERE id IN (:deckIds)")
    suspend fun deleteDecksByIds(deckIds: List<String>)

    @Transaction
    suspend fun insertDeckWithCards(deck: FlashcardDeckEntity, cards: List<FlashcardEntity>) {
        insertDeck(deck)
        deleteCardsForDeck(deck.id)
        insertCards(cards)
    }

    @Transaction
    suspend fun insertDecksWithCards(decksWithCards: List<Pair<FlashcardDeckEntity, List<FlashcardEntity>>>) {
        for ((deck, cards) in decksWithCards) {
            insertDeckWithCards(deck, cards)
        }
    }

    @Query("DELETE FROM flashcard_decks")
    suspend fun clearAll()
}
