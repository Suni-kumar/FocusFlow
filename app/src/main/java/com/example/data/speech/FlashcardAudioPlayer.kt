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
        val sample = customPhrase ?: when (prefsManager.voiceAccent) {
            "HINDI_IN" -> "नमस्ते! मैं आपका FocusFlow AI ट्यूटर हूँ। चलिए साथ मिलकर अध्ययन करते हैं।"
            "ENGLISH_IN" -> "Hello! I am your FocusFlow AI tutor. Ready for our study session?"
            "ENGLISH_UK" -> "Hello! I am your FocusFlow AI tutor. Let us begin our revision today."
            else -> "Hello! I am your FocusFlow AI tutor. Ready to master your cards today?"
        }

        stop()
        _currentText.value = sample

        val lang = resolveLocaleForAccent(prefsManager.voiceAccent, sample)
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
            // If no key or API failed, play optimized offline sample with distinct persona acoustic tuning
            _currentEngineType.value = "Offline Engine ($voiceName)"
            playWithLocalTts(sample, lang, voicePersona = voiceName)
        }
    }

    /**
     * Previews an accent or regional dialect explicitly.
     */
    fun previewAccent(accentId: String) {
        val sample = when (accentId) {
            "HINDI_IN" -> "नमस्ते! यह प्राकृतिक भारतीय हिन्दी उच्चारण है। क्या आप तैयार हैं?"
            "ENGLISH_IN" -> "Hello! This is natural Indian English accent for your study sessions."
            "ENGLISH_US" -> "Hello! This is standard American English pronunciation for your cards."
            "ENGLISH_UK" -> "Hello! This is British English pronunciation for your study deck."
            else -> "Hello! Auto-detect will switch between Hindi and English depending on your card text."
        }

        stop()
        _currentText.value = sample

        val lang = resolveLocaleForAccent(accentId, sample)
        val selectedVoice = prefsManager.geminiVoiceName
        scope.launch {
            val apiKey = getGeminiApiKey()
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                val geminiSuccess = tryGeminiTts(sample, lang, apiKey, selectedVoice, forcedAccent = accentId)
                _isLoading.value = false
                if (geminiSuccess) {
                    _currentEngineType.value = "Gemini Live ($selectedVoice • $accentId)"
                    return@launch
                }
            }
            _currentEngineType.value = "Offline Engine ($accentId)"
            playWithLocalTts(sample, lang, voicePersona = selectedVoice)
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
        return resolveLocaleForAccent(prefsManager.voiceAccent, text)
    }

    private fun resolveLocaleForAccent(accent: String, text: String): Locale {
        return when (accent) {
            "HINDI_IN" -> Locale.forLanguageTag("hi-IN")
            "ENGLISH_IN" -> Locale.forLanguageTag("en-IN")
            "ENGLISH_US" -> Locale.US
            "ENGLISH_UK" -> Locale.UK
            else -> detectLanguage(text)
        }
    }

    private fun playWithLocalTts(
        text: String,
        locale: Locale,
        voicePersona: String = prefsManager.geminiVoiceName
    ) {
        if (!isTtsReady || tts == null) {
            initTts()
        }

        tts?.let { engine ->
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }

            val baseSpeed = prefsManager.voiceSpeed
            val basePitch = prefsManager.voicePitch

            // Apply distinct Persona pitch & rate modifiers so each voice sounds unique even offline
            val (personaPitchMod, personaSpeedMod) = when (voicePersona.lowercase()) {
                "aoede" -> Pair(1.20f, 1.02f)      // High expressive melodic female
                "kore" -> Pair(1.04f, 0.90f)       // Soft gentle calm female
                "puck" -> Pair(1.10f, 1.14f)       // Fast upbeat youthful male
                "charon" -> Pair(0.72f, 0.88f)     // Deep authoritative baritone male
                "fenrir" -> Pair(0.88f, 0.98f)     // Balanced articulate STEM male
                else -> Pair(1.0f, 1.0f)
            }

            val finalPitch = (basePitch * personaPitchMod).coerceIn(0.5f, 2.0f)
            val finalRate = (baseSpeed * personaSpeedMod).coerceIn(0.5f, 2.0f)

            engine.setPitch(finalPitch)
            engine.setSpeechRate(finalRate)

            // Select best matching voice for locale and gender persona if available
            try {
                val matchingVoices = engine.voices?.filter { voice ->
                    voice.locale.language == locale.language && !voice.isNetworkConnectionRequired
                }
                if (!matchingVoices.isNullOrEmpty()) {
                    val isPersonaFemale = voicePersona.equals("Aoede", ignoreCase = true) || voicePersona.equals("Kore", ignoreCase = true)
                    val genderFiltered = matchingVoices.filter { v ->
                        val name = v.name.lowercase()
                        if (isPersonaFemale) name.contains("female") || name.contains("f0") || name.contains("-f-")
                        else name.contains("male") || name.contains("m0") || name.contains("-m-")
                    }
                    val selectedVoice = (genderFiltered.maxByOrNull { it.quality } ?: matchingVoices.maxByOrNull { it.quality })
                    if (selectedVoice != null) {
                        engine.voice = selectedVoice
                    }
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
        voiceName: String,
        forcedAccent: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val models = listOf(
            "gemini-2.0-flash",
            "gemini-2.0-flash-exp",
            "gemini-2.5-flash-preview-tts",
            "gemini-2.5-flash",
            "gemini-2.5-flash-native-audio-preview-12-2025"
        )

        val activeAccent = forcedAccent ?: prefsManager.voiceAccent

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val promptInstruction = when {
                    activeAccent == "HINDI_IN" || locale.language == "hi" ->
                        "You are a friendly tutor. Speak the following text clearly in natural, warm Indian Hindi pronunciation without spelling symbols or punctuation markers:\n$text"
                    activeAccent == "ENGLISH_IN" ->
                        "You are a tutor. Speak the following text in natural, clear Indian English accent:\n$text"
                    activeAccent == "ENGLISH_UK" ->
                        "You are a tutor. Read the following text in clear British English accent:\n$text"
                    else ->
                        "You are a friendly tutor. Read the following text in a clear, natural, expressive voice with human cadence:\n$text"
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
                                    val mimeType = inlineData.optString("mimeType", "audio/pcm;rate=24000")
                                    val base64Data = inlineData.optString("data")
                                    if (base64Data.isNotBlank()) {
                                        val rawBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                        val playableBytes = preparePlayableAudioBytes(rawBytes, mimeType)
                                        return@withContext playAudioBytes(playableBytes)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Model $model returned code ${response.code}: $responseStr")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Voice API attempt on $model failed: ${e.message}")
            }
        }
        return@withContext false
    }

    /**
     * Converts raw PCM or wav data into standard playable format with proper RIFF WAV header.
     */
    private fun preparePlayableAudioBytes(rawBytes: ByteArray, mimeType: String): ByteArray {
        if (rawBytes.size < 4) return rawBytes

        // If already has RIFF WAV header or MP3 header, return as-is
        val isWav = rawBytes[0] == 'R'.code.toByte() && rawBytes[1] == 'I'.code.toByte() && rawBytes[2] == 'F'.code.toByte() && rawBytes[3] == 'F'.code.toByte()
        val isMp3 = (rawBytes[0] == 0xFF.toByte() && (rawBytes[1].toInt() and 0xE0) == 0xE0) ||
                (rawBytes[0] == 'I'.code.toByte() && rawBytes[1] == 'D'.code.toByte() && rawBytes[2] == '3'.code.toByte())

        if (isWav || isMp3) {
            return rawBytes
        }

        // Parse sample rate from mimeType if present (e.g. "audio/pcm;rate=24000")
        var sampleRate = 24000
        try {
            val rateRegex = Regex("rate=(\\d+)")
            val match = rateRegex.find(mimeType)
            if (match != null) {
                sampleRate = match.groupValues[1].toIntOrNull() ?: 24000
            }
        } catch (e: Exception) {
            sampleRate = 24000
        }

        return pcmToWav(rawBytes, sampleRate = sampleRate, channels = 1, bitsPerSample = 16)
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int = 24000, channels: Int = 1, bitsPerSample: Int = 16): ByteArray {
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)

        // "RIFF"
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()

        // "WAVE"
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()

        // "fmt "
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Subchunk1Size (16 for PCM)
        header[20] = 1; header[21] = 0 // AudioFormat 1 = PCM
        header[22] = channels.toByte(); header[23] = 0 // NumChannels
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = ((channels * bitsPerSample) / 8).toByte(); header[33] = 0 // BlockAlign
        header[34] = bitsPerSample.toByte(); header[35] = 0 // BitsPerSample

        // "data"
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        val audioLen = pcmData.size
        header[40] = (audioLen and 0xff).toByte()
        header[41] = ((audioLen shr 8) and 0xff).toByte()
        header[42] = ((audioLen shr 16) and 0xff).toByte()
        header[43] = ((audioLen shr 24) and 0xff).toByte()

        val wavBytes = ByteArray(44 + pcmData.size)
        System.arraycopy(header, 0, wavBytes, 0, 44)
        System.arraycopy(pcmData, 0, wavBytes, 44, pcmData.size)
        return wavBytes
    }

    private suspend fun playAudioBytes(audioBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File.createTempFile("gemini_tts_", ".wav", appContext.cacheDir)
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
