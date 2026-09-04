package com.example.data.ai

/**
 * Robust Source Text Parser for offline and explicit Q&A extraction.
 * Handles:
 * - Front: / Back:
 * - Question: / Answer:
 * - Q: / A: (or Q) / A) or Q. / A.)
 * - Front - / Back - or Front — / Back —
 * - Numbered Q&A (1. Question ... Answer ...)
 * - Multi-line answers and questions
 * - Structural label filtering
 * - Sentence-level definition extraction for non-Q&A prose
 */
object OfflineFlashcardParser {

    private val FRONT_PREFIX_REGEX = Regex(
        "(?i)^\\s*(?:(?:\\d+[.)]\\s*)?(?:front|question|prompt|term|q\\b|q\\)|q\\.)\\s*[:\\-—–]\\s*|(?:\\d+[.)]\\s+question\\s*[:\\-—–]?\\s*))",
        RegexOption.IGNORE_CASE
    )

    private val BACK_PREFIX_REGEX = Regex(
        "(?i)^\\s*(?:(?:back|answer|definition|explanation|a\\b|a\\)|a\\.)\\s*[:\\-—–]\\s*|(?:answer\\s*[:\\-—–]?\\s*))",
        RegexOption.IGNORE_CASE
    )

    /**
     * Determines whether the raw text contains explicit Q&A markers.
     */
    fun hasExplicitQaStructure(text: String): Boolean {
        var frontCount = 0
        var backCount = 0
        for (line in text.lineSequence()) {
            val trimmed = line.trim()
            if (FRONT_PREFIX_REGEX.containsMatchIn(trimmed)) frontCount++
            if (BACK_PREFIX_REGEX.containsMatchIn(trimmed)) backCount++
            if (frontCount >= 1 && backCount >= 1) return true
        }
        return false
    }

    /**
     * Extracts explicit Q&A pairs from text, supporting multi-line questions and multi-line answers.
     */
    fun extractExplicitQa(text: String, topic: String = ""): List<FlashcardCandidate> {
        val lines = text.lines()
        val candidates = mutableListOf<FlashcardCandidate>()

        var currentFrontBuilder = StringBuilder()
        var currentBackBuilder = StringBuilder()
        var parsingState = ParsingState.NONE

        fun commitCurrentCard() {
            val front = currentFrontBuilder.toString().trim()
            val back = currentBackBuilder.toString().trim()
            if (front.isNotBlank() && back.isNotBlank()) {
                candidates.add(
                    FlashcardCandidate(
                        front = cleanText(front),
                        back = cleanText(back),
                        topic = topic,
                        tags = listOf("#Extracted", "#ActiveRecall"),
                        isExplicitExtracted = true
                    )
                )
            }
            currentFrontBuilder.clear()
            currentBackBuilder.clear()
            parsingState = ParsingState.NONE
        }

        for (rawLine in lines) {
            val line = rawLine.trimEnd()
            val trimmedLine = line.trim()

            // Check if this line starts a new FRONT / Question
            if (FRONT_PREFIX_REGEX.containsMatchIn(trimmedLine)) {
                if (parsingState == ParsingState.IN_BACK || parsingState == ParsingState.IN_FRONT) {
                    commitCurrentCard()
                }
                val content = trimmedLine.replace(FRONT_PREFIX_REGEX, "").trim()
                currentFrontBuilder.append(content)
                parsingState = ParsingState.IN_FRONT
                continue
            }

            // Check if this line starts a BACK / Answer
            if (BACK_PREFIX_REGEX.containsMatchIn(trimmedLine)) {
                if (parsingState == ParsingState.IN_FRONT) {
                    val content = trimmedLine.replace(BACK_PREFIX_REGEX, "").trim()
                    currentBackBuilder.append(content)
                    parsingState = ParsingState.IN_BACK
                } else if (parsingState == ParsingState.IN_BACK) {
                    // Double back prefix without front? Commit and start fresh
                    commitCurrentCard()
                }
                continue
            }

            // Otherwise, line is a continuation line or formatting
            when (parsingState) {
                ParsingState.IN_FRONT -> {
                    if (trimmedLine.isNotBlank()) {
                        if (currentFrontBuilder.isNotEmpty()) currentFrontBuilder.append("\n")
                        currentFrontBuilder.append(trimmedLine)
                    }
                }
                ParsingState.IN_BACK -> {
                    // Blank lines within answers are preserved as paragraph breaks
                    if (trimmedLine.isEmpty()) {
                        if (currentBackBuilder.isNotEmpty()) currentBackBuilder.append("\n")
                    } else {
                        if (currentBackBuilder.isNotEmpty() && !currentBackBuilder.endsWith("\n")) {
                            currentBackBuilder.append("\n")
                        }
                        currentBackBuilder.append(trimmedLine)
                    }
                }
                ParsingState.NONE -> {
                    // Check for single-line delimiter formats like "Q: What is X? A: Y"
                    val inlineCandidate = parseInlineQa(trimmedLine, topic)
                    if (inlineCandidate != null) {
                        candidates.add(inlineCandidate)
                    }
                }
            }
        }

        // Commit final card at EOF
        commitCurrentCard()

        return candidates
    }

    private fun parseInlineQa(line: String, topic: String): FlashcardCandidate? {
        val inlineMatch = Regex("(?i)^(?:q|front|question)\\s*[:\\-—–]\\s*(.+?)\\s+(?:a|back|answer)\\s*[:\\-—–]\\s*(.+)$")
            .find(line)
        if (inlineMatch != null && inlineMatch.groupValues.size >= 3) {
            val q = inlineMatch.groupValues[1].trim()
            val a = inlineMatch.groupValues[2].trim()
            if (q.length >= 3 && a.length >= 2) {
                return FlashcardCandidate(
                    front = cleanText(q),
                    back = cleanText(a),
                    topic = topic,
                    tags = listOf("#Extracted", "#ActiveRecall"),
                    isExplicitExtracted = true
                )
            }
        }
        return null
    }

    /**
     * Extracts educational concepts, definitions, and relationships from unstructured prose/notes.
     */
    fun extractFromProse(sourceText: String, topic: String, targetCount: Int): List<FlashcardCandidate> {
        val candidates = mutableListOf<FlashcardCandidate>()

        // Split text into meaningful blocks (paragraphs or major bullet points)
        val blocks = sourceText.split(Regex("\n{2,}|\n(?=[•\\-*\\d+.]\\s+)"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Definition patterns: "X is defined as Y", "X refers to Y", "X: Y", "X is the process by which..."
        val definitionRegex = Regex(
            "(?i)^([A-Z0-9][a-zA-Z0-9\\s\\-()]{2,40})\\s*(?::|—|–|-|\\s+is defined as\\s+|\\s+refers to\\s+|\\s+is the\\s+|\\s+is a\\s+|\\s+are the\\s+)\\s*(.+)$"
        )

        for (block in blocks) {
            if (candidates.size >= targetCount) break

            val singleLine = block.replace(Regex("\\s+"), " ").trim()
            if (singleLine.length < 15) continue

            val match = definitionRegex.find(singleLine)
            if (match != null && match.groupValues.size >= 3) {
                val subject = match.groupValues[1].trim().trimEnd(':', '-', '—', '–')
                val definition = match.groupValues[2].trim()

                // Ensure subject is not structural metadata
                if (subject.lowercase() !in listOf("front", "back", "question", "answer", "note", "tip", "summary", "topic")) {
                    candidates.add(
                        FlashcardCandidate(
                            front = "What is $subject?",
                            back = cleanText(definition.replaceFirstChar { it.uppercase() }),
                            topic = topic,
                            tags = listOf("#Definitions", "#KeyConcepts")
                        )
                    )
                }
            }
        }

        // If prose has bullet points or sentences that denote cause-effect, process, or components
        if (candidates.size < targetCount) {
            val sentences = sourceText.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }
                .filter { it.length > 25 }

            for (sentence in sentences) {
                if (candidates.size >= targetCount) break

                // Check for "because", "causes", "leads to", "produces"
                val causeMatch = Regex("(?i)^(.+?)\\s+(?:causes|leads to|results in|produces)\\s+(.+)$").find(sentence)
                if (causeMatch != null) {
                    val cause = causeMatch.groupValues[1].trim()
                    val effect = causeMatch.groupValues[2].trim()
                    candidates.add(
                        FlashcardCandidate(
                            front = "What does $cause result in?",
                            back = cleanText(effect.replaceFirstChar { it.uppercase() }),
                            topic = topic,
                            tags = listOf("#CauseEffect", "#Concepts")
                        )
                    )
                    continue
                }

                // Check for "functions to", "is used to", "responsible for"
                val functionMatch = Regex("(?i)^(.+?)\\s+(?:is responsible for|functions to|is used for|serves to)\\s+(.+)$").find(sentence)
                if (functionMatch != null) {
                    val entity = functionMatch.groupValues[1].trim()
                    val role = functionMatch.groupValues[2].trim()
                    candidates.add(
                        FlashcardCandidate(
                            front = "What is the primary function of $entity?",
                            back = cleanText(role.replaceFirstChar { it.uppercase() }),
                            topic = topic,
                            tags = listOf("#Function", "#HighYield")
                        )
                    )
                }
            }
        }

        return candidates
    }

    private fun cleanText(text: String): String {
        return text.trim()
            .replace(Regex("^[•\\-*\\d+.)\\s]+"), "") // remove leading list bullets/numbering
            .trim()
    }

    private enum class ParsingState {
        NONE,
        IN_FRONT,
        IN_BACK
    }
}
