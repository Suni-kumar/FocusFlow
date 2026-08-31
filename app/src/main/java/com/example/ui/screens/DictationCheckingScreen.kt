package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.speech.FlashcardAudioPlayer
import com.example.model.DictationDeck
import com.example.model.DictationWord
import com.example.model.DictationWordStatus
import com.example.viewmodel.DictationViewModel

@Composable
fun DictationCheckingScreen(
    deck: DictationDeck,
    viewModel: DictationViewModel,
    onBackClick: () -> Unit,
    onRestartDictation: () -> Unit,
    onFinishAndSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayer = remember { FlashcardAudioPlayer.getInstance(context) }
    val uiState by viewModel.uiState.collectAsState()

    val totalWords = deck.words.size
    val results = uiState.wordResults

    val correctCount = deck.words.count { results[it.id] == DictationWordStatus.CORRECT }
    val incorrectCount = deck.words.count { results[it.id] == DictationWordStatus.INCORRECT }
    val needsPracticeCount = deck.words.count { results[it.id] == DictationWordStatus.NEEDS_PRACTICE }
    val accuracy = if (totalWords > 0) correctCount.toFloat() / totalWords.toFloat() else 0f

    val accentColor = deck.categoryColor

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("dictation_checking_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sequence Checking Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = deck.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onRestartDictation,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart dictation",
                        tint = accentColor
                    )
                }
            }

            // 2. Main Sequence List & Summary
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Summary Score Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Dictation Summary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "✅ $correctCount Correct",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4EDEA3),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "❌ $incorrectCount Incorrect",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF7886),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (needsPracticeCount > 0) {
                                        Text(
                                            text = "⚠️ $needsPracticeCount Review",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFF59E0B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Text(
                                    text = "Check each word in written sequence below:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Accuracy Ring
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { accuracy },
                                    modifier = Modifier.size(54.dp),
                                    color = accentColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeWidth = 5.dp
                                )
                                Text(
                                    text = "${(accuracy * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Sequence Header
                item {
                    Text(
                        text = "Sequence Order (${deck.words.size} Words)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sequential Word Checking Cards
                itemsIndexed(deck.words) { index, word ->
                    val status = results[word.id] ?: DictationWordStatus.UNTESTED
                    val statusBgColor by animateColorAsState(
                        targetValue = when (status) {
                            DictationWordStatus.CORRECT -> Color(0xFF4EDEA3).copy(alpha = 0.12f)
                            DictationWordStatus.INCORRECT -> Color(0xFFFF7886).copy(alpha = 0.12f)
                            DictationWordStatus.NEEDS_PRACTICE -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                            DictationWordStatus.UNTESTED -> MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        label = "cardBg"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checking_word_item_$index"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = statusBgColor),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (status) {
                                DictationWordStatus.CORRECT -> Color(0xFF4EDEA3).copy(alpha = 0.6f)
                                DictationWordStatus.INCORRECT -> Color(0xFFFF7886).copy(alpha = 0.6f)
                                DictationWordStatus.NEEDS_PRACTICE -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                                DictationWordStatus.UNTESTED -> MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.3f
                                )
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Word Title & Sequence Number
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = accentColor.copy(alpha = 0.20f)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = word.word,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (word.phonetic.isNotBlank()) {
                                            Text(
                                                text = word.phonetic,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = accentColor,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Audio Replay Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { audioPlayer.speakDictationWord(word.word) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Speak word",
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            audioPlayer.speakDictationMeaning(
                                                word.word,
                                                word.meaning
                                            )
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Speak meaning",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Meaning & Example Sentence
                            Text(
                                text = word.meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (word.exampleSentence.isNotBlank()) {
                                Text(
                                    text = "\"${word.exampleSentence}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            // Grading Chips (Correct, Incorrect, Review)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Correct Button
                                CheckingGradeChip(
                                    label = "Correct",
                                    icon = Icons.Default.CheckCircle,
                                    color = Color(0xFF4EDEA3),
                                    isSelected = status == DictationWordStatus.CORRECT,
                                    onClick = {
                                        viewModel.markWordStatus(
                                            word.id,
                                            DictationWordStatus.CORRECT
                                        )
                                    }
                                )

                                // Incorrect Button
                                CheckingGradeChip(
                                    label = "Incorrect",
                                    icon = Icons.Default.Close,
                                    color = Color(0xFFFF7886),
                                    isSelected = status == DictationWordStatus.INCORRECT,
                                    onClick = {
                                        viewModel.markWordStatus(
                                            word.id,
                                            DictationWordStatus.INCORRECT
                                        )
                                    }
                                )

                                // Review Button
                                CheckingGradeChip(
                                    label = "Review",
                                    icon = Icons.Default.ErrorOutline,
                                    color = Color(0xFFF59E0B),
                                    isSelected = status == DictationWordStatus.NEEDS_PRACTICE,
                                    onClick = {
                                        viewModel.markWordStatus(
                                            word.id,
                                            DictationWordStatus.NEEDS_PRACTICE
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Bottom Complete & Save Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onRestartDictation) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Restart",
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.finishPracticeSession(recordAccuracy = true)
                            onFinishAndSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dictation_finish_and_save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Finish & Save Score",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckingGradeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.2.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
