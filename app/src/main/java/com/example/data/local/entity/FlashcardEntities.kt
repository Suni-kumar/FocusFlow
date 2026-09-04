package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.backup.BackupManager
import com.example.model.Flashcard
import com.example.model.FlashcardDeck

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val cardCount: Int,
    val lastReviewed: String,
    val progress: Float,
    val iconName: String,
    val categoryColorHex: String,
    val tagsCsv: String,
    val isAiGenerated: Boolean,
    val isStarred: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val topic: String,
    val tagsCsv: String,
    val isMastered: Boolean,
    val orderIndex: Int
)

data class DeckWithCards(
    @Embedded val deck: FlashcardDeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId"
    )
    val cards: List<FlashcardEntity>
) {
    fun toDomain(): FlashcardDeck {
        val sortedCards = cards.sortedBy { it.orderIndex }.map { card ->
            Flashcard(
                id = card.id,
                front = card.front,
                back = card.back,
                topic = card.topic,
                tags = if (card.tagsCsv.isBlank()) emptyList() else card.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                isMastered = card.isMastered
            )
        }
        return FlashcardDeck(
            id = deck.id,
            title = deck.title,
            description = deck.description,
            cardCount = sortedCards.size,
            lastReviewed = deck.lastReviewed,
            progress = deck.progress,
            iconName = deck.iconName,
            categoryColor = BackupManager.hexToColor(deck.categoryColorHex),
            cards = sortedCards,
            tags = if (deck.tagsCsv.isBlank()) emptyList() else deck.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            isAiGenerated = deck.isAiGenerated,
            isStarred = deck.isStarred
        )
    }

    companion object {
        fun fromDomain(deck: FlashcardDeck): Pair<FlashcardDeckEntity, List<FlashcardEntity>> {
            val deckEntity = FlashcardDeckEntity(
                id = deck.id,
                title = deck.title,
                description = deck.description,
                cardCount = deck.cards.size,
                lastReviewed = deck.lastReviewed,
                progress = deck.progress,
                iconName = deck.iconName,
                categoryColorHex = BackupManager.colorToHex(deck.categoryColor),
                tagsCsv = deck.tags.joinToString(","),
                isAiGenerated = deck.isAiGenerated,
                isStarred = deck.isStarred
            )
            val cardEntities = deck.cards.mapIndexed { index, card ->
                FlashcardEntity(
                    id = card.id,
                    deckId = deck.id,
                    front = card.front,
                    back = card.back,
                    topic = card.topic,
                    tagsCsv = card.tags.joinToString(","),
                    isMastered = card.isMastered,
                    orderIndex = index
                )
            }
            return Pair(deckEntity, cardEntities)
        }
    }
}
