package com.example.data.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Natural, lifelike Speech & Audio Engine for Flashcards.
 * Uses Gemini Official AI Voice API when online/available, and high-fidelity
 * Android TextToSpeech with smart multilingual accent detection (Hindi/English/Japanese)
 * for instant, natural pronunciations.
 */
class FlashcardAudioPlayer private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentText = MutableStateFlow<String?>(null)
    val currentText: StateFlow<String?> = _currentText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentText.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentText.value = null
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _currentText.value = null
                    }
                })
            } else {
                Log.w(TAG, "Native TextToSpeech initialization failed with status $status")
            }
        }
    }

    /**
     * Reads out the text in natural, expressive cadence.
     * Detects Hindi (Devanagari & Hinglish) vs English vs Japanese accents automatically.
     */
    fun speak(text: String, onDone: () -> Unit = {}) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        // If clicking the same text that is currently speaking -> Toggle stop
        if (_isSpeaking.value && _currentText.value == cleanText) {
            stop()
            return
        }

        stop()
        _currentText.value = cleanText

        val lang = detectLanguage(cleanText)

        scope.launch {
            // Attempt high-fidelity Gemini Official Voice first if API key configured
            val apiKey = getGeminiApiKey()
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                val geminiSuccess = tryGeminiTts(cleanText, lang, apiKey)
                _isLoading.value = false
                if (geminiSuccess) {
                    return@launch
                }
            }

            // Fallback to optimized high-grade Local TTS engine with natural voice & accent tuning
            playWithLocalTts(cleanText, lang)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }

        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }

        _isSpeaking.value = false
        _isLoading.value = false
        _currentText.value = null
    }

    private fun playWithLocalTts(text: String, locale: Locale) {
        if (!isTtsReady || tts == null) {
            initTts()
        }

        tts?.let { engine ->
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }

            // Tune natural prosody & human-like cadence
            if (locale.language == "hi") {
                // Natural pitch for clear Hindi pronunciation
                engine.setPitch(1.02f)
                engine.setSpeechRate(0.95f) // Slightly relaxed speed for clear pronunciation
            } else {
                engine.setPitch(1.0f)
                engine.setSpeechRate(0.98f)
            }

            // Select highest quality voice if available
            try {
                val matchingVoices = engine.voices?.filter { voice ->
                    voice.locale.language == locale.language && !voice.isNetworkConnectionRequired
                }
                val bestVoice = matchingVoices?.maxByOrNull { it.quality }
                if (bestVoice != null) {
                    engine.voice = bestVoice
                }
            } catch (e: Exception) {
                Log.d(TAG, "Using default voice selector: ${e.message}")
            }

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "flashcard_speech_${System.currentTimeMillis()}")
            }

            _isSpeaking.value = true
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "flashcard_speech_${System.currentTimeMillis()}")
        }
    }

    private suspend fun tryGeminiTts(text: String, locale: Locale, apiKey: String): Boolean = withContext(Dispatchers.IO) {
        val models = listOf("gemini-2.5-flash-preview-tts", "gemini-2.5-flash")
        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                
                // Voice selection: "Aoede" / "Kore" for ultra natural delivery
                val voiceName = if (locale.language == "hi") "Kore" else "Aoede"

                val promptInstruction = if (locale.language == "hi") {
                    "कृपया निम्नलिखित पाठ को स्पष्ट, मधुर और प्राकृतिक भारतीय हिंदी उच्चारण में पढ़ें:\n$text"
                } else {
                    "Read the following text clearly in a natural, expressive, tutor voice:\n$text"
                }

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().put("text", promptInstruction))
                            }
                            put("parts", partsArray)
                        })
                    }
                    put("contents", contentsArray)

                    val generationConfig = JSONObject().apply {
                        val modalities = JSONArray().apply {
                            put("AUDIO")
                        }
                        put("responseModalities", modalities)

                        val speechConfig = JSONObject().apply {
                            val voiceConfig = JSONObject().apply {
                                val prebuiltVoiceConfig = JSONObject().apply {
                                    put("voiceName", voiceName)
                                }
                                put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                            }
                            put("voiceConfig", voiceConfig)
                        }
                        put("speechConfig", speechConfig)
                    }
                    put("generationConfig", generationConfig)
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseStr = response.body?.string().orEmpty()

                if (response.isSuccessful && responseStr.isNotBlank()) {
                    val respObj = JSONObject(responseStr)
                    val candidates = respObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val inlineData = part.optJSONObject("inlineData")
                                if (inlineData != null) {
                                    val mimeType = inlineData.optString("mimeType")
                                    val base64Data = inlineData.optString("data")
                                    if (base64Data.isNotBlank()) {
                                        val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                        return@withContext playAudioBytes(audioBytes, mimeType)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Voice API attempt on $model failed, trying fallback: ${e.message}")
            }
        }
        return@withContext false
    }

    private suspend fun playAudioBytes(audioBytes: ByteArray, mimeType: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File.createTempFile("gemini_tts_", ".audio", appContext.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioBytes)
            }

            withContext(Dispatchers.Main) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        _isSpeaking.value = true
                        start()
                    }
                    setOnCompletionListener {
                        _isSpeaking.value = false
                        _currentText.value = null
                        tempFile.delete()
                    }
                    setOnErrorListener { _, _, _ ->
                        _isSpeaking.value = false
                        _currentText.value = null
                        tempFile.delete()
                        false
                    }
                    prepareAsync()
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio bytes", e)
            return@withContext false
        }
    }

    private fun getGeminiApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val TAG = "FlashcardAudioPlayer"

        @Volatile
        private var INSTANCE: FlashcardAudioPlayer? = null

        fun getInstance(context: Context): FlashcardAudioPlayer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlashcardAudioPlayer(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Detects script and language to choose the most natural accent.
         */
        fun detectLanguage(text: String): Locale {
            // Check for Devanagari Unicode Block (Hindi, Marathi, Sanskrit, Nepali)
            val hasDevanagari = text.any { it.code in 0x0900..0x097F }
            if (hasDevanagari) {
                return Locale.forLanguageTag("hi-IN")
            }

            // Check for common Roman Hindi / Hinglish indicator words
            val lower = text.lowercase()
            val hinglishKeywords = listOf(
                " kya ", " hai ", " kyu ", " kaise ", " kahan ", " nahi ", " hota ", " hoti ", 
                " karte ", " kijiye ", " matlab ", " samjhaiye ", " arth ", " paribhasha "
            )
            if (hinglishKeywords.any { lower.contains(it) }) {
                return Locale.forLanguageTag("hi-IN")
            }

            // Japanese characters (Hiragana, Katakana, CJK Unified)
            val hasJapanese = text.any { it.code in 0x3040..0x30FF || it.code in 0x4E00..0x9FAF }
            if (hasJapanese) {
                return Locale.JAPANESE
            }

            return Locale.US
        }
    }
}
