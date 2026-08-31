package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Flashcard
import com.example.ui.theme.Local3DGlassEnabled
import com.example.ui.theme.LocalAccentTheme
import com.example.ui.util.AppHaptic

@Composable
fun FlippableFlashcard(
    flashcard: Flashcard,
    modifier: Modifier = Modifier,
    initialFlipped: Boolean = false,
    onFlipChanged: (Boolean) -> Unit = {}
) {
    var isFlipped by remember(flashcard.id) { mutableStateOf(initialFlipped) }
    val context = LocalContext.current
    val hapticView = LocalView.current
    val density = LocalDensity.current.density
    val is3DEnabled = Local3DGlassEnabled.current
    val accentTheme = LocalAccentTheme.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val frontScroll = rememberScrollState()
    val backScroll = rememberScrollState()

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "flashcardFlipAnimation"
    )

    val isFrontFacing = rotation <= 90f

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = accentTheme.secondaryColor
    val answerColor = Color(0xFF10B981)

    // Dynamic 3D glass background & borders
    val cardBackground = remember(is3DEnabled, isDark, isFlipped) {
        if (is3DEnabled) {
            if (isDark) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1C38).copy(alpha = 0.88f),
                        Color(0xFF0F0F1E).copy(alpha = 0.94f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFF1F5F9).copy(alpha = 0.85f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    if (isDark) Color(0xFF16152B) else Color.White,
                    if (isDark) Color(0xFF16152B) else Color.White
                )
            )
        }
    }

    val cardBorder = remember(is3DEnabled, isDark, isFlipped, primaryColor, accentTheme) {
        if (is3DEnabled) {
            if (isFlipped) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.45f else 0.90f),
                        answerColor.copy(alpha = 0.70f),
                        answerColor.copy(alpha = 0.20f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.40f else 0.95f),
                        accentTheme.primaryColor.copy(alpha = 0.70f),
                        accentTheme.secondaryColor.copy(alpha = 0.30f)
                    )
                )
            }
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    (if (isFlipped) answerColor else primaryColor).copy(alpha = 0.4f),
                    (if (isFlipped) answerColor else primaryColor).copy(alpha = 0.2f)
                )
            )
        }
    }

    val shadowModifier = if (is3DEnabled) {
        Modifier.shadow(
            elevation = 14.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = if (isDark) (if (isFlipped) answerColor else accentTheme.primaryColor).copy(alpha = 0.45f) else Color(0x25000000),
            spotColor = if (isDark) (if (isFlipped) answerColor else accentTheme.secondaryColor).copy(alpha = 0.35f) else Color(0x35000000)
        )
    } else {
        Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = (if (is3DEnabled) 18f else 10f) * density
            }
            .clip(RoundedCornerShape(24.dp))
            .background(cardBackground)
            .border(if (is3DEnabled) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = accentTheme.primaryColor),
                onClick = {
                    AppHaptic.vibrateClick(context, hapticView)
                    isFlipped = !isFlipped
                    onFlipChanged(isFlipped)
                }
            )
            .testTag("flippable_flashcard_${flashcard.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Specular glint line at top
        if (is3DEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.12f else 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 180f)
                        )
                    )
            )
        }

        if (isFrontFacing) {
            // FRONT FACE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Meta Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(accentTheme.primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, accentTheme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(9999.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "QUESTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) accentTheme.primaryColor else accentTheme.secondaryColor,
                            letterSpacing = 0.15.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = flashcard.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        FlashcardSpeakerButton(
                            textToSpeak = flashcard.front,
                            activeColor = accentTheme.primaryColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Front Question / Term (Scrollable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(frontScroll),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flashcard.front,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Clean Bottom Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Tap to flip",
                        tint = (if (isDark) accentTheme.primaryColor else accentTheme.secondaryColor).copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // BACK FACE (Rotated 180deg to display correctly when flipped)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Meta Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(answerColor.copy(alpha = 0.15f))
                            .border(1.dp, answerColor.copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ANSWER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = answerColor,
                            letterSpacing = 0.15.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = flashcard.topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        FlashcardSpeakerButton(
                            textToSpeak = flashcard.back,
                            activeColor = answerColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Back Answer / Explanation (Scrollable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(backScroll),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flashcard.back,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 25.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Clean Bottom Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Tap to flip",
                        tint = answerColor.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
