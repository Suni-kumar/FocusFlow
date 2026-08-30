package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.backup.BackupManager
import com.example.model.Flashcard
import com.example.model.FlashcardDeck
import com.sepfol.app.ui.folder.FolderItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupManagerTest {

    @Test
    fun testFullBackupCreationAndParsing() {
        val testFiles = listOf(
            FolderItem(
                id = "doc1",
                name = "Neurobiology.md",
                isDirectory = false,
                extension = "md",
                mimeType = "text/markdown",
                contentData = "# Neuroplasticity\nNotes on synaptic plasticity and long-term potentiation.",
                sizeBytes = 256L,
                lastModified = System.currentTimeMillis()
            ),
            FolderItem(
                id = "folder1",
                name = "University",
                isDirectory = true,
                extension = "",
                mimeType = "vnd.android.document/directory",
                itemCount = 3
            )
        )

        val testDecks = listOf(
            FlashcardDeck(
                id = "deck_test_1",
                title = "Cognitive Science",
                description = "Fundamental principles of cognitive architecture",
                cardCount = 2,
                lastReviewed = "Just now",
                progress = 0.75f,
                iconName = "psychology",
                categoryColor = Color(0xFF8B5CF6),
                cards = listOf(
                    Flashcard("c1", "What is working memory?", "The system responsible for transient holding and processing.", "Cognitive Science"),
                    Flashcard("c2", "What is chunking?", "Grouping individual pieces of information into larger meaningful units.", "Cognitive Science")
                ),
                tags = listOf("neuro", "memory"),
                isAiGenerated = true
            )
        )

        val jsonBackup = BackupManager.createFullBackupJson(testFiles, testDecks)
        assertTrue(jsonBackup.isNotBlank())
        assertTrue(jsonBackup.contains("FocusFlow"))
        assertTrue(jsonBackup.contains("Cognitive Science"))
        assertTrue(jsonBackup.contains("Neurobiology.md"))

        val parseResult = BackupManager.validateAndParseBackup(jsonBackup)
        assertTrue(parseResult.isValid)
        assertNotNull(parseResult.payload)

        val payload = parseResult.payload!!
        assertEquals(2, payload.files.size)
        assertEquals(1, payload.decks.size)
        assertEquals(2, payload.decks[0].cards.size)
        assertEquals("Cognitive Science", payload.decks[0].title)
    }

    @Test
    fun testSingleDeckSerializationAndParsing() {
        val testDeck = FlashcardDeck(
            id = "deck_single_1",
            title = "Quantum Physics",
            description = "Wave functions and superposition",
            cardCount = 1,
            lastReviewed = "Yesterday",
            progress = 0.5f,
            iconName = "school",
            categoryColor = Color(0xFF38BDF8),
            cards = listOf(
                Flashcard("qc1", "What is wave-particle duality?", "Every quantum entity can be described as either a particle or a wave.", "Quantum")
            ),
            tags = listOf("physics", "quantum")
        )

        val singleJson = BackupManager.serializeSingleDeck(testDeck)
        val parseResult = BackupManager.validateAndParseBackup(singleJson)

        assertTrue(parseResult.isValid)
        assertNotNull(parseResult.payload)
        assertEquals(1, parseResult.payload!!.decks.size)
        assertEquals("Quantum Physics", parseResult.payload!!.decks[0].title)
        assertEquals(1, parseResult.payload!!.decks[0].cards.size)
    }
}
