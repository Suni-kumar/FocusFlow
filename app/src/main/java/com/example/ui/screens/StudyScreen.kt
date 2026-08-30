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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.FlashcardDeck
import com.example.model.MockDataSource
import com.example.ui.theme.FocusBlue
import com.example.ui.theme.LiquidGlassReflection
import com.example.ui.util.AppHaptic
import kotlinx.coroutines.launch

@Composable
fun StudyScreen(
    deck: FlashcardDeck = MockDataSource.decks[0],
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onDeckProgressUpdate: ((progress: Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var cardsList by remember(deck) {
        mutableStateOf(deck.cards.ifEmpty { MockDataSource.neuralPlasticityCards })
    }
    var currentCardIndex by remember(cardsList) { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isCardExpanded by remember { mutableStateOf(false) }
    var masteredCardIds by remember(deck) { mutableStateOf(setOf<String>()) }
    var isCompleted by remember { mutableStateOf(false) }
    var isZenMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val hapticView = LocalView.current
    val density = LocalDensity.current.density

    val scope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }

    val currentCard = cardsList[currentCardIndex.coerceIn(0, cardsList.size - 1)]

    val frontScrollState = rememberScrollState()
    val backScrollState = rememberScrollState()

    LaunchedEffect(currentCardIndex, isFlipped) {
        frontScrollState.scrollTo(0)
        backScrollState.scrollTo(0)
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

    // Intercept hardware/gesture back press
    BackHandler(enabled = true) {
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

            Spacer(modifier = Modifier.height(8.dp))

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

                // Main Flippable Card with Horizontal Swipe + Vertical Expand Swipe
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = dragOffsetX.value
                            rotationZ = (dragOffsetX.value / 35f).coerceIn(-12f, 12f)
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .pointerInput(currentCardIndex, isCardExpanded) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    scope.launch {
                                        dragOffsetX.snapTo(dragOffsetX.value + dragAmount)
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        val offset = dragOffsetX.value
                                        if (offset < -130f && currentCardIndex < cardsList.size - 1) {
                                            AppHaptic.vibrateClick(context, hapticView)
                                            dragOffsetX.animateTo(-500f, tween(150))
                                            isFlipped = false
                                            currentCardIndex++
                                            dragOffsetX.snapTo(400f)
                                            dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                                        } else if (offset > 130f && currentCardIndex > 0) {
                                            AppHaptic.vibrateClick(context, hapticView)
                                            dragOffsetX.animateTo(500f, tween(150))
                                            isFlipped = false
                                            currentCardIndex--
                                            dragOffsetX.snapTo(-400f)
                                            dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                                        } else {
                                            dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        dragOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                    }
                                }
                            )
                        }
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
                            // Top Row: Tag & Expand button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                                    currentCard.tags.take(2).forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                                .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFC084FC),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Quick Expand/Collapse Toggle Button
                                IconButton(
                                    onClick = {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        isCardExpanded = !isCardExpanded
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCardExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                        contentDescription = if (isCardExpanded) "Collapse Card" else "Expand Card for long reading",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
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
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { _, dragAmount ->
                                            if (dragAmount > 15f && !isCardExpanded) {
                                                AppHaptic.vibrateClick(context, hapticView)
                                                isCardExpanded = true
                                            } else if (dragAmount < -15f && isCardExpanded) {
                                                AppHaptic.vibrateClick(context, hapticView)
                                                isCardExpanded = false
                                            }
                                        }
                                    }
                                    .padding(vertical = 2.dp),
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
                            // Top Row: Tag & Expand button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                                    currentCard.tags.take(2).forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6EE7B7),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        AppHaptic.vibrateClick(context, hapticView)
                                        isCardExpanded = !isCardExpanded
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCardExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                        contentDescription = if (isCardExpanded) "Collapse Card" else "Expand Card",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
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
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { _, dragAmount ->
                                            if (dragAmount > 15f && !isCardExpanded) {
                                                AppHaptic.vibrateClick(context, hapticView)
                                                isCardExpanded = true
                                            } else if (dragAmount < -15f && isCardExpanded) {
                                                AppHaptic.vibrateClick(context, hapticView)
                                                isCardExpanded = false
                                            }
                                        }
                                    }
                                    .padding(vertical = 2.dp),
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Flip Card",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Mastered Button
                        Button(
                            onClick = {
                                AppHaptic.vibrateHeavy(context, hapticView)
                                masteredCardIds = masteredCardIds + currentCard.id
                                val calculatedProgress = (masteredCardIds.size.toFloat() / cardsList.size.toFloat()).coerceIn(0f, 1f)
                                onDeckProgressUpdate?.invoke(calculatedProgress)

                                if (currentCardIndex < cardsList.size - 1) {
                                    isFlipped = false
                                    currentCardIndex++
                                } else {
                                    isCompleted = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusBlue
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = FocusBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mastered",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
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
                            text = "Shuffle Deck",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
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
                                    masteredCardIds = emptySet()
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
