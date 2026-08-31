package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesManager
import com.example.data.speech.FlashcardAudioPlayer
import com.example.model.AccentTheme
import com.example.ui.components.GlassCard
import kotlin.math.roundToInt

data class GeminiVoiceOption(
    val id: String,
    val name: String,
    val gender: String,
    val toneDescription: String,
    val tags: List<String>,
    val accentColor: Color
)

val GEMINI_VOICES = listOf(
    GeminiVoiceOption(
        id = "Aoede",
        name = "Aoede",
        gender = "Female",
        toneDescription = "Warm, expressive & melodic. Natural conversational cadence.",
        tags = listOf("Recommended", "Expressive", "Tutor"),
        accentColor = Color(0xFF8B5CF6)
    ),
    GeminiVoiceOption(
        id = "Kore",
        name = "Kore",
        gender = "Female",
        toneDescription = "Soft, soothing & clear. Ideal for calm, deep study sessions.",
        tags = listOf("Gentle", "Clear", "Hindi-Ready"),
        accentColor = Color(0xFF10B981)
    ),
    GeminiVoiceOption(
        id = "Puck",
        name = "Puck",
        gender = "Male",
        toneDescription = "Energetic, youthful & engaging. High clarity and enthusiasm.",
        tags = listOf("Dynamic", "Fast", "Upbeat"),
        accentColor = Color(0xFF38BDF8)
    ),
    GeminiVoiceOption(
        id = "Charon",
        name = "Charon",
        gender = "Male",
        toneDescription = "Deep, resonant & authoritative. Serious academic delivery.",
        tags = listOf("Deep", "Authoritative", "Steady"),
        accentColor = Color(0xFFF59E0B)
    ),
    GeminiVoiceOption(
        id = "Fenrir",
        name = "Fenrir",
        gender = "Male",
        toneDescription = "Calm, balanced & articulate. Great for technical & STEM concepts.",
        tags = listOf("Balanced", "Crisp", "Neutral"),
        accentColor = Color(0xFFEC4899)
    )
)

data class AccentModeOption(
    val id: String,
    val label: String,
    val subtitle: String,
    val flagOrIcon: String
)

val ACCENT_MODES = listOf(
    AccentModeOption(
        id = "AUTO",
        label = "Auto-Detect Accent",
        subtitle = "Automatically matches Hindi & English based on flashcard script",
        flagOrIcon = "✨"
    ),
    AccentModeOption(
        id = "HINDI_IN",
        label = "Natural Indian Hindi (हिन्दी)",
        subtitle = "Clear Devanagari & Indian cadence pronunciation",
        flagOrIcon = "🇮🇳"
    ),
    AccentModeOption(
        id = "ENGLISH_IN",
        label = "Indian English (en-IN)",
        subtitle = "Natural Indian English study accent",
        flagOrIcon = "🎙️"
    ),
    AccentModeOption(
        id = "ENGLISH_US",
        label = "US English (en-US)",
        subtitle = "Standard American accent and articulation",
        flagOrIcon = "🇺🇸"
    ),
    AccentModeOption(
        id = "ENGLISH_UK",
        label = "British English (en-GB)",
        subtitle = "Refined British English cadence",
        flagOrIcon = "🇬🇧"
    )
)

@Composable
fun VoiceSettingsScreen(
    onBackClick: () -> Unit,
    selectedAccent: AccentTheme = AccentTheme.CYBER_CORE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferencesManager(context) }
    val audioPlayer = remember { FlashcardAudioPlayer.getInstance(context) }

    val isSpeaking by audioPlayer.isSpeaking.collectAsState()
    val isLoading by audioPlayer.isLoading.collectAsState()
    val currentText by audioPlayer.currentText.collectAsState()

    val currentEngineType by audioPlayer.currentEngineType.collectAsState()

    var selectedVoiceId by remember { mutableStateOf(prefs.geminiVoiceName) }
    var selectedAccentId by remember { mutableStateOf(prefs.voiceAccent) }
    var speed by remember { mutableFloatStateOf(prefs.voiceSpeed) }
    var pitch by remember { mutableFloatStateOf(prefs.voicePitch) }
    var preferGeminiVoice by remember { mutableStateOf(prefs.isPreferGeminiVoice) }
    var customApiKeyInput by remember { mutableStateOf(prefs.customApiKey) }
    var showApiKeyField by remember { mutableStateOf(false) }

    val hasApiKey = prefs.customApiKey.isNotBlank()

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
        }
    }

    BackHandler(enabled = true) {
        audioPlayer.stop()
        onBackClick()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ),
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                audioPlayer.stop()
                                onBackClick()
                            },
                            modifier = Modifier.size(36.dp).testTag("voice_settings_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = selectedAccent.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "Voices & Accent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 19.sp
                        )
                    }

                    if (isSpeaking) {
                        OutlinedButton(
                            onClick = { audioPlayer.stop() },
                            shape = RoundedCornerShape(9999.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Stop",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Settings Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Status Banner: Live Gemini HD Voice Switch & Mode Indicator
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Main Live AI HD Voice Toggle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (preferGeminiVoice && hasApiKey) Color(0xFF10B981).copy(alpha = 0.20f)
                                                else if (preferGeminiVoice) Color(0xFFF59E0B).copy(alpha = 0.20f)
                                                else selectedAccent.primaryColor.copy(alpha = 0.18f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (preferGeminiVoice && hasApiKey) Icons.Default.AutoAwesome
                                            else if (preferGeminiVoice) Icons.Default.Key
                                            else Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = if (preferGeminiVoice && hasApiKey) Color(0xFF10B981)
                                            else if (preferGeminiVoice) Color(0xFFF59E0B)
                                            else selectedAccent.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Gemini Live AI HD Voice",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = if (preferGeminiVoice && hasApiKey) "Generative Neural HD Streaming • Active"
                                            else if (preferGeminiVoice) "Key Required • Tap below to save key"
                                            else "100% Offline Acoustic Mode • Zero Lag & Instant",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = preferGeminiVoice,
                                    onCheckedChange = { isChecked ->
                                        preferGeminiVoice = isChecked
                                        prefs.isPreferGeminiVoice = isChecked
                                    },
                                    modifier = Modifier.testTag("switch_gemini_live_voice"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = if (hasApiKey) Color(0xFF10B981) else selectedAccent.primaryColor,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }

                            // Status Tag & Explanation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(
                                            if (preferGeminiVoice && hasApiKey) Color(0xFF10B981).copy(alpha = 0.15f)
                                            else if (preferGeminiVoice) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            else Color(0xFF38BDF8).copy(alpha = 0.15f)
                                        )
                                        .border(
                                            1.dp,
                                            if (preferGeminiVoice && hasApiKey) Color(0xFF10B981).copy(alpha = 0.35f)
                                            else if (preferGeminiVoice) Color(0xFFF59E0B).copy(alpha = 0.35f)
                                            else Color(0xFF38BDF8).copy(alpha = 0.35f),
                                            RoundedCornerShape(9999.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (preferGeminiVoice && hasApiKey) "⚡ LIVE HD VOICE ACTIVE"
                                        else if (preferGeminiVoice) "🔑 ENTER API KEY"
                                        else "🔋 100% OFFLINE ZERO-LAG",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (preferGeminiVoice && hasApiKey) Color(0xFF34D399)
                                        else if (preferGeminiVoice) Color(0xFFF59E0B)
                                        else Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                if (currentEngineType.isNotBlank()) {
                                    Text(
                                        text = "Active: $currentEngineType",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Quick Inline API Key Setup when Gemini HD is ON but no key is present
                            if (preferGeminiVoice && !hasApiKey) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "To stream real human-like Gemini Live voices, enter your Gemini API Key below:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )

                                    OutlinedTextField(
                                        value = customApiKeyInput,
                                        onValueChange = { customApiKeyInput = it },
                                        placeholder = {
                                            Text(
                                                text = "AIzaSy...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { showApiKeyField = !showApiKeyField }) {
                                                Icon(
                                                    imageVector = if (showApiKeyField) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (showApiKeyField) VisualTransformation.None else PasswordVisualTransformation(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("inline_voice_api_key_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedIndicatorColor = selectedAccent.primaryColor,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        singleLine = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                prefs.customApiKey = customApiKeyInput.trim()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = selectedAccent.primaryColor,
                                                contentColor = selectedAccent.buttonTextColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .testTag("save_voice_api_key_btn")
                                        ) {
                                            Text(
                                                text = "Save & Activate HD Voice",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 1: Gemini Live Voice Personas
                item {
                    Text(
                        text = "GEMINI LIVE VOICES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = selectedAccent.primaryColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                items(GEMINI_VOICES) { voice ->
                    val isSelected = selectedVoiceId == voice.id

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedVoiceId = voice.id
                                prefs.geminiVoiceName = voice.id
                            }
                            .testTag("voice_option_${voice.id.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        elevation = if (isSelected) 6.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar Pill
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(voice.accentColor.copy(alpha = if (isSelected) 0.28f else 0.12f))
                                    .border(
                                        1.5.dp,
                                        if (isSelected) voice.accentColor else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isSelected) voice.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Name & Vibe Description
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = voice.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "• ${voice.gender}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = voice.toneDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    voice.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Preview Button
                            IconButton(
                                onClick = {
                                    selectedVoiceId = voice.id
                                    prefs.geminiVoiceName = voice.id
                                    audioPlayer.previewVoice(
                                        voiceName = voice.id,
                                        customPhrase = if (selectedAccentId == "HINDI_IN") {
                                            "नमस्ते, मैं ${voice.name} हूँ। आपकी पढ़ाई को आसान और रोचक बनाने के लिए तैयार हूँ।"
                                        } else {
                                            "Hello, I am ${voice.name}. Ready to guide your flashcard study session."
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(selectedAccent.primaryColor.copy(alpha = 0.15f))
                                    .testTag("preview_voice_${voice.id.lowercase()}")
                            ) {
                                if (isLoading && isSelected) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = selectedAccent.primaryColor
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Test ${voice.name}",
                                        tint = selectedAccent.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Accent & Regional Delivery
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ACCENT & LANGUAGE DELIVERY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = selectedAccent.primaryColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                items(ACCENT_MODES) { accentMode ->
                    val isSelected = selectedAccentId == accentMode.id
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAccentId = accentMode.id
                                prefs.voiceAccent = accentMode.id
                                audioPlayer.previewAccent(accentMode.id)
                            }
                            .testTag("accent_mode_${accentMode.id.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        elevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = accentMode.flagOrIcon,
                                fontSize = 22.sp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = accentMode.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = accentMode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            // Accent Preview Listen Button
                            IconButton(
                                onClick = {
                                    selectedAccentId = accentMode.id
                                    prefs.voiceAccent = accentMode.id
                                    audioPlayer.previewAccent(accentMode.id)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(selectedAccent.primaryColor.copy(alpha = if (isSelected) 0.20f else 0.08f))
                                    .testTag("preview_accent_${accentMode.id.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                    contentDescription = "Test ${accentMode.label}",
                                    tint = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = selectedAccent.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Section 3: Speech Pace & Pitch Tuning
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SPEECH CADENCE & SPEED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = selectedAccent.primaryColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Speed Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = selectedAccent.primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Speaking Speed",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${(speed * 100).roundToInt()}% (${if (speed < 0.9f) "Relaxed" else if (speed > 1.1f) "Fast" else "Normal"})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedAccent.primaryColor
                                    )
                                }

                                Slider(
                                    value = speed,
                                    onValueChange = {
                                        speed = it
                                        prefs.voiceSpeed = it
                                    },
                                    valueRange = 0.75f..1.5f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = selectedAccent.primaryColor,
                                        activeTrackColor = selectedAccent.primaryColor,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.testTag("slider_voice_speed")
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                thickness = 1.dp
                            )

                            // Pitch Slider
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = selectedAccent.primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Voice Pitch",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${(pitch * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedAccent.primaryColor
                                    )
                                }

                                Slider(
                                    value = pitch,
                                    onValueChange = {
                                        pitch = it
                                        prefs.voicePitch = it
                                    },
                                    valueRange = 0.8f..1.2f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = selectedAccent.primaryColor,
                                        activeTrackColor = selectedAccent.primaryColor,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.testTag("slider_voice_pitch")
                                )
                            }

                            // Reset Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        speed = 1.0f
                                        pitch = 1.0f
                                        prefs.voiceSpeed = 1.0f
                                        prefs.voicePitch = 1.0f
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Reset Defaults",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Live Interactive Test Banner
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Test Card Pronunciation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (selectedAccentId == "HINDI_IN") {
                                    "\"सर्वनाम के मुख्य 6 भेद होते हैं: पुरुषवाचक, निजवाचक, निश्चयवाचक, अनिश्चयवाचक, संबंधवाचक और प्रश्नवाचक।\""
                                } else {
                                    "\"Photosynthesis converts light energy into chemical energy within plant chloroplasts.\""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Button(
                                onClick = {
                                    val testText = if (selectedAccentId == "HINDI_IN") {
                                        "सर्वनाम के मुख्य 6 भेद होते हैं: पुरुषवाचक, निजवाचक, निश्चयवाचक, अनिश्चयवाचक, संबंधवाचक और प्रश्नवाचक।"
                                    } else {
                                        "Photosynthesis converts light energy into chemical energy within plant chloroplasts."
                                    }
                                    audioPlayer.speak(testText)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = selectedAccent.primaryColor,
                                    contentColor = selectedAccent.buttonTextColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("test_voice_sample_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isSpeaking) "Playing Voice..." else "Listen to Test Card",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
