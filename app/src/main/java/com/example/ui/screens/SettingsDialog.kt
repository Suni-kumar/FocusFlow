package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentTheme
import com.example.model.BrightnessMode
import com.example.model.VisualEngine
import com.example.ui.theme.AccentCyber

@Composable
fun SettingsScreen(
    onDoneClick: () -> Unit,
    isDarkTheme: Boolean = true,
    brightnessMode: BrightnessMode = BrightnessMode.DARK,
    onBrightnessModeChanged: (BrightnessMode) -> Unit = {},
    onThemeToggled: (Boolean) -> Unit = {},
    selectedEngine: VisualEngine = VisualEngine.CLASSIC_OBSIDIAN,
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

    // Intercept hardware/gesture back press to return to main workspace
    BackHandler(enabled = true) {
        onDoneClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek, Borderless Header Bar with Subtle Bottom Hairline
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(selectedAccent.primaryColor, selectedAccent.secondaryColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = selectedAccent.buttonTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Settings",
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
                            containerColor = selectedAccent.primaryColor.copy(alpha = 0.2f),
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

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Global Theme & Brightness Mode Section (3-Way Toggle)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(selectedAccent.primaryColor, selectedAccent.secondaryColor)
                                        )
                                    )
                                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = selectedAccent.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (brightnessMode) {
                                        BrightnessMode.LIGHT -> Icons.Default.LightMode
                                        BrightnessMode.DARK -> Icons.Default.DarkMode
                                        BrightnessMode.SYSTEM -> Icons.Default.SettingsBrightness
                                    },
                                    contentDescription = null,
                                    tint = selectedAccent.buttonTextColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Theme & Appearance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Visual brightness and dynamic accent palettes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 3-Way Brightness Mode Choice Buttons (Dark, Light, System)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                Triple(BrightnessMode.DARK, Icons.Default.DarkMode, "Dark"),
                                Triple(BrightnessMode.LIGHT, Icons.Default.LightMode, "Light"),
                                Triple(BrightnessMode.SYSTEM, Icons.Default.SettingsBrightness, "System")
                            )

                            modes.forEach { (mode, icon, label) ->
                                val isSelected = brightnessMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            onBrightnessModeChanged(mode)
                                            when (mode) {
                                                BrightnessMode.DARK -> onThemeToggled(true)
                                                BrightnessMode.LIGHT -> onThemeToggled(false)
                                                BrightnessMode.SYSTEM -> {}
                                            }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                        .testTag("brightness_mode_${mode.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6 Dynamic Accent Themes Matrix Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DYNAMIC ACCENT PALETTES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.08.sp
                            )
                            Text(
                                text = selectedAccent.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = selectedAccent.primaryColor
                            )
                        }

                        val themes = AccentTheme.values().toList()
                        val rows = themes.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (row in rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (theme in row) {
                                        val isSelected = selectedAccent == theme
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .shadow(
                                                    elevation = if (isSelected) 10.dp else 0.dp,
                                                    shape = RoundedCornerShape(14.dp),
                                                    spotColor = theme.primaryColor
                                                )
                                                .clickable { onAccentChanged(theme) }
                                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                                .testTag("theme_accent_${theme.name.lowercase()}"),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // Concentric dual gradient dots
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                Brush.linearGradient(
                                                                    listOf(theme.primaryColor, theme.secondaryColor)
                                                                )
                                                            )
                                                            .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                                            .shadow(if (isSelected) 6.dp else 2.dp, CircleShape, spotColor = theme.primaryColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(Color.White)
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = theme.label,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = theme.subtitle,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(theme.primaryColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = theme.buttonTextColor,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Visual Engine Theme Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(selectedAccent.primaryColor, selectedAccent.accentGlowColor)
                                        )
                                    )
                                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = selectedAccent.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = selectedAccent.buttonTextColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Visual Surface Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Toggle 3D Liquid Glass or Classic Obsidian surfaces",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 2 Engine Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Classic Obsidian Option
                            val isObsidian = selectedEngine == VisualEngine.CLASSIC_OBSIDIAN
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(
                                        width = if (isObsidian) 2.dp else 1.dp,
                                        color = if (isObsidian) selectedAccent.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onEngineChanged(VisualEngine.CLASSIC_OBSIDIAN) }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Classic Flat",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Clean Structured UI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isObsidian) selectedAccent.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // 3D Liquid Glass Option
                            val isGlass = selectedEngine == VisualEngine.LIQUID_GLASS_3D
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isGlass) {
                                            Brush.linearGradient(
                                                listOf(selectedAccent.primaryColor, selectedAccent.secondaryColor, selectedAccent.accentGlowColor)
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isGlass) 2.dp else 1.dp,
                                        color = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onEngineChanged(VisualEngine.LIQUID_GLASS_3D) }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = if (isGlass) selectedAccent.buttonTextColor else selectedAccent.primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "3D Liquid Glass",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGlass) selectedAccent.buttonTextColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Iridescent Sheen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isGlass) selectedAccent.buttonTextColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Vault Grid Layout Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "VAULT GRID LAYOUT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.08.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { cols ->
                                val isSelected = gridCols == cols
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .border(
                                            1.dp,
                                            if (isSelected) selectedAccent.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onGridColsChanged(cols) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$cols Col${if (cols > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) selectedAccent.buttonTextColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Haptic Feedback Toggle
                item {
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = AccentCyber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Haptic Touch Feedback",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Vibrate on FAB, workspace switch & cards",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = {
                                hapticFeedbackEnabled = it
                                onHapticToggled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentCyber,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Data Backup & Storage (Offline-First Export & Import)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                                        )
                                    )
                                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Data Backup & Storage",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Full offline backup, migration & restoration",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Storage Metrics Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LOCAL PERSISTENT VAULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.08.sp
                                    )
                                    Text(
                                        text = "100% Offline-First",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Files & Folders Stat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "$filesCount files",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "$foldersCount folders",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Decks & Cards Stat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Style,
                                                contentDescription = null,
                                                tint = Color(0xFFA855F7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "$decksCount decks",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "$cardsCount cards",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Export and Import Primary Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Export Backup (JSON) with Download icon
                            Button(
                                onClick = onExportBackupClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_backup_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Export Backup",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // 2. Import Backup (JSON) with Upload icon
                            Button(
                                onClick = onImportBackupClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .testTag("import_backup_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Import Backup",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Text Action for Manual JSON Paste
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = onPasteJsonClick,
                                modifier = Modifier.testTag("paste_json_backup_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Paste Raw Backup JSON",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // AI API Key Section (Dual-Tier BYOK)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GEMINI API KEY (CLIENT-SIDE BYOK)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.08.sp
                            )

                            if (apiKeyInput.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFF065F46).copy(alpha = 0.4f))
                                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(9999.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "BYOK ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Tier 1: Input your Google Gemini API key to run active recall deck generation with your personal quota directly from your device. If left empty, Tier 2 (System Gemini Cloud / Smart Heuristic Fallback) will run seamlessly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            visualTransformation = if (showApiKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("settings_api_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = AccentCyber,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showApiKey = !showApiKey }
                            ) {
                                Text(
                                    text = if (showApiKey) "Hide Key" else "Show Key",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (apiKeyInput.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            apiKeyInput = ""
                                            onCustomApiKeyChanged("")
                                            saveFeedback = true
                                        }
                                    ) {
                                        Text(
                                            text = "Clear Key",
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
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (saveFeedback) Color(0xFF10B981) else AccentCyber
                                    ),
                                    modifier = Modifier.testTag("save_api_key_btn")
                                ) {
                                    Text(
                                        text = if (saveFeedback) "Saved!" else "Save Key",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
