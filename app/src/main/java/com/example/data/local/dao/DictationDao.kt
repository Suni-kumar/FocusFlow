package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.DictationDeckEntity
import com.example.data.local.entity.DictationDeckWithWords
import com.example.data.local.entity.DictationWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictationDao {
    @Transaction
    @Query("SELECT * FROM dictation_decks ORDER BY createdAt DESC")
    fun getAllDecksWithWords(): Flow<List<DictationDeckWithWords>>

    @Transaction
    @Query("SELECT * FROM dictation_decks WHERE id = :deckId")
    suspend fun getDeckWithWordsById(deckId: String): DictationDeckWithWords?

    @Query("SELECT COUNT(*) FROM dictation_decks")
    suspend fun getDeckCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DictationDeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictationWordEntity>)

    @Update
    suspend fun updateWord(word: DictationWordEntity)

    @Query("UPDATE dictation_words SET status = :status WHERE id = :wordId")
    suspend fun updateWordStatus(wordId: String, status: String)

    @Query("UPDATE dictation_decks SET title = :title, description = :description WHERE id = :deckId")
    suspend fun updateDeckInfo(deckId: String, title: String, description: String)

    @Query("UPDATE dictation_decks SET isStarred = NOT isStarred WHERE id = :deckId")
    suspend fun toggleStarDeck(deckId: String)

    @Query("UPDATE dictation_decks SET accuracy = :accuracy, lastPracticed = :lastPracticed WHERE id = :deckId")
    suspend fun updateDeckPracticeStats(deckId: String, accuracy: Float, lastPracticed: String)

    @Query("DELETE FROM dictation_words WHERE deckId = :deckId")
    suspend fun deleteWordsForDeck(deckId: String)

    @Query("DELETE FROM dictation_decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: String)

    @Query("DELETE FROM dictation_decks WHERE id IN (:deckIds)")
    suspend fun deleteDecksByIds(deckIds: List<String>)

    @Transaction
    suspend fun insertDeckWithWords(deck: DictationDeckEntity, words: List<DictationWordEntity>) {
        insertDeck(deck)
        deleteWordsForDeck(deck.id)
        insertWords(words)
    }

    @Transaction
    suspend fun insertDecksWithWords(decksWithWords: List<Pair<DictationDeckEntity, List<DictationWordEntity>>>) {
        for ((deck, words) in decksWithWords) {
            insertDeckWithWords(deck, words)
        }
    }

    @Query("DELETE FROM dictation_decks")
    suspend fun clearAll()
}
