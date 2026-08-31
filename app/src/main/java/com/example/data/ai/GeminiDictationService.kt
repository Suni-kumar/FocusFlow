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

    suspend fun generateOrParseDictationDeck(
        inputContent: String,
        userCustomApiKey: String = "",
        deckTitleOverride: String? = null,
        targetWordCount: Int = 10
    ): DictationDeck = withContext(Dispatchers.IO) {
        val trimmedInput = inputContent.trim()
        val apiKeyToUse = if (userCustomApiKey.isNotBlank() && userCustomApiKey != "MY_GEMINI_API_KEY") {
            userCustomApiKey
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
                Log.w(TAG, "Gemini Dictation generation failed, using intelligent offline parser", e)
            }
        }

        // Offline Intelligent Heuristic Parser Fallback
        val parsedWords = fallbackOfflineParser(trimmedInput, targetWordCount)
        val title = deckTitleOverride?.takeIf { it.isNotBlank() } ?: "Dictation Deck: ${trimmedInput.take(24)}..."
        return@withContext DictationDeck(
            id = "dict_${UUID.randomUUID()}",
            title = title,
            description = "Custom Dictation Deck with ${parsedWords.size} words.",
            categoryColor = getRandomCategoryColor(),
            iconName = "RecordVoiceOver",
            words = parsedWords,
            tags = listOf("Custom", "Dictation"),
            isAiGenerated = false
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
            You are an expert language and dictation tutor. 
            The user will provide either a Topic (e.g. "Biology Cell Terms", "GRE Words") OR raw pasted text/vocabulary notes.
            Your task is to generate/extract a structured list of $targetCount rich words for dictation practice.
            For each word, provide:
            1. 'word': The exact clean word or short phrase.
            2. 'meaning': A clear, concise definition or translation (in English or Hindi-English if relevant).
            3. 'phonetic': Approximate IPA pronunciation (e.g., /ˌel.ə.kwənt/).
            4. 'exampleSentence': A natural, educational example sentence using the word.

            Output ONLY valid JSON matching this schema:
            [
              {
                "word": "Serendipity",
                "meaning": "Finding valuable things by happy accident or chance.",
                "phonetic": "/ˌser.ənˈdɪp.ə.ti/",
                "exampleSentence": "Discovering this quiet library was pure serendipity."
              }
            ]
        """.trimIndent()

        val prompt = "Generate or extract structured dictation words from this content: $input"

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
                        return words
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
                obj.optJSONArray("words") ?: obj.optJSONArray("items") ?: JSONArray()
            } else {
                JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val word = item.optString("word").trim()
                val meaning = item.optString("meaning").trim()
                val phonetic = item.optString("phonetic").trim()
                val example = item.optString("exampleSentence").trim()

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

    private fun fallbackOfflineParser(rawInput: String, targetCount: Int): List<DictationWord> {
        val lines = rawInput.split("\n", ";", "\r").map { it.trim() }.filter { it.isNotBlank() }
        val wordsList = mutableListOf<DictationWord>()

        for (line in lines) {
            if (line.contains(":") || line.contains("-") || line.contains("=")) {
                val delimiter = if (line.contains(":")) ":" else if (line.contains("-")) "-" else "="
                val parts = line.split(delimiter, limit = 2)
                val word = parts[0].trim()
                val meaning = parts.getOrNull(1)?.trim() ?: ""
                if (word.isNotBlank()) {
                    wordsList.add(
                        DictationWord(
                            id = "dw_${UUID.randomUUID()}",
                            word = word,
                            meaning = if (meaning.isNotBlank()) meaning else "Contextual meaning of $word"
                        )
                    )
                }
            } else {
                // Space separated or single words
                val tokens = line.split(",").map { it.trim() }.filter { it.isNotBlank() }
                for (token in tokens) {
                    if (token.isNotBlank()) {
                        wordsList.add(
                            DictationWord(
                                id = "dw_${UUID.randomUUID()}",
                                word = token,
                                meaning = "Meaning and spelling for $token"
                            )
                        )
                    }
                }
            }
            if (wordsList.size >= targetCount) break
        }

        if (wordsList.isEmpty()) {
            val words = rawInput.split("\\s+".toRegex()).filter { it.length > 2 }.take(targetCount)
            for (w in words) {
                wordsList.add(
                    DictationWord(
                        id = "dw_${UUID.randomUUID()}",
                        word = w.trim(),
                        meaning = "Pronunciation & dictation practice for $w"
                    )
                )
            }
        }

        return wordsList
    }

    private fun extractTitleFromTopicOrWords(input: String, words: List<DictationWord>): String {
        return if (input.length in 3..40 && !input.contains("\n")) {
            input.replaceFirstChar { it.uppercase() }
        } else if (words.isNotEmpty()) {
            "Chapter: ${words.first().word} & Vocabulary"
        } else {
            "Custom Dictation Deck"
        }
    }

    private fun getRandomCategoryColor(): Color {
        val colors = listOf(
            Color(0xFF4EDEA3),
            Color(0xFF818CF8),
            Color(0xFFFF7886),
            Color(0xFF06B6D4),
            Color(0xFFF59E0B),
            Color(0xFFEC4899)
        )
        return colors.random()
    }

    companion object {
        private const val TAG = "GeminiDictationService"
    }
}
