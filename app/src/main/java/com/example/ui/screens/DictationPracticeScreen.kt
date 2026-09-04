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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.AudioRadarVisualizer
import com.example.ui.theme.LocalAccentTheme
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

    var isControlsExpanded by remember { mutableStateOf(false) }
    var isAutoDictationActive by remember { mutableStateOf(false) }
    var autoRepeatTimes by remember { mutableIntStateOf(2) } // 1x, 2x, 3x
    var autoPauseSeconds by remember { mutableIntStateOf(5) } // 3s, 5s, 8s
    var autoPauseRemaining by remember { mutableIntStateOf(0) }
    var currentRepeatStep by remember { mutableIntStateOf(0) }
    var speechRate by remember { mutableFloatStateOf(prefsManager.voiceSpeed) }

    val currentIndex = uiState.currentWordIndex.coerceIn(0, (deck.words.size - 1).coerceAtLeast(0))
    val currentWord = deck.words.getOrNull(currentIndex)
    val totalWords = deck.words.size
    val accentColor = deck.categoryColor

    // Hands-Free Auto-Dictation Engine Loop
    LaunchedEffect(isAutoDictationActive, currentIndex) {
        if (isAutoDictationActive && currentWord != null) {
            for (r in 1..autoRepeatTimes) {
                if (!isAutoDictationActive) break
                currentRepeatStep = r
                autoPauseRemaining = 0
                viewModel.playCurrentWord()
                kotlinx.coroutines.delay(2400)
            }
            if (isAutoDictationActive) {
                currentRepeatStep = 0
                for (sec in autoPauseSeconds downTo 1) {
                    autoPauseRemaining = sec
                    kotlinx.coroutines.delay(1000)
                    if (!isAutoDictationActive) break
                }
                autoPauseRemaining = 0
                if (isAutoDictationActive) {
                    if (currentIndex < totalWords - 1) {
                        viewModel.nextWord()
                    } else {
                        isAutoDictationActive = false
                        onOpenCheckingTime()
                    }
                }
            }
        } else {
            autoPauseRemaining = 0
            currentRepeatStep = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isAutoDictationActive = false
            viewModel.voiceCommander.stopListening()
            audioPlayer.stop()
        }
    }

    BackHandler {
        if (isAutoDictationActive) {
            isAutoDictationActive = false
        } else if (isControlsExpanded) {
            isControlsExpanded = false
        } else {
            viewModel.finishPracticeSession(recordAccuracy = false)
            onBackClick()
        }
    }

    LaunchedEffect(lastDetectedCommand) {
        if (lastDetectedCommand == DictationVoiceCommand.CHECK_TIME) {
            onOpenCheckingTime()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isControlsExpanded) {
                    isControlsExpanded = false
                }
            }
            .testTag("dictation_practice_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Ultra-Clean Minimal Top Navigation (Back button, Auto Dictate Pill, Check button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
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
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                        .testTag("dictation_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Center Auto-Dictate Quick Pill
                Surface(
                    onClick = { isAutoDictationActive = !isAutoDictationActive },
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isAutoDictationActive) {
                        accentColor.copy(alpha = 0.22f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAutoDictationActive) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("auto_dictate_toggle_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoDictationActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = if (isAutoDictationActive) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAutoDictationActive) {
                                if (currentRepeatStep > 0) "Spk $currentRepeatStep/$autoRepeatTimes"
                                else "Pause: ${autoPauseRemaining}s"
                            } else {
                                "Auto Play"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAutoDictationActive) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onOpenCheckingTime,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                        .testTag("open_checking_time_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Check",
                        tint = accentColor
                    )
                }
            }

            // 2. Center Audio Radar Visualizer (Native Compose HUD Radar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Native Futuristic Audio-Reactive Radar
                AudioRadarVisualizer(
                    size = 210.dp,
                    isActive = !isSessionAsleep,
                    isListening = isMicListening || isAudioSpeaking,
                    audioLevel = audioLevel,
                    accentTheme = LocalAccentTheme.current,
                    onClick = {
                        if (isControlsExpanded) {
                            isControlsExpanded = false
                        }
                        if (isSessionAsleep) {
                            viewModel.wakeUpSession()
                        } else {
                            viewModel.playCurrentWord()
                        }
                    }
                )

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

            // 3. Word Reveal Card (Shown when requested via "Show" / "Dikhao")
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

            // 4. Clean Bottom Chevron Trigger & Slide-Up Controls Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // When collapsed: Clean outlined Up Arrow with NO background box/container (Pure outline icon, light/dark theme adaptive)
                if (!isControlsExpanded) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 24.dp),
                                onClick = { isControlsExpanded = true }
                            )
                            .testTag("dictation_expand_controls_arrow"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Show controls",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // When expanded: Slide up from bottom into exact position
                androidx.compose.animation.AnimatedVisibility(
                    visible = isControlsExpanded,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(tween(200)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(150))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Consume click inside */ },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dismiss down arrow handle
                        IconButton(
                            onClick = { isControlsExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Hide controls",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Hands-Free Auto Settings Strip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Auto-Dictate Toggle & Repeat Times
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
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Auto-Dictation Repeats",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(1, 2, 3).forEach { r ->
                                            val isSelected = autoRepeatTimes == r
                                            Surface(
                                                onClick = { autoRepeatTimes = r },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier.testTag("auto_repeat_${r}x_chip")
                                            ) {
                                                Text(
                                                    text = "${r}x",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Writing Pause Interval
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Writing Pause",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(3, 5, 8, 12).forEach { sec ->
                                            val isSelected = autoPauseSeconds == sec
                                            Surface(
                                                onClick = { autoPauseSeconds = sec },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier.testTag("auto_pause_${sec}s_chip")
                                            ) {
                                                Text(
                                                    text = "${sec}s",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Actions Row (Previous, Again, Meaning, Show, Next)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                icon = Icons.Default.SlowMotionVideo,
                                label = "Slow",
                                accentColor = accentColor,
                                testTag = "dictation_slow_btn",
                                onClick = { viewModel.playCurrentWordSlowly() }
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

