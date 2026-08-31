package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.speech.FlashcardAudioPlayer
import com.example.ui.util.AppHaptic

/**
 * Natural Voice Speaker Button for Flashcards.
 * Replaces the expand icon with a responsive, high-yield audio player
 * that reads aloud the card text in its native accent (Hindi or English).
 */
@Composable
fun FlashcardSpeakerButton(
    textToSpeak: String,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val hapticView = LocalView.current
    val audioPlayer = remember { FlashcardAudioPlayer.getInstance(context) }

    val isSpeaking by audioPlayer.isSpeaking.collectAsState()
    val currentSpeakingText by audioPlayer.currentText.collectAsState()
    val isLoading by audioPlayer.isLoading.collectAsState()

    val isThisCardSpeaking = isSpeaking && currentSpeakingText == textToSpeak.trim()

    val infiniteTransition = rememberInfiniteTransition(label = "speechPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(
        modifier = modifier
            .size(34.dp)
            .scale(if (isThisCardSpeaking) pulseScale else 1f)
            .then(
                if (isThisCardSpeaking) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = activeColor.copy(alpha = 0.5f),
                        spotColor = activeColor.copy(alpha = 0.4f)
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .background(
                if (isThisCardSpeaking) {
                    activeColor.copy(alpha = if (isDark) 0.30f else 0.22f)
                } else {
                    if (isDark) Color(0xFF2A284A).copy(alpha = 0.6f)
                    else Color(0xFFF1E4EE).copy(alpha = 0.75f)
                }
            )
            .border(
                width = if (isThisCardSpeaking) 1.5.dp else 1.dp,
                color = if (isThisCardSpeaking) activeColor else (if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFD4B8CB).copy(alpha = 0.45f)),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 18.dp, color = activeColor),
                onClick = {
                    AppHaptic.vibrateClick(context, hapticView)
                    if (isThisCardSpeaking) {
                        audioPlayer.stop()
                    } else {
                        audioPlayer.speak(textToSpeak)
                    }
                }
            )
            .testTag("flashcard_speaker_button"),
        contentAlignment = Alignment.Center
    ) {
        if (isThisCardSpeaking) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Stop reading flashcard",
                tint = activeColor,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Read flashcard aloud",
                tint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF4A4458),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
