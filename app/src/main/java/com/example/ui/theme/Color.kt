package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. CANVAS & VOID TOKENS
// ==========================================
val CanvasVoidDark = Color(0xFF07060B) // 98% Pure Black Void
val CanvasLight = Color(0xFFF8FAFC)     // Slate-50 Crisp Canvas

// ==========================================
// 2. GLASS SURFACE TOKENS
// ==========================================
val GlassSurfaceDarkLowest = Color(0x08FFFFFF)  // rgba(255, 255, 255, 0.03)
val GlassSurfaceDarkLow = Color(0x0DFFFFFF)     // rgba(255, 255, 255, 0.05)
val GlassSurfaceDarkDefault = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
val GlassSurfaceDarkHigh = Color(0x1FFFFFFF)    // rgba(255, 255, 255, 0.12)

val GlassBorderDarkSubtle = Color(0x14FFFFFF)   // rgba(255, 255, 255, 0.08)
val GlassBorderDarkFrosted = Color(0x26FFFFFF)  // rgba(255, 255, 255, 0.15)
val GlassBorderDarkHighlight = Color(0x40FFFFFF) // rgba(255, 255, 255, 0.25)

// Light Glass Tokens
val GlassSurfaceLightLowest = Color(0xE6FFFFFF)
val GlassSurfaceLightLow = Color(0xF2FFFFFF)
val GlassSurfaceLightDefault = Color(0xFFFFFFFF)
val GlassBorderLightSubtle = Color(0x1A0F172A)
val GlassBorderLightFrosted = Color(0x330F172A)

// Legacy compatibility surface mappings
val SurfaceDark = Color(0xFF0D0C14)
val SurfaceDimDark = Color(0xFF07060B)
val SurfaceBrightDark = Color(0xFF181622)
val SurfaceContainerLowestDark = Color(0xFF07060B)
val SurfaceContainerLowDark = Color(0xFF0F0E18)
val SurfaceContainerDark = Color(0xFF151422)
val SurfaceContainerHighDark = Color(0xFF1D1C2E)
val SurfaceContainerHighestDark = Color(0xFF27253D)
val SurfaceCharcoalDark = Color(0xFF07060B)
val SurfaceSlateDark = Color(0xFF151422)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDimLight = Color(0xFFF1F5F9)
val SurfaceBrightLight = Color(0xFFFFFFFF)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8FAFC)
val SurfaceContainerLight = Color(0xFFF1F5F9)
val SurfaceContainerHighLight = Color(0xFFE2E8F0)
val SurfaceContainerHighestLight = Color(0xFFCBD5E1)
val SurfaceCharcoalLight = Color(0xFFF8FAFC)
val SurfaceSlateLight = Color(0xFFF1F5F9)

// ==========================================
// 3. TYPOGRAPHY NEUTRALS
// ==========================================
val OnSurfaceDark = Color(0xFFFFFFFF)              // 100% Crisp White
val OnSurfaceVariantDark = Color(0xA6FFFFFF)       // 65% Translucent Soft White
val OnSurfaceMutedDark = Color(0x59FFFFFF)         // 35% Translucent Muted White

val OnSurfaceLight = Color(0xFF0F172A)             // 100% Slate-900
val OnSurfaceVariantLight = Color(0xA60F172A)      // 65% Slate
val OnSurfaceMutedLight = Color(0x590F172A)        // 35% Slate

// ==========================================
// 4. STATUS COLORS (CRISP & FUNCTIONAL)
// ==========================================
val StatusSuccess = Color(0xFF10B981) // Emerald Green (Mastered / Ready)
val StatusWarning = Color(0xFFF59E0B) // Amber Orange (Weak Items / Review)
val StatusDanger = Color(0xFFEF4444)  // Crimson Red (Delete / Critical)
val StatusInfo = Color(0xFF3B82F6)    // Electric Sky Blue (Badges / Info)

// ==========================================
// 5. BASE THEME COLORS (DEFAULT INDIGO / CYBER)
// ==========================================
val PrimaryDark = Color(0xFF6366F1)
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF4F46E5)
val OnPrimaryContainerDark = Color(0xFFEEF2FF)

val PrimaryLight = Color(0xFF4F46E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0E7FF)
val OnPrimaryContainerLight = Color(0xFF312E81)

val SecondaryDark = Color(0xFFA855F7)
val OnSecondaryDark = Color(0xFFFFFFFF)
val SecondaryContainerDark = Color(0xFF7E22CE)
val OnSecondaryContainerDark = Color(0xFFFAF5FF)

val SecondaryLight = Color(0xFF7C3AED)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFEDE9FE)
val OnSecondaryContainerLight = Color(0xFF4C1D95)

val TertiaryDark = Color(0xFF06B6D4)
val OnTertiaryDark = Color(0xFF082F49)
val TertiaryContainerDark = Color(0xFF0891B2)
val OnTertiaryContainerDark = Color(0xFFECFEFF)

val TertiaryLight = Color(0xFF0284C7)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFE0F2FE)
val OnTertiaryContainerLight = Color(0xFF075985)

val OutlineDark = Color(0x26FFFFFF)
val OutlineVariantDark = Color(0x14FFFFFF)

val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

val FocusBlue = Color(0xFF3B82F6)

// Legacy accent color constants
val AccentCyber = Color(0xFF6366F1)
val AccentMidnight = Color(0xFF06B6D4)
val AccentEmerald = Color(0xFF10B981)
val AccentSunset = Color(0xFFF59E0B)
val AccentNebula = Color(0xFFEF4444)
val AccentFrosted = Color(0xFFFFFFFF)

// ==========================================
// 6. LIQUID & GLOW GRADIENTS
// ==========================================
val FabGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF9333EA), Color(0xFFDB2777))
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x256366F1), Color(0x25A855F7), Color(0x25EC4899))
)

val LiquidGlassReflection = Brush.linearGradient(
    colors = listOf(Color(0x26FFFFFF), Color(0x00FFFFFF), Color(0x1AFFFFFF))
)

val HeaderGradientText = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFC7D2FE))
)
