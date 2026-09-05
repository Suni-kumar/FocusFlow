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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.BadgeChip
import com.example.ui.components.GlassCard
import com.example.ui.dialogs.ShareDictationDeckExportDialog
import com.example.viewmodel.DictationViewModel

@Composable
fun DictationWorkspaceScreen(
    viewModel: DictationViewModel,
    onDeckClick: (DictationDeck) -> Unit,
    onVoiceSettingsClick: () -> Unit,
    onSwipeUpFab: () -> Unit,
    gridColumns: Int = 2,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSelectionMode = uiState.selectedDeckIds.isNotEmpty()
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    var deckToDelete by remember { mutableStateOf<DictationDeck?>(null) }
    var deckToExport by remember { mutableStateOf<DictationDeck?>(null) }
    var isBatchDeleteConfirmOpen by remember { mutableStateOf(false) }

    if (deckToExport != null) {
        ShareDictationDeckExportDialog(
            deck = deckToExport!!,
            onDismiss = { deckToExport = null }
        )
    }

    // Filter decks based on search & tag
    val filteredDecks = remember(uiState.decks, uiState.searchQuery, uiState.selectedTag) {
        uiState.decks.filter { deck ->
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    deck.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    deck.description.contains(uiState.searchQuery, ignoreCase = true) ||
                    deck.words.any { it.word.contains(uiState.searchQuery, ignoreCase = true) }

            val matchesTag = when (uiState.selectedTag) {
                null, "All" -> true
                "Starred" -> deck.isStarred
                "AI Generated" -> deck.isAiGenerated
                "Custom" -> !deck.isAiGenerated
                else -> deck.tags.contains(uiState.selectedTag)
            }

            matchesSearch && matchesTag
        }
    }

    val deckChunks = remember(filteredDecks, gridColumns) { filteredDecks.chunked(gridColumns.coerceIn(1, 4)) }

    // Intercept back button if speed dial is open or in selection mode
    BackHandler(enabled = isSpeedDialOpen || isSelectionMode) {
        if (isSpeedDialOpen) {
            isSpeedDialOpen = false
        } else {
            viewModel.clearSelection()
        }
    }

    val totalWords = uiState.decks.sumOf { it.words.size }
    val avgAccuracy = if (uiState.decks.isNotEmpty()) {
        (uiState.decks.map { it.accuracy }.average() * 100).toInt()
    } else 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("dictation_workspace_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selection Mode Header (When active)
            if (isSelectionMode) {
                item(key = "dictation_selection_top_bar") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.clearSelection() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear selection",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "${uiState.selectedDeckIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        if (uiState.selectedDeckIds.size == uiState.decks.size) {
                                            viewModel.clearSelection()
                                        } else {
                                            uiState.decks.forEach {
                                                if (it.id !in uiState.selectedDeckIds) {
                                                    viewModel.toggleDeckSelection(it.id)
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (uiState.selectedDeckIds.size == uiState.decks.size) "Deselect All" else "Select All",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { isBatchDeleteConfirmOpen = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete selected",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Dictation Recall & Stats Banner
            item(key = "dictation_stats_banner") {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = null,
                    elevation = 6.dp,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    borderWidth = 1.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ambient decorative background
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Dictation Studio",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Audio practice & active recall",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { onVoiceSettingsClick() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Voice",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Dynamic Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DictationStatItem(title = "Decks", value = "${uiState.decks.size}")
                                DictationStatItem(title = "Words", value = "$totalWords")
                                DictationStatItem(title = "Accuracy", value = "$avgAccuracy", suffix = "%")
                            }
                        }
                    }
                }
            }

            // 3. Search Bar & Tag Filter Row
            item(key = "dictation_search_filter") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search dictation decks or words...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    // Tag Filter Chips
                    val filterTags = listOf("All", "Starred", "AI Generated", "Custom")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterTags) { tag ->
                            val isSelected = (uiState.selectedTag == tag) || (tag == "All" && uiState.selectedTag == null)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.setSelectedTag(if (tag == "All") null else tag)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Managed Decks Header
            item(key = "managed_dictation_decks_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Managed Decks (${filteredDecks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Total: ${uiState.decks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 5. Decks 2-Column Grid
            if (filteredDecks.isEmpty()) {
                item(key = "empty_dictation_decks_view") {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        backgroundColor = null,
                        elevation = 2.dp,
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
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "No Matching Decks" else "No Dictation Decks Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "Try changing your search terms or filter tag."
                                else "Tap the '+' button below to generate AI chapters or create manual decks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = deckChunks,
                    key = { chunk -> chunk.joinToString("_") { it.id } },
                    contentType = { "dictation_deck_chunk" }
                ) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (deck in chunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                val isSelected = deck.id in uiState.selectedDeckIds
                                ManagedDictationDeckItem(
                                    deck = deck,
                                    gridColumns = gridColumns,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleDeckSelection(deck.id)
                                        } else {
                                            onDeckClick(deck)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleDeckSelection(deck.id)
                                    },
                                    onEditClick = {
                                        viewModel.openEditDeckDialog(deck)
                                    },
                                    onShareClick = {
                                        deckToExport = deck
                                    },
                                    onToggleStar = {
                                        viewModel.toggleStarDeck(deck.id)
                                    },
                                    onDeleteClick = {
                                        deckToDelete = deck
                                    }
                                )
                            }
                        }
                        if (chunk.size < gridColumns) {
                            repeat(gridColumns - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item(key = "dictation_bottom_spacer") {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Single Deck Delete Confirmation Dialog
        deckToDelete?.let { targetDeck ->
            AlertDialog(
                onDismissRequest = { deckToDelete = null },
                title = { Text("Delete Deck?") },
                text = { Text("Are you sure you want to delete \"${targetDeck.title}\"? All ${targetDeck.words.size} words will be removed.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDeck(targetDeck.id)
                            deckToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deckToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Batch Delete Confirmation Dialog
        if (isBatchDeleteConfirmOpen) {
            AlertDialog(
                onDismissRequest = { isBatchDeleteConfirmOpen = false },
                title = { Text("Delete Selected Decks?") },
                text = { Text("Are you sure you want to delete ${uiState.selectedDeckIds.size} selected dictation decks?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSelectedDecks()
                            isBatchDeleteConfirmOpen = false
                        }
                    ) {
                        Text("Delete All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBatchDeleteConfirmOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 6. Speed Dial Floating Action Menu
        DictationSpeedDialFab(
            isExpanded = isSpeedDialOpen,
            onToggle = { isSpeedDialOpen = !isSpeedDialOpen },
            onDismiss = { isSpeedDialOpen = false },
            onAiGenerateClick = {
                isSpeedDialOpen = false
                viewModel.openAiGenerateDialog()
            },
            onCreateDeckClick = {
                isSpeedDialOpen = false
                viewModel.openCreateDeckDialog()
            },
            onSwipeUp = onSwipeUpFab
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManagedDictationDeckItem(
    deck: DictationDeck,
    gridColumns: Int = 2,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onToggleStar: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalView.current
    var isMenuOpen by remember { mutableStateOf(false) }

    val isCompact = gridColumns >= 3
    val isUltraCompact = gridColumns >= 4
    val cardHeight = if (isUltraCompact) 144.dp else if (isCompact) 148.dp else 152.dp
    val cardPadding = if (isUltraCompact) 8.dp else if (isCompact) 10.dp else 14.dp

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .testTag("managed_dictation_deck_${deck.id}"),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else null,
        elevation = if (isSelected) 4.dp else 1.5.dp,
        borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
        borderWidth = if (isSelected) 1.5.dp else 0.8.dp,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onLongClick()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 6.dp)) {
                // Top Row: Icon + Star + Selection Check / 3-dot Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = deck.categoryColor,
                            modifier = Modifier.size(if (isCompact) 17.dp else 20.dp)
                        )
                        if (deck.isStarred && !isUltraCompact) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
                            )
                        }
                    }

                    if (isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 18.dp else 22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isCompact) 11.dp else 14.dp)
                                )
                            }
                        }
                    } else {
                        // 3-dot Menu
                        Box {
                            IconButton(
                                onClick = { isMenuOpen = true },
                                modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Deck options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(if (isCompact) 14.dp else 18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isMenuOpen,
                                onDismissRequest = { isMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Practice Words") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = deck.categoryColor)
                                    },
                                    onClick = {
                                        isMenuOpen = false
                                        onClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit Deck & Words") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    },
                                    onClick = {
                                        isMenuOpen = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export & Share") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    onClick = {
                                        isMenuOpen = false
                                        onShareClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (deck.isStarred) "Unstar Deck" else "Star Deck") },
                                    leadingIcon = {
                                        Icon(
                                            if (deck.isStarred) Icons.Default.StarBorder else Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B)
                                        )
                                    },
                                    onClick = {
                                        isMenuOpen = false
                                        onToggleStar()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Deck", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        isMenuOpen = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }
                }

                // Deck Title
                Text(
                    text = deck.title,
                    style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = if (isUltraCompact) 11.sp else if (isCompact) 12.sp else 14.sp
                )

                // Chips row
                if (isUltraCompact) {
                    Text(
                        text = "${deck.words.size}w",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (deck.isAiGenerated) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                    .padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC),
                                    fontSize = if (isCompact) 9.sp else 10.sp
                                )
                            }
                        }
                        BadgeChip(
                            text = if (isCompact) "${deck.words.size}w" else "${deck.words.size} words",
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Progress & Accuracy section
            Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (deck.accuracy > 0.6f) "Mastered" else if (isCompact) "Due" else "Due: ${deck.words.size.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (isUltraCompact) 9.sp else if (isCompact) 10.sp else 11.sp
                    )
                    Text(
                        text = "${(deck.accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = deck.categoryColor,
                        fontSize = if (isUltraCompact) 9.sp else if (isCompact) 10.sp else 11.sp
                    )
                }

                // Custom Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 3.dp else 4.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(deck.accuracy.coerceIn(0f, 1f))
                            .height(if (isCompact) 3.dp else 4.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(deck.categoryColor)
                    )
                }
            }
        }
    }
}

@Composable
fun DictationSpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onAiGenerateClick: () -> Unit,
    onCreateDeckClick: () -> Unit,
    onSwipeUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalView.current
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }

    // Rotation animation: 0 deg (Plus '+') -> 135 deg (Cross '✕')
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dictationFabRotation"
    )

    // Morphing shape animation: Circle (28dp radius) -> Rounded Square (18dp radius)
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 18.dp else 28.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "dictationFabCornerRadius"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Dynamic Theme-Aware FAB Gradients
    val closedGradient = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor)
    )
    val expandedBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop: Consumes outside clicks when speed dial is open
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                    }
                    .testTag("dictation_speed_dial_scrim")
            )
        }

        // Speed Dial Container pinned to bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speed Dial Options with clean animation
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
                            initialScale = 0.90f,
                            animationSpec = tween(160, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(110, easing = FastOutSlowInEasing)) +
                        slideOutVertically(
                            targetOffsetY = { it / 3 },
                            animationSpec = tween(110)
                        ) +
                        scaleOut(
                            targetScale = 0.90f,
                            animationSpec = tween(110)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: AI Generate Dictation Deck
                    SpeedDialOption(
                        label = "AI Generate Deck",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = Color(0xFFC084FC),
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        testTag = "speed_dial_ai_generate_dictation",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onAiGenerateClick()
                        }
                    )

                    // Option 2: Create Manual Dictation Deck
                    SpeedDialOption(
                        label = "Create Deck",
                        icon = Icons.Default.RecordVoiceOver,
                        iconTint = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        testTag = "speed_dial_create_dictation_deck",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onCreateDeckClick()
                        }
                    )
                }
            }

            // Main Primary FAB Button
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = if (isExpanded) 12.dp else 8.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = primaryColor.copy(alpha = 0.4f),
                        spotColor = primaryColor.copy(alpha = 0.6f)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            verticalDragAccumulator += delta
                            if (verticalDragAccumulator < -40f) {
                                haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                verticalDragAccumulator = 0f
                                onSwipeUp()
                            }
                        },
                        onDragStopped = {
                            verticalDragAccumulator = 0f
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onToggle()
                    }
                    .testTag("dictation_fab"),
                shape = RoundedCornerShape(cornerRadius),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isExpanded) Brush.linearGradient(listOf(expandedBgColor, expandedBgColor))
                            else closedGradient
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Close actions" else "Add or generate deck",
                        tint = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialOption(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    labelContainerColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.testTag(testTag)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = labelContainerColor,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.clickable { onClick() }
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .clickable { onClick() },
            shape = CircleShape,
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DictationStatItem(
    title: String,
    value: String,
    suffix: String = ""
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
