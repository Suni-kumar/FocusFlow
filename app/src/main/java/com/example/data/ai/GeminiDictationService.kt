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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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

        if (apiKeyToUse.isNotBlank() && apiKeyToUse != "MY_GEMINI_API_KEY") {
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
                Log.w(TAG, "Gemini Dictation generation failed, falling back to smart heuristic taxonomy", e)
            }
        }

        // Smart Intelligent Heuristic & Vocabulary Taxonomy Engine Fallback
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
        val models = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")

        val systemInstruction = """
            You are an expert language tutor and dictation specialist.
            The user will provide either a Topic (e.g. "GRE High Frequency Words", "Medical Terms", "Daily Idioms", "Hindi-English Words") OR raw study text/notes.
            Your task is to generate/extract exactly $targetCount clear, high-yield words or short phrases for audio dictation and spelling practice.
            
            For each item, provide:
            1. 'word': The exact word or short phrase to be pronounced and spelled.
            2. 'meaning': A clear, memorable definition or translation (in English or Hindi-English).
            3. 'phonetic': Approximate IPA pronunciation (e.g., /ˌel.ə.kwənt/).
            4. 'exampleSentence': A concise, natural example sentence demonstrating usage.

            Output ONLY a single valid JSON array of objects.
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

        val prompt = "Generate or extract $targetCount structured dictation words from this topic/content:\n$input"

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

    private fun fallbackOfflineSmartParser(rawInput: String, targetCount: Int): List<DictationWord> {
        val lower = rawInput.lowercase()

        // 1. Check if user typed a known domain/subject to supply rich curated lists
        if (lower.contains("gre") || lower.contains("vocab") || lower.contains("advanced english")) {
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

        // 2. Parse raw text lines with bullet/number/special character sanitization
        val lines = rawInput.split("\n", ";", "\r").map { it.trim() }.filter { it.isNotBlank() }
        val wordsList = mutableListOf<DictationWord>()
        val seenWords = mutableSetOf<String>()

        for (line in lines) {
            val (cleanWord, cleanMeaning) = cleanAndExtractWordLine(line)
            if (cleanWord.isNotBlank() && cleanWord.length >= 2) {
                val normalizedKey = cleanWord.lowercase().replace("[^a-z0-9]".toRegex(), "")
                if (normalizedKey !in seenWords) {
                    seenWords.add(normalizedKey)
                    wordsList.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = cleanWord,
                            meaning = if (cleanMeaning.isNotBlank()) cleanMeaning else "Spelling and meaning for $cleanWord",
                            exampleSentence = "Listen carefully to $cleanWord and practice writing it."
                        )
                    )
                }
            }
            if (wordsList.size >= targetCount) break
        }

        if (wordsList.isEmpty()) {
            val words = rawInput.split("\\s+".toRegex())
                .map { sanitizeWordToken(it) }
                .filter { it.length > 2 }
                .distinct()
                .take(targetCount)

            for (w in words) {
                val normalizedKey = w.lowercase()
                if (normalizedKey !in seenWords) {
                    seenWords.add(normalizedKey)
                    wordsList.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = w.replaceFirstChar { it.uppercase() },
                            meaning = "Essential vocabulary term: $w",
                            exampleSentence = "Listen and spell $w."
                        )
                    )
                }
            }
        }

        return if (wordsList.isNotEmpty()) wordsList.take(targetCount) else getGreVocabularySample(targetCount)
    }

    private fun cleanAndExtractWordLine(rawLine: String): Pair<String, String> {
        // Remove leading bullets, numbers, lists: e.g. "1. ", "• ", "- ", "(a) ", "1) "
        var cleaned = rawLine.trim()
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

        // If line is a single term or comma separated
        val rawWord = sanitizeWordToken(cleaned)
        return Pair(rawWord, "")
    }

    private fun sanitizeWordToken(token: String): String {
        return token.trim()
            .replace("^[\"\'\\[\\(«“‘]+".toRegex(), "")
            .replace("[\"\'\\]\\)»”’]+$".toRegex(), "")
            .replace("^[•\\-*_~]+\\s*".toRegex(), "")
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
            DictationWord(word = "Tenacious", meaning = "Tending to keep a firm hold of something; persistent.", phonetic = "/təˈneɪ.ʃəs/", exampleSentence = "She had a tenacious grip on her principles and goals.")
        )
        return pool.take(count)
    }

    private fun getHindiVocabularySample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Abhyas", meaning = "Continuous practice or dedicated study (अभ्यास).", phonetic = "/əbʱˈjaːs/", exampleSentence = "Regular abhyas is essential for mastering any language."),
            DictationWord(word = "Sankalp", meaning = "Determination, solemn resolution, or firm resolve (संकल्प).", phonetic = "/səŋˈkəlp/", exampleSentence = "With strong sankalp, no goal is impossible to achieve."),
            DictationWord(word = "Prerana", meaning = "Inspiration or motivation to achieve something noble (प्रेरणा).", phonetic = "/preːɾ.ɳaː/", exampleSentence = "Her dedication was a constant source of prerana for the students."),
            DictationWord(word = "Satya", meaning = "Truthfulness, honesty, and alignment with reality (सत्य).", phonetic = "/sət̪.jə/", exampleSentence = "Satya is the foundation of integrity."),
            DictationWord(word = "Sahaj", meaning = "Natural, effortless, and simple (सहज).", phonetic = "/sə.ɦədʒ/", exampleSentence = "His speaking style was wonderfully sahaj and engaging.")
        )
        return pool.take(count)
    }

    private fun getIdiomsSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Bite the bullet", meaning = "To face a difficult situation with courage.", phonetic = "/baɪt ðə ˈbʊl.ɪt/", exampleSentence = "I decided to bite the bullet and give the presentation."),
            DictationWord(word = "Break the ice", meaning = "To make people feel more comfortable in a social setting.", phonetic = "/breɪk ði aɪs/", exampleSentence = "A quick warm-up game helped break the ice."),
            DictationWord(word = "Piece of cake", meaning = "Something that is very easy to accomplish.", phonetic = "/piːs əv keɪk/", exampleSentence = "Once we practiced, the test felt like a piece of cake."),
            DictationWord(word = "Burn the midnight oil", meaning = "To work or study late into the night.", phonetic = "/bɜːrn ðə ˈmɪd.naɪt ɔɪl/", exampleSentence = "Students often burn the midnight oil before final exams."),
            DictationWord(word = "Hit the nail on the head", meaning = "To describe exactly what is causing a situation.", phonetic = "/hɪt ðə neɪl ɒn ðə hed/", exampleSentence = "Her summary hit the nail right on the head.")
        )
        return pool.take(count)
    }

    private fun getBiologySample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Mitochondria", meaning = "The powerhouse of the cell responsible for ATP energy production.", phonetic = "/ˌmaɪ.təˈkɒn.dri.ə/", exampleSentence = "Mitochondria generate cellular energy through aerobic respiration."),
            DictationWord(word = "Photosynthesis", meaning = "Process by which green plants use sunlight to synthesize nutrients.", phonetic = "/ˌfəʊ.təʊˈsɪn.θə.sɪs/", exampleSentence = "Chlorophyll absorbs sunlight to drive photosynthesis."),
            DictationWord(word = "Homeostasis", meaning = "The maintenance of stable internal physiological conditions.", phonetic = "/ˌhəʊ.mi.əʊˈsteɪ.sɪs/", exampleSentence = "The human body maintains homeostasis through feedback mechanisms."),
            DictationWord(word = "Chromosome", meaning = "Thread-like structure of nucleic acids carrying genetic information.", phonetic = "/ˈkrəʊ.mə.səʊm/", exampleSentence = "Humans normally possess 23 pairs of chromosomes."),
            DictationWord(word = "Ribosome", meaning = "Cellular particle made of RNA and protein that synthesizes proteins.", phonetic = "/ˈraɪ.bə.səʊm/", exampleSentence = "Ribosomes translate mRNA sequences into polypeptide chains.")
        )
        return pool.take(count)
    }

    private fun getPhysicsSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Thermodynamics", meaning = "The branch of physical science dealing with heat and mechanical energy.", phonetic = "/ˌθɜː.məʊ.daɪˈnæm.ɪks/", exampleSentence = "The first law of thermodynamics states energy cannot be created or destroyed."),
            DictationWord(word = "Superconductivity", meaning = "Zero electrical resistance occurring in certain materials at low temperatures.", phonetic = "/ˌsuː.pəˌkɒn.dʌkˈtɪv.ə.ti/", exampleSentence = "Superconductivity enables powerful magnetic levitation."),
            DictationWord(word = "Diffraction", meaning = "The bending of waves around the corners of an obstacle.", phonetic = "/dɪˈfræk.ʃən/", exampleSentence = "Laser light showed a clear diffraction pattern on the screen."),
            DictationWord(word = "Centripetal force", meaning = "Force that makes a body follow a curved path directed toward center.", phonetic = "/senˈtrɪp.ɪ.təl fɔːs/", exampleSentence = "Gravity acts as the centripetal force keeping planets in orbit.")
        )
        return pool.take(count)
    }

    private fun getComputerScienceSample(count: Int): List<DictationWord> {
        val pool = listOf(
            DictationWord(word = "Asynchronous", meaning = "Operations that occur independently of the main program flow.", phonetic = "/eɪˈsɪŋ.krə.nəs/", exampleSentence = "Coroutines simplify asynchronous networking tasks in Kotlin."),
            DictationWord(word = "Polymorphism", meaning = "Ability of an object or function to take on many forms.", phonetic = "/ˌpɒl.iˈmɔː.fɪ.zəm/", exampleSentence = "Method overriding is a classic example of runtime polymorphism."),
            DictationWord(word = "Recursion", meaning = "Method where the solution to a problem depends on solutions to smaller instances.", phonetic = "/rɪˈkɜː.ʒən/", exampleSentence = "Tree traversal algorithms naturally lend themselves to recursion."),
            DictationWord(word = "Encapsulation", meaning = "Bundling of data and methods that operate on that data within one unit.", phonetic = "/ɪnˌkæp.sjəˈleɪ.ʃən/", exampleSentence = "Encapsulation protects class fields from unintended external access.")
        )
        return pool.take(count)
    }
}

