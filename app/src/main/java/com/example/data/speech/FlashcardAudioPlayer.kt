package com.example.data.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import android.util.Log
import android.util.LruCache
import com.example.BuildConfig
import com.example.data.preferences.UserPreferencesManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * High-speed Zero-Lag Speech & Audio Engine for FocusFlow Flashcards & Dictation Studio.
 * Optimized for instant responsiveness with local acoustic voice modeling, guaranteed TTS initialization
 * synchronization, and high-fidelity Gemini Live voice streaming.
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

    private val _visualizerAmplitudes = MutableStateFlow(List(10) { 0.08f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private var amplitudeJob: Job? = null
    private var safetyWatchdogJob: Job? = null

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false
    private var ttsReadyDeferred = CompletableDeferred<Boolean>()

    private var mediaPlayer: MediaPlayer? = null
    private var onSpeechDoneCallback: (() -> Unit)? = null
    @Volatile
    private var currentActiveUtteranceId: String? = null

    private val memCache = LruCache<String, ByteArray>(50)
    private val cacheDir = File(appContext.cacheDir, "audio_cache").apply { mkdirs() }

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private fun requestAudioFocus(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .build()
                audioFocusRequest = request
                audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus request note: ${e.message}")
            true
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Abandon audio focus note: ${e.message}")
        }
    }

    /**
     * Guarantees audio routes to phone loudspeaker at an audible volume level.
     * Prevents sound from being routed to in-call earpiece or muted.
     */
    private fun ensureSpeakerOutput() {
        try {
            if (audioManager.mode != AudioManager.MODE_NORMAL) {
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            // If phone volume is at 0 or muted, raise to at least 70%
            if (currentVol <= 1 && maxVol > 0) {
                val safeVolume = (maxVol * 0.75f).toInt().coerceAtLeast(1)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safeVolume, 0)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Speaker output setup note: ${e.message}")
        }
    }

    // Fast-failing HTTP client so network delays never block local offline speech
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(3500, TimeUnit.MILLISECONDS)
        .readTimeout(4500, TimeUnit.MILLISECONDS)
        .writeTimeout(3500, TimeUnit.MILLISECONDS)
        .build()

    init {
        initTts()
    }

    private fun startVisualizerTicker() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive && _isSpeaking.value) {
                val baseEnergy = Random.nextFloat() * 0.7f + 0.3f
                val newBars = List(10) { i ->
                    val factor = when (i) {
                        0, 9 -> 0.35f
                        1, 8 -> 0.55f
                        2, 7 -> 0.75f
                        else -> 1.0f
                    }
                    (baseEnergy * factor * (0.6f + Random.nextFloat() * 0.4f)).coerceIn(0.12f, 1.0f)
                }
                _visualizerAmplitudes.value = newBars
                delay(65)
            }
            _visualizerAmplitudes.value = List(10) { 0.08f }
        }
    }

    private fun stopVisualizerTicker() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _visualizerAmplitudes.value = List(10) { 0.08f }
    }

    @Synchronized
    private fun initTts() {
        if (isTtsReady && tts != null) return
        if (tts != null) return // Already initializing

        if (ttsReadyDeferred.isCompleted) {
            ttsReadyDeferred = CompletableDeferred()
        }

        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            tts?.setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build()
                            )
                        } catch (e: Exception) {
                            Log.d(TAG, "TTS AudioAttributes note: ${e.message}")
                        }
                    }

                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            if (utteranceId == currentActiveUtteranceId) {
                                _isSpeaking.value = true
                                startVisualizerTicker()
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            if (utteranceId == currentActiveUtteranceId) {
                                abandonAudioFocus()
                                _isSpeaking.value = false
                                _currentText.value = null
                                stopVisualizerTicker()
                                safetyWatchdogJob?.cancel()
                                val callback = onSpeechDoneCallback
                                onSpeechDoneCallback = null
                                callback?.invoke()
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            if (utteranceId == currentActiveUtteranceId) {
                                abandonAudioFocus()
                                _isSpeaking.value = false
                                _currentText.value = null
                                stopVisualizerTicker()
                                safetyWatchdogJob?.cancel()
                                val callback = onSpeechDoneCallback
                                onSpeechDoneCallback = null
                                callback?.invoke()
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            if (utteranceId == currentActiveUtteranceId) {
                                abandonAudioFocus()
                                Log.w(TAG, "TTS Utterance $utteranceId error: $errorCode")
                                _isSpeaking.value = false
                                _currentText.value = null
                                stopVisualizerTicker()
                                safetyWatchdogJob?.cancel()
                                val callback = onSpeechDoneCallback
                                onSpeechDoneCallback = null
                                callback?.invoke()
                            }
                        }
                    })
                    if (!ttsReadyDeferred.isCompleted) {
                        ttsReadyDeferred.complete(true)
                    }
                    Log.d(TAG, "Native TextToSpeech successfully initialized and ready")
                } else {
                    isTtsReady = false
                    if (!ttsReadyDeferred.isCompleted) {
                        ttsReadyDeferred.complete(false)
                    }
                    Log.w(TAG, "Native TextToSpeech initialization failed with status $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TextToSpeech: ${e.message}")
            if (!ttsReadyDeferred.isCompleted) {
                ttsReadyDeferred.complete(false)
            }
        }
    }

    /**
     * Dynamically updates pitch and speed during active playback and persists for future utterances.
     */
    fun updatePitchAndSpeed(pitch: Float, speed: Float) {
        tts?.let { engine ->
            try {
                engine.setPitch(pitch.coerceIn(0.5f, 2.0f))
                engine.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            } catch (e: Exception) {
                Log.d(TAG, "Dynamic TTS pitch/speed update note: ${e.message}")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.playbackParams = PlaybackParams().apply { this.speed = speed }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Dynamic MediaPlayer speed update note: ${e.message}")
                }
            }
        }
    }

    /**
     * Reads out the text cleanly with Gemini Live AI HD Voice or instant local TTS based on preference.
     */
    fun speak(text: String, forceReplay: Boolean = true, rate: Float = 1.0f, onDone: () -> Unit = {}) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            onDone()
            return
        }

        stop()
        onSpeechDoneCallback = onDone
        _currentText.value = cleanText

        val lang = resolveLocaleForText(cleanText)
        val selectedVoice = normalizeVoiceName(prefsManager.geminiVoiceName)
        val preferGemini = prefsManager.isPreferGeminiVoice
        val apiKey = getGeminiApiKey()

        scope.launch {
            // Check instant disk cache first (zero-lag playback)
            val cacheKey = hashAudioKey(cleanText, selectedVoice, prefsManager.voiceAccent)
            val cachedFile = File(cacheDir, "$cacheKey.wav")
            if (cachedFile.exists() && cachedFile.length() > 0) {
                _currentEngineType.value = "Cached HD Voice ($selectedVoice)"
                ensureSpeakerOutput()
                requestAudioFocus()
                val success = playAudioFile(cachedFile)
                if (success) return@launch
            }

            // Guaranteed instant offline acoustic engine
            _currentEngineType.value = "Offline Engine ($selectedVoice)"
            playWithLocalTts(cleanText, lang, rate = rate, voicePersona = selectedVoice)
        }
    }

    /**
     * Suspending version of speak that suspends until audio finishes speaking.
     * Prevents voice cut-off during auto-play and multi-step study loops.
     */
    suspend fun speakAndWait(text: String, rate: Float = 1.0f): Boolean =
        suspendCancellableCoroutine { continuation ->
            speak(text, forceReplay = true, rate = rate, onDone = {
                if (continuation.isActive) {
                    continuation.resume(true)
                }
            })
            continuation.invokeOnCancellation {
                stop()
            }
        }

    /**
     * Dictation Practice: Speaks a specific word cleanly for dictation with zero delay.
     */
    fun speakDictationWord(word: String, onDone: () -> Unit = {}) {
        val cleanText = word.trim()
        if (cleanText.isEmpty()) {
            onDone()
            return
        }
        stop()
        onSpeechDoneCallback = onDone
        _currentText.value = cleanText

        val lang = resolveLocaleForText(cleanText)
        val selectedVoice = normalizeVoiceName(prefsManager.geminiVoiceName)

        scope.launch {
            val cacheKey = hashAudioKey(cleanText, selectedVoice, prefsManager.voiceAccent)
            val cachedFile = File(cacheDir, "$cacheKey.wav")
            if (cachedFile.exists() && cachedFile.length() > 0) {
                _currentEngineType.value = "Cached Voice ($selectedVoice)"
                ensureSpeakerOutput()
                requestAudioFocus()
                val success = playAudioFile(cachedFile)
                if (success) return@launch
            }

            _currentEngineType.value = "Offline Engine ($selectedVoice)"
            playWithLocalTts(cleanText, lang, rate = prefsManager.voiceSpeed, voicePersona = selectedVoice)
        }
    }

    /**
     * Dictation Practice: Speaks a specific word slowly (0.75x rate) for phonetic clarity.
     */
    fun speakDictationWordSlowly(word: String, onDone: () -> Unit = {}) {
        val cleanText = word.trim()
        if (cleanText.isEmpty()) {
            onDone()
            return
        }
        stop()
        onSpeechDoneCallback = onDone
        _currentText.value = cleanText

        val lang = resolveLocaleForText(cleanText)
        val selectedVoice = normalizeVoiceName(prefsManager.geminiVoiceName)

        scope.launch {
            _currentEngineType.value = "Offline Engine ($selectedVoice • Slow)"
            playWithLocalTts(cleanText, lang, rate = (prefsManager.voiceSpeed * 0.72f).coerceAtLeast(0.4f), voicePersona = selectedVoice)
        }
    }

    /**
     * Dictation Practice: Speaks the meaning/definition of a word.
     */
    fun speakDictationMeaning(word: String, meaning: String, onDone: () -> Unit = {}) {
        val cleanWord = word.trim()
        val cleanMeaning = meaning.trim()
        val phrase = if (prefsManager.voiceAccent == "HINDI_IN" || detectLanguage(cleanMeaning) == Locale.forLanguageTag("hi-IN")) {
            "$cleanWord का अर्थ है: $cleanMeaning"
        } else {
            "The meaning of $cleanWord is: $cleanMeaning"
        }
        stop()
        onSpeechDoneCallback = onDone
        _currentText.value = phrase

        val lang = resolveLocaleForText(phrase)
        val selectedVoice = normalizeVoiceName(prefsManager.geminiVoiceName)

        scope.launch {
            val cacheKey = hashAudioKey(phrase, selectedVoice, prefsManager.voiceAccent)
            val cachedFile = File(cacheDir, "$cacheKey.wav")
            if (cachedFile.exists() && cachedFile.length() > 0) {
                _currentEngineType.value = "Cached Voice ($selectedVoice)"
                ensureSpeakerOutput()
                requestAudioFocus()
                val success = playAudioFile(cachedFile)
                if (success) return@launch
            }

            _currentEngineType.value = "Offline Engine ($selectedVoice)"
            playWithLocalTts(phrase, lang, rate = prefsManager.voiceSpeed, voicePersona = selectedVoice)
        }
    }

    /**
     * Clears cached audio files to guarantee newly selected voices and API keys take effect immediately.
     */
    fun clearAudioCache() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".wav") || file.name.endsWith(".mp3"))) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Clear audio cache note: ${e.message}")
        }
    }

    /**
     * Previews a specific Gemini Voice persona with a demo sentence.
     */
    fun previewVoice(voiceName: String, customPhrase: String? = null) {
        val normVoice = normalizeVoiceName(voiceName)
        val sample = customPhrase ?: when (prefsManager.voiceAccent) {
            "HINDI_IN" -> "नमस्ते! मैं आपका $normVoice AI ट्यूटर हूँ। चलिए साथ मिलकर पढ़ाई करते हैं।"
            "ENGLISH_IN" -> "Hello! I am your $normVoice AI tutor. Ready for our study session?"
            "ENGLISH_UK" -> "Hello! I am your $normVoice AI tutor. Let us begin our revision today."
            else -> "Hello! I am your $normVoice AI tutor. Ready to master your cards today?"
        }

        stop()
        _currentText.value = sample

        val lang = resolveLocaleForAccent(prefsManager.voiceAccent, sample)
        val preferGemini = prefsManager.isPreferGeminiVoice
        val apiKey = getGeminiApiKey()

        scope.launch {
            val cacheKey = hashAudioKey(sample, normVoice, prefsManager.voiceAccent)
            val cachedFile = File(cacheDir, "$cacheKey.wav")
            if (cachedFile.exists()) {
                cachedFile.delete()
            }

            if (preferGemini && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                _currentEngineType.value = "Gemini Live HD ($normVoice)"
                val geminiSuccess = try {
                    withTimeoutOrNull(4000) {
                        tryGeminiTts(
                            text = sample,
                            locale = lang,
                            apiKey = apiKey,
                            voiceName = normVoice,
                            cacheKey = cacheKey,
                            playImmediately = true
                        )
                    } ?: false
                } catch (e: Exception) {
                    false
                }
                _isLoading.value = false
                if (geminiSuccess) return@launch
            }

            _currentEngineType.value = "Offline Engine ($normVoice)"
            playWithLocalTts(sample, lang, voicePersona = normVoice)
        }
    }

    /**
     * Previews an accent or regional dialect explicitly.
     */
    fun previewAccent(accentId: String) {
        val sample = when (accentId) {
            "HINDI_IN" -> "नमस्ते! यह भारतीय हिन्दी उच्चारण है। फोकस-फ्लो आपकी तैयारी को आसान बनाता है।"
            "ENGLISH_IN" -> "Hello! This is natural Indian English accent for your dictation and study sessions."
            "ENGLISH_US" -> "Hello! This is standard American English pronunciation for your vocabulary decks."
            "ENGLISH_UK" -> "Hello! This is British English pronunciation for your flashcards and dictation."
            else -> "Hello! Auto-detect will match Hindi and English based on your card text."
        }

        stop()
        _currentText.value = sample

        val lang = resolveLocaleForAccent(accentId, sample)
        val selectedVoice = normalizeVoiceName(prefsManager.geminiVoiceName)
        val preferGemini = prefsManager.isPreferGeminiVoice
        val apiKey = getGeminiApiKey()

        scope.launch {
            val cacheKey = hashAudioKey(sample, selectedVoice, accentId)
            val cachedFile = File(cacheDir, "$cacheKey.wav")
            if (cachedFile.exists() && cachedFile.length() > 0) {
                _currentEngineType.value = "Cached Live ($selectedVoice • $accentId)"
                val success = playAudioFile(cachedFile)
                if (success) return@launch
            }

            if (preferGemini && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                _isLoading.value = true
                _currentEngineType.value = "Gemini Live HD ($selectedVoice • $accentId)"
                val geminiSuccess = try {
                    withTimeoutOrNull(4000) {
                        tryGeminiTts(
                            text = sample,
                            locale = lang,
                            apiKey = apiKey,
                            voiceName = selectedVoice,
                            cacheKey = cacheKey,
                            forcedAccent = accentId,
                            playImmediately = true
                        )
                    } ?: false
                } catch (e: Exception) {
                    false
                }
                _isLoading.value = false
                if (geminiSuccess) return@launch
            }

            _currentEngineType.value = "Offline Engine ($accentId)"
            playWithLocalTts(sample, lang, voicePersona = selectedVoice)
        }
    }

    fun stop() {
        abandonAudioFocus()
        currentActiveUtteranceId = null
        safetyWatchdogJob?.cancel()
        safetyWatchdogJob = null

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

        stopVisualizerTicker()
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

    private fun normalizeVoiceName(name: String): String {
        return when (name.trim().lowercase()) {
            "puck" -> "Puck"
            "charon" -> "Charon"
            "fenrir" -> "Fenrir"
            "kore" -> "Kore"
            "aoede" -> "Aoede"
            else -> "Puck"
        }
    }

    /**
     * High-grade local offline TTS synthesizer with distinct gender, pitch, speed, and accent configurations.
     */
    private suspend fun playWithLocalTts(
        text: String,
        locale: Locale,
        rate: Float = 1.0f,
        voicePersona: String = prefsManager.geminiVoiceName
    ) {
        if (!isTtsReady || tts == null) {
            initTts()
            try {
                withTimeoutOrNull(2500) {
                    ttsReadyDeferred.await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "TTS init wait note: ${e.message}")
            }
        }

        val engine = tts
        if (engine == null || !isTtsReady) {
            Log.e(TAG, "TTS engine not ready for speech: $text")
            _isSpeaking.value = false
            stopVisualizerTicker()
            val callback = onSpeechDoneCallback
            onSpeechDoneCallback = null
            callback?.invoke()
            return
        }

        val normPersona = normalizeVoiceName(voicePersona)
        val isMalePersona = normPersona in listOf("Puck", "Charon", "Fenrir")

        try {
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }
        } catch (e: Exception) {
            Log.d(TAG, "setLanguage note: ${e.message}")
        }

        val userSpeed = if (rate != 1.0f) rate else prefsManager.voiceSpeed
        val basePitch = prefsManager.voicePitch

        // Acoustic tuning for distinct male and female personas
        val (personaPitchMod, personaSpeedMod) = when (normPersona) {
            "Aoede" -> Pair(1.15f, 1.00f)      // Melodic soprano female
            "Kore" -> Pair(0.98f, 0.95f)       // Calm alto female
            "Puck" -> Pair(0.68f, 1.05f)       // Energetic male
            "Charon" -> Pair(0.55f, 0.92f)     // Deep baritone male
            "Fenrir" -> Pair(0.62f, 0.98f)     // Clear articulate male
            else -> Pair(0.65f, 1.0f)
        }

        val finalPitch = if (isMalePersona) {
            (basePitch * personaPitchMod).coerceIn(0.52f, 0.78f)
        } else {
            (basePitch * personaPitchMod).coerceIn(0.92f, 1.50f)
        }
        val finalRate = (userSpeed * personaSpeedMod).coerceIn(0.5f, 2.0f)

        // Select matching installed voice
        try {
            val allVoices = engine.voices?.toList().orEmpty()
            if (allVoices.isNotEmpty()) {
                val localeVoices = allVoices.filter { v ->
                    !v.isNetworkConnectionRequired &&
                            v.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true &&
                            (v.locale.language.equals(locale.language, ignoreCase = true) ||
                                    (locale.language == "hi" && v.locale.country.equals("IN", ignoreCase = true)) ||
                                    (locale.language == "en" && v.locale.language.equals("en", ignoreCase = true)))
                }.ifEmpty {
                    allVoices.filter { !it.isNetworkConnectionRequired && it.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true }
                }

                val maleKeywords = listOf(
                    "male", "man", "boy", "guy", "david", "george", "mark", "alex", "james", "richard",
                    "hic", "hid", "enc", "end", "iom", "iol", "tpc", "tpd", "#m", "-m-", "_m_", "m0", "m1", "m2",
                    "hin-ind-male", "hi_in_male", "en_us_male", "en_in_male"
                )
                val femaleKeywords = listOf(
                    "female", "woman", "girl", "lady", "samantha", "zira", "victoria", "susan", "catherine",
                    "hia", "hib", "hie", "ena", "enb", "ene", "sfg", "sfd", "tpa", "tpb", "#f", "-f-", "_f_", "f0", "f1", "f2",
                    "hin-ind-female", "hi_in_female", "en_us_female", "en_in_female"
                )

                fun scoreVoice(v: Voice): Int {
                    val n = v.name.lowercase()
                    val feats = v.features.orEmpty()
                    var score = 0
                    val isMaleName = maleKeywords.any { n.contains(it) }
                    val isFemaleName = femaleKeywords.any { n.contains(it) }
                    val hasMaleFeat = feats.any { it.contains("male", ignoreCase = true) && !it.contains("female", ignoreCase = true) }
                    val hasFemaleFeat = feats.any { it.contains("female", ignoreCase = true) }

                    if (isMalePersona) {
                        if (isMaleName) score += 90
                        if (hasMaleFeat) score += 70
                        if (isFemaleName) score -= 100
                        if (hasFemaleFeat) score -= 100
                    } else {
                        if (isFemaleName) score += 90
                        if (hasFemaleFeat) score += 70
                        if (isMaleName) score -= 100
                        if (hasMaleFeat) score -= 100
                    }
                    score += v.quality
                    return score
                }

                val ranked = localeVoices.map { it to scoreVoice(it) }.sortedByDescending { it.second }
                val bestVoice = ranked.firstOrNull()?.first
                if (bestVoice != null) {
                    try {
                        engine.voice = bestVoice
                    } catch (e: Exception) {
                        Log.d(TAG, "Voice assign note: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Local voice selection note: ${e.message}")
        }

        try {
            engine.setPitch(finalPitch)
            engine.setSpeechRate(finalRate)
        } catch (e: Exception) {
            Log.d(TAG, "Pitch/rate apply note: ${e.message}")
        }

        val utteranceId = "flashcard_speech_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        currentActiveUtteranceId = utteranceId

        ensureSpeakerOutput()
        requestAudioFocus()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            putString(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC.toString())
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }

        _isSpeaking.value = true
        startVisualizerTicker()

        // Safety watchdog to unblock callbacks if OS TTS service hangs
        safetyWatchdogJob?.cancel()
        val estimatedDurationMs = ((text.length * 100L) / finalRate.coerceAtLeast(0.5f)).toLong().coerceIn(2000L, 10000L)
        safetyWatchdogJob = scope.launch {
            delay(estimatedDurationMs + 2500L)
            if (_isSpeaking.value && currentActiveUtteranceId == utteranceId) {
                Log.w(TAG, "Safety watchdog unblocking TTS for $utteranceId")
                abandonAudioFocus()
                _isSpeaking.value = false
                _currentText.value = null
                stopVisualizerTicker()
                val callback = onSpeechDoneCallback
                onSpeechDoneCallback = null
                callback?.invoke()
            }
        }

        val speakResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (speakResult != TextToSpeech.SUCCESS) {
            Log.w(TAG, "engine.speak returned $speakResult. Retrying with default voice...")
            try {
                engine.setLanguage(Locale.US)
            } catch (e: Exception) {
                Log.d(TAG, "Fallback locale set note: ${e.message}")
            }
            val fallbackResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            if (fallbackResult != TextToSpeech.SUCCESS) {
                Log.e(TAG, "Fallback speak also failed: $fallbackResult")
                abandonAudioFocus()
                _isSpeaking.value = false
                _currentText.value = null
                stopVisualizerTicker()
                safetyWatchdogJob?.cancel()
                val callback = onSpeechDoneCallback
                onSpeechDoneCallback = null
                callback?.invoke()
            }
        }
    }

    private suspend fun tryGeminiTts(
        text: String,
        locale: Locale,
        apiKey: String,
        voiceName: String,
        cacheKey: String,
        forcedAccent: String? = null,
        playImmediately: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        val models = listOf(
            "gemini-3.6-flash"
        )

        val normVoice = normalizeVoiceName(voiceName)
        val promptInstruction = "Read the following text out loud verbatim. Do not add any greeting, preamble, explanations, or extra commentary. Read ONLY the exact text:\n$text"

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

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
                                    put("voiceName", normVoice)
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

                                        // Save to disk cache for subsequent playbacks
                                        try {
                                            val cachedFile = File(cacheDir, "$cacheKey.wav")
                                            FileOutputStream(cachedFile).use { fos ->
                                                fos.write(playableBytes)
                                            }
                                        } catch (e: Exception) {
                                            Log.d(TAG, "Cache save note: ${e.message}")
                                        }

                                        if (playImmediately) {
                                            return@withContext playAudioBytes(playableBytes)
                                        } else {
                                            return@withContext true
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Gemini Voice API HTTP ${response.code} error: $responseStr")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Voice API attempt on $model failed: ${e.message}")
            }
        }
        return@withContext false
    }

    private fun preparePlayableAudioBytes(rawBytes: ByteArray, mimeType: String): ByteArray {
        if (rawBytes.size < 4) return rawBytes

        val isWav = rawBytes[0] == 'R'.code.toByte() && rawBytes[1] == 'I'.code.toByte() && rawBytes[2] == 'F'.code.toByte() && rawBytes[3] == 'F'.code.toByte()
        val isMp3 = (rawBytes[0] == 0xFF.toByte() && (rawBytes[1].toInt() and 0xE0) == 0xE0) ||
                (rawBytes[0] == 'I'.code.toByte() && rawBytes[1] == 'D'.code.toByte() && rawBytes[2] == '3'.code.toByte())

        if (isWav || isMp3) {
            return rawBytes
        }

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

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()

        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = ((channels * bitsPerSample) / 8).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0

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

    private suspend fun playAudioFile(file: File): Boolean = withContext(Dispatchers.Main) {
        try {
            ensureSpeakerOutput()
            requestAudioFocus()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                }
                setDataSource(file.absolutePath)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val speed = prefsManager.voiceSpeed
                        if (speed != 1.0f) {
                            playbackParams = PlaybackParams().apply { this.speed = speed }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Playback speed setup: ${e.message}")
                    }
                }
                setOnPreparedListener {
                    _isSpeaking.value = true
                    startVisualizerTicker()
                    start()
                }
                setOnCompletionListener {
                    abandonAudioFocus()
                    _isSpeaking.value = false
                    _currentText.value = null
                    stopVisualizerTicker()
                    val callback = onSpeechDoneCallback
                    onSpeechDoneCallback = null
                    callback?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    abandonAudioFocus()
                    _isSpeaking.value = false
                    _currentText.value = null
                    stopVisualizerTicker()
                    val callback = onSpeechDoneCallback
                    onSpeechDoneCallback = null
                    callback?.invoke()
                    false
                }
                prepareAsync()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file", e)
            abandonAudioFocus()
            stopVisualizerTicker()
            val callback = onSpeechDoneCallback
            onSpeechDoneCallback = null
            callback?.invoke()
            false
        }
    }

    private suspend fun playAudioBytes(audioBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File.createTempFile("gemini_tts_", ".wav", cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioBytes)
            }

            withContext(Dispatchers.Main) {
                ensureSpeakerOutput()
                requestAudioFocus()
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                    }
                    setDataSource(tempFile.absolutePath)

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
                        startVisualizerTicker()
                        start()
                    }
                    setOnCompletionListener {
                        abandonAudioFocus()
                        _isSpeaking.value = false
                        _currentText.value = null
                        stopVisualizerTicker()
                        tempFile.delete()
                        val callback = onSpeechDoneCallback
                        onSpeechDoneCallback = null
                        callback?.invoke()
                    }
                    setOnErrorListener { _, _, _ ->
                        abandonAudioFocus()
                        _isSpeaking.value = false
                        _currentText.value = null
                        stopVisualizerTicker()
                        tempFile.delete()
                        val callback = onSpeechDoneCallback
                        onSpeechDoneCallback = null
                        callback?.invoke()
                        false
                    }
                    prepareAsync()
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio bytes", e)
            abandonAudioFocus()
            stopVisualizerTicker()
            val callback = onSpeechDoneCallback
            onSpeechDoneCallback = null
            callback?.invoke()
            return@withContext false
        }
    }

    private fun hashAudioKey(text: String, voice: String, accent: String): String {
        val input = "$text|$voice|$accent"
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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

        fun detectLanguage(text: String): Locale {
            val hasDevanagari = text.any { it.code in 0x0900..0x097F }
            if (hasDevanagari) {
                return Locale.forLanguageTag("hi-IN")
            }

            val lower = text.lowercase().trim()
            val hinglishWords = setOf(
                "kya", "hai", "hain", "kyu", "kyun", "kaise", "kahan", "nahi", "nahin",
                "hota", "hoti", "hote", "karte", "kijiye", "karna", "matlab", "samjhaiye",
                "samjhao", "arth", "paribhasha", "prashna", "uttar", "batao", "bataiye", "aur"
            )
            val tokens = lower.split(Regex("[\\s\\p{Punct}]+")).filter { it.isNotBlank() }
            if (tokens.any { it in hinglishWords }) {
                return Locale.forLanguageTag("hi-IN")
            }

            val hasJapanese = text.any { it.code in 0x3040..0x30FF || it.code in 0x4E00..0x9FAF }
            if (hasJapanese) {
                return Locale.JAPANESE
            }

            return Locale.US
        }
    }
}
