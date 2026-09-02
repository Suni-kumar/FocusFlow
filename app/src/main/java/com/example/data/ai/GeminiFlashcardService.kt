package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.Flashcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class FlashcardGenerationResult(
    val title: String,
    val description: String,
    val cards: List<Flashcard>,
    val source: GenerationSource,
    val tags: List<String>,
    val warningMessage: String? = null
)

class GeminiApiException(
    val httpCode: Int,
    val userFacingMessage: String,
    cause: Throwable? = null
) : Exception(userFacingMessage, cause)

enum class GenerationSource {
    BYOK_CLIENT,
    SERVER_PROXY_FALLBACK,
    OFFLINE_HEURISTIC
}

class GeminiFlashcardService {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateFlashcards(
        topicOrPrompt: String,
        targetCardCount: Int,
        userCustomApiKey: String? = null
    ): FlashcardGenerationResult = withContext(Dispatchers.IO) {
        val trimmedTopic = topicOrPrompt.trim()
        if (trimmedTopic.isEmpty()) {
            return@withContext FlashcardGenerationResult(
                title = "Quick Study Deck",
                description = "General study concepts and active recall items.",
                cards = generateHeuristicCards("General Study", targetCardCount),
                source = GenerationSource.OFFLINE_HEURISTIC,
                tags = listOf("#HighYield", "#Definitions")
            )
        }

        // 1. Dual-Tier Key Resolution
        val (apiKeyToUse, source) = resolveApiKey(userCustomApiKey)

        var warningNote: String? = null
        if (apiKeyToUse.isNotBlank() && apiKeyToUse != "MY_GEMINI_API_KEY") {
            try {
                val aiCards = callGeminiApi(trimmedTopic, targetCardCount, apiKeyToUse)
                if (aiCards.isNotEmpty()) {
                    val allTags = aiCards.flatMap { it.tags }.distinct().take(5)
                    val derivedTitle = deriveTitleFromTopic(trimmedTopic)
                    return@withContext FlashcardGenerationResult(
                        title = derivedTitle,
                        description = "AI-generated active recall deck for $derivedTitle (${aiCards.size} cards).",
                        cards = aiCards,
                        source = source,
                        tags = if (allTags.isNotEmpty()) allTags else listOf("#AI_Generated", "#HighYield")
                    )
                }
            } catch (e: GeminiApiException) {
                warningNote = e.userFacingMessage
                Log.w("GeminiFlashcardService", "Gemini API explicit error: ${e.userFacingMessage}", e)
            } catch (e: Exception) {
                warningNote = "Cloud AI synthesis timed out. Smart taxonomy engine applied."
                Log.w("GeminiFlashcardService", "AI generation failed, falling back to smart heuristic engine", e)
            }
        }

        // 3. Smart Heuristic Taxonomy Fallback (Offline & Error Resilience)
        val fallbackCards = generateHeuristicCards(trimmedTopic, targetCardCount)
        val fallbackTags = fallbackCards.flatMap { it.tags }.distinct().take(4)
        val title = deriveTitleFromTopic(trimmedTopic)

        FlashcardGenerationResult(
            title = title,
            description = "High-yield smart taxonomy deck for $title (${fallbackCards.size} cards).",
            cards = fallbackCards,
            source = GenerationSource.OFFLINE_HEURISTIC,
            tags = if (fallbackTags.isNotEmpty()) fallbackTags else listOf("#HighYield", "#KeyConcepts"),
            warningMessage = warningNote
        )
    }

    private fun resolveApiKey(userKey: String?): Pair<String, GenerationSource> {
        // Tier 1: User's custom BYOK key
        if (!userKey.isNullOrBlank()) {
            return Pair(userKey.trim(), GenerationSource.BYOK_CLIENT)
        }

        // Tier 2: BuildConfig / Server environment fallback
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return Pair(buildConfigKey.trim(), GenerationSource.SERVER_PROXY_FALLBACK)
        }

        return Pair("", GenerationSource.OFFLINE_HEURISTIC)
    }

    private fun callGeminiApi(
        topic: String,
        targetCardCount: Int,
        apiKey: String
    ): List<Flashcard> {
        val models = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")
        var lastException: Exception? = null

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val systemPrompt = """
                    You are an expert tutor creating high-yield active recall study flashcards.
                    Analyze the given topic or notes and generate exactly $targetCardCount unique, non-duplicate flashcards.
                    
                    CRITICAL BEHAVIORAL RULE (STRICT PRESERVATION):
                    - IF the user provides explicit questions and answers (e.g. "Q: ... A: ...", "Front: ... Back: ...", or a direct list of items to memorize), you MUST extract and use them EXACTLY as provided. Do NOT re-write, modify, or invent new questions for the provided pairs. Just format them into the JSON structure.
                    - ONLY generate new conceptual questions if the user provides general, unstructured notes.
                    
                    CRITICAL FORMAT RULES:
                    - Output MUST be a single valid JSON array of objects. Do NOT include markdown text outside the JSON array.
                    - Ensure every question and answer is complete and not truncated.
                    - Each object must have:
                      * "front": Clear question or the exact provided front text.
                      * "back": Concise answer or the exact provided back text.
                      * "tags": Array of 1 to 3 relevant categorical tags (e.g., ["#Definitions", "#HighYield", "#KeyConcept"]).
                    
                    Example:
                    [
                      {
                        "front": "What is Hebbian Plasticity?",
                        "back": "Neurons that fire together wire together: repeated activation strengthens synaptic efficiency.",
                        "tags": ["#Definitions", "#HighYield"]
                      }
                    ]
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().put("text", "Topic / Notes / Q&A:\n$topic\n\nGenerate exactly $targetCardCount flashcards based on the above content. Remember: if exact questions and answers are provided above, preserve them exactly!"))
                            }
                            put("parts", partsArray)
                        })
                    }
                    put("contents", contentsArray)

                    val sysContent = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        }
                        put("parts", partsArray)
                    }
                    put("systemInstruction", sysContent)

                    val genConfig = JSONObject().apply {
                        put("temperature", 0.2)
                        put("topP", 0.95)
                        put("responseMimeType", "application/json")
                    }
                    put("generationConfig", genConfig)
                }

                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.w("GeminiFlashcardService", "Model $model returned code ${response.code}: $responseBodyStr")
                    if (response.code == 400 || response.code == 403) {
                        throw GeminiApiException(response.code, "Invalid Gemini API Key. Please verify your key in Settings.")
                    } else if (response.code == 429) {
                        throw GeminiApiException(429, "Gemini API rate limit or quota exceeded. Free tier quota limit reached.")
                    }
                    continue
                }

                val parsedCards = parseGeminiResponse(responseBodyStr, topic)
                if (parsedCards.isNotEmpty()) {
                    return parsedCards
                }
            } catch (e: Exception) {
                lastException = e
                Log.w("GeminiFlashcardService", "Attempt with $model failed: ${e.message}")
            }
        }

        if (lastException != null) {
            throw lastException
        }
        return emptyList()
    }

    private fun parseGeminiResponse(rawJson: String, topic: String): List<Flashcard> {
        val root = JSONObject(rawJson)
        val candidates = root.optJSONArray("candidates") ?: return emptyList()
        if (candidates.length() == 0) return emptyList()

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return emptyList()
        val parts = content.optJSONArray("parts") ?: return emptyList()
        if (parts.length() == 0) return emptyList()

        var text = parts.getJSONObject(0).optString("text", "").trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        }
        if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }

        val jsonArray = try {
            JSONArray(text)
        } catch (e: Exception) {
            // Find JSON array bounds
            val start = text.indexOf('[')
            val end = text.lastIndexOf(']')
            if (start != -1 && end != -1 && end > start) {
                JSONArray(text.substring(start, end + 1))
            } else {
                return emptyList()
            }
        }

        val flashcards = mutableListOf<Flashcard>()
        val seenFronts = mutableSetOf<String>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            val front = item.optString("front", "").trim()
            val back = item.optString("back", "").trim()

            // Skip truncated, empty, or duplicate questions
            if (front.length < 3 || back.length < 2) continue
            val normalizedKey = front.lowercase().replace("[^a-z0-9]".toRegex(), "")
            if (normalizedKey in seenFronts) continue
            seenFronts.add(normalizedKey)

            val tagsArray = item.optJSONArray("tags")
            val tagsList = mutableListOf<String>()
            if (tagsArray != null) {
                for (j in 0 until tagsArray.length()) {
                    val tag = tagsArray.optString(j, "").trim()
                    if (tag.isNotEmpty()) {
                        tagsList.add(if (tag.startsWith("#")) tag else "#$tag")
                    }
                }
            }

            val safeTags = if (tagsList.isNotEmpty()) tagsList else classifyTaxonomy(front, back)
            flashcards.add(
                Flashcard(
                    id = "ai_c_${System.currentTimeMillis()}_$i",
                    front = front,
                    back = back,
                    topic = topic.take(40),
                    tags = safeTags
                )
            )
        }
        return flashcards
    }

    /**
     * Smart Regex Taxonomy Engine (Offline Fallback & Tag Classifier)
     */
    fun classifyTaxonomy(front: String, back: String): List<String> {
        val combined = "$front $back".lowercase()
        val tags = mutableListOf<String>()

        // 1. Formulas & Math
        val formulaPattern = Pattern.compile("(\\bformula\\b|\\bequation\\b|\\bcalculate\\b|\\bderiv\\w*\\b|\\=|[+*/^]|\\btheorem\\b|\\bratio\\b|\\bconstant\\b)")
        if (formulaPattern.matcher(combined).find()) {
            tags.add("#Formulas")
        }

        // 2. Definitions & Terminology
        val defPattern = Pattern.compile("(\\bdefined as\\b|\\brefers to\\b|\\bmeaning\\b|\\bwhat is\\b|\\bterm\\b|\\bdenotes\\b|\\bstands for\\b)")
        if (defPattern.matcher(combined).find()) {
            tags.add("#Definitions")
        }

        // 3. Science & Medicine / Biology / Chemistry / Physics
        val sciencePattern = Pattern.compile("(\\bcell\\b|\\bneuron\\b|\\bblood\\b|\\bcardiac\\b|\\bprotein\\b|\\bdna\\b|\\brna\\b|\\bgene\\b|\\borgan\\b|\\bacid\\b|\\bmolecule\\b|\\bquantum\\b|\\benergy\\b|\\bpressure\\b|\\bvelocity\\b)")
        if (sciencePattern.matcher(combined).find()) {
            tags.add("#Science")
        }

        // 4. Programming & Engineering
        val techPattern = Pattern.compile("(\\bcode\\b|\\balgorithm\\b|\\bapi\\b|\\bdatabase\\b|\\bclass\\b|\\bfunction\\b|\\barchitecture\\b|\\bthread\\b|\\bmemory\\b|\\bserver\\b|\\bnetwork\\b)")
        if (techPattern.matcher(combined).find()) {
            tags.add("#Tech")
        }

        // 5. High-Yield & Exam Essentials
        val highYieldPattern = Pattern.compile("(\\bcritical\\b|\\bkey\\b|\\bprimary\\b|\\bmajor\\b|\\bessential\\b|\\bfunction of\\b|\\badvantage\\b|\\bprinciple\\b|\\bmechanism\\b|\\bwhy\\b|\\bhow\\b)")
        if (highYieldPattern.matcher(combined).find() || tags.isEmpty()) {
            tags.add("#HighYield")
        }

        return tags.distinct().take(3)
    }

    /**
     * Smart heuristic generation for offline mode or fallback
     */
    private fun generateHeuristicCards(rawText: String, count: Int): List<Flashcard> {
        val lines = rawText.split("\n", ";", ".")
            .map { it.trim() }
            .filter { it.length > 5 }

        val cards = mutableListOf<Flashcard>()
        val topicName = deriveTitleFromTopic(rawText)

        // Pattern 1: "X is Y" or "X: Y" or "X - Y"
        val splitRegex = Regex("(?i)\\s+(?:is|refers to|means|defines|represents)\\s+|\\s*:\\s*|\\s*-\\s*|\\s*=\\s*")

        for ((idx, line) in lines.withIndex()) {
            if (cards.size >= count) break
            val parts = line.split(splitRegex, limit = 2)
            if (parts.size == 2 && parts[0].length >= 3 && parts[1].length >= 5) {
                val q = "What is ${parts[0].trim()}?"
                val a = parts[1].trim().replaceFirstChar { it.uppercase() }
                cards.add(
                    Flashcard(
                        id = "heur_${System.currentTimeMillis()}_$idx",
                        front = q,
                        back = a,
                        topic = topicName,
                        tags = classifyTaxonomy(q, a)
                    )
                )
            }
        }

        // If not enough cards from delimiters, generate contextual high-yield prompts
        var extraIndex = 1
        while (cards.size < count) {
            val cardIdx = cards.size + 1
            val (front, back) = when (cardIdx % 6) {
                1 -> Pair(
                    "What is the foundational definition and primary role of $topicName?",
                    "It encompasses the core underlying principles, structural mechanisms, and primary real-world application."
                )
                2 -> Pair(
                    "What key mechanisms drive $topicName?",
                    "Key pathways, state transitions, and environmental interactions regulate how $topicName functions dynamically."
                )
                3 -> Pair(
                    "What are the critical variables and formulas associated with $topicName?",
                    "Core equations measure rate of change, equilibrium balance, and boundary efficiency."
                )
                4 -> Pair(
                    "Explain the primary advantages and constraints of $topicName.",
                    "Strengths include high efficiency and scalability, while constraints involve edge-case dependencies and input sensitivity."
                )
                5 -> Pair(
                    "What are the most common exam questions and clinical/technical nuances for $topicName?",
                    "Focus on distinguishing differential factors, reciprocal interactions, and high-yield edge cases."
                )
                else -> Pair(
                    "How does $topicName integrate into broader systems and practical workflows?",
                    "It acts as a vital bridge connecting structural components with end-to-end systemic performance."
                )
            }

            cards.add(
                Flashcard(
                    id = "heur_${System.currentTimeMillis()}_extra_${extraIndex++}",
                    front = front,
                    back = back,
                    topic = topicName,
                    tags = classifyTaxonomy(front, back)
                )
            )
        }

        return cards.take(count)
    }

    private fun deriveTitleFromTopic(text: String): String {
        val firstLine = text.lineSequence().firstOrNull()?.trim() ?: "Study Topic"
        val clean = firstLine.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
        val words = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return if (words.size in 1..5) {
            words.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else if (words.size > 5) {
            words.take(4).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } + "..."
        } else {
            "Mastery Deck"
        }
    }
}
