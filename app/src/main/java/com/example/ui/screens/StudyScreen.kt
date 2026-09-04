package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.preferences.UserPreferencesManager
import com.example.data.speech.DictationVoiceCommand
import com.example.data.speech.DictationVoiceCommander
import com.example.data.speech.FlashcardAudioPlayer
import com.example.model.Flashcard
import com.example.model.FlashcardDeck
import com.example.model.MockDataSource
import com.example.ui.components.FlashcardSpeakerButton
import com.example.ui.theme.FocusBlue
import com.example.ui.theme.LiquidGlassReflection
import com.example.ui.util.AppHaptic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudyScreen(
    deck: FlashcardDeck = MockDataSource.decks[0],
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onDeckProgressUpdate: ((progress: Float) -> Unit)? = null,
    onToggleCardMastery: ((cardId: String, isMastered: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticView = LocalView.current
    val density = LocalDensity.current.density
    val prefsManager = remember { UserPreferencesManager(context) }
    val audioPlayer = remember { FlashcardAudioPlayer.getInstance(context) }

    val allCards = remember(deck.cards) {
        deck.cards
    }
    
    // Instead of local state, calculate from the deck cards
    val masteredCardIds = remember(deck.cards) {
        deck.cards.filter { it.isMastered }.map { it.id }.toSet()
    }
    var filterOnlyUnmastered by remember { mutableStateOf(false) }

    var cardsList by remember(deck.id) {
        mutableStateOf(allCards)
    }

    LaunchedEffect(filterOnlyUnmastered, allCards) {
        val orderMap = cardsList.withIndex().associate { it.value.id to it.index }
        var updated = allCards.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
        
        if (filterOnlyUnmastered) {
            val unmastered = updated.filter { !it.isMastered }
            cardsList = if (unmastered.isNotEmpty()) unmastered else allCards
        } else {
            cardsList = updated
        }
    }

    var currentCardIndex by remember(cardsList) { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isCardExpanded by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var isZenMode by remember { mutableStateOf(false) }
    var showTags by remember { mutableStateOf(false) }

    // Auto-Play / Hands-Free Loop state
    var isAutoPlayActive by remember { mutableStateOf(false) }
    var autoPlayCountdown by remember { mutableIntStateOf(0) }
    var currentVoiceSpeed by remember { mutableStateOf(prefsManager.voiceSpeed) }

    // Hands-Free Voice Control Engine
    val voiceCommander = remember { DictationVoiceCommander(context) }
    var isVoiceControlActive by remember { mutableStateOf(false) }
    val lastVoiceCommand by voiceCommander.lastDetectedCommand.collectAsState()
    val isListeningToVoice by voiceCommander.isListening.collectAsState()

    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }

    val safeIndex = currentCardIndex.coerceIn(0, (cardsList.size - 1).coerceAtLeast(0))
    val currentCard = if (cardsList.isNotEmpty()) {
        cardsList[safeIndex]
    } else {
        Flashcard(
            id = "empty_deck_${deck.id}",
            front = "No cards available in this deck",
            back = "Use the Edit or Add buttons to create flashcards for this deck.",
            topic = deck.title
        )
    }

    val frontScrollState = rememberScrollState()
    val backScrollState = rememberScrollState()

    LaunchedEffect(currentCardIndex, isFlipped) {
        frontScrollState.scrollTo(0)
        backScrollState.scrollTo(0)
    }

    LaunchedEffect(currentCardIndex) {
        showTags = false
    }

    // Hands-Free Auto-Play Orchestration with Full Utterance Completion (No cut-off)
    LaunchedEffect(isAutoPlayActive, currentCardIndex, isFlipped) {
        if (isAutoPlayActive && cardsList.isNotEmpty()) {
            if (!isFlipped) {
                // 1. Speak Question completely (waits until full audio finishes)
                audioPlayer.speakAndWait(currentCard.front, rate = currentVoiceSpeed)
                if (!isAutoPlayActive) return@LaunchedEffect

                // 2. Wait 3 seconds with countdown
                for (sec in 3 downTo 1) {
                    autoPlayCountdown = sec
                    delay(1000)
                    if (!isAutoPlayActive) break
                }
                if (isAutoPlayActive) {
                    isFlipped = true
                }
            } else {
                // 3. Speak Answer completely (waits until full audio finishes)
                audioPlayer.speakAndWait(currentCard.back, rate = currentVoiceSpeed)
                if (!isAutoPlayActive) return@LaunchedEffect

                // 4. Wait 3.5 seconds
                for (sec in 3 downTo 1) {
                    autoPlayCountdown = sec
                    delay(1000)
                    if (!isAutoPlayActive) break
                }
                if (isAutoPlayActive) {
                    if (currentCardIndex < cardsList.size - 1) {
                        isFlipped = false
                        currentCardIndex++
                    } else {
                        isAutoPlayActive = false
                        isCompleted = true
                    }
                }
            }
        } else {
            autoPlayCountdown = 0
        }
    }

    // Voice Command Event Listener for Hands-Free Flashcard Navigation
    LaunchedEffect(lastVoiceCommand) {
        if (!isVoiceControlActive) return@LaunchedEffect
        when (lastVoiceCommand) {
            DictationVoiceCommand.FLIP -> {
                AppHaptic.vibrateClick(context, hapticView)
                isFlipped = !isFlipped
            }
            DictationVoiceCommand.NEXT -> {
                if (currentCardIndex < cardsList.size - 1) {
                    AppHaptic.vibrateClick(context, hapticView)
                    isFlipped = false
                    currentCardIndex++
                }
            }
            DictationVoiceCommand.PREVIOUS -> {
                if (currentCardIndex > 0) {
                    AppHaptic.vibrateClick(context, hapticView)
                    isFlipped = false
                    currentCardIndex--
                }
            }
            DictationVoiceCommand.REPEAT -> {
                AppHaptic.vibrateClick(context, hapticView)
                val textToSay = if (isFlipped) currentCard.back else currentCard.front
                audioPlayer.speak(textToSay, rate = currentVoiceSpeed)
            }
            DictationVoiceCommand.MASTER -> {
                AppHaptic.vibrateHeavy(context, hapticView)
                onToggleCardMastery?.invoke(currentCard.id, true)
                if (currentCardIndex < cardsList.size - 1) {
                    isFlipped = false
                    currentCardIndex++
                }
            }
            DictationVoiceCommand.UNMASTER -> {
                AppHaptic.vibrateClick(context, hapticView)
                onToggleCardMastery?.invoke(currentCard.id, false)
            }
            DictationVoiceCommand.PAUSE -> {
                AppHaptic.vibrateClick(context, hapticView)
                isAutoPlayActive = false
                audioPlayer.stop()
            }
            else -> {}
        }
    }

    LaunchedEffect(isVoiceControlActive) {
        if (isVoiceControlActive) {
            voiceCommander.startListening()
        } else {
            voiceCommander.stopListening()
        }
    }

    // Flip animation rotation with smooth perceptible 3D rotation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 180f
        ),
        label = "cardFlip"
    )

    // Animated card height for the expanded/reading mode
    val cardHeight by animateDpAsState(
        targetValue = if (isCardExpanded) 520.dp else 300.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardHeight"
    )

    // Stop audio when changing card or leaving
    LaunchedEffect(currentCardIndex) {
        if (!isAutoPlayActive) {
            audioPlayer.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
            voiceCommander.stopListening()
            voiceCommander.destroy()
        }
    }

    // Intercept hardware/gesture back press
    BackHandler(enabled = true) {
        audioPlayer.stop()
        voiceCommander.stopListening()
        isAutoPlayActive = false
        isVoiceControlActive = false
        if (isCardExpanded) {
            AppHaptic.vibrateClick(context, hapticView)
            isCardExpanded = false
        } else {
            onBackClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping outside card closes expanded mode or toggles zen mode
                if (isCardExpanded) {
                    AppHaptic.vibrateClick(context, hapticView)
                    isCardExpanded = false
                } else {
                    isZenMode = !isZenMode
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Topic Badge, Back Button, Theme Toggle & Progress
            AnimatedVisibility(
                visible = !isZenMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                onBackClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Topic Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = deck.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Theme Toggle & Progress Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Study Mode Theme Switch
                        IconButton(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                onToggleTheme()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                .testTag("study_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Progress Bar & Counter
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val progressFraction = if (cardsList.isNotEmpty()) {
                                (currentCardIndex + 1).toFloat() / cardsList.size.toFloat()
                            } else 1f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        Text(
                            text = "${currentCardIndex + 1}/${cardsList.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-header: Auto-Play Indicator & Speed Chips
            AnimatedVisibility(
                visible = !isZenMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Auto-Play Toggle Button
                        Surface(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                isAutoPlayActive = !isAutoPlayActive
                            },
                            shape = RoundedCornerShape(9999.dp),
                            color = if (isAutoPlayActive) FocusBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAutoPlayActive) FocusBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAutoPlayActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAutoPlayActive) "Pause Auto-Advance" else "Start Auto-Advance",
                                    tint = if (isAutoPlayActive) FocusBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isAutoPlayActive) "Auto-Play (${autoPlayCountdown}s)" else "Auto-Play",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAutoPlayActive) FocusBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Hands-Free Voice Commands Button
                        Surface(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                isVoiceControlActive = !isVoiceControlActive
                            },
                            shape = RoundedCornerShape(9999.dp),
                            color = if (isVoiceControlActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isVoiceControlActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVoiceControlActive) Icons.Default.Mic else Icons.Default.MicNone,
                                    contentDescription = if (isVoiceControlActive) "Disable Voice Control" else "Enable Voice Control",
                                    tint = if (isVoiceControlActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isVoiceControlActive) "Voice: ON" else "Voice",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isVoiceControlActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Inline Speed Selector (0.75x, 1.0x, 1.25x, 1.5x)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            val isSelected = (currentVoiceSpeed == speed)
                            Surface(
                                onClick = {
                                    AppHaptic.vibrateClick(context, hapticView)
                                    currentVoiceSpeed = speed
                                    prefsManager.voiceSpeed = speed
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedVisibility(
                visible = isVoiceControlActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Say: \"Flip\", \"Next\", \"Back\", \"Repeat\", or \"Master\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 3D Flashcard Stack Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight),
                contentAlignment = Alignment.Center
            ) {
                // Ambient Liquid Glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(20.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Background stack layer (only visible when not expanded)
                if (!isCardExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 18.dp, start = 12.dp, end = 12.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
                    )
                }

                // Main Flippable Card with Horizontal Swipe
                val horizontalDraggableState = rememberDraggableState { delta ->
                    scope.launch {
                        dragOffsetX.snapTo(dragOffsetX.value + delta)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = dragOffsetX.value
                            rotationZ = (dragOffsetX.value / 35f).coerceIn(-12f, 12f)
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .draggable(
                            state = horizontalDraggableState,
                            orientation = Orientation.Horizontal,
                            onDragStopped = { velocity ->
                                scope.launch {
                                    val offset = dragOffsetX.value
                                    if ((offset < -120f || velocity < -500f) && currentCardIndex < cardsList.size - 1) {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        dragOffsetX.animateTo(-500f, tween(140))
                                        isFlipped = false
                                        currentCardIndex++
                                        dragOffsetX.snapTo(400f)
                                        dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                                    } else if ((offset > 120f || velocity > 500f) && currentCardIndex > 0) {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        dragOffsetX.animateTo(500f, tween(140))
                                        isFlipped = false
                                        currentCardIndex--
                                        dragOffsetX.snapTo(-400f)
                                        dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                                    } else {
                                        dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                    }
                                }
                            }
                        )
                        .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f))
                        .border(
                            1.2.dp,
                            if (isFlipped) Color(0xFF10B981).copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            RoundedCornerShape(22.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                isFlipped = !isFlipped
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Glass reflection gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LiquidGlassReflection)
                    )

                    if (rotation <= 90f) {
                        // FRONT SIDE
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Label badge, Hashtag button & Audio button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "QUESTION",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.15.sp
                                        )
                                    }

                                    if (currentCard.tags.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(
                                                    if (showTags) Color(0xFF8B5CF6).copy(alpha = 0.28f)
                                                    else Color(0xFF8B5CF6).copy(alpha = 0.14f)
                                                )
                                                .border(
                                                    width = if (showTags) 1.5.dp else 1.dp,
                                                    color = if (showTags) Color(0xFFC084FC) else Color(0xFF8B5CF6).copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(9999.dp)
                                                )
                                                .clickable {
                                                    AppHaptic.vibrateClick(context, hapticView)
                                                    showTags = !showTags
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("toggle_tags_front_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "#",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFC084FC),
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${currentCard.tags.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (showTags) Color.White else Color(0xFFE9D5FF),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Flashcard Natural Voice Audio Speaker Button
                                FlashcardSpeakerButton(
                                    textToSpeak = currentCard.front,
                                    activeColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Inline Animated Tag Row (Revealed inline upon tapping # button without popups)
                            AnimatedVisibility(
                                visible = showTags && currentCard.tags.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    currentCard.tags.forEach { tag ->
                                        val formattedTag = if (tag.startsWith("#")) tag else "#$tag"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF8B5CF6).copy(alpha = 0.18f))
                                                .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = formattedTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE9D5FF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // SCROLLABLE QUESTION TEXT CONTAINER (Prevents any clipping of long text)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .verticalScroll(frontScrollState),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentCard.front,
                                    style = if (isCardExpanded) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 28.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }

                            // Bottom Edge: Simple downside arrow (No background boxes/text)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        isCardExpanded = !isCardExpanded
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isCardExpanded) "Collapse card" else "Expand card",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        // BACK SIDE (Mirrored so text reads normally)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Label badge, Hashtag button & Audio button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(9999.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ANSWER",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            letterSpacing = 0.15.sp
                                        )
                                    }

                                    if (currentCard.tags.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(
                                                    if (showTags) Color(0xFF10B981).copy(alpha = 0.28f)
                                                    else Color(0xFF10B981).copy(alpha = 0.14f)
                                                )
                                                .border(
                                                    width = if (showTags) 1.5.dp else 1.dp,
                                                    color = if (showTags) Color(0xFF34D399) else Color(0xFF10B981).copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(9999.dp)
                                                )
                                                .clickable {
                                                    AppHaptic.vibrateClick(context, hapticView)
                                                    showTags = !showTags
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("toggle_tags_back_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "#",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF34D399),
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${currentCard.tags.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (showTags) Color.White else Color(0xFFA7F3D0),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Flashcard Natural Voice Audio Speaker Button
                                FlashcardSpeakerButton(
                                    textToSpeak = currentCard.back,
                                    activeColor = Color(0xFF10B981)
                                )
                            }

                            // Inline Animated Tag Row (Revealed inline upon tapping # button without popups)
                            AnimatedVisibility(
                                visible = showTags && currentCard.tags.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    currentCard.tags.forEach { tag ->
                                        val formattedTag = if (tag.startsWith("#")) tag else "#$tag"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF10B981).copy(alpha = 0.18f))
                                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = formattedTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFA7F3D0),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // SCROLLABLE ANSWER TEXT CONTAINER (Smooth scrolling for long answers)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .verticalScroll(backScrollState),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentCard.back,
                                    style = if (isCardExpanded) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }

                            // Bottom Edge: Simple downside arrow (No background boxes/text)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        isCardExpanded = !isCardExpanded
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isCardExpanded) "Collapse card" else "Expand card",
                                    tint = Color(0xFF10B981).copy(alpha = 0.65f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Controls Area (Prev, Flip, Mastered, Next)
            AnimatedVisibility(
                visible = !isZenMode && !isCardExpanded,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Card
                        IconButton(
                            onClick = {
                                if (currentCardIndex > 0) {
                                    AppHaptic.vibrateClick(context, hapticView)
                                    isFlipped = false
                                    currentCardIndex--
                                }
                            },
                            enabled = currentCardIndex > 0,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Card",
                                tint = if (currentCardIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Flip Button
                        Button(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                isFlipped = !isFlipped
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Flip Card",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Mastered Button
                        Button(
                            onClick = {
                                AppHaptic.vibrateHeavy(context, hapticView)
                                onToggleCardMastery?.invoke(currentCard.id, true)
                                // We don't recalculate here, as the deck's new state will trickle down
                                // and trigger recomposition of masteredCardIds
                                val calculatedProgress = ((masteredCardIds.size + 1).toFloat() / cardsList.size.toFloat()).coerceIn(0f, 1f)
                                onDeckProgressUpdate?.invoke(calculatedProgress)

                                if (currentCardIndex < cardsList.size - 1) {
                                    isFlipped = false
                                    currentCardIndex++
                                } else {
                                    isCompleted = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusBlue
                            ),
                            modifier = Modifier
                                .weight(1.15f)
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = FocusBlue)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mastered",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Next Card
                        IconButton(
                            onClick = {
                                if (currentCardIndex < cardsList.size - 1) {
                                    AppHaptic.vibrateClick(context, hapticView)
                                    isFlipped = false
                                    currentCardIndex++
                                } else {
                                    AppHaptic.vibrateHeavy(context, hapticView)
                                    isCompleted = true
                                }
                            },
                            enabled = true,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Card",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Secondary Actions: Shuffle Deck & Review Unmastered Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle Deck Action
                        TextButton(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                cardsList = cardsList.shuffled()
                                currentCardIndex = 0
                                isFlipped = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Shuffle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Filter Mastered / Unmastered
                        val unmasteredCount = allCards.size - masteredCardIds.size
                        TextButton(
                            onClick = {
                                AppHaptic.vibrateClick(context, hapticView)
                                filterOnlyUnmastered = !filterOnlyUnmastered
                                currentCardIndex = 0
                                isFlipped = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (filterOnlyUnmastered) FocusBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (filterOnlyUnmastered) "All Cards (${allCards.size})" else "Unmastered ($unmasteredCount)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (filterOnlyUnmastered) FocusBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (filterOnlyUnmastered) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Deck Completion Celebration Dialog
        if (isCompleted) {
            Dialog(onDismissRequest = { isCompleted = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Celebration Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF10B981), Color(0xFF059669))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Deck Completed!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val masteryPercent = if (cardsList.isNotEmpty()) {
                            ((masteredCardIds.size.toFloat() / cardsList.size.toFloat()) * 100).toInt()
                        } else 100

                        Text(
                            text = "You reviewed all ${cardsList.size} cards!\nMastery Score: ${masteredCardIds.size}/${cardsList.size} ($masteryPercent%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    AppHaptic.vibrateClick(context, hapticView)
                                    cardsList = cardsList.shuffled()
                                    currentCardIndex = 0
                                    cardsList.forEach { card ->
                                        onToggleCardMastery?.invoke(card.id, false)
                                    }
                                    isFlipped = false
                                    isCompleted = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Shuffle & Restart", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    AppHaptic.vibrateClick(context, hapticView)
                                    isCompleted = false
                                    onBackClick()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
