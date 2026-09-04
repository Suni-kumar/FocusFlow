package com.example.data.ai

import org.junit.Assert.*
import org.junit.Test

class FlashcardGenerationTest {

    @Test
    fun testCase1_TopicOnly() {
        // Topic only: "Mitochondria"
        val input = FlashcardGenerationInput(
            topic = "Mitochondria",
            targetCardCount = 5
        )
        val mode = FlashcardInputClassifier.classify(input)
        assertEquals(FlashcardGenerationMode.TOPIC_BASED_GENERATION, mode)

        val cards = OfflineFlashcardService.generate(input)
        assertTrue("Should generate cards for topic", cards.size >= 3)

        for (card in cards) {
            // Must not have mechanical repeating prefix
            assertFalse(card.front.equals("What is Mitochondria?", ignoreCase = true))
            assertTrue(card.front.contains("Mitochondria", ignoreCase = true))
            assertTrue(card.back.isNotBlank())
            // Labels must not be content
            assertFalse(card.front.startsWith("Front:", ignoreCase = true))
            assertFalse(card.back.startsWith("Back:", ignoreCase = true))
        }
    }

    @Test
    fun testCase2_ExplicitFrontBack() {
        val rawSource = """
            Front: What is the powerhouse of the cell?
            Back: Mitochondria
            
            Front: What is the function of ribosomes?
            Back: Protein synthesis
        """.trimIndent()

        val input = FlashcardGenerationInput(
            sourceText = rawSource,
            targetCardCount = 10
        )
        val mode = FlashcardInputClassifier.classify(input)
        assertEquals(FlashcardGenerationMode.EXPLICIT_QA_EXTRACTION, mode)

        val cards = OfflineFlashcardService.generate(input)
        assertEquals(2, cards.size)

        assertEquals("What is the powerhouse of the cell?", cards[0].front)
        assertEquals("Mitochondria", cards[0].back)

        assertEquals("What is the function of ribosomes?", cards[1].front)
        assertEquals("Protein synthesis", cards[1].back)
    }

    @Test
    fun testCase3_ExplicitQuestionAnswer() {
        val rawSource = """
            Q: What is the capital of France?
            A: Paris
            
            Question: What is photosynthesis?
            Answer: The process by which plants use sunlight to synthesize nutrients from CO2 and water.
        """.trimIndent()

        val input = FlashcardGenerationInput(
            sourceText = rawSource,
            targetCardCount = 10
        )
        val mode = FlashcardInputClassifier.classify(input)
        assertEquals(FlashcardGenerationMode.EXPLICIT_QA_EXTRACTION, mode)

        val cards = OfflineFlashcardService.generate(input)
        assertEquals(2, cards.size)

        assertEquals("What is the capital of France?", cards[0].front)
        assertEquals("Paris", cards[0].back)

        assertEquals("What is photosynthesis?", cards[1].front)
        assertTrue(cards[1].back.contains("The process by which plants use sunlight"))
    }

    @Test
    fun testCase4_MultiLineAnswers() {
        val rawSource = """
            Front: What are the primary stages of cellular respiration?
            Back: 1. Glycolysis in cytoplasm
            2. Krebs cycle in mitochondrial matrix
            3. Electron transport chain across inner membrane
            
            Front: Define homeostasis.
            Back: The state of steady internal physical and chemical conditions
            maintained by living systems despite external changes.
        """.trimIndent()

        val input = FlashcardGenerationInput(
            sourceText = rawSource,
            targetCardCount = 10
        )
        val cards = OfflineFlashcardService.generate(input)
        assertEquals(2, cards.size)

        assertTrue(cards[0].back.contains("Glycolysis in cytoplasm"))
        assertTrue(cards[0].back.contains("Krebs cycle in mitochondrial matrix"))
        assertTrue(cards[0].back.contains("Electron transport chain"))

        assertTrue(cards[1].back.contains("steady internal physical and chemical conditions"))
    }

    @Test
    fun testCase5_TopicSourceInstructions() {
        val input = FlashcardGenerationInput(
            deckName = "Cellular Biology",
            topic = "Cellular Respiration",
            sourceText = "Glycolysis breaks glucose down into pyruvate producing a net of 2 ATP and 2 NADH.",
            userInstructions = "Focus on ATP yields and substrate level phosphorylation.",
            targetCardCount = 5
        )
        val mode = FlashcardInputClassifier.classify(input)
        assertEquals(FlashcardGenerationMode.TOPIC_SOURCE_INSTRUCTION, mode)

        val cards = OfflineFlashcardService.generate(input)
        assertTrue(cards.isNotEmpty())
        for (card in cards) {
            // Deck name should not be asked about
            assertFalse(card.front.contains("Cellular Biology", ignoreCase = true))
            // Instructions should not be asked about
            assertFalse(card.front.contains("User Instructions", ignoreCase = true))
        }
    }

    @Test
    fun testCase6_StructuralLabelsNeverTreatedAsEducationalContent() {
        val rawSource = """
            Front: What is action potential?
            Back: A rapid sequence of changes in the voltage across a membrane.
            
            Front: Front
            Back: Back
            
            Question: Answer
            Answer: Question
        """.trimIndent()

        val input = FlashcardGenerationInput(
            sourceText = rawSource,
            targetCardCount = 10
        )
        val cards = OfflineFlashcardService.generate(input)

        // Only the genuine educational card should pass validation; "Front/Back" and "Question/Answer" must be discarded!
        assertEquals(1, cards.size)
        assertEquals("What is action potential?", cards[0].front)
        assertEquals("A rapid sequence of changes in the voltage across a membrane.", cards[0].back)
    }
}
