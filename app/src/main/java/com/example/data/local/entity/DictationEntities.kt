package com.example.data.local.entity

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.backup.BackupManager
import com.example.model.DictationDeck
import com.example.model.DictationWord
import com.example.model.DictationWordStatus

@Entity(tableName = "dictation_decks")
data class DictationDeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val categoryColorHex: String,
    val iconName: String,
    val tagsCsv: String,
    val isAiGenerated: Boolean,
    val isStarred: Boolean,
    val lastPracticed: String,
    val accuracy: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "dictation_words",
    foreignKeys = [
        ForeignKey(
            entity = DictationDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class DictationWordEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val word: String,
    val meaning: String,
    val phonetic: String,
    val exampleSentence: String,
    val status: String,
    val userNotes: String,
    val orderIndex: Int
)

data class DictationDeckWithWords(
    @Embedded val deck: DictationDeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId"
    )
    val words: List<DictationWordEntity>
) {
    fun toDomain(): DictationDeck {
        val sortedWords = words.sortedBy { it.orderIndex }.map { wordEntity ->
            DictationWord(
                id = wordEntity.id,
                word = wordEntity.word,
                meaning = wordEntity.meaning,
                phonetic = wordEntity.phonetic,
                exampleSentence = wordEntity.exampleSentence,
                status = try {
                    DictationWordStatus.valueOf(wordEntity.status)
                } catch (e: Exception) {
                    DictationWordStatus.UNTESTED
                },
                userNotes = wordEntity.userNotes
            )
        }
        return DictationDeck(
            id = deck.id,
            title = deck.title,
            description = deck.description,
            categoryColor = BackupManager.hexToColor(deck.categoryColorHex, Color(0xFF4EDEA3)),
            iconName = deck.iconName,
            words = sortedWords,
            tags = if (deck.tagsCsv.isBlank()) emptyList() else deck.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            isAiGenerated = deck.isAiGenerated,
            isStarred = deck.isStarred,
            lastPracticed = deck.lastPracticed,
            accuracy = deck.accuracy,
            wordCount = sortedWords.size
        )
    }

    companion object {
        fun fromDomain(deck: DictationDeck): Pair<DictationDeckEntity, List<DictationWordEntity>> {
            val deckEntity = DictationDeckEntity(
                id = deck.id,
                title = deck.title,
                description = deck.description,
                categoryColorHex = BackupManager.colorToHex(deck.categoryColor),
                iconName = deck.iconName,
                tagsCsv = deck.tags.joinToString(","),
                isAiGenerated = deck.isAiGenerated,
                isStarred = deck.isStarred,
                lastPracticed = deck.lastPracticed,
                accuracy = deck.accuracy
            )
            val wordEntities = deck.words.mapIndexed { index, word ->
                DictationWordEntity(
                    id = word.id,
                    deckId = deck.id,
                    word = word.word,
                    meaning = word.meaning,
                    phonetic = word.phonetic,
                    exampleSentence = word.exampleSentence,
                    status = word.status.name,
                    userNotes = word.userNotes,
                    orderIndex = index
                )
            }
            return Pair(deckEntity, wordEntities)
        }
    }
}
