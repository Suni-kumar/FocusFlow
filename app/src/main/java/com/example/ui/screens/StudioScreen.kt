package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashcardDeck
import com.example.ui.components.BadgeChip
import com.example.ui.components.GlassCard
import com.example.ui.theme.PrimaryContainerDark
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SurfaceSlateDark

@Composable
fun StudioScreen(
    onViewAllDecksClick: () -> Unit = {},
    onDeckClick: (FlashcardDeck) -> Unit = {},
    onCreateDeckClick: () -> Unit = {},
    onAiGenerateClick: () -> Unit = {},
    onSwipeUpFab: () -> Unit = {},
    decks: List<FlashcardDeck> = emptyList(),
    selectedDeckIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isSelectionMode = selectedDeckIds.isNotEmpty()
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    // Intercept back button if speed dial is open or in selection mode
    BackHandler(enabled = isSpeedDialOpen || isSelectionMode) {
        if (isSpeedDialOpen) {
            isSpeedDialOpen = false
        } else {
            onClearSelection()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Study Summary & Quick Stats Banner
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Active Recall Vault",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${decks.size} Decks • ${decks.sumOf { it.cardCount }} Flashcards in rotation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onViewAllDecksClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Managed Decks Section with Multi-selection support
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Managed Decks",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                        if (isSelectionMode) {
                            Text(
                                text = "${selectedDeckIds.size} decks selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TextButton(onClick = onViewAllDecksClick) {
                        Text(
                            text = "Dashboard (${decks.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Decks Grid
                if (decks.isEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Flashcard Decks Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the '+' button below to generate AI decks or create manual cards.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (chunk in decks.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (deck in chunk) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        val isSelected = deck.id in selectedDeckIds
                                        ManagedDeckItem(
                                            deck = deck,
                                            isSelected = isSelected,
                                            isSelectionMode = isSelectionMode,
                                            onClick = {
                                                if (isSelectionMode) {
                                                    onToggleSelection(deck.id)
                                                } else {
                                                    onDeckClick(deck)
                                                }
                                            },
                                            onLongClick = {
                                                onToggleSelection(deck.id)
                                            }
                                        )
                                    }
                                }
                                if (chunk.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }

        // Speed Dial Floating Action Menu with AI Generate Deck & Create Deck
        StudioSpeedDialFab(
            isExpanded = isSpeedDialOpen,
            onToggle = { isSpeedDialOpen = !isSpeedDialOpen },
            onDismiss = { isSpeedDialOpen = false },
            onAiGenerateClick = {
                isSpeedDialOpen = false
                onAiGenerateClick()
            },
            onCreateDeckClick = {
                isSpeedDialOpen = false
                onCreateDeckClick()
            },
            onSwipeUp = onSwipeUpFab
        )
    }
}

@Composable
fun StudioSpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onAiGenerateClick: () -> Unit,
    onCreateDeckClick: () -> Unit,
    onSwipeUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }

    // Rotation animation: 0 deg (Plus '+') -> 135 deg (Cross '✕')
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "studioFabRotation"
    )

    // Morphing shape animation: Circle (30dp radius) -> Rounded Square (16dp radius)
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 30.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "studioFabCornerRadius"
    )

    // Main FAB Colorful Gradients
    val closedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8B5CF6), // Neon Purple
            Color(0xFFEC4899), // Neon Pink
            Color(0xFF06B6D4)  // Neon Cyan
        )
    )
    val expandedGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF3366), // Hot Crimson
            Color(0xFFFF5E3A)  // Sunset Coral
        )
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop: Consumes outside clicks when speed dial is open
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                    }
                    .testTag("studio_speed_dial_scrim")
            )
        }

        // Speed Dial Container (Options Menu + Main FAB) pinned to bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Speed Dial Vertical Options with clean animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) +
                        scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(120)
                        ) +
                        scaleOut(
                            targetScale = 0.85f,
                            animationSpec = tween(120)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Option 1: AI Generate Deck (Vibrant Purple / Pink with Gemini sparkle)
                    StudioSpeedDialItem(
                        icon = Icons.Default.AutoAwesome,
                        label = "AI Generate Deck",
                        subtitle = "Generate cards with Gemini AI",
                        gradientColors = listOf(Color(0xFF9333EA), Color(0xFFDB2777)),
                        glowColor = Color(0xFFC084FC),
                        testTag = "speed_dial_ai_generate_deck",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAiGenerateClick()
                        }
                    )

                    // Option 2: Create Deck (Vibrant Cyan / Sky Blue)
                    StudioSpeedDialItem(
                        icon = Icons.Default.Style,
                        label = "Create Deck",
                        subtitle = "Manual flashcard deck",
                        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
                        glowColor = Color(0xFF38BDF8),
                        testTag = "speed_dial_create_deck",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCreateDeckClick()
                        }
                    )
                }
            }

            // Main Action FAB Button (Morphs from Circle '+' to Rounded Square '✕')
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = if (isExpanded) 18.dp else 12.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = if (isExpanded) Color(0xFFFF3366) else Color(0xFF8B5CF6),
                        spotColor = if (isExpanded) Color(0xFFFF5E3A) else Color(0xFFEC4899)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(if (isExpanded) expandedGradient else closedGradient)
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            verticalDragAccumulator += delta
                        },
                        onDragStopped = { velocity ->
                            if (velocity < -120f || verticalDragAccumulator < -25f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isExpanded) onDismiss()
                                onSwipeUp()
                            }
                            verticalDragAccumulator = 0f
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    }
                    .testTag("studio_speed_dial_main_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close studio menu" else "Open studio menu",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun StudioSpeedDialItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    gradientColors: List<Color>,
    glowColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = glowColor.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Option Text Pill Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.2.dp,
                color = glowColor.copy(alpha = 0.5f)
            ),
            shadowElevation = 8.dp,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Vibrant Gradient Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = gradientColors))
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManagedDeckItem(
    deck: FlashcardDeck,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .testTag("managed_deck_${deck.id}"),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconVector = when (deck.iconName) {
                        "psychology" -> Icons.Default.Psychology
                        "calculate" -> Icons.Default.Calculate
                        "translate" -> Icons.Default.Language
                        else -> Icons.Default.Terminal
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = deck.categoryColor,
                        modifier = Modifier.size(22.dp)
                    )

                    if (isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryDark else Color.Transparent)
                                .border(1.5.dp, if (isSelected) PrimaryDark else Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else if (deck.isAiGenerated) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC),
                                    fontSize = 10.sp
                                )
                            }
                            BadgeChip(
                                text = "${deck.cardCount} cards",
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        BadgeChip(
                            text = "${deck.cardCount} cards",
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = deck.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (deck.progress > 0.5f) "Reviewing" else "Due: ${deck.cardCount / 2}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(deck.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = deck.categoryColor,
                        fontSize = 11.sp
                    )
                }

                // Custom Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(deck.progress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(deck.categoryColor)
                    )
                }
            }
        }
    }
}
