package com.example.data.ai

/**
 * Classifies user inputs into the appropriate conceptual generation mode:
 * MODE A: Explicit Q&A Extraction
 * MODE B: Source-Based Generation
 * MODE C: Topic-Based Generation
 * MODE D: Topic + Source + Instructions
 */
object FlashcardInputClassifier {

    fun classify(input: FlashcardGenerationInput): FlashcardGenerationMode {
        val hasSource = input.sourceText.isNotBlank()
        val hasTopic = input.topic.isNotBlank() || (input.deckName.isNotBlank() && input.deckName != "Mastery Deck")
        val hasInstructions = input.userInstructions.isNotBlank()

        if (hasSource && OfflineFlashcardParser.hasExplicitQaStructure(input.sourceText)) {
            return FlashcardGenerationMode.EXPLICIT_QA_EXTRACTION
        }

        if (hasTopic && hasSource && hasInstructions) {
            return FlashcardGenerationMode.TOPIC_SOURCE_INSTRUCTION
        }

        if (hasSource) {
            return FlashcardGenerationMode.SOURCE_BASED_GENERATION
        }

        return FlashcardGenerationMode.TOPIC_BASED_GENERATION
    }
}
