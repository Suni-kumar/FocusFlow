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
        input: FlashcardGenerationInput,
        userCustomApiKey: String? = null
    ): FlashcardGenerationResult = withContext(Dispatchers.IO) {
        val effectiveTopic = input.effectiveTopic.ifBlank {
            deriveTitleFromText(input.sourceText)
        }

        // Check if input has explicit Q&A structure. If so, source is authoritative.
        val hasExplicitQa = input.sourceText.isNotBlank() && OfflineFlashcardParser.hasExplicitQaStructure(input.sourceText)
        if (hasExplicitQa) {
            val extracted = OfflineFlashcardService.generate(input)
            if (extracted.isNotEmpty()) {
                val allTags = extracted.flatMap { it.tags }.distinct().take(4)
                val finalTitle = input.deckName.ifBlank { effectiveTopic }
                return@withContext FlashcardGenerationResult(
                    title = finalTitle,
                    description = "Authoritative extracted flashcard deck for $finalTitle (${extracted.size} cards).",
                    cards = extracted,
                    source = GenerationSource.OFFLINE_HEURISTIC,
                    tags = if (allTags.isNotEmpty()) allTags else listOf("#ActiveRecall", "#Extracted")
                )
            }
        }

        // 1. Dual-Tier Key Resolution
        val (apiKeyToUse, source) = resolveApiKey(userCustomApiKey)

        var warningNote: String? = null
        if (apiKeyToUse.isNotBlank() && apiKeyToUse != "MY_GEMINI_API_KEY") {
            try {
                val aiCards = callGeminiApi(input, effectiveTopic, apiKeyToUse)
                if (aiCards.isNotEmpty()) {
                    val allTags = aiCards.flatMap { it.tags }.distinct().take(5)
                    val derivedTitle = input.deckName.ifBlank { effectiveTopic }
                    return@withContext FlashcardGenerationResult(
                        title = derivedTitle,
                        description = "AI-generated active recall deck for $derivedTitle (${aiCards.size} cards).",
                        cards = aiCards,
                        source = source,
                        tags = if (allTags.isNotEmpty()) allTags else listOf("#AI_Generated", "#HighYield")
                    )
                }
            } catch (e: GeminiApiException) {
                Log.w("GeminiFlashcardService", "Gemini API explicit error: ${e.userFacingMessage}", e)
                if (source == GenerationSource.BYOK_CLIENT || e.httpCode in listOf(400, 401, 403, 429)) {
                    throw e
                }
                warningNote = e.userFacingMessage
            } catch (e: Exception) {
                if (source == GenerationSource.BYOK_CLIENT) {
                    throw GeminiApiException(500, "Gemini generation failed: ${e.message ?: "Network timeout"}")
                }
                warningNote = "Cloud AI synthesis unavailable. Offline deterministic engine applied."
                Log.w("GeminiFlashcardService", "AI generation failed, falling back to deterministic offline engine", e)
            }
        }

        // 3. Offline Deterministic Fallback
        val offlineCards = OfflineFlashcardService.generate(input)
        val finalTitle = input.deckName.ifBlank { effectiveTopic }

        if (offlineCards.isEmpty()) {
            throw GeminiApiException(
                400,
                "Could not find a clear question/answer structure in this text. Try adding Question/Answer or Front/Back labels, or provide a topic for generation."
            )
        }

        val fallbackTags = offlineCards.flatMap { it.tags }.distinct().take(4)
        FlashcardGenerationResult(
            title = finalTitle,
            description = "High-yield active recall deck for $finalTitle (${offlineCards.size} cards).",
            cards = offlineCards,
            source = GenerationSource.OFFLINE_HEURISTIC,
            tags = if (fallbackTags.isNotEmpty()) fallbackTags else listOf("#HighYield", "#ActiveRecall"),
            warningMessage = warningNote
        )
    }

    /**
     * Backward-compatible convenience wrapper for callers providing a raw string.
     */
    suspend fun generateFlashcards(
        topicOrPrompt: String,
        targetCardCount: Int,
        userCustomApiKey: String? = null
    ): FlashcardGenerationResult {
        val trimmed = topicOrPrompt.trim()
        val isExplicitQa = OfflineFlashcardParser.hasExplicitQaStructure(trimmed)
        val isMultiLineNotes = trimmed.contains("\n") && trimmed.length > 60

        val input = FlashcardGenerationInput(
            deckName = if (!isExplicitQa && !isMultiLineNotes) trimmed.take(50) else "",
            topic = if (!isExplicitQa && !isMultiLineNotes) trimmed else "",
            sourceText = if (isExplicitQa || isMultiLineNotes) trimmed else "",
            userInstructions = "",
            targetCardCount = targetCardCount
        )
        return generateFlashcards(input, userCustomApiKey)
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
        input: FlashcardGenerationInput,
        effectiveTopic: String,
        apiKey: String
    ): List<Flashcard> {
        val models = listOf("gemini-3.6-flash")
        var lastException: Exception? = null

        val systemPrompt = """
            You are a master academic educator, cognitive science specialist, and active recall author.
            Your task is to create high-yield, conceptually rigorous study flashcards.
            
            SEMANTIC ROLES OF INPUT FIELDS:
            1. DECK NAME: Organizational metadata. NEVER generate questions about the deck name.
            2. TOPIC: Defines the academic subject and boundary.
            3. SOURCE TEXT: Primary authoritative reference. When provided, ground cards strictly in this content.
            4. USER INSTRUCTIONS: Controls pedagogical style, difficulty level, target audience, or focus areas.
            
            STRICT QUALITY & PEDAGOGY RULES:
            - SOURCE FIRST: If the source contains explicit Front/Back, Q/A, or Question/Answer pairs, extract and preserve them faithfully.
            - NO MECHANICAL QUESTIONS: NEVER generate repetitive questions starting only with "What is...".
            - DIVERSE QUESTION ARCHETYPES: Appropriately vary between mechanisms ("How does..."), causal relationships ("Why does..."), distinctions/comparisons ("How does X differ from Y?"), key formulas/variables, boundary conditions, and practical applications.
            - NO STRUCTURAL ARTIFACTS: NEVER create questions about labels like "Front", "Back", "Question", "Answer", "Topic", or "Source Text".
            - PRESERVE MULTI-LINE COMPLETENESS: Complex answers, multi-step mechanisms, or list points must be fully captured on the card back.
            - STRICT JSON OUTPUT: Return ONLY a JSON array of objects or an object containing a "cards" array.
            
            JSON Object Structure:
            [
              {
                "front": "High-yield active recall prompt or question",
                "back": "Accurate, concise, and complete explanatory answer",
                "tags": ["#Tag1", "#Tag2"]
              }
            ]
        """.trimIndent()

        val userPromptBuilder = StringBuilder()
        if (input.deckName.isNotBlank()) {
            userPromptBuilder.append("DECK NAME (Organizational Metadata):\n${input.deckName.trim()}\n\n")
        }
        if (effectiveTopic.isNotBlank()) {
            userPromptBuilder.append("TOPIC (Scope & Subject):\n$effectiveTopic\n\n")
        }
        if (input.sourceText.isNotBlank()) {
            userPromptBuilder.append("SOURCE TEXT (Primary Reference Material):\n${input.sourceText.trim()}\n\n")
        }
        if (input.userInstructions.isNotBlank()) {
            userPromptBuilder.append("USER INSTRUCTIONS (Pedagogical Direction & Constraints):\n${input.userInstructions.trim()}\n\n")
        }
        userPromptBuilder.append("TARGET CARD COUNT: Generate up to ${input.targetCardCount} unique, high-yield flashcards adhering strictly to the system instructions.")

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().put("text", userPromptBuilder.toString()))
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
                    if (response.code in listOf(400, 401, 403)) {
                        throw GeminiApiException(response.code, "Invalid or unauthorized Gemini API Key. Please verify your key in Settings.")
                    } else if (response.code == 429) {
                        throw GeminiApiException(429, "Gemini API rate limit or quota exceeded. Free tier quota limit reached.")
                    }
                    continue
                }

                val candidates = parseGeminiResponseCandidates(responseBodyStr, effectiveTopic)
                val validatedCards = FlashcardValidator.filterAndDeduplicate(
                    candidates = candidates,
                    fallbackTopic = effectiveTopic,
                    targetCount = input.targetCardCount
                )

                if (validatedCards.isNotEmpty()) {
                    return validatedCards
                }
            } catch (e: Exception) {
                lastException = e
                Log.w("GeminiFlashcardService", "Attempt with $model failed: ${e.message}")
                if (e is GeminiApiException && (e.httpCode in listOf(400, 401, 403, 429))) {
                    throw e
                }
            }
        }

        if (lastException != null) {
            throw lastException
        }
        return emptyList()
    }

    private fun parseGeminiResponseCandidates(rawJson: String, topic: String): List<FlashcardCandidate> {
        val root = try {
            JSONObject(rawJson)
        } catch (e: Exception) {
            return emptyList()
        }

        val candidatesArr = root.optJSONArray("candidates") ?: return emptyList()
        if (candidatesArr.length() == 0) return emptyList()

        val candidateObj = candidatesArr.getJSONObject(0)
        val content = candidateObj.optJSONObject("content") ?: return emptyList()
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

        val jsonArray: JSONArray = try {
            if (text.startsWith("[")) {
                JSONArray(text)
            } else {
                val jsonObject = JSONObject(text)
                jsonObject.optJSONArray("cards")
                    ?: jsonObject.optJSONArray("flashcards")
                    ?: jsonObject.optJSONArray("items")
                    ?: JSONArray()
            }
        } catch (e: Exception) {
            // Find JSON array bounds
            val start = text.indexOf('[')
            val end = text.lastIndexOf(']')
            if (start != -1 && end != -1 && end > start) {
                try {
                    JSONArray(text.substring(start, end + 1))
                } catch (e2: Exception) {
                    return emptyList()
                }
            } else {
                return emptyList()
            }
        }

        val cardCandidates = mutableListOf<FlashcardCandidate>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            val front = (item.optString("front").takeIf { it.isNotBlank() }
                ?: item.optString("question").takeIf { it.isNotBlank() }
                ?: item.optString("term").takeIf { it.isNotBlank() }
                ?: item.optString("prompt").takeIf { it.isNotBlank() }
                ?: item.optString("q")).orEmpty().trim()

            val back = (item.optString("back").takeIf { it.isNotBlank() }
                ?: item.optString("answer").takeIf { it.isNotBlank() }
                ?: item.optString("definition").takeIf { it.isNotBlank() }
                ?: item.optString("explanation").takeIf { it.isNotBlank() }
                ?: item.optString("a")).orEmpty().trim()

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

            cardCandidates.add(
                FlashcardCandidate(
                    front = front,
                    back = back,
                    topic = topic,
                    tags = if (tagsList.isNotEmpty()) tagsList else classifyTaxonomy(front, back)
                )
            )
        }

        return cardCandidates
    }

    /**
     * Smart Regex Taxonomy Engine (Tag Classifier)
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

    private fun deriveTitleFromText(text: String): String {
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

