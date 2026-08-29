package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentTheme
import com.example.model.BrightnessMode
import com.example.model.VisualEngine
import com.example.ui.theme.AccentCyber

/**
 * Ultra-Premium, Minimalist Apple/Notion-grade Settings Architecture
 *
 * Streamlined into elegant, grouped frosted glass cards:
 * 1. Appearance & Theme (Dark/Light mode, Accent Swatches, Surface Engine)
 * 2. Interaction & Layout (Haptic Feedback, Vault Columns)
 * 3. AI Studio Engine (Compact BYOK Key Field)
 * 4. Vault Storage & Backup (Metrics, Instant Export/Import/Paste)
 * 5. System Info & Version
 */
@Composable
fun SettingsScreen(
    onDoneClick: () -> Unit,
    isDarkTheme: Boolean = true,
    brightnessMode: BrightnessMode = BrightnessMode.DARK,
    onBrightnessModeChanged: (BrightnessMode) -> Unit = {},
    onThemeToggled: (Boolean) -> Unit = {},
    selectedEngine: VisualEngine = VisualEngine.LIQUID_GLASS_3D,
    onEngineChanged: (VisualEngine) -> Unit = {},
    gridCols: Int = 2,
    onGridColsChanged: (Int) -> Unit = {},
    selectedAccent: AccentTheme = AccentTheme.CYBER_CORE,
    onAccentChanged: (AccentTheme) -> Unit = {},
    customApiKey: String = "",
    onCustomApiKeyChanged: (String) -> Unit = {},
    isHapticEnabled: Boolean = true,
    onHapticToggled: (Boolean) -> Unit = {},
    filesCount: Int = 0,
    foldersCount: Int = 0,
    decksCount: Int = 0,
    cardsCount: Int = 0,
    onExportBackupClick: () -> Unit = {},
    onImportBackupClick: () -> Unit = {},
    onPasteJsonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hapticFeedbackEnabled by remember(isHapticEnabled) { mutableStateOf(isHapticEnabled) }
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        onDoneClick()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(selectedAccent.primaryColor, selectedAccent.secondaryColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = selectedAccent.buttonTextColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Text(
                            text = "Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    }

                    Button(
                        onClick = onDoneClick,
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedAccent.primaryColor.copy(alpha = 0.18f),
                            contentColor = selectedAccent.primaryColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            selectedAccent.primaryColor.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("settings_done_button")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = selectedAccent.primaryColor
                        )
                    }
                }
            }

            // Grouped Settings Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Appearance & Theme
                item {
                    SettingsGroupSection(
                        title = "APPEARANCE",
                        icon = Icons.Default.Palette,
                        accentColor = selectedAccent.primaryColor
                    ) {
                        // 1. Theme Mode Segmented Control
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Color Scheme",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val modes = listOf(
                                    Triple(BrightnessMode.DARK, Icons.Default.DarkMode, "Dark"),
                                    Triple(BrightnessMode.LIGHT, Icons.Default.LightMode, "Light"),
                                    Triple(BrightnessMode.SYSTEM, Icons.Default.SettingsBrightness, "Auto")
                                )

                                modes.forEach { (mode, icon, label) ->
                                    val isSelected = brightnessMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) selectedAccent.primaryColor.copy(alpha = 0.22f)
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                onBrightnessModeChanged(mode)
                                                when (mode) {
                                                    BrightnessMode.DARK -> onThemeToggled(true)
                                                    BrightnessMode.LIGHT -> onThemeToggled(false)
                                                    BrightnessMode.SYSTEM -> {}
                                                }
                                            }
                                            .padding(vertical = 8.dp)
                                            .testTag("brightness_mode_${mode.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        // 2. Accent Theme Swatches
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Accent Palette",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = selectedAccent.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = selectedAccent.primaryColor
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(AccentTheme.values()) { theme ->
                                    val isSelected = selectedAccent == theme
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onAccentChanged(theme) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                            .testTag("theme_accent_${theme.name.lowercase()}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(theme.primaryColor, theme.secondaryColor)
                                                    )
                                                )
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = theme.buttonTextColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = theme.label.split(" ").firstOrNull() ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        // 3. Visual Surface Engine Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3D Liquid Glass Effect",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Translucent refraction highlights & depth",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val isGlass = selectedEngine == VisualEngine.LIQUID_GLASS_3D
                            Switch(
                                checked = isGlass,
                                onCheckedChange = { checked ->
                                    onEngineChanged(if (checked) VisualEngine.LIQUID_GLASS_3D else VisualEngine.CLASSIC_OBSIDIAN)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = selectedAccent.primaryColor,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // Section 2: Interaction & Layout
                item {
                    SettingsGroupSection(
                        title = "INTERACTION & LAYOUT",
                        icon = Icons.Default.Speed,
                        accentColor = selectedAccent.primaryColor
                    ) {
                        // 1. Haptic Feedback Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Touch Feedback",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tactile vibration on tap & card flip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = hapticFeedbackEnabled,
                                onCheckedChange = {
                                    hapticFeedbackEnabled = it
                                    onHapticToggled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = selectedAccent.primaryColor,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        // 2. Vault Grid Columns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Vault Grid Density",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Columns shown in deck vault",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(1, 2, 3, 4).forEach { cols ->
                                    val isSelected = gridCols == cols
                                    Box(
                                        modifier = Modifier
                                            .size(width = 30.dp, height = 28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) selectedAccent.primaryColor.copy(alpha = 0.25f)
                                                else Color.Transparent
                                            )
                                            .clickable { onGridColsChanged(cols) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$cols",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: AI Intelligence Engine
                item {
                    SettingsGroupSection(
                        title = "AI INTELLIGENCE (BYOK)",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = selectedAccent.primaryColor
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gemini API Key",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (apiKeyInput.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(9999.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Use your personal Gemini key for unlimited high-speed flashcard generation. If empty, standard system generation is used.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = {
                                    apiKeyInput = it
                                    saveFeedback = false
                                },
                                placeholder = {
                                    Text(
                                        text = "AIzaSy...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("settings_api_key_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = selectedAccent.primaryColor,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (apiKeyInput.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            apiKeyInput = ""
                                            onCustomApiKeyChanged("")
                                            saveFeedback = true
                                        }
                                    ) {
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onCustomApiKeyChanged(apiKeyInput.trim())
                                        saveFeedback = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (saveFeedback) Color(0xFF10B981) else selectedAccent.primaryColor,
                                        contentColor = selectedAccent.buttonTextColor
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("save_api_key_btn")
                                ) {
                                    Text(
                                        text = if (saveFeedback) "Saved" else "Save Key",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Data & Vault Storage
                item {
                    SettingsGroupSection(
                        title = "VAULT DATA & BACKUP",
                        icon = Icons.Default.Storage,
                        accentColor = selectedAccent.primaryColor
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Minimal Storage Metrics Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MetricItem(icon = Icons.Default.Folder, count = filesCount, label = "Files", tint = Color(0xFF38BDF8))
                                MetricDivider()
                                MetricItem(icon = Icons.Default.Style, count = decksCount, label = "Decks", tint = Color(0xFFA855F7))
                                MetricDivider()
                                MetricItem(icon = Icons.Default.AutoAwesome, count = cardsCount, label = "Cards", tint = selectedAccent.primaryColor)
                            }

                            // Compact Backup Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onExportBackupClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = selectedAccent.primaryColor,
                                        contentColor = selectedAccent.buttonTextColor
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("export_backup_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Export",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = onImportBackupClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("import_backup_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Import",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Manual JSON Paste Link
                            TextButton(
                                onClick = onPasteJsonClick,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("paste_json_backup_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = selectedAccent.primaryColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Paste Raw JSON Backup",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = selectedAccent.primaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 5: App Version Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SepFol Liquid Studio • v2.4.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Local-First • Jetpack Compose Powered",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable Frosted Glass Grouped Settings Card
 */
@Composable
private fun SettingsGroupSection(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.08.sp,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    count: Int,
    label: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    )
}
