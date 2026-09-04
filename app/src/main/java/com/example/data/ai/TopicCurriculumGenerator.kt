package com.example.data.ai

/**
 * Deterministic, offline topic-based active recall curriculum generator.
 * Produces structured, diverse educational questions across multiple cognitive axes:
 * - Foundational mechanisms & processes
 * - Why & How causal relationships
 * - Comparisons & Distinctions
 * - Real-world applications & boundary conditions
 * - Key formulas / variables / metrics
 * - Common misconceptions & exam priorities
 *
 * NEVER mechanically repeats "What is [topic]?", "What is purpose of [topic]?"
 */
object TopicCurriculumGenerator {

    data class QuestionTemplate(
        val questionBuilder: (topic: String) -> String,
        val answerBuilder: (topic: String) -> String,
        val tags: List<String>
    )

    private val CURRICULUM_TEMPLATES = listOf(
        QuestionTemplate(
            questionBuilder = { topic -> "What foundational principle or mechanism governs $topic?" },
            answerBuilder = { topic -> "$topic is fundamentally governed by underlying chemical, physical, or logical laws determining state changes, energy/information transfer, and boundary constraints." },
            tags = listOf("#Principles", "#HighYield")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "How does $topic operate in practice, and what are its core steps?" },
            answerBuilder = { topic -> "The core progression involves initial inputs or activation triggers, intermediate state transformation or catalysis, and the resulting synthesized output or equilibrium." },
            tags = listOf("#Process", "#Mechanisms")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "Why is understanding $topic critical in its broader discipline?" },
            answerBuilder = { topic -> "It serves as a foundational building block for higher-order systems, allowing prediction of behaviors, troubleshooting failures, and optimization." },
            tags = listOf("#Conceptual", "#ActiveRecall")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What are the primary factors that regulate or influence the rate and efficiency of $topic?" },
            answerBuilder = { topic -> "Key regulators include concentration/availability of reactants, temperature or environmental conditions, catalytic accelerators, and feedback inhibition loops." },
            tags = listOf("#Regulation", "#Factors")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What is the key distinction between $topic and closely related concepts?" },
            answerBuilder = { topic -> "Focus on unique structural features, operational domains, directional flow, and the specific criteria that differentiate it from parallel phenomena." },
            tags = listOf("#Distinctions", "#ExamFocus")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What happens if a core component of $topic malfunctions or is omitted?" },
            answerBuilder = { topic -> "The systemic chain of reactions is disrupted, resulting in rate-limiting bottlenecks, toxic buildup, or failure to reach the target equilibrium state." },
            tags = listOf("#CauseEffect", "#Pathology")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What equations, quantitative relationships, or metrics are used to measure $topic?" },
            answerBuilder = { topic -> "Standard rate laws, yield formulas, logarithmic scales, and efficiency ratios quantify the operational dynamics of $topic." },
            tags = listOf("#Formulas", "#Quantitative")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What are the most common practical applications and real-world implementations of $topic?" },
            answerBuilder = { topic -> "Practical implementations span diagnostic tools, industrial synthesis, therapeutic protocols, and algorithmic engineering optimizations." },
            tags = listOf("#Application", "#Practical")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What is the relationship between the inputs and outputs of $topic?" },
            answerBuilder = { topic -> "Inputs provide the necessary raw precursors and energy; the process reorganizes these components into functional end-products while conserving mass/energy." },
            tags = listOf("#InputsOutputs", "#Concepts")
        ),
        QuestionTemplate(
            questionBuilder = { topic -> "What high-yield exam traps or common misconceptions are associated with $topic?" },
            answerBuilder = { topic -> "Students commonly confuse correlation with causation, miss negative feedback triggers, or overlook required cofactors and environmental parameters." },
            tags = listOf("#HighYield", "#MistakesToAvoid")
        )
    )

    fun generateCardsForTopic(topic: String, targetCount: Int): List<FlashcardCandidate> {
        val cleanTopic = topic.trim()
        val candidates = mutableListOf<FlashcardCandidate>()

        val totalAvailable = CURRICULUM_TEMPLATES.size
        for (i in 0 until targetCount) {
            val template = CURRICULUM_TEMPLATES[i % totalAvailable]
            candidates.add(
                FlashcardCandidate(
                    front = template.questionBuilder(cleanTopic),
                    back = template.answerBuilder(cleanTopic),
                    topic = cleanTopic,
                    tags = template.tags
                )
            )
        }

        return candidates
    }
}
