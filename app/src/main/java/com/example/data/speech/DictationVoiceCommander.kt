package com.example.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class DictationVoiceCommand {
    REPEAT,
    NEXT,
    PREVIOUS,
    SHOW_WORD,
    SAY_MEANING,
    CHECK_TIME,
    PAUSE,
    NONE
}

/**
 * Ultra-responsive Hands-Free Continuous Voice Engine for Dictation Practice.
 * Keeps listening continuously in the background so the user can place the phone on the desk
 * and write on paper/notebook. Includes 5-minute inactivity auto-sleep.
 */
class DictationVoiceCommander(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    @Volatile
    private var isListeningActive = false

    @Volatile
    private var isPausedTemporarily = false

    @Volatile
    private var lastAudioPlaybackFinishTime = 0L

    private var consecutiveErrors = 0

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _lastDetectedCommand = MutableStateFlow<DictationVoiceCommand>(DictationVoiceCommand.NONE)
    val lastDetectedCommand: StateFlow<DictationVoiceCommand> = _lastDetectedCommand.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val _isAsleepDueToInactivity = MutableStateFlow(false)
    val isAsleepDueToInactivity: StateFlow<Boolean> = _isAsleepDueToInactivity.asStateFlow()

    var onCommandRecognized: ((DictationVoiceCommand) -> Unit)? = null
    var onInactivityTimeout: (() -> Unit)? = null

    // Inactivity tracking (5 minutes = 300,000 ms)
    private var lastInteractionTime = System.currentTimeMillis()
    private val inactivityLimitMillis = 5 * 60 * 1000L
    private val inactivityScope = CoroutineScope(Dispatchers.Main)
    private var inactivityTimerJob: Job? = null
    private var watchdogJob: Job? = null

    init {
        initRecognizerOnMain()
    }

    private fun initRecognizerOnMain() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SpeechRecognizer initialization failed: ${e.message}")
            }
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            consecutiveErrors = 0
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
            resetInactivityTimer()
        }

        private var lastEmittedRms = 0f

        override fun onRmsChanged(rmsdB: Float) {
            val norm = (rmsdB.coerceIn(0f, 10f) / 10f)
            if (kotlin.math.abs(norm - lastEmittedRms) > 0.08f) {
                lastEmittedRms = norm
                _audioRmsLevel.value = norm
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            _audioRmsLevel.value = 0f
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _audioRmsLevel.value = 0f
            consecutiveErrors++
            Log.d(TAG, "Recognition error: $error (consecutive: $consecutiveErrors)")

            if (isListeningActive && !isPausedTemporarily && !_isAsleepDueToInactivity.value) {
                // If 3+ consecutive errors or fatal busy/client state, do a full reset
                if (consecutiveErrors >= 3 || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                    consecutiveErrors = 0
                    mainHandler.postDelayed({
                        initRecognizerOnMain()
                        mainHandler.postDelayed({ startListeningInternal() }, 250)
                    }, 350)
                } else {
                    // Quick seamless restart on no-match or timeout
                    mainHandler.postDelayed({
                        if (isListeningActive && !isPausedTemporarily) {
                            startListeningInternal()
                        }
                    }, 120)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioRmsLevel.value = 0f
            consecutiveErrors = 0
            resetInactivityTimer()

            // Echo guard: ignore results that finish within 350ms of audio player output
            val timeSinceAudio = System.currentTimeMillis() - lastAudioPlaybackFinishTime
            if (timeSinceAudio > 350) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spoken = matches[0].trim()
                    _lastRecognizedText.value = spoken
                    val command = parseSpokenCommand(spoken)
                    if (command != DictationVoiceCommand.NONE) {
                        _lastDetectedCommand.value = command
                        onCommandRecognized?.invoke(command)
                    }
                }
            }

            // Immediately continue listening for the next voice command
            if (isListeningActive && !isPausedTemporarily && !_isAsleepDueToInactivity.value) {
                mainHandler.postDelayed({
                    startListeningInternal()
                }, 100)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            resetInactivityTimer()
            val timeSinceAudio = System.currentTimeMillis() - lastAudioPlaybackFinishTime
            if (timeSinceAudio > 350) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spoken = matches[0].trim()
                    _lastRecognizedText.value = spoken
                    val command = parseSpokenCommand(spoken)
                    if (command != DictationVoiceCommand.NONE) {
                        _lastDetectedCommand.value = command
                        onCommandRecognized?.invoke(command)
                    }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Starts continuous hands-free listening with watchdog monitor.
     */
    fun startListening() {
        isListeningActive = true
        isPausedTemporarily = false
        _isAsleepDueToInactivity.value = false
        consecutiveErrors = 0
        resetInactivityTimer()
        startInactivityMonitor()
        startWatchdog()

        mainHandler.post {
            startListeningInternal()
        }
    }

    /**
     * Wakes up from 5-minute inactivity sleep.
     */
    fun wakeUp() {
        _isAsleepDueToInactivity.value = false
        resetInactivityTimer()
        startListening()
    }

    private fun startListeningInternal() {
        if (!isListeningActive || isPausedTemporarily || _isAsleepDueToInactivity.value) return

        try {
            if (speechRecognizer == null) {
                initRecognizerOnMain()
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                // Support multilingual hindi/english dictation
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.w(TAG, "Start listening internal error: ${e.message}")
            _isListening.value = false
        }
    }

    fun pauseTemporarilyForAudio() {
        isPausedTemporarily = true
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                // Ignored
            }
            _isListening.value = false
            _audioRmsLevel.value = 0f
        }
    }

    fun resumeAfterAudio() {
        lastAudioPlaybackFinishTime = System.currentTimeMillis()
        isPausedTemporarily = false
        if (isListeningActive && !_isAsleepDueToInactivity.value) {
            mainHandler.postDelayed({
                if (isListeningActive && !isPausedTemporarily) {
                    startListeningInternal()
                }
            }, 380) // 380ms acoustic room clearance delay to prevent self-trigger
        }
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = inactivityScope.launch {
            while (isActive && isListeningActive) {
                delay(3500) // Check health every 3.5 seconds
                if (isListeningActive && !isPausedTemporarily && !_isAsleepDueToInactivity.value && !_isListening.value) {
                    Log.d(TAG, "Watchdog detected inactive recognizer while active. Auto-recovering...")
                    mainHandler.post {
                        startListeningInternal()
                    }
                }
            }
        }
    }

    fun stopListening() {
        isListeningActive = false
        isPausedTemporarily = false
        inactivityTimerJob?.cancel()
        watchdogJob?.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.d(TAG, "Stop listening notice: ${e.message}")
            }
            _isListening.value = false
            _audioRmsLevel.value = 0f
        }
    }

    fun resetInactivityTimer() {
        lastInteractionTime = System.currentTimeMillis()
        if (_isAsleepDueToInactivity.value) {
            _isAsleepDueToInactivity.value = false
        }
    }

    private fun startInactivityMonitor() {
        inactivityTimerJob?.cancel()
        inactivityTimerJob = inactivityScope.launch {
            while (isActive && isListeningActive) {
                delay(2000) // Check every 2 seconds
                val elapsed = System.currentTimeMillis() - lastInteractionTime
                if (elapsed >= inactivityLimitMillis && !_isAsleepDueToInactivity.value) {
                    _isAsleepDueToInactivity.value = true
                    pauseTemporarilyForAudio()
                    onInactivityTimeout?.invoke()
                }
            }
        }
    }

    fun destroy() {
        stopListening()
        inactivityTimerJob?.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignored
            }
            speechRecognizer = null
        }
    }

    /**
     * Robust Multilingual Command Parser (Devanagari Hindi, Roman Hindi / Hinglish, English).
     */
    fun parseSpokenCommand(rawSpokenText: String): DictationVoiceCommand {
        val text = rawSpokenText.lowercase().trim()

        return when {
            // "Again" / "Repeat" / "दोबारा" / "फिर से" / "एक बार और" / "wapas" / "dobara" / "phir se" / "fir se"
            text.contains("again") || text.contains("repeat") || text.contains("phir se") ||
                    text.contains("fir se") || text.contains("dobara") || text.contains("wapas") ||
                    text.contains("one more time") || text.contains("once more") ||
                    text.contains("दोबारा") || text.contains("फिर से") || text.contains("फिर") ||
                    text.contains("एक बार और") || text.contains("वापस") || text.contains("रिपीट") -> {
                DictationVoiceCommand.REPEAT
            }

            // "Next" / "Continue" / "अगला" / "आगे" / "आगामी" / "chalo" / "agla" / "aage"
            text.contains("next") || text.contains("continue") || text.contains("aage") ||
                    text.contains("agla") || text.contains("chalo") || text.contains("forward") ||
                    text.contains("skip") || text.contains("अगला") || text.contains("आगे") ||
                    text.contains("चलो") || text.contains("नेक्स्ट") || text.contains("आगामी") -> {
                DictationVoiceCommand.NEXT
            }

            // "Previous" / "Back" / "पिछला" / "पीछे" / "pichla" / "peeche"
            text.contains("previous") || text.contains("back") || text.contains("peeche") ||
                    text.contains("pichla") || text.contains("last word") ||
                    text.contains("पिछला") || text.contains("पीछे") || text.contains("बैक") -> {
                DictationVoiceCommand.PREVIOUS
            }

            // "Show" / "Reveal" / "दिखाओ" / "कार्ड" / "dikhao" / "dekhna"
            text.contains("show") || text.contains("dikhao") || text.contains("reveal") ||
                    text.contains("dekhna") || text.contains("display") || text.contains("card") ||
                    text.contains("दिखाओ") || text.contains("शो") || text.contains("कार्ड") ||
                    text.contains("देखना") -> {
                DictationVoiceCommand.SHOW_WORD
            }

            // "Meaning" / "Matlab" / "मतलब" / "अर्थ" / "बताओ" / "samjhao" / "arth"
            text.contains("meaning") || text.contains("matlab") || text.contains("arth") ||
                    text.contains("samjhao") || text.contains("definition") || text.contains("define") ||
                    text.contains("मतलब") || text.contains("अर्थ") || text.contains("मीनिंग") ||
                    text.contains("समझाओ") || text.contains("बताओ") -> {
                DictationVoiceCommand.SAY_MEANING
            }

            // "Check" / "Checking time" / "समीक्षा" / "चेक" / "samiksha" / "result" / "khatam" / "finish" / "done"
            text.contains("checking time") || text.contains("check time") || text.contains("check") ||
                    text.contains("result") || text.contains("samiksha") || text.contains("finish") ||
                    text.contains("done") || text.contains("khatam") || text.contains("चेक") ||
                    text.contains("समीक्षा") || text.contains("खत्म") || text.contains("रिजल्ट") -> {
                DictationVoiceCommand.CHECK_TIME
            }

            // "Pause" / "Stop" / "रुकिए" / "रुको" / "शांत" / "shant" / "ruko" / "stop"
            text.contains("stop") || text.contains("pause") || text.contains("ruko") ||
                    text.contains("shant") || text.contains("hold") ||
                    text.contains("रुको") || text.contains("रुकिए") || text.contains("स्टॉप") ||
                    text.contains("शांत") -> {
                DictationVoiceCommand.PAUSE
            }

            else -> DictationVoiceCommand.NONE
        }
    }

    companion object {
        private const val TAG = "DictationVoiceCommander"
    }
}
