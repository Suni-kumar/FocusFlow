package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sepfol_focusflow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_BRIGHTNESS_MODE = "brightness_mode"
        private const val KEY_GRID_COLS = "grid_cols"
        private const val KEY_ACCENT_THEME = "accent_theme"
        private const val KEY_SEPFOL_THEME = "sepfol_theme"
        private const val KEY_VISUAL_ENGINE = "visual_engine"
        private const val KEY_GEMINI_VOICE_NAME = "gemini_voice_name"
        private const val KEY_VOICE_ACCENT = "voice_accent"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_PREFER_GEMINI_VOICE = "prefer_gemini_voice"
        private const val KEY_DATABASE_INITIALIZED = "focusflow_database_seeded_v1"
    }

    var isDatabaseInitialized: Boolean
        get() = prefs.getBoolean(KEY_DATABASE_INITIALIZED, false)
        set(value) = prefs.edit().putBoolean(KEY_DATABASE_INITIALIZED, value).apply()

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value.trim()).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var brightnessModeName: String
        get() = prefs.getString(KEY_BRIGHTNESS_MODE, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_BRIGHTNESS_MODE, value).apply()

    var gridColumns: Int
        get() = prefs.getInt(KEY_GRID_COLS, 2)
        set(value) = prefs.edit().putInt(KEY_GRID_COLS, value).apply()

    var accentThemeName: String
        get() = prefs.getString(KEY_SEPFOL_THEME, prefs.getString(KEY_ACCENT_THEME, "CYBER_CORE") ?: "CYBER_CORE") ?: "CYBER_CORE"
        set(value) {
            prefs.edit()
                .putString(KEY_ACCENT_THEME, value)
                .putString(KEY_SEPFOL_THEME, value)
                .apply()
        }

    var visualEngineName: String
        get() = prefs.getString(KEY_VISUAL_ENGINE, "LIQUID_GLASS_3D") ?: "LIQUID_GLASS_3D"
        set(value) = prefs.edit().putString(KEY_VISUAL_ENGINE, value).apply()

    var geminiVoiceName: String
        get() = prefs.getString(KEY_GEMINI_VOICE_NAME, "Puck") ?: "Puck"
        set(value) = prefs.edit().putString(KEY_GEMINI_VOICE_NAME, value).apply()

    var voiceAccent: String
        get() = prefs.getString(KEY_VOICE_ACCENT, "AUTO") ?: "AUTO"
        set(value) = prefs.edit().putString(KEY_VOICE_ACCENT, value).apply()

    var voiceSpeed: Float
        get() = prefs.getFloat(KEY_VOICE_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var isPreferGeminiVoice: Boolean
        get() = prefs.getBoolean(KEY_PREFER_GEMINI_VOICE, true)
        set(value) = prefs.edit().putBoolean(KEY_PREFER_GEMINI_VOICE, value).apply()
}
