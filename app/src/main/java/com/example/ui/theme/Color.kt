package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Obsidian Aurora Palette
val SurfaceDark = Color(0xFF131315)
val SurfaceContainerLowestDark = Color(0xFF0E0E10)
val SurfaceContainerLowDark = Color(0xFF1C1B1D)
val SurfaceContainerDark = Color(0xFF201F21)
val SurfaceContainerHighDark = Color(0xFF2A2A2C)
val SurfaceContainerHighestDark = Color(0xFF353437)

val OnSurfaceDark = Color(0xFFE5E1E4)
val OnSurfaceVariantDark = Color(0xFFCBC3D7)

val OutlineDark = Color(0xFF958EA0)
val OutlineVariantDark = Color(0xFF494454)

val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF3C0091)
val PrimaryContainerDark = Color(0xFFA078FF)
val OnPrimaryContainerDark = Color(0xFF340080)
val PrimaryFixedDim = Color(0xFFD0BCFF)
val OnPrimaryFixed = Color(0xFF23005C)

val SecondaryDark = Color(0xFFD2BBFF)
val OnSecondaryDark = Color(0xFF3F008E)
val SecondaryContainerDark = Color(0xFF6001D1)
val OnSecondaryContainerDark = Color(0xFFC9AEFF)

val TertiaryDark = Color(0xFFADC6FF)
val OnTertiaryDark = Color(0xFF002E6A)
val TertiaryContainerDark = Color(0xFF4D8EFF)
val OnTertiaryContainerDark = Color(0xFF00285D)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF131315)
val OnBackgroundDark = Color(0xFFE5E1E4)

// Legacy Colors
val AccentCyber = Color(0xFF06B6D4)
val AccentEmerald = Color(0xFF10B981)
val AccentFrosted = Color(0xFFF1F5F9)
val AccentMidnight = Color(0xFF6366F1)
val AccentNebula = Color(0xFF8B5CF6)
val AccentSunset = Color(0xFFF43F5E)
val BorderGlass = Color(0x33FFFFFF)
val GlassRefractionTop = Color(0x1AFFFFFF)
val SurfaceCharcoalDark = Color(0xFF121212)
val SurfaceSlateDark = Color(0xFF1E1E1E)
val FocusBlue = Color(0xFF3B82F6)
val FabGradient = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))

// ---------------------------
// Gradients & Effects
// ---------------------------
val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x1FD0BCFF), Color(0x15D2BBFF), Color(0x10ADC6FF))
)

val LiquidGlassReflection = Brush.linearGradient(
    colors = listOf(Color(0x33FFFFFF), Color(0x05FFFFFF), Color(0x1AFFFFFF))
)
