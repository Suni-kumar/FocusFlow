package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.preferences.UserPreferencesManager
import com.example.data.speech.DictationVoiceCommand
import com.example.data.speech.FlashcardAudioPlayer
import com.example.model.DictationDeck
import com.example.viewmodel.DictationViewModel

@Composable
fun DictationPracticeScreen(
    deck: DictationDeck,
    viewModel: DictationViewModel,
    onBackClick: () -> Unit,
    onOpenCheckingTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefsManager = remember { UserPreferencesManager(context) }
    val audioPlayer = remember { FlashcardAudioPlayer.getInstance(context) }
    val isAudioSpeaking by audioPlayer.isSpeaking.collectAsState()
    val isAudioLoading by audioPlayer.isLoading.collectAsState()
    val currentEngineType by audioPlayer.currentEngineType.collectAsState()

    var isPreferGeminiVoice by remember { mutableStateOf(prefsManager.isPreferGeminiVoice) }
    val hasApiKey = prefsManager.customApiKey.isNotBlank()

    val uiState by viewModel.uiState.collectAsState()
    val isMicListening by viewModel.voiceCommander.isListening.collectAsState()
    val audioLevel by viewModel.voiceCommander.audioRmsLevel.collectAsState()
    val lastSpokenText by viewModel.voiceCommander.lastRecognizedText.collectAsState()
    val lastDetectedCommand by viewModel.voiceCommander.lastDetectedCommand.collectAsState()
    val isSessionAsleep by viewModel.voiceCommander.isAsleepDueToInactivity.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.voiceCommander.startListening()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.voiceCommander.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.voiceCommander.stopListening()
            audioPlayer.stop()
        }
    }

    BackHandler {
        viewModel.finishPracticeSession(recordAccuracy = false)
        onBackClick()
    }

    LaunchedEffect(lastDetectedCommand) {
        if (lastDetectedCommand == DictationVoiceCommand.CHECK_TIME) {
            onOpenCheckingTime()
        }
    }

    val currentIndex = uiState.currentWordIndex.coerceIn(0, (deck.words.size - 1).coerceAtLeast(0))
    val currentWord = deck.words.getOrNull(currentIndex)
    val totalWords = deck.words.size
    val progress = if (totalWords > 0) (currentIndex + 1).toFloat() / totalWords.toFloat() else 0f
    val accentColor = deck.categoryColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("dictation_practice_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Navigation & Progress Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.finishPracticeSession(recordAccuracy = false)
                            onBackClick()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .testTag("dictation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = deck.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Word ${currentIndex + 1} of $totalWords",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Checking Time Button
                    Button(
                        onClick = onOpenCheckingTime,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_checking_time_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Check",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Progress Bar & Voice Engine Switcher
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Word ${currentIndex + 1} of $totalWords",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        // Voice Engine Quick Toggle Pill
                        Surface(
                            onClick = {
                                val nextState = !isPreferGeminiVoice
                                isPreferGeminiVoice = nextState
                                prefsManager.isPreferGeminiVoice = nextState
                            },
                            shape = RoundedCornerShape(9999.dp),
                            color = if (isPreferGeminiVoice && hasApiKey) Color(0xFF10B981).copy(alpha = 0.12f)
                            else if (isPreferGeminiVoice) Color(0xFFF59E0B).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPreferGeminiVoice && hasApiKey) Color(0xFF10B981).copy(alpha = 0.35f)
                                else if (isPreferGeminiVoice) Color(0xFFF59E0B).copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("dictation_voice_toggle_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isAudioLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = if (isPreferGeminiVoice && hasApiKey) Color(0xFF10B981) else accentColor
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPreferGeminiVoice && hasApiKey) Color(0xFF10B981)
                                                else if (isPreferGeminiVoice) Color(0xFFF59E0B)
                                                else Color(0xFF38BDF8)
                                            )
                                    )
                                }

                                Text(
                                    text = if (isPreferGeminiVoice && hasApiKey) "Gemini Live HD (${prefsManager.geminiVoiceName})"
                                    else if (isPreferGeminiVoice) "HD Key Required"
                                    else "Offline Voice",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPreferGeminiVoice && hasApiKey) Color(0xFF34D399)
                                    else if (isPreferGeminiVoice) Color(0xFFF59E0B)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // 2. Hands-Free Ambient Listening Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = if (isSessionAsleep) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        accentColor.copy(alpha = 0.10f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSessionAsleep) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else accentColor.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSessionAsleep) MaterialTheme.colorScheme.surfaceContainerHigh
                                        else if (isMicListening) accentColor.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSessionAsleep) Icons.Default.Bedtime
                                    else if (isMicListening) Icons.Default.Mic
                                    else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (isSessionAsleep) MaterialTheme.colorScheme.onSurfaceVariant
                                    else if (isMicListening) accentColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isSessionAsleep) "💤 Standby Mode (5m Inactivity)"
                                    else "🎙️ Hands-Free Listening Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSessionAsleep) "Tap orb to wake up & continue"
                                    else "Say \"Next\", \"Again\", \"Meaning\", \"Show\", \"Check\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = if (isSessionAsleep) "Paused" else "Live Mic",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSessionAsleep) MaterialTheme.colorScheme.onSurfaceVariant else accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 3. Center Soundwave & Interactive Acoustic Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background Glowing Rings (Isolated RenderNode - No Screen Recomposition)
                AcousticPulseRings(
                    isActive = !isSessionAsleep && (isAudioSpeaking || isMicListening),
                    accentColor = accentColor,
                    audioLevel = audioLevel
                )

                // Interactive Center Orb
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .shadow(
                            elevation = if (isAudioSpeaking) 24.dp else 14.dp,
                            shape = CircleShape,
                            ambientColor = accentColor.copy(alpha = 0.4f),
                            spotColor = accentColor.copy(alpha = 0.6f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = accentColor),
                            onClick = {
                                if (isSessionAsleep) {
                                    viewModel.wakeUpSession()
                                } else {
                                    viewModel.playCurrentWord()
                                }
                            }
                        )
                        .testTag("dictation_hero_play_button"),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    if (isSessionAsleep) {
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceContainerHighest,
                                            MaterialTheme.colorScheme.surfaceContainerHigh,
                                            Color(0xFF1E293B)
                                        )
                                    } else {
                                        listOf(
                                            accentColor,
                                            accentColor.copy(alpha = 0.85f),
                                            Color(0xFF0F172A)
                                        )
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isSessionAsleep -> Icons.Default.WbSunny
                                    isAudioSpeaking -> Icons.Default.GraphicEq
                                    else -> Icons.Default.VolumeUp
                                },
                                contentDescription = "Play word audio",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    isSessionAsleep -> "Tap to Wake"
                                    isAudioSpeaking -> "Speaking..."
                                    else -> "Tap to Hear"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }
                }

                // Live Spoken Feedback Toast Pill
                if (lastSpokenText.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Heard: \"$lastSpokenText\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. Word Reveal Card (Shown when requested via "Show" / "Dikhao")
            AnimatedVisibility(
                visible = uiState.isWordCardVisible && currentWord != null,
                enter = fadeIn(tween(180)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + scaleIn(initialScale = 0.92f),
                exit = fadeOut(tween(140)) + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(
                    targetScale = 0.92f
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                if (currentWord != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transient_word_reveal_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Revealed Word #${currentIndex + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.hideCurrentWordCard() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hide card",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = currentWord.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (currentWord.phonetic.isNotBlank()) {
                                Text(
                                    text = currentWord.phonetic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = currentWord.meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (currentWord.exampleSentence.isNotBlank()) {
                                Text(
                                    text = "\"${currentWord.exampleSentence}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }

            // 5. Bottom Navigation & Action Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DictationControlAction(
                    icon = Icons.Default.SkipPrevious,
                    label = "Previous",
                    accentColor = accentColor,
                    enabled = currentIndex > 0,
                    testTag = "dictation_previous_btn",
                    onClick = { viewModel.previousWord() }
                )

                DictationControlAction(
                    icon = Icons.Default.Replay,
                    label = "Again",
                    accentColor = accentColor,
                    testTag = "dictation_again_btn",
                    onClick = { viewModel.repeatCurrentWord() }
                )

                DictationControlAction(
                    icon = Icons.Outlined.Lightbulb,
                    label = "Meaning",
                    accentColor = accentColor,
                    testTag = "dictation_meaning_btn",
                    onClick = { viewModel.speakCurrentWordMeaning() }
                )

                DictationControlAction(
                    icon = Icons.Default.Visibility,
                    label = "Show",
                    accentColor = accentColor,
                    testTag = "dictation_show_btn",
                    onClick = {
                        if (uiState.isWordCardVisible) viewModel.hideCurrentWordCard()
                        else viewModel.showCurrentWordCard()
                    }
                )

                DictationControlAction(
                    icon = Icons.Default.SkipNext,
                    label = if (currentIndex == totalWords - 1) "Check" else "Next",
                    accentColor = accentColor,
                    testTag = "dictation_next_btn",
                    onClick = {
                        if (currentIndex == totalWords - 1) {
                            onOpenCheckingTime()
                        } else {
                            viewModel.nextWord()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DictationControlAction(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    enabled: Boolean = true,
    testTag: String = "",
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                .testTag(testTag),
            shape = CircleShape,
            color = if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
            border = if (enabled) androidx.compose.foundation.BorderStroke(
                1.dp,
                accentColor.copy(alpha = 0.35f)
            ) else null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.4f
            ),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AcousticPulseRings(
    isActive: Boolean,
    accentColor: Color,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_waves")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer {
                    val s = pulseScale2 + (audioLevel * 0.35f)
                    scaleX = s
                    scaleY = s
                }
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    val s = pulseScale1 + (audioLevel * 0.25f)
                    scaleX = s
                    scaleY = s
                }
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f))
        )
    }
}

