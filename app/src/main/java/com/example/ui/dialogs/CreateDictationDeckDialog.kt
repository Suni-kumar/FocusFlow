package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DictationWord
import java.util.UUID

@Composable
fun CreateDictationDeckDialog(
    onDismiss: () -> Unit,
    onCreateDeck: (title: String, desc: String, color: Color, words: List<DictationWord>, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF4EDEA3)) }

    // List of words being added
    val wordsList = remember {
        mutableStateListOf(
            Pair("Ephemeral", "Lasting for a very short time; fleeting"),
            Pair("Eloquent", "Fluent or persuasive in speaking or writing"),
            Pair("Serendipity", "Finding good things without looking for them")
        )
    }

    var newWordText by remember { mutableStateOf("") }
    var newMeaningText by remember { mutableStateOf("") }
    var isBulkPasteOpen by remember { mutableStateOf(false) }
    var bulkPasteContent by remember { mutableStateOf("") }

    val palette = listOf(
        Color(0xFF4EDEA3),
        Color(0xFF818CF8),
        Color(0xFFFF7886),
        Color(0xFF06B6D4),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF10B981),
        Color(0xFF6366F1)
    )

    fun handleSave() {
        val finalWords = wordsList.toMutableList()
        // If user typed something in input fields, add it automatically
        if (newWordText.isNotBlank()) {
            finalWords.add(Pair(newWordText.trim(), newMeaningText.trim()))
        }
        if (finalWords.isEmpty()) return

        val tagList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val dictWords = finalWords.map { (w, m) ->
            DictationWord(
                id = "dw_${UUID.randomUUID()}",
                word = w,
                meaning = if (m.isNotBlank()) m else "Definition for $w",
                exampleSentence = "Spell and memorize the word $w."
            )
        }

        onCreateDeck(
            title.trim().ifBlank { "Custom Chapter ${System.currentTimeMillis() % 1000}" },
            description.trim(),
            selectedColor,
            dictWords,
            if (tagList.isEmpty()) listOf("Dictation", "Custom") else tagList
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 690.dp)
                .padding(vertical = 16.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("create_dictation_deck_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(selectedColor.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = selectedColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Create Dictation Deck",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Chapter words with meanings for audio testing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Deck / Chapter Title (e.g. Chapter 4: GRE Terms)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = selectedColor,
                                focusedLabelColor = selectedColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Topic summary (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = selectedColor,
                                focusedLabelColor = selectedColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Color Palette
                    item {
                        Text(
                            text = "Deck Color Accent",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            palette.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 2.5.dp else 0.dp,
                                            color = if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }

                    // Words Header & Bulk Paste Toggle
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Words in Deck (${wordsList.size + if (newWordText.isNotBlank()) 1 else 0})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { isBulkPasteOpen = !isBulkPasteOpen }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = selectedColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBulkPasteOpen) "Manual Input" else "Bulk Paste",
                                    color = selectedColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bulk Paste Section
                    if (isBulkPasteOpen) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Paste multiple words (one per line, format: Word : Meaning)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedTextField(
                                        value = bulkPasteContent,
                                        onValueChange = { bulkPasteContent = it },
                                        placeholder = {
                                            Text("Resilience : Ability to bounce back\nPragmatic : Realistic\nEphemeral : Short-lived")
                                        },
                                        minLines = 4,
                                        maxLines = 8,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Button(
                                        onClick = {
                                            val lines = bulkPasteContent.split("\n", ";", "\r").map { it.trim() }.filter { it.isNotBlank() }
                                            for (line in lines) {
                                                val delimiter = if (line.contains(":")) ":" else if (line.contains("-")) "-" else "="
                                                if (line.contains(delimiter)) {
                                                    val parts = line.split(delimiter, limit = 2)
                                                    val w = parts[0].trim()
                                                    val m = parts.getOrNull(1)?.trim() ?: ""
                                                    if (w.isNotBlank()) {
                                                        wordsList.add(Pair(w, m))
                                                    }
                                                } else {
                                                    wordsList.add(Pair(line, "Definition for $line"))
                                                }
                                            }
                                            bulkPasteContent = ""
                                            isBulkPasteOpen = false
                                        },
                                        enabled = bulkPasteContent.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Add Bulk Words to Deck",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Existing Words List
                    itemsIndexed(wordsList) { index, pair ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedColor
                                    )
                                    Column {
                                        Text(
                                            text = pair.first,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (pair.second.isNotBlank()) {
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { wordsList.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete word",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add Word Row Input Box (When not in bulk paste mode)
                    if (!isBulkPasteOpen) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newWordText,
                                        onValueChange = { newWordText = it },
                                        label = { Text("Word to spell / pronounce") },
                                        placeholder = { Text("E.g. Magnanimous") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = newMeaningText,
                                        onValueChange = { newMeaningText = it },
                                        label = { Text("Meaning / Hindi or English definition") },
                                        placeholder = { Text("E.g. Generous or forgiving") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Button(
                                        onClick = {
                                            if (newWordText.isNotBlank()) {
                                                wordsList.add(Pair(newWordText.trim(), newMeaningText.trim()))
                                                newWordText = ""
                                                newMeaningText = ""
                                            }
                                        },
                                        enabled = newWordText.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add Word to Deck",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            label = { Text("Tags separated by commas (e.g. Chapter 1, Exam)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSave = (wordsList.isNotEmpty() || newWordText.isNotBlank())
                    Button(
                        onClick = { handleSave() },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_create_dictation_deck")
                    ) {
                        val count = wordsList.size + (if (newWordText.isNotBlank()) 1 else 0)
                        Text(
                            text = "Save Deck ($count Words)",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
