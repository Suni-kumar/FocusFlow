package com.example.data.ai

import com.example.model.Flashcard

/**
 * Quality validation and duplicate prevention layer for Flashcards.
 * Enforces strict educational content standards, discards metadata artifacts,
 * and eliminates duplicate/near-identical items.
 */
object FlashcardValidator {

    private val STRUCTURAL_METADATA_LABELS = setOf(
        "front", "back", "question", "answer", "q", "a", "topic",
        "source text", "source", "instruction", "instructions", "notes",
        "deck name", "title", "flashcard", "cards", "q&a", "q/a"
    )

    private val REJECTED_MECHANICAL_QUESTIONS = listOf(
        Regex("(?i)^what is\\s+(front|back|question|answer|topic|source|deck name|instruction)\\b.*"),
        Regex("(?i)^what is the purpose of\\s+(front|back|question|answer|topic|source|deck name)\\b.*"),
        Regex("(?i)^what is\\s+(front|back|question|answer|topic)\\s+used for\\b.*"),
        Regex("(?i)^explain the primary advantages and constraints of (study topic|mastery deck|topic)\\b.*"),
        Regex("(?i)^what key mechanisms drive (study topic|mastery deck|topic)\\b.*")
    )

    /**
     * Validates a candidate flashcard.
     * Returns true if valid, false if it represents metadata, empty values, or trivial artifacts.
     */
    fun isValid(candidate: FlashcardCandidate): Boolean {
        val f = candidate.front.trim()
        val b = candidate.back.trim()

        // 1. Check basic non-empty constraints
        if (f.length < 3 || b.length < 2) return false

        // 2. Reject if front or back is merely a structural metadata label
        val normFront = f.lowercase().replace(Regex("[^a-z0-9]"), "")
        val normBack = b.lowercase().replace(Regex("[^a-z0-9]"), "")

        if (normFront in STRUCTURAL_METADATA_LABELS || normBack in STRUCTURAL_METADATA_LABELS) {
            return false
        }

        // 3. Reject if front and back are essentially identical
        if (normFront == normBack) {
            return false
        }

        // 4. Reject mechanical questions about structural field names
        for (pattern in REJECTED_MECHANICAL_QUESTIONS) {
            if (pattern.matches(f)) {
                return false
            }
        }

        // 5. Reject single-word trivial questions like "Front?" or "Topic:"
        if (f.endsWith("?") && f.dropLast(1).trim().lowercase() in STRUCTURAL_METADATA_LABELS) {
            return false
        }

        // 6. Ensure answer has informative substance (not just "Yes", "No", "N/A", "Answer:")
        val trivialAnswers = setOf("yes", "no", "na", "n/a", "none", "answer", "back", "true", "false")
        if (normBack in trivialAnswers && !candidate.isExplicitExtracted) {
            return false
        }

        return true
    }

    /**
     * Normalizes a question string for duplicate detection.
     * Strips punctuation, non-alphanumeric chars, leading filler ("what is", "explain"), and whitespace.
     */
    fun normalizeKey(text: String): String {
        var clean = text.lowercase().trim()
        // Strip common leading question prefixes for fuzzy deduplication
        clean = clean.replace(Regex("^(what is|what are|explain|describe|define|how does|why is|what)\\s+"), "")
        return clean.replace(Regex("[^a-z0-9]"), "")
    }

    /**
     * Filters, deduplicates, and converts valid candidates into production Flashcard entities.
     */
    fun filterAndDeduplicate(
        candidates: List<FlashcardCandidate>,
        fallbackTopic: String,
        targetCount: Int? = null
    ): List<Flashcard> {
        val validCards = mutableListOf<Flashcard>()
        val seenNormalizedKeys = mutableSetOf<String>()
        val seenAnswers = mutableSetOf<String>()

        for ((idx, candidate) in candidates.withIndex()) {
            if (!isValid(candidate)) continue

            val normQ = normalizeKey(candidate.front)
            val normA = candidate.back.lowercase().replace(Regex("[^a-z0-9]"), "")

            // Deduplicate by question essence
            if (normQ.isNotBlank() && normQ in seenNormalizedKeys) {
                continue
            }
            // Also deduplicate identical answers if questions are virtually the same length
            if (normA.length > 8 && normA in seenAnswers && !candidate.isExplicitExtracted) {
                continue
            }

            if (normQ.isNotBlank()) seenNormalizedKeys.add(normQ)
            if (normA.isNotBlank()) seenAnswers.add(normA)

            val assignedTopic = if (candidate.topic.isNotBlank()) candidate.topic else fallbackTopic
            val safeTags = if (candidate.tags.isNotEmpty()) candidate.tags else listOf("#HighYield", "#ActiveRecall")

            validCards.add(
                Flashcard(
                    id = "c_${System.currentTimeMillis()}_${idx}_${validCards.size}",
                    front = candidate.front.trim(),
                    back = candidate.back.trim(),
                    topic = assignedTopic.take(50),
                    tags = safeTags
                )
            )

            if (targetCount != null && validCards.size >= targetCount) {
                break
            }
        }

        return validCards
    }
}
