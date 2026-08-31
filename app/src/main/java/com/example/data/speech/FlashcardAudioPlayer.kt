package com.example.data.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.preferences.UserPreferencesManager
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
 * Uses Gemini Official AI Voice API with conversational Live voices (Aoede, Kore, Puck, Charon, Fenrir)
 * when online/available, and high-fidelity Android TextToSpeech with smart multilingual accent detection
 * (Hindi/English/Regional) for 100% offline reliability.
 */
class FlashcardAudioPlayer private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val prefsManager = UserPreferencesManager(appContext)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentText = MutableStateFlow<String?>(null)
    val currentText: StateFlow<String?> = _currentText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentEngineType = MutableStateFlow("Offline Engine")
    val currentEngineType: StateFlow<String> = _currentEngineType.asStateFlow()

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
     * Uses Gemini Live Voice if API key is active and preferGeminiVoice is enabled,
     * otherwise smoothly falls back to high-grade local offline engine.
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

        val lang = resolveLocaleForText(cleanText)
        val selectedVoice = prefsManager.geminiVoiceName
        val preferGemini = prefsManager.isPreferGeminiVoice

        scope.launch {
            // Attempt high-fidelity Gemini Official Voice first if API key configured
            val apiKey = getGeminiApiKey()
            if (preferGemini && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                val geminiSuccess = tryGeminiTts(cleanText, lang, apiKey, selectedVoice)
                _isLoading.value = false
                if (geminiSuccess) {
                    _currentEngineType.value = "Gemini Live ($selectedVoice)"
                    return@launch
                }
            }

            // Fallback to optimized high-grade Local TTS engine with natural voice & accent tuning
            _currentEngineType.value = "Offline Engine"
            playWithLocalTts(cleanText, lang)
        }
    }

    /**
     * Previews a specific Gemini Voice persona with a demo sentence.
     */
    fun previewVoice(voiceName: String, customPhrase: String? = null) {
        val sample = customPhrase ?: if (prefsManager.voiceAccent == "HINDI_IN") {
            "नमस्ते! मैं आपका FocusFlow AI ट्यूटर हूँ। चलिए साथ मिलकर अध्ययन करते हैं।"
        } else {
            "Hello! I am your FocusFlow AI tutor. Ready to master your cards today?"
        }

        stop()
        _currentText.value = sample

        val lang = resolveLocaleForText(sample)
        scope.launch {
            val apiKey = getGeminiApiKey()
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                val geminiSuccess = tryGeminiTts(sample, lang, apiKey, voiceName)
                _isLoading.value = false
                if (geminiSuccess) {
                    _currentEngineType.value = "Gemini Live ($voiceName)"
                    return@launch
                }
            }
            // If no key, play offline sample with note
            playWithLocalTts(sample, lang)
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

    private fun resolveLocaleForText(text: String): Locale {
        return when (prefsManager.voiceAccent) {
            "HINDI_IN" -> Locale.forLanguageTag("hi-IN")
            "ENGLISH_IN" -> Locale.forLanguageTag("en-IN")
            "ENGLISH_US" -> Locale.US
            "ENGLISH_UK" -> Locale.UK
            else -> detectLanguage(text)
        }
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

            val userSpeed = prefsManager.voiceSpeed
            val userPitch = prefsManager.voicePitch

            // Tune natural prosody & human-like cadence
            if (locale.language == "hi") {
                engine.setPitch(userPitch * 1.02f)
                engine.setSpeechRate(userSpeed * 0.95f)
            } else {
                engine.setPitch(userPitch)
                engine.setSpeechRate(userSpeed * 0.98f)
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

    private suspend fun tryGeminiTts(
        text: String,
        locale: Locale,
        apiKey: String,
        voiceName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val models = listOf("gemini-2.5-flash-preview-tts", "gemini-2.5-flash")
        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val promptInstruction = if (locale.language == "hi" || prefsManager.voiceAccent == "HINDI_IN") {
                    "You are a friendly and clear tutor in a live conversation. Speak the following text clearly in a natural, warm Indian Hindi pronunciation without reading punctuation labels:\n$text"
                } else if (prefsManager.voiceAccent == "ENGLISH_IN") {
                    "You are a friendly tutor in a live study session. Speak the following text in a natural, warm Indian English accent:\n$text"
                } else {
                    "You are a friendly tutor in a live study session. Read the following text in a clear, natural, expressive voice with human cadence:\n$text"
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
                Log.w(TAG, "Gemini Voice API attempt on $model failed: ${e.message}")
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
                    
                    // Apply speed setting if supported
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val speed = prefsManager.voiceSpeed
                            if (speed != 1.0f) {
                                playbackParams = PlaybackParams().apply {
                                    this.speed = speed
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "PlaybackParams speed setup: ${e.message}")
                        }
                    }

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
        val userKey = prefsManager.customApiKey.trim()
        if (userKey.isNotBlank()) {
            return userKey
        }
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
