package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// True Deep Obsidian Slate Palette
val SurfaceDark = Color(0xFF0B0B12)
val SurfaceContainerLowestDark = Color(0xFF05050A)
val SurfaceContainerLowDark = Color(0xFF0B0B12)
val SurfaceContainerDark = Color(0xFF12111C)
val SurfaceContainerHighDark = Color(0xFF181726)
val SurfaceContainerHighestDark = Color(0xFF1E1D30)

val OnSurfaceDark = Color(0xFFF8FAFC)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF334155)
val OutlineVariantDark = Color(0xFF1E293B)

// Titanium Indigo & Electric Violet
val PrimaryDark = Color(0xFFA78BFA) // Electric Violet light
val OnPrimaryDark = Color(0xFF2E1065)
val PrimaryContainerDark = Color(0xFF6366F1) // Titanium Indigo
val OnPrimaryContainerDark = Color(0xFFE0E7FF)
val PrimaryFixedDim = Color(0xFFA78BFA)
val OnPrimaryFixed = Color(0xFF2E1065)

val SecondaryDark = Color(0xFF818CF8)
val OnSecondaryDark = Color(0xFF312E81)
val SecondaryContainerDark = Color(0xFF4338CA)
val OnSecondaryContainerDark = Color(0xFFC7D2FE)

val TertiaryDark = Color(0xFF38BDF8) // Cyber Cyan touch
val OnTertiaryDark = Color(0xFF082F49)
val TertiaryContainerDark = Color(0xFF0284C7)
val OnTertiaryContainerDark = Color(0xFFE0F2FE)

val ErrorDark = Color(0xFFF43F5E) // Sunset Red
val OnErrorDark = Color(0xFF881337)
val ErrorContainerDark = Color(0xFFBE123C)
val OnErrorContainerDark = Color(0xFFFFE4E6)

val BackgroundDark = Color(0xFF05050A) // Ultra Deep Space
val OnBackgroundDark = Color(0xFFF8FAFC)

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
