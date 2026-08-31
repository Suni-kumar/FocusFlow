package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class DictationWordStatus {
    UNTESTED,
    CORRECT,
    INCORRECT,
    NEEDS_PRACTICE
}

data class DictationWord(
    val id: String = UUID.randomUUID().toString(),
    val word: String,
    val meaning: String,
    val phonetic: String = "",
    val exampleSentence: String = "",
    val status: DictationWordStatus = DictationWordStatus.UNTESTED,
    val userNotes: String = ""
)

data class DictationDeck(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val categoryColor: Color = Color(0xFF4EDEA3),
    val iconName: String = "RecordVoiceOver",
    val words: List<DictationWord> = emptyList(),
    val tags: List<String> = emptyList(),
    val isAiGenerated: Boolean = false,
    val isStarred: Boolean = false,
    val lastPracticed: String = "Not practiced yet",
    val accuracy: Float = 0f,
    val wordCount: Int = words.size
)

object MockDictationData {
    val starterDecks: List<DictationDeck> = listOf(
        DictationDeck(
            id = "dict_deck_1",
            title = "Chapter 1: High-Yield Academic Vocabulary",
            description = "Essential high-frequency academic words with precise meanings and usage context.",
            categoryColor = Color(0xFF4EDEA3),
            iconName = "RecordVoiceOver",
            isStarred = true,
            lastPracticed = "2 hours ago",
            accuracy = 0.85f,
            tags = listOf("Academic", "GRE", "Chapter 1"),
            words = listOf(
                DictationWord(
                    id = "dw_1_1",
                    word = "Ephemeral",
                    meaning = "Lasting for a very short time; fleeting or transient.",
                    phonetic = "/ɪˈfem.ər.əl/",
                    exampleSentence = "Fashions are ephemeral, but true style remains timeless."
                ),
                DictationWord(
                    id = "dw_1_2",
                    word = "Ubiquitous",
                    meaning = "Present, appearing, or found everywhere simultaneously.",
                    phonetic = "/juːˈbɪk.wɪ.təs/",
                    exampleSentence = "Smartphones have become ubiquitous in modern society."
                ),
                DictationWord(
                    id = "dw_1_3",
                    word = "Meticulous",
                    meaning = "Showing great attention to detail; very careful and precise.",
                    phonetic = "/məˈtɪk.jə.ləs/",
                    exampleSentence = "The researcher kept meticulous records of every experiment."
                ),
                DictationWord(
                    id = "dw_1_4",
                    word = "Resilience",
                    meaning = "The capacity to recover quickly from difficulties or toughness.",
                    phonetic = "/rɪˈzɪl.jəns/",
                    exampleSentence = "Her mental resilience helped her overcome every obstacle."
                ),
                DictationWord(
                    id = "dw_1_5",
                    word = "Eloquent",
                    meaning = "Fluent or persuasive in speaking or writing.",
                    phonetic = "/ˈel.ə.kwənt/",
                    exampleSentence = "He gave an eloquent speech that moved the entire audience."
                ),
                DictationWord(
                    id = "dw_1_6",
                    word = "Pragmatic",
                    meaning = "Dealing with things sensibly and realistically based on practical considerations.",
                    phonetic = "/præɡˈmæt.ɪk/",
                    exampleSentence = "We need a pragmatic solution rather than an idealistic theory."
                ),
                DictationWord(
                    id = "dw_1_7",
                    word = "Tenacious",
                    meaning = "Tending to keep a firm hold of something; persistent and determined.",
                    phonetic = "/təˈneɪ.ʃəs/",
                    exampleSentence = "The defense attorney was tenacious in pursuing the truth."
                ),
                DictationWord(
                    id = "dw_1_8",
                    word = "Serendipity",
                    meaning = "The occurrence of valuable discoveries by happy chance or luck.",
                    phonetic = "/ˌser.ənˈdɪp.ə.ti/",
                    exampleSentence = "Meeting my business mentor on the train was pure serendipity."
                )
            )
        ),
        DictationDeck(
            id = "dict_deck_2",
            title = "Chapter 2: Science & Tech Terminology",
            description = "Scientific terms, concepts and spelling challenges for STEM revisions.",
            categoryColor = Color(0xFF818CF8),
            iconName = "Psychology",
            isStarred = false,
            lastPracticed = "Yesterday",
            accuracy = 0.90f,
            tags = listOf("STEM", "Science", "Chapter 2"),
            words = listOf(
                DictationWord(
                    id = "dw_2_1",
                    word = "Photosynthesis",
                    meaning = "The process by which green plants synthesize nutrients using sunlight.",
                    phonetic = "/ˌfoʊ.t̬oʊˈsɪn.θə.sɪs/",
                    exampleSentence = "Chlorophyll plays a vital role in photosynthesis."
                ),
                DictationWord(
                    id = "dw_2_2",
                    word = "Equilibrium",
                    meaning = "A state in which opposing forces or influences are balanced.",
                    phonetic = "/ˌiː.kwəˈlɪb.ri.əm/",
                    exampleSentence = "The chemical reaction eventually reached dynamic equilibrium."
                ),
                DictationWord(
                    id = "dw_2_3",
                    word = "Algorithm",
                    meaning = "A step-by-step procedure or formula for solving a problem.",
                    phonetic = "/ˈæl.ɡə.rɪð.əm/",
                    exampleSentence = "The search algorithm sorts millions of queries in milliseconds."
                ),
                DictationWord(
                    id = "dw_2_4",
                    word = "Hypothesis",
                    meaning = "A proposed explanation made on the basis of limited evidence as a starting point.",
                    phonetic = "/haɪˈpɑː.θə.sɪs/",
                    exampleSentence = "The scientists designed an experiment to test the hypothesis."
                ),
                DictationWord(
                    id = "dw_2_5",
                    word = "Superconductivity",
                    meaning = "The property of zero electrical resistance occurring in certain materials.",
                    phonetic = "/ˌsuː.pɚˌkɑːn.dʌkˈtɪv.ə.t̬i/",
                    exampleSentence = "Superconductivity requires cooling materials to extremely low temperatures."
                )
            )
        ),
        DictationDeck(
            id = "dict_deck_3",
            title = "Chapter 3: Daily Conversational Idioms & Words",
            description = "Natural conversational vocabulary and expressions with Hindi-English meanings.",
            categoryColor = Color(0xFFFF7886),
            iconName = "Forum",
            isStarred = true,
            lastPracticed = "3 days ago",
            accuracy = 0.70f,
            tags = listOf("Conversational", "Idioms", "Daily"),
            words = listOf(
                DictationWord(
                    id = "dw_3_1",
                    word = "Spill the beans",
                    meaning = "To reveal secret information unintentionally or indiscreetly (रहस्य खोल देना).",
                    phonetic = "/spɪl ðə biːnz/",
                    exampleSentence = "Don't spill the beans about the surprise anniversary party!"
                ),
                DictationWord(
                    id = "dw_3_2",
                    word = "Bite the bullet",
                    meaning = "To endure a painful or difficult situation with courage (कठिन परिस्थिति का सामना करना).",
                    phonetic = "/baɪt ðə ˈbʊl.ɪt/",
                    exampleSentence = "I decided to bite the bullet and complete the tough assignment."
                ),
                DictationWord(
                    id = "dw_3_3",
                    word = "Candid",
                    meaning = "Truthful and straightforward; frank and sincere (स्पष्टवादी, निष्कपट).",
                    phonetic = "/ˈkæn.dɪd/",
                    exampleSentence = "She gave a candid assessment of the project's weaknesses."
                ),
                DictationWord(
                    id = "dw_3_4",
                    word = "Perseverance",
                    meaning = "Persistence in doing something despite difficulty or delay in achieving success (दृढ़ता, लगन).",
                    phonetic = "/ˌpɝː.səˈvɪr.əns/",
                    exampleSentence = "With steady perseverance, he cracked the civil services exam."
                )
            )
        )
    )

    fun getInitialDictationDecks(): List<DictationDeck> = starterDecks
}

typealias DictationMockDataSource = MockDictationData
