package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import com.example.ui.theme.Local3DGlassEnabled
import com.example.ui.theme.LocalAccentTheme

import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SepFolTopAppBar(
    modifier: Modifier = Modifier,
    title: String = "FocusFlow",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    selectionCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onRenameSelected: () -> Unit = {}
) {
    val haptic = LocalView.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    val is3DEnabled = Local3DGlassEnabled.current
    val accentTheme = LocalAccentTheme.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val topBarBackground = remember(is3DEnabled, isDark, surfaceContainerHigh) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF16152B).copy(alpha = 0.85f),
                        Color(0xFF100F20).copy(alpha = 0.90f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF7FC).copy(alpha = 0.88f),
                        Color(0xFFF3E3EF).copy(alpha = 0.78f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                listOf(
                    surfaceContainerHigh,
                    surfaceContainerHigh
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .pointerInput(isSearchActive, selectionCount) {
                if (selectionCount == 0) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val startPos = down.position
                        var triggered = false

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                val currentPos = change.position
                                val totalDx = currentPos.x - startPos.x
                                val totalDy = currentPos.y - startPos.y

                                if (Math.abs(totalDx) > Math.abs(totalDy) * 1.1f) {
                                    if (!isSearchActive && totalDx < -30f && !triggered) {
                                        // Swipe LEFT on top bar -> Open search!
                                        triggered = true
                                        haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        onSearchActiveChange(true)
                                        change.consume()
                                        break
                                    } else if (isSearchActive && totalDx > 30f && !triggered) {
                                        // Swipe RIGHT in search bar -> Close search!
                                        triggered = true
                                        haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        onSearchActiveChange(false)
                                        onSearchQueryChange("")
                                        change.consume()
                                        break
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
            .background(topBarBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        if (is3DEnabled) accentTheme.primaryColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ),
                shape = RectangleShape
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = when {
                selectionCount > 0 -> "SELECTION"
                isSearchActive -> "SEARCH"
                else -> "NORMAL"
            },
            transitionSpec = {
                val enterSpec = spring<Float>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
                val exitSpec = tween<Float>(durationMillis = 180)

                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { if (targetState == "SEARCH") it else -it } + fadeIn(animationSpec = enterSpec))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { if (initialState == "SEARCH") it else -it } + fadeOut(animationSpec = exitSpec))
            },
            label = "topAppBarModeTransition"
        ) { mode ->
            when (mode) {
                "SELECTION" -> {
                    // Multi-Select Action Bar Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Close Selection and Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onClearSelection,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selection",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "$selectionCount selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        // Right: Rename & Delete
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (selectionCount == 1) {
                                IconButton(
                                    onClick = onRenameSelected,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DriveFileRenameOutline,
                                        contentDescription = "Rename selected item",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDeleteSelected,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete selected items",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                "SEARCH" -> {
                    // Active Search Bar Mode
                    val searchCapsuleBg = if (is3DEnabled) {
                        if (isDark) Color(0xFF1E1D36).copy(alpha = 0.85f) else Color(0xFFFFF2F8).copy(alpha = 0.90f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    val searchBorderBrush = if (is3DEnabled) {
                        Brush.horizontalGradient(
                            listOf(
                                accentTheme.primaryColor.copy(alpha = 0.6f),
                                accentTheme.secondaryColor.copy(alpha = 0.4f)
                            )
                        )
                    } else {
                        SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(searchCapsuleBg)
                            .border(1.2.dp, searchBorderBrush, RoundedCornerShape(22.dp))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onSearchActiveChange(false)
                                onSearchQueryChange("")
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search files, notes, decks...",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .testTag("top_bar_search_input")
                            )
                        }

                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search query",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Clean Top Bar: Brand & Profile on Left, Settings Gear on Right (Swipe left to open search)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile & Brand Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
                                    onClick = onProfileClick
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (is3DEnabled) {
                                            Brush.radialGradient(
                                                listOf(
                                                    accentTheme.primaryColor.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                        } else {
                                            SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (is3DEnabled) accentTheme.primaryColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "FocusFlow Brand",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }

                        // Action Icon: Settings Gear (Tapping opens Settings, Swiping Left opens Search)
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onSettingsClick()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                        val startX = down.position.x
                                        do {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.firstOrNull()
                                            if (change != null) {
                                                val dx = change.position.x - startX
                                                if (dx < -25f) {
                                                    haptic.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                                    onSearchActiveChange(true)
                                                    change.consume()
                                                    break
                                                }
                                            }
                                        } while (event.changes.any { it.pressed })
                                    }
                                }
                                .testTag("top_bar_settings_gear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
