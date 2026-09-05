package com.example.data.ai

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.BuildConfig
import com.example.model.DictationDeck
import com.example.model.DictationWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiDictationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiDictationService"
    }

    suspend fun generateOrParseDictationDeck(
        inputContent: String,
        userCustomApiKey: String = "",
        deckTitleOverride: String? = null,
        targetWordCount: Int = 10
    ): DictationDeck = withContext(Dispatchers.IO) {
        val trimmedInput = inputContent.trim()
        val apiKeyToUse = if (userCustomApiKey.isNotBlank() && userCustomApiKey != "MY_GEMINI_API_KEY") {
            userCustomApiKey.trim()
        } else {
            getBuildConfigApiKey()
        }

        val hasOnlineKey = apiKeyToUse.isNotBlank() && apiKeyToUse != "MY_GEMINI_API_KEY"

        if (hasOnlineKey) {
            try {
                val words = callGeminiForDictation(trimmedInput, apiKeyToUse, targetWordCount)
                if (words.isNotEmpty()) {
                    val resolvedTitle = deckTitleOverride?.takeIf { it.isNotBlank() }
                        ?: extractTitleFromTopicOrWords(trimmedInput, words)
                    return@withContext DictationDeck(
                        id = "dict_${UUID.randomUUID()}",
                        title = resolvedTitle,
                        description = "AI Curated Dictation Deck with ${words.size} high-yield words & contextual meanings.",
                        categoryColor = getRandomCategoryColor(),
                        iconName = "AutoAwesome",
                        words = words,
                        tags = listOf("AI Generated", "Dictation", "Chapter"),
                        isAiGenerated = true,
                        isStarred = false
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Dictation online generation failed, falling back to rich offline semantic engine", e)
            }
        }

        // Deep offline semantic extraction & morpho-semantic dictionary engine
        val parsedWords = fallbackOfflineSmartParser(trimmedInput, targetWordCount)
        val resolvedTitle = deckTitleOverride?.takeIf { it.isNotBlank() }
            ?: extractTitleFromTopicOrWords(trimmedInput, parsedWords)

        return@withContext DictationDeck(
            id = "dict_${UUID.randomUUID()}",
            title = resolvedTitle,
            description = "Custom Dictation Deck with ${parsedWords.size} words.",
            categoryColor = getRandomCategoryColor(),
            iconName = "RecordVoiceOver",
            words = parsedWords,
            tags = listOf("Custom", "Dictation"),
            isAiGenerated = false,
            isStarred = false
        )
    }

    private fun getBuildConfigApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun callGeminiForDictation(
        input: String,
        apiKey: String,
        targetCount: Int
    ): List<DictationWord> {
        val models = listOf("gemini-3.6-flash")

        val systemInstruction = """
            You are an expert language tutor, lexicographer, and audio dictation specialist.
            The user will provide either:
            1. Raw study text, pasted notes, chapter excerpt, or a list of words.
            2. OR a topic/domain name (e.g. "GRE High Frequency Words", "Advanced Physics", "Hindi-English Words").

            YOUR MANDATE:
            Extract or generate exactly $targetCount distinct, high-value words or short phrases for spelling and audio dictation practice.
            IMPORTANT:
            - If the user uploaded or pasted words or notes, prioritize extracting and defining THEIR actual words rather than generating unrelated random words.
            - Ensure you produce AT LEAST $targetCount words (do NOT return just 1 or 2 items).
            - For each item provide:
              1. 'word': The exact word or short phrase to be spelled.
              2. 'meaning': A crystal-clear, memorable definition or Hindi translation.
              3. 'phonetic': Approximate IPA pronunciation (e.g. /ˌser.ənˈdɪp.ə.ti/).
              4. 'exampleSentence': A concise, natural example sentence demonstrating the word.

            Output ONLY a valid JSON array of objects.
            Format:
            [
              {
                "word": "Serendipity",
                "meaning": "Finding valuable or agreeable things not sought for; happy chance.",
                "phonetic": "/ˌser.ənˈdɪp.ə.ti/",
                "exampleSentence": "Finding this cozy bookstore in the rain was pure serendipity."
              }
            ]
        """.trimIndent()

        val prompt = "Create exactly $targetCount dictation flashcards from this text/topic:\n$input"

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    }
                    put("contents", contentsArr)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemInstruction))
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.3)
                    })
                }

                val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(requestBody).build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseStr.isNotBlank()) {
                    val words = parseGeminiWordsResponse(responseStr)
                    if (words.isNotEmpty()) {
                        return words.take(targetCount)
                    }
                } else {
                    Log.w(TAG, "Model $model returned code ${response.code}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error with model $model: ${e.message}")
            }
        }
        return emptyList()
    }

    private fun parseGeminiWordsResponse(responseStr: String): List<DictationWord> {
        val result = mutableListOf<DictationWord>()
        try {
            val root = JSONObject(responseStr)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            if (candidates.length() == 0) return emptyList()

            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            if (parts.length() == 0) return emptyList()

            val rawText = parts.getJSONObject(0).optString("text")
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()

            val jsonArray = if (cleanJson.startsWith("[")) {
                JSONArray(cleanJson)
            } else if (cleanJson.startsWith("{")) {
                val obj = JSONObject(cleanJson)
                obj.optJSONArray("words") ?: obj.optJSONArray("items") ?: obj.optJSONArray("data") ?: JSONArray()
            } else {
                JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val word = (item.optString("word").takeIf { it.isNotBlank() }
                    ?: item.optString("term").takeIf { it.isNotBlank() }
                    ?: item.optString("spelling").takeIf { it.isNotBlank() }
                    ?: item.optString("text")).orEmpty().trim()
                val meaning = (item.optString("meaning").takeIf { it.isNotBlank() }
                    ?: item.optString("definition").takeIf { it.isNotBlank() }
                    ?: item.optString("translation").takeIf { it.isNotBlank() }
                    ?: item.optString("explanation")).orEmpty().trim()
                val phonetic = item.optString("phonetic").trim()
                val example = (item.optString("exampleSentence").takeIf { it.isNotBlank() }
                    ?: item.optString("example").takeIf { it.isNotBlank() }
                    ?: item.optString("sentence")).orEmpty().trim()

                if (word.isNotBlank()) {
                    result.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = word,
                            meaning = if (meaning.isNotBlank()) meaning else "Definition for $word",
                            phonetic = phonetic,
                            exampleSentence = example
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini dictation JSON", e)
        }
        return result
    }

    /**
     * Deep offline semantic parser that prioritizes user's uploaded text/notes/word list.
     * Looks up each word in OfflineVocabularyDictionary and auto-synthesizes accurate
     * definitions, phonetic spellings, and context sentences when needed.
     */
    fun fallbackOfflineSmartParser(rawInput: String, targetCount: Int): List<DictationWord> {
        val trimmed = rawInput.trim()
        val lower = trimmed.lowercase()

        val isTopicOnly = (trimmed.length < 50 && !trimmed.contains("\n") && !trimmed.contains(",") && !trimmed.contains(":") && !trimmed.contains(" - "))

        // If the user entered a pure broad topic (not a pasted list/notes)
        if (isTopicOnly) {
            if (lower.contains("gre") || lower.contains("vocab") || lower.contains("advanced english") || lower.contains("ielts")) {
                return getGreVocabularySample(targetCount)
            }
            if (lower.contains("hindi") || lower.contains("shabd")) {
                return getHindiVocabularySample(targetCount)
            }
            if (lower.contains("idiom") || lower.contains("phrase")) {
                return getIdiomsSample(targetCount)
            }
            if (lower.contains("bio") || lower.contains("cell") || lower.contains("organ")) {
                return getBiologySample(targetCount)
            }
            if (lower.contains("physics") || lower.contains("science") || lower.contains("space")) {
                return getPhysicsSample(targetCount)
            }
            if (lower.contains("tech") || lower.contains("computer") || lower.contains("coding") || lower.contains("ai")) {
                return getComputerScienceSample(targetCount)
            }
        }

        val wordsList = mutableListOf<DictationWord>()
        val seenWords = mutableSetOf<String>()

        // 1. Line-by-line parsing (Handles: "Word: Meaning", "Word - Meaning", "1. Word", "Word")
        val lines = trimmed.split("\n", "\r", ";").map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            val (cleanWord, explicitMeaning) = cleanAndExtractWordLine(line)
            if (cleanWord.isNotBlank() && cleanWord.length >= 2) {
                val normalizedKey = cleanWord.lowercase().replace("[^a-z0-9]".toRegex(), "")
                if (normalizedKey.isNotBlank() && normalizedKey !in seenWords) {
                    seenWords.add(normalizedKey)

                    // Lookup in rich offline dictionary or synthesize
                    val dictEntry = OfflineVocabularyDictionary.findExactOrStem(cleanWord)
                    val meaningToUse = when {
                        explicitMeaning.isNotBlank() -> explicitMeaning
                        dictEntry != null -> dictEntry.meaning
                        else -> OfflineVocabularyDictionary.synthesizeWordDefinition(cleanWord, trimmed).meaning
                    }

                    val phoneticToUse = dictEntry?.phonetic
                        ?: OfflineVocabularyDictionary.generateApproximatePhonetic(cleanWord)

                    val exampleToUse = when {
                        dictEntry != null -> dictEntry.example
                        else -> OfflineVocabularyDictionary.synthesizeWordDefinition(cleanWord, trimmed).example
                    }

                    wordsList.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = cleanWord.replaceFirstChar { it.uppercase() },
                            meaning = meaningToUse,
                            phonetic = phoneticToUse,
                            exampleSentence = exampleToUse
                        )
                    )
                }
            }
            if (wordsList.size >= targetCount) break
        }

        // 2. Comma-separated or bullet list fallback
        if (wordsList.size < targetCount && (trimmed.contains(",") || trimmed.contains("•"))) {
            val tokens = trimmed.split(",", "•", "|", "/").map { it.trim() }.filter { it.isNotBlank() }
            for (token in tokens) {
                val (cleanWord, explicitMeaning) = cleanAndExtractWordLine(token)
                if (cleanWord.isNotBlank() && cleanWord.length >= 2) {
                    val normalizedKey = cleanWord.lowercase().replace("[^a-z0-9]".toRegex(), "")
                    if (normalizedKey.isNotBlank() && normalizedKey !in seenWords) {
                        seenWords.add(normalizedKey)
                        val dictEntry = OfflineVocabularyDictionary.findExactOrStem(cleanWord)
                        val meaningToUse = when {
                            explicitMeaning.isNotBlank() -> explicitMeaning
                            dictEntry != null -> dictEntry.meaning
                            else -> OfflineVocabularyDictionary.synthesizeWordDefinition(cleanWord, trimmed).meaning
                        }
                        val phoneticToUse = dictEntry?.phonetic
                            ?: OfflineVocabularyDictionary.generateApproximatePhonetic(cleanWord)
                        val exampleToUse = dictEntry?.example
                            ?: OfflineVocabularyDictionary.synthesizeWordDefinition(cleanWord, trimmed).example

                        wordsList.add(
                            DictationWord(
                                id = "dw_${UUID.randomUUID()}",
                                word = cleanWord.replaceFirstChar { it.uppercase() },
                                meaning = meaningToUse,
                                phonetic = phoneticToUse,
                                exampleSentence = exampleToUse
                            )
                        )
                    }
                }
                if (wordsList.size >= targetCount) break
            }
        }

        // 3. Raw Prose / Essay / Notes Keyword Extraction (extracts meaningful vocabulary terms)
        if (wordsList.size < targetCount) {
            val stopWords = setOf(
                "the", "and", "that", "have", "for", "not", "with", "you", "this", "but", "his", "from",
                "they", "say", "her", "she", "will", "one", "all", "would", "there", "their", "what",
                "out", "about", "who", "get", "which", "when", "make", "can", "like", "time", "just",
                "him", "know", "take", "people", "into", "year", "your", "good", "some", "could",
                "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over",
                "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well",
                "way", "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
            )

            val rawTokens = trimmed.split("\\s+".toRegex())
                .map { sanitizeWordToken(it) }
                .filter { it.length >= 4 && it.lowercase() !in stopWords }

            for (rawToken in rawTokens) {
                val normalized = rawToken.lowercase().replace("[^a-z0-9]".toRegex(), "")
                if (normalized.length >= 3 && normalized !in seenWords) {
                    seenWords.add(normalized)
                    val dictEntry = OfflineVocabularyDictionary.findExactOrStem(rawToken)
                    val meaningToUse = dictEntry?.meaning
                        ?: OfflineVocabularyDictionary.synthesizeWordDefinition(rawToken, trimmed).meaning
                    val phoneticToUse = dictEntry?.phonetic
                        ?: OfflineVocabularyDictionary.generateApproximatePhonetic(rawToken)
                    val exampleToUse = dictEntry?.example
                        ?: OfflineVocabularyDictionary.synthesizeWordDefinition(rawToken, trimmed).example

                    wordsList.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = rawToken.replaceFirstChar { it.uppercase() },
                            meaning = meaningToUse,
                            phonetic = phoneticToUse,
                            exampleSentence = exampleToUse
                        )
                    )
                }
                if (wordsList.size >= targetCount) break
            }
        }

        // If user provided very few words, backfill with top academic vocabulary up to targetCount
        if (wordsList.size < targetCount) {
            val backfill = getGreVocabularySample(targetCount)
            for (fallbackWord in backfill) {
                val norm = fallbackWord.word.lowercase()
                if (norm !in seenWords) {
                    seenWords.add(norm)
                    wordsList.add(fallbackWord)
                }
                if (wordsList.size >= targetCount) break
            }
        }

        return wordsList.take(targetCount)
    }

    private fun cleanAndExtractWordLine(rawLine: String): Pair<String, String> {
        // Remove leading bullets, numbers, lists: e.g. "1. ", "• ", "- ", "(a) ", "1) "
        val cleaned = rawLine.trim()
            .replace("^([0-9]{1,3}[.)\\]]|\\([0-9]{1,3}\\)|[a-zA-Z][.)\\]]|•|\\*|\\-|\\+|\\>|→|~)\\s*".toRegex(), "")
            .trim()

        if (cleaned.contains(":") || cleaned.contains(" - ") || cleaned.contains("—") || cleaned.contains("=")) {
            val delimiter = if (cleaned.contains(":")) ":" 
                else if (cleaned.contains("—")) "—" 
                else if (cleaned.contains(" - ")) " - " 
                else "="
            val parts = cleaned.split(delimiter, limit = 2)
            val rawWord = sanitizeWordToken(parts[0])
            val meaning = parts.getOrNull(1)?.trim()?.removePrefix("-")?.removePrefix(":")?.trim() ?: ""
            return Pair(rawWord, meaning)
        }

        val rawWord = sanitizeWordToken(cleaned)
        return Pair(rawWord, "")
    }

    private fun sanitizeWordToken(token: String): String {
        return token.trim()
            .replace("^[\"\'\\[\\(«“‘]+".toRegex(), "")
            .replace("[\"\'\\]\\)»”’]+$".toRegex(), "")
            .replace("^[•\\-*_~.,]+\\s*".toRegex(), "")
            .replace("[.,;:!?]+$".toRegex(), "")
            .trim()
    }

    private fun extractTitleFromTopicOrWords(input: String, words: List<DictationWord>): String {
        if (input.isNotBlank() && input.length <= 35 && !input.contains("\n")) {
            return input.replaceFirstChar { it.uppercase() }
        }
        val firstFew = words.take(2).joinToString(" & ") { it.word }
        return if (firstFew.isNotBlank()) "Chapter: $firstFew" else "Dictation Chapter"
    }

    private fun getRandomCategoryColor(): Color {
        val colors = listOf(
            Color(0xFF4EDEA3),
            Color(0xFF818CF8),
            Color(0xFFFF7886),
            Color(0xFF06B6D4),
            Color(0xFFF59E0B),
            Color(0xFFEC4899),
            Color(0xFF10B981),
            Color(0xFF6366F1)
        )
        return colors.random()
    }

    // --- Curated Domain Samples for Smart Fallback ---

    private fun getGreVocabularySample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Ephemeral", meaning = "Lasting for a very short time; fleeting.", phonetic = "/ɪˈfem.ər.əl/", exampleSentence = "Fame in the internet age can be fleeting and ephemeral."),
            DictationWord(word = "Eloquent", meaning = "Fluent or persuasive in speaking or writing.", phonetic = "/ˈel.ə.kwənt/", exampleSentence = "Her eloquent speech moved the entire audience to tears."),
            DictationWord(word = "Serendipity", meaning = "The occurrence of events by chance in a happy way.", phonetic = "/ˌser.ənˈdɪp.ə.ti/", exampleSentence = "Discovering this rare old book was pure serendipity."),
            DictationWord(word = "Ubiquitous", meaning = "Present, appearing, or found everywhere.", phonetic = "/juːˈbɪk.wɪ.təs/", exampleSentence = "Smartphones have become ubiquitous in modern daily life."),
            DictationWord(word = "Pernicious", meaning = "Having a harmful effect, especially in a gradual way.", phonetic = "/pərˈnɪʃ.əs/", exampleSentence = "Pollution has a pernicious influence on public health."),
            DictationWord(word = "Meticulous", meaning = "Showing great attention to detail; very careful and precise.", phonetic = "/məˈtɪk.jə.ləs/", exampleSentence = "He kept meticulous records of all experimental measurements."),
            DictationWord(word = "Pragmatic", meaning = "Dealing with things sensibly and realistically.", phonetic = "/præɡˈmæt.ɪk/", exampleSentence = "We need a pragmatic approach rather than theoretical ideals."),
            DictationWord(word = "Ineffable", meaning = "Too great or extreme to be expressed in words.", phonetic = "/ɪnˈef.ə.bəl/", exampleSentence = "The view from the mountain peak inspired ineffable joy."),
            DictationWord(word = "Resilience", meaning = "The capacity to recover quickly from difficulties.", phonetic = "/rɪˈzɪl.jəns/", exampleSentence = "The community showed remarkable resilience after the storm."),
            DictationWord(word = "Tenacious", meaning = "Tending to keep a firm hold of something; persistent.", phonetic = "/təˈneɪ.ʃəs/", exampleSentence = "She had a tenacious grip on her principles and goals."),
            DictationWord(word = "Alleviate", meaning = "To make suffering or a problem less severe.", phonetic = "/əˈliː.vi.eɪt/", exampleSentence = "The medicine helped alleviate her severe symptoms."),
            DictationWord(word = "Ambiguous", meaning = "Open to more than one interpretation; unclear.", phonetic = "/æmˈbɪɡ.ju.əs/", exampleSentence = "He gave an ambiguous answer to the difficult question."),
            DictationWord(word = "Benevolent", meaning = "Well-meaning, kindly, and charitable.", phonetic = "/bəˈnev.əl.ənt/", exampleSentence = "The benevolent donor funded scholarships for rural youth."),
            DictationWord(word = "Candid", meaning = "Truthful and straightforward; frank.", phonetic = "/ˈkæn.dɪd/", exampleSentence = "We had a candid conversation about the project delays."),
            DictationWord(word = "Diligent", meaning = "Having or showing care and conscientiousness in work.", phonetic = "/ˈdɪl.ɪ.dʒənt/", exampleSentence = "Diligent students practice every day to achieve fluency.")
        )
        return pool.take(count)
    }

    private fun getHindiVocabularySample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Abhyas", meaning = "Continuous practice or dedicated study (अभ्यास).", phonetic = "/əbʱˈjaːs/", exampleSentence = "Regular abhyas is essential for mastering any language."),
            DictationWord(word = "Sankalp", meaning = "Determination, solemn resolution, or firm resolve (संकल्प).", phonetic = "/səŋˈkəlp/", exampleSentence = "With strong sankalp, no goal is impossible to achieve."),
            DictationWord(word = "Prerana", meaning = "Inspiration or motivation to achieve something noble (प्रेरणा).", phonetic = "/preːɾ.ɳaː/", exampleSentence = "Her dedication was a constant source of prerana for the students."),
            DictationWord(word = "Satya", meaning = "Truthfulness, honesty, and alignment with reality (सत्य).", phonetic = "/sət̪.jə/", exampleSentence = "Satya is the foundation of integrity."),
            DictationWord(word = "Sahaj", meaning = "Natural, effortless, and simple (सहज).", phonetic = "/sə.ɦədʒ/", exampleSentence = "His speaking style was wonderfully sahaj and engaging."),
            DictationWord(word = "Shanti", meaning = "Peace, tranquility, and calmness of mind (शान्ति).", phonetic = "/ˈʃaːn.t̪i/", exampleSentence = "Meditation brings inner shanti amidst everyday noise."),
            DictationWord(word = "Karm", meaning = "Action, duty, or deed that shapes one's future (कर्म).", phonetic = "/kərm/", exampleSentence = "Focus on your karm without anxiety for the outcome."),
            DictationWord(word = "Samarpan", meaning = "Total dedication or surrender to a higher cause (समर्पण).", phonetic = "/sə.mərˈpəɳ/", exampleSentence = "Success in research demands immense dedication and samarpan."),
            DictationWord(word = "Anurag", meaning = "Deep affection, love, or devotion (अनुराग).", phonetic = "/ə.nʊˈraːɡ/", exampleSentence = "He pursued classical literature with profound anurag."),
            DictationWord(word = "Vivek", meaning = "Discretion, wisdom, and right discernment (विवेक).", phonetic = "/vɪˈveːk/", exampleSentence = "Exercise vivek when deciding on important matters.")
        )
        return pool.take(count)
    }

    private fun getIdiomsSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Bite the bullet", meaning = "To face a difficult situation with courage.", phonetic = "/baɪt ðə ˈbʊl.ɪt/", exampleSentence = "I decided to bite the bullet and give the presentation."),
            DictationWord(word = "Break the ice", meaning = "To make people feel more comfortable in a social setting.", phonetic = "/breɪk ði aɪs/", exampleSentence = "A quick warm-up game helped break the ice."),
            DictationWord(word = "Piece of cake", meaning = "Something that is very easy to accomplish.", phonetic = "/piːs əv keɪk/", exampleSentence = "Once we practiced, the test felt like a piece of cake."),
            DictationWord(word = "Burn the midnight oil", meaning = "To work or study late into the night.", phonetic = "/bɜːrn ðə ˈmɪd.naɪt ɔɪl/", exampleSentence = "Students often burn the midnight oil before final exams."),
            DictationWord(word = "Hit the nail on the head", meaning = "To describe exactly what is causing a situation.", phonetic = "/hɪt ðə neɪl ɒn ðə hed/", exampleSentence = "Her summary hit the nail right on the head."),
            DictationWord(word = "Once in a blue moon", meaning = "Happening very rarely.", phonetic = "/wʌns ɪn ə bluː muːn/", exampleSentence = "We visit our hometown once in a blue moon."),
            DictationWord(word = "Spill the beans", meaning = "To disclose a secret prematurely.", phonetic = "/spɪl ðə biːnz/", exampleSentence = "Do not spill the beans about the surprise anniversary party."),
            DictationWord(word = "Under the weather", meaning = "Feeling slightly unwell or sick.", phonetic = "/ˈʌn.dər ðə ˈweð.ər/", exampleSentence = "He stayed home today because he was feeling under the weather."),
            DictationWord(word = "Cost an arm and a leg", meaning = "To be extraordinarily expensive.", phonetic = "/kɒst ən ɑːm ænd ə leɡ/", exampleSentence = "The latest flagship smartphone costs an arm and a leg."),
            DictationWord(word = "See eye to eye", meaning = "To agree fully with someone.", phonetic = "/siː aɪ tuː aɪ/", exampleSentence = "The partners do not always see eye to eye on hiring decisions.")
        )
        return pool.take(count)
    }

    private fun getBiologySample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Mitochondria", meaning = "The powerhouse of the cell responsible for ATP energy production.", phonetic = "/ˌmaɪ.təˈkɒn.dri.ə/", exampleSentence = "Mitochondria generate cellular energy through aerobic respiration."),
            DictationWord(word = "Photosynthesis", meaning = "Process by which green plants use sunlight to synthesize nutrients.", phonetic = "/ˌfəʊ.təʊˈsɪn.θə.sɪs/", exampleSentence = "Chlorophyll absorbs sunlight to drive photosynthesis."),
            DictationWord(word = "Homeostasis", meaning = "The maintenance of stable internal physiological conditions.", phonetic = "/ˌhəʊ.mi.əʊˈsteɪ.sɪs/", exampleSentence = "The human body maintains homeostasis through feedback mechanisms."),
            DictationWord(word = "Chromosome", meaning = "Thread-like structure of nucleic acids carrying genetic information.", phonetic = "/ˈkrəʊ.mə.səʊm/", exampleSentence = "Humans normally possess 23 pairs of chromosomes."),
            DictationWord(word = "Ribosome", meaning = "Cellular particle made of RNA and protein that synthesizes proteins.", phonetic = "/ˈraɪ.bə.səʊm/", exampleSentence = "Ribosomes translate mRNA sequences into polypeptide chains."),
            DictationWord(word = "Hemoglobin", meaning = "Red protein responsible for transporting oxygen in the blood.", phonetic = "/ˌhiː.məˈɡloʊ.bɪn/", exampleSentence = "Iron is a vital component for healthy hemoglobin synthesis."),
            DictationWord(word = "Metabolism", meaning = "The chemical processes that occur within a living organism to maintain life.", phonetic = "/məˈtæb.əl.ɪ.zəm/", exampleSentence = "Regular exercise stimulates an active metabolism."),
            DictationWord(word = "Enzyme", meaning = "A biological catalyst that accelerates specific chemical reactions.", phonetic = "/ˈen.zaɪm/", exampleSentence = "Digestive enzymes break down complex carbohydrates into glucose."),
            DictationWord(word = "Pathogen", meaning = "A bacterium, virus, or other microorganism that can cause disease.", phonetic = "/ˈpæθ.ə.dʒən/", exampleSentence = "Antibodies help the immune system identify and neutralize pathogens."),
            DictationWord(word = "Neuron", meaning = "A specialized cell transmitting nerve impulses in the nervous system.", phonetic = "/ˈnjʊər.ɒn/", exampleSentence = "Billions of neurons form the intricate neural network of the brain.")
        )
        return pool.take(count)
    }

    private fun getPhysicsSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Thermodynamics", meaning = "The branch of physical science dealing with heat and mechanical energy.", phonetic = "/ˌθɜː.məʊ.daɪˈnæm.ɪks/", exampleSentence = "The first law of thermodynamics states energy cannot be created or destroyed."),
            DictationWord(word = "Superconductivity", meaning = "Zero electrical resistance occurring in certain materials at low temperatures.", phonetic = "/ˌsuː.pəˌkɒn.dʌkˈtɪv.ə.ti/", exampleSentence = "Superconductivity enables powerful magnetic levitation."),
            DictationWord(word = "Diffraction", meaning = "The bending of waves around the corners of an obstacle.", phonetic = "/dɪˈfræk.ʃən/", exampleSentence = "Laser light showed a clear diffraction pattern on the screen."),
            DictationWord(word = "Centripetal force", meaning = "Force that makes a body follow a curved path directed toward center.", phonetic = "/senˈtrɪp.ɪ.təl fɔːs/", exampleSentence = "Gravity acts as the centripetal force keeping planets in orbit."),
            DictationWord(word = "Refraction", meaning = "The change in direction of a wave passing from one medium to another.", phonetic = "/rɪˈfræk.ʃən/", exampleSentence = "Light refraction through water droplets creates a colorful rainbow."),
            DictationWord(word = "Momentum", meaning = "The quantity of motion of a moving body, measured as product of mass and velocity.", phonetic = "/məˈmen.təm/", exampleSentence = "Conservation of momentum applies during elastic billiard collisions."),
            DictationWord(word = "Capacitance", meaning = "The ability of a system to store an electric charge.", phonetic = "/kəˈpæs.ɪ.təns/", exampleSentence = "Capacitors with higher capacitance store more electrical energy."),
            DictationWord(word = "Electromagnetism", meaning = "The interaction of electric currents or fields and magnetic fields.", phonetic = "/iˌlek.troʊˈmæɡ.nə.tɪ.zəm/", exampleSentence = "Maxwell's equations unified electricity and magnetism into electromagnetism."),
            DictationWord(word = "Oscillation", meaning = "Repetitive movement back and forth at a regular speed.", phonetic = "/ˌɒs.ɪˈleɪ.ʃən/", exampleSentence = "The oscillation of a grandfather clock pendulum marks the seconds."),
            DictationWord(word = "Gravitation", meaning = "The universal force of attraction acting between all matter.", phonetic = "/ˌɡræv.ɪˈteɪ.ʃən/", exampleSentence = "Newton formulated the universal law of gravitation.")
        )
        return pool.take(count)
    }

    private fun getComputerScienceSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Asynchronous", meaning = "Operations that occur independently of the main program flow.", phonetic = "/eɪˈsɪŋ.krə.nəs/", exampleSentence = "Coroutines simplify asynchronous networking tasks in Kotlin."),
            DictationWord(word = "Polymorphism", meaning = "Ability of an object or function to take on many forms.", phonetic = "/ˌpɒl.iˈmɔː.fɪ.zəm/", exampleSentence = "Method overriding is a classic example of runtime polymorphism."),
            DictationWord(word = "Recursion", meaning = "Method where the solution to a problem depends on solutions to smaller instances.", phonetic = "/rɪˈkɜː.ʒən/", exampleSentence = "Tree traversal algorithms naturally lend themselves to recursion."),
            DictationWord(word = "Encapsulation", meaning = "Bundling of data and methods that operate on that data within one unit.", phonetic = "/ɪnˌkæp.sjəˈleɪ.ʃən/", exampleSentence = "Encapsulation protects class fields from unintended external access."),
            DictationWord(word = "Concurrency", meaning = "The execution of multiple instruction sequences at the same time.", phonetic = "/kənˈkɜːr.ən.si/", exampleSentence = "Thread synchronization prevents race conditions during concurrency."),
            DictationWord(word = "Immutable", meaning = "Unchanging over time or unable to be modified after creation.", phonetic = "/ɪˈmjuː.tə.bəl/", exampleSentence = "Kotlin val properties define immutable references for safer code."),
            DictationWord(word = "Repository", meaning = "A central place where data is stored, managed, and abstracted.", phonetic = "/rɪˈpɒz.ɪ.tər.i/", exampleSentence = "The repository pattern decouples the UI from direct database queries."),
            DictationWord(word = "Serialization", meaning = "The process of converting an object into a format that can be stored or transmitted.", phonetic = "/ˌsɪər.i.ə.laɪˈzeɪ.ʃən/", exampleSentence = "We use kotlinx.serialization to convert Kotlin objects to JSON strings."),
            DictationWord(word = "Inheritance", meaning = "The mechanism where a new class inherits attributes from an existing class.", phonetic = "/ɪnˈher.ɪ.təns/", exampleSentence = "Object-oriented design utilizes inheritance to promote code reuse."),
            DictationWord(word = "Abstraction", meaning = "Hiding complex implementation details and showing only necessary features.", phonetic = "/æbˈstræk.ʃən/", exampleSentence = "Interfaces provide clean abstraction layers between modular components.")
        )
        return pool.take(count)
    }
}
