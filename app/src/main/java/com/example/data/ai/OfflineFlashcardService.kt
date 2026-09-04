package com.example.data.ai

import com.example.model.Flashcard

/**
 * High-performance, offline deterministic flashcard generator.
 * Respects source primacy, extracts explicit Q&A directly without rewrites,
 * synthesizes concept flashcards from study notes, or constructs multi-dimensional
 * active recall curricula for topic-only inputs.
 */
object OfflineFlashcardService {

    fun generate(input: FlashcardGenerationInput): List<Flashcard> {
        val mode = FlashcardInputClassifier.classify(input)
        val fallbackTopic = input.effectiveTopic.ifBlank { "Study Deck" }

        val candidates = when (mode) {
            FlashcardGenerationMode.EXPLICIT_QA_EXTRACTION -> {
                OfflineFlashcardParser.extractExplicitQa(input.sourceText, fallbackTopic)
            }
            FlashcardGenerationMode.SOURCE_BASED_GENERATION,
            FlashcardGenerationMode.TOPIC_SOURCE_INSTRUCTION -> {
                val extracted = OfflineFlashcardParser.extractFromProse(
                    sourceText = input.sourceText,
                    topic = fallbackTopic,
                    targetCount = input.targetCardCount
                )
                if (extracted.size >= input.targetCardCount / 2 || fallbackTopic == "Study Deck") {
                    extracted
                } else {
                    // Complement with topic curriculum if prose is short
                    extracted + TopicCurriculumGenerator.generateCardsForTopic(
                        topic = fallbackTopic,
                        targetCount = (input.targetCardCount - extracted.size).coerceAtLeast(1)
                    )
                }
            }
            FlashcardGenerationMode.TOPIC_BASED_GENERATION -> {
                TopicCurriculumGenerator.generateCardsForTopic(
                    topic = fallbackTopic,
                    targetCount = input.targetCardCount
                )
            }
        }

        return FlashcardValidator.filterAndDeduplicate(
            candidates = candidates,
            fallbackTopic = fallbackTopic,
            targetCount = if (mode == FlashcardGenerationMode.EXPLICIT_QA_EXTRACTION) null else input.targetCardCount
        )
    }
}
