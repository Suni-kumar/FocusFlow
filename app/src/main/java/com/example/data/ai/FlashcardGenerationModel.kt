package com.example.data.ai

/**
 * Represents the distinct inputs provided to the Flashcard Generator.
 */
data class FlashcardGenerationInput(
    val deckName: String = "",
    val topic: String = "",
    val sourceText: String = "",
    val userInstructions: String = "",
    val targetCardCount: Int = 15
) {
    val effectiveTopic: String
        get() = when {
            topic.isNotBlank() -> topic.trim()
            deckName.isNotBlank() && !deckName.equals("Mastery Deck", ignoreCase = true) -> deckName.trim()
            else -> ""
        }
}

/**
 * Conceptual generation modes determined by semantic inspection of user input.
 */
enum class FlashcardGenerationMode {
    /** Explicit Q&A pairs detected (Front/Back, Q/A, Question/Answer). Highest priority. */
    EXPLICIT_QA_EXTRACTION,

    /** Normal study text/notes supplied without explicit Q&A structure. */
    SOURCE_BASED_GENERATION,

    /** Only topic supplied without source text. */
    TOPIC_BASED_GENERATION,

    /** Topic + Source text + User prompt instructions all provided for fine control. */
    TOPIC_SOURCE_INSTRUCTION
}

/**
 * Raw candidate card before final validation and duplicate pruning.
 */
data class FlashcardCandidate(
    val front: String,
    val back: String,
    val topic: String = "",
    val tags: List<String> = emptyList(),
    val isExplicitExtracted: Boolean = false
)
