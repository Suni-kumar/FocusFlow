package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SurfaceSlateDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiGenerateDeckDialog(
    initialPrompt: String = "",
    isGenerating: Boolean = false,
    progressMessage: String = "Generating AI Study Deck...",
    hasCustomApiKey: Boolean = false,
    onDismiss: () -> Unit,
    onConfigureApiKeyClick: () -> Unit = {},
    onGenerate: (topicOrNotes: String, cardCount: Int) -> Unit
) {
    var promptText by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var selectedCardCount by remember { mutableIntStateOf(10) }
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "ai_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isGenerating) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_scale"
    )

    val countOptions = listOf(5, 10, 15, 20)

    val quickTopics = listOf(
        "Cardiac Physiology",
        "Quantum Mechanics",
        "Organic Chemistry",
        "System Architecture",
        "Spanish Vocabulary"
    )

    Dialog(
        onDismissRequest = {
            if (!isGenerating) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isGenerating,
            dismissOnClickOutside = !isGenerating,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF131024), // Luxury Obsidian Purple
                            Color(0xFF090714)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFA855F7).copy(alpha = glowAlpha),
                            Color(0xFFEC4899).copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.12f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFA855F7))
                .padding(20.dp)
                .testTag("ai_deck_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Sparkles badge & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                                    )
                                )
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFDB2777)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AI Deck Generator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Powered by Google Gemini",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC084FC),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (!isGenerating) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Dual-Tier Routing Status Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (hasCustomApiKey) Color(0xFF064E3B).copy(alpha = 0.4f)
                            else Color(0xFF1E1B4B).copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (hasCustomApiKey) Color(0xFF10B981).copy(alpha = 0.4f)
                            else Color(0xFF818CF8).copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (hasCustomApiKey) Color(0xFF10B981) else Color(0xFF818CF8))
                            )
                            Text(
                                text = if (hasCustomApiKey) "Tier 1: Personal Gemini API Key (BYOK)"
                                else "Tier 2: System Gemini Cloud / Smart Taxonomy",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasCustomApiKey) Color(0xFF6EE7B7) else Color(0xFFC7D2FE),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!hasCustomApiKey && !isGenerating) {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA78BFA),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onConfigureApiKeyClick() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Topic / Notes Input Area
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "STUDY TOPIC OR SYLLABUS NOTES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                        letterSpacing = 0.08.sp
                    )

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                text = "e.g. \"Cardiac Action Potentials\", \"Quantum Entanglement\", or paste raw lecture notes...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0E17))
                            .border(
                                1.dp,
                                if (promptText.isNotBlank()) Color(0xFFA855F7).copy(alpha = 0.6f)
                                else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("ai_prompt_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }

                // Quick Suggestion Chips (when input is blank)
                if (promptText.isBlank() && !isGenerating) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "QUICK TOPICS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickTopics.forEach { topic ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(9999.dp))
                                        .clickable { promptText = topic }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Card Count Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TARGET FLASHCARD COUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                        letterSpacing = 0.08.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        countOptions.forEach { count ->
                            val isSelected = selectedCardCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(
                                            listOf(Color(0xFF9333EA), Color(0xFF7C3AED))
                                        )
                                        else Brush.horizontalGradient(
                                            listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFC084FC) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = !isGenerating) {
                                        selectedCardCount = count
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count Cards",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Output Taxonomy Features Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Includes: #Definitions, #Formulas, #HighYield tags",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "3D Recall Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Loading Indicator / Status Banner
                AnimatedVisibility(visible = isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF581C87).copy(alpha = 0.5f))
                            .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFF472B6),
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = progressMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isGenerating) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Main Action CTA Button with Gradient & Glow
                    Button(
                        onClick = {
                            if (promptText.isNotBlank() && !isGenerating) {
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onGenerate(promptText, selectedCardCount)
                            }
                        },
                        enabled = promptText.isNotBlank() && !isGenerating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .weight(if (isGenerating) 1f else 2f)
                            .height(48.dp)
                            .scale(buttonScale)
                            .background(
                                brush = if (promptText.isNotBlank() && !isGenerating) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f))
                                    )
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("ai_generate_submit_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (promptText.isNotBlank()) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isGenerating) "Synthesizing Deck..." else "Generate Study Deck",
                                fontWeight = FontWeight.Bold,
                                color = if (promptText.isNotBlank()) Color.White else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
