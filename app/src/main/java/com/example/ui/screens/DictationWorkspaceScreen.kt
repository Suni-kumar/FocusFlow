package com.example.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DictationDeck
import com.example.ui.components.GlassCard
import com.example.viewmodel.DictationViewModel

@Composable
fun DictationWorkspaceScreen(
    viewModel: DictationViewModel,
    onDeckClick: (DictationDeck) -> Unit,
    onVoiceSettingsClick: () -> Unit,
    onSwipeUpFab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    var isSpeedDialOpen by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isSpeedDialOpen) {
        isSpeedDialOpen = false
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Filter decks based on search and tag
    val filteredDecks = remember(uiState.decks, uiState.searchQuery, uiState.selectedTag) {
        uiState.decks.filter { deck ->
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    deck.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    deck.description.contains(uiState.searchQuery, ignoreCase = true) ||
                    deck.words.any { it.word.contains(uiState.searchQuery, ignoreCase = true) }
            val matchesTag = uiState.selectedTag == null ||
                    (uiState.selectedTag == "Starred" && deck.isStarred) ||
                    (uiState.selectedTag == "AI Generated" && deck.isAiGenerated) ||
                    deck.tags.contains(uiState.selectedTag)
            matchesQuery && matchesTag
        }
    }

    val totalWordsCount = uiState.decks.sumOf { it.words.size }
    val averageAccuracy = if (uiState.decks.isNotEmpty()) {
        uiState.decks.map { it.accuracy }.average().toFloat()
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("dictation_workspace_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Hero Dictation Studio Stats Banner
            item(key = "dictation_hero_banner") {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = null,
                    elevation = 6.dp,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    borderWidth = 1.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ambient Radial Glow
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .align(Alignment.TopEnd)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.18f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        primaryColor,
                                                        primaryColor.copy(alpha = 0.7f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Dictation Studio",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Blind Audio Spoken Practice & Voice Commands",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onVoiceSettingsClick,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Voice Settings",
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 3-Stat Counters Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DictationStatItem(
                                    value = "${uiState.decks.size}",
                                    label = "Chapters"
                                )
                                DictationStatItem(
                                    value = "$totalWordsCount",
                                    label = "Words"
                                )
                                DictationStatItem(
                                    value = "${(averageAccuracy * 100).toInt()}%",
                                    label = "Accuracy"
                                )
                                DictationStatItem(
                                    value = "Voice Mic",
                                    label = "Ready"
                                )
                            }
                        }
                    }
                }
            }

            // 2. Search & Filter Bar
            item(key = "search_and_filters") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search chapters, vocabulary words or tags...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dictation_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    // Tag Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterTagChip(
                                label = "All Decks",
                                isSelected = uiState.selectedTag == null,
                                onClick = { viewModel.setSelectedTag(null) }
                            )
                        }
                        item {
                            FilterTagChip(
                                label = "⭐ Starred",
                                isSelected = uiState.selectedTag == "Starred",
                                onClick = { viewModel.setSelectedTag("Starred") }
                            )
                        }
                        item {
                            FilterTagChip(
                                label = "✨ AI Generated",
                                isSelected = uiState.selectedTag == "AI Generated",
                                onClick = { viewModel.setSelectedTag("AI Generated") }
                            )
                        }
                        item {
                            FilterTagChip(
                                label = "Academic",
                                isSelected = uiState.selectedTag == "Academic",
                                onClick = { viewModel.setSelectedTag("Academic") }
                            )
                        }
                        item {
                            FilterTagChip(
                                label = "STEM",
                                isSelected = uiState.selectedTag == "STEM",
                                onClick = { viewModel.setSelectedTag("STEM") }
                            )
                        }
                    }
                }
            }

            // 3. Chapter-wise Dictation Decks Header
            item(key = "decks_list_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chapter Decks (${filteredDecks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Tap Play for Blind Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // 4. Chapter Deck Cards List
            if (filteredDecks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No Dictation Decks Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the + button to create a chapter deck or generate words with AI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredDecks, key = { it.id }) { deck ->
                    DictationDeckCard(
                        deck = deck,
                        onPlayClick = {
                            onDeckClick(deck)
                        },
                        onEditClick = {
                            viewModel.openEditDeckDialog(deck)
                        },
                        onToggleStar = {
                            viewModel.toggleStarDeck(deck.id)
                        },
                        onDeleteClick = {
                            viewModel.deleteDeck(deck.id)
                        }
                    )
                }
            }
        }

        // 5. Floating Action Button & Speed Dial
        DictationSpeedDialFab(
            isOpen = isSpeedDialOpen,
            onToggle = {
                isSpeedDialOpen = !isSpeedDialOpen
                haptic.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            },
            onCreateDeck = {
                isSpeedDialOpen = false
                viewModel.openCreateDeckDialog()
            },
            onAiGenerate = {
                isSpeedDialOpen = false
                viewModel.openAiGenerateDialog()
            },
            onSwipeUp = onSwipeUpFab,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun DictationDeckCard(
    deck: DictationDeck,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onToggleStar: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accentColor = deck.categoryColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dictation_deck_card_${deck.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Title, Tag & Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = deck.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${deck.words.size} Words • ${deck.lastPracticed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (deck.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star deck",
                        tint = if (deck.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (deck.description.isNotBlank()) {
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Accuracy & Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Accuracy Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(deck.accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                LinearProgressIndicator(
                    progress = { deck.accuracy },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            // Bottom Actions Row: Edit, Delete & Big Play Practice Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit Words & Meanings Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onEditClick)
                            .testTag("edit_deck_btn_${deck.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit Words",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Delete Deck Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete deck",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Start Blind Dictation Play Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onPlayClick)
                        .testTag("start_dictation_play_${deck.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Start Dictation",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DictationStatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun FilterTagChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Dictation Speed Dial FAB with Swipe-Up detection for Workspace switching
 */
@Composable
private fun DictationSpeedDialFab(
    isOpen: Boolean,
    onToggle: () -> Unit,
    onCreateDeck: () -> Unit,
    onAiGenerate: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fab_rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Speed Dial Action 1: Create Manual Deck
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(140)) + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.8f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Create Deck",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCreateDeck)
                        .testTag("speed_dial_create_dictation_deck"),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Deck",
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Speed Dial Action 2: AI Deck Generator
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.8f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "AI Word / Deck Import",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAiGenerate)
                        .testTag("speed_dial_ai_dictation_generate"),
                    shape = CircleShape,
                    color = primaryColor,
                    shadowElevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Generate",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Main Plus Hero FAB
        Surface(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .shadow(12.dp, CircleShape, ambientColor = primaryColor.copy(alpha = 0.5f), spotColor = primaryColor.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onToggle
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        dragOffsetY += delta
                        if (dragOffsetY < -50f) {
                            dragOffsetY = 0f
                            onSwipeUp()
                        }
                    },
                    onDragStopped = { dragOffsetY = 0f }
                )
                .testTag("dictation_main_fab"),
            shape = CircleShape,
            color = primaryColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Dictation Actions",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}
