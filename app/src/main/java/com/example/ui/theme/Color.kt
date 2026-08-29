package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. CANVAS & VOID TOKENS (DARK ONLY)
// ==========================================
val CanvasVoidDark = Color(0xFF0B1326) // Deep Midnight Navy Canvas (#0b1326)
val CanvasLight = Color(0xFF0B1326)    // Kept dark only

// Surface hierarchy tokens
val SurfaceDark = Color(0xFF0B1326)
val SurfaceDimDark = Color(0xFF060D20)
val SurfaceBrightDark = Color(0xFF31394E)
val SurfaceContainerLowestDark = Color(0xFF060D20)
val SurfaceContainerLowDark = Color(0xFF131B2E)
val SurfaceContainerDark = Color(0xFF171F33)
val SurfaceContainerHighDark = Color(0xFF222A3E)
val SurfaceContainerHighestDark = Color(0xFF2D3449)
val SurfaceCharcoalDark = Color(0xFF060D20)
val SurfaceSlateDark = Color(0xFF171F33)

val SurfaceLight = Color(0xFF0B1326)
val SurfaceDimLight = Color(0xFF060D20)
val SurfaceBrightLight = Color(0xFF31394E)
val SurfaceContainerLowestLight = Color(0xFF060D20)
val SurfaceContainerLowLight = Color(0xFF131B2E)
val SurfaceContainerLight = Color(0xFF171F33)
val SurfaceContainerHighLight = Color(0xFF222A3E)
val SurfaceContainerHighestLight = Color(0xFF2D3449)
val SurfaceCharcoalLight = Color(0xFF060D20)
val SurfaceSlateLight = Color(0xFF171F33)

// ==========================================
// 2. LIQUID GLASS SURFACE & REFRACTION TOKENS
// ==========================================
val GlassSurfaceDarkLowest = Color(0x05FFFFFF)  // rgba(255, 255, 255, 0.02)
val GlassSurfaceDarkLow = Color(0x0AFFFFFF)     // rgba(255, 255, 255, 0.04)
val GlassSurfaceDarkDefault = Color(0x12FFFFFF) // rgba(255, 255, 255, 0.07)
val GlassSurfaceDarkHigh = Color(0x1FFFFFFF)    // rgba(255, 255, 255, 0.12)

val GlassBorderDarkSubtle = Color(0x14FFFFFF)   // rgba(255, 255, 255, 0.08)
val GlassBorderDarkFrosted = Color(0x26FFFFFF)  // rgba(255, 255, 255, 0.15)
val GlassBorderDarkHighlight = Color(0x4DFFFFFF) // rgba(255, 255, 255, 0.30) - Refraction top/left light catch

val SurfaceGlass = Color(0x08FFFFFF)            // rgba(255, 255, 255, 0.03)
val BorderGlass = Color(0x1FFFFFFF)             // rgba(255, 255, 255, 0.12)
val GlassRefractionTop = Color(0x33FFFFFF)      // rgba(255, 255, 255, 0.20)
val GlassRefractionLeft = Color(0x1AFFFFFF)     // rgba(255, 255, 255, 0.10)

// Light Glass Tokens (fallback mapped to dark glass)
val GlassSurfaceLightLowest = GlassSurfaceDarkLowest
val GlassSurfaceLightLow = GlassSurfaceDarkLow
val GlassSurfaceLightDefault = GlassSurfaceDarkDefault
val GlassBorderLightSubtle = GlassBorderDarkSubtle
val GlassBorderLightFrosted = GlassBorderDarkFrosted

// ==========================================
// 3. TYPOGRAPHY NEUTRALS (HIGH CONTRAST)
// ==========================================
val OnSurfaceDark = Color(0xFFDBE2FD)              // Crisp Luminous White/Ice (#dbe2fd)
val OnSurfaceVariantDark = Color(0xFFBBCABF)       // Muted Sage/Slate (#bbcabf)
val OnSurfaceMutedDark = Color(0xFF86948A)         // Subdued Outline/Slate (#86948a)

val OnSurfaceLight = OnSurfaceDark
val OnSurfaceVariantLight = OnSurfaceVariantDark
val OnSurfaceMutedLight = OnSurfaceMutedDark

// ==========================================
// 4. ACCENT & SEMANTIC PALETTES (FROM REFERENCES)
// ==========================================
// Primary: Bio Matrix Emerald Glow (#4edea3 / #10b981)
val AccentEmerald = Color(0xFF4EDEA3)
val PrimaryEmerald = Color(0xFF4EDEA3)
val PrimaryContainerEmerald = Color(0xFF10B981)
val OnPrimaryEmerald = Color(0xFF003824)
val PrimaryFixedEmerald = Color(0xFF6FFBBE)

// Secondary: Periwinkle Indigo (#c0c1ff / #818cf8)
val SecondaryIndigo = Color(0xFFC0C1FF)
val SecondaryContainerIndigo = Color(0xFF3131C0)
val OnSecondaryIndigo = Color(0xFF1000A9)

// Tertiary: Soft Rose / Coral / PDF (#ffb2b7 / #ff7886)
val TertiaryRose = Color(0xFFFFB2B7)
val TertiaryContainerRose = Color(0xFFFF7886)
val OnTertiaryRose = Color(0xFF67001B)

// Status colors
val StatusSuccess = Color(0xFF4EDEA3) // Emerald
val StatusWarning = Color(0xFFF59E0B) // Amber
val StatusDanger = Color(0xFFFFB4AB)  // Rose Red (#ffb4ab)
val StatusInfo = Color(0xFF818CF8)    // Periwinkle Blue

// Default Active Accent Mappings
val PrimaryDark = Color(0xFF4EDEA3)
val OnPrimaryDark = Color(0xFF003824)
val PrimaryContainerDark = Color(0xFF10B981)
val OnPrimaryContainerDark = Color(0xFF00422B)

val PrimaryLight = PrimaryDark
val OnPrimaryLight = OnPrimaryDark
val PrimaryContainerLight = PrimaryContainerDark
val OnPrimaryContainerLight = OnPrimaryContainerDark

val SecondaryDark = Color(0xFFC0C1FF)
val OnSecondaryDark = Color(0xFF1000A9)
val SecondaryContainerDark = Color(0xFF3131C0)
val OnSecondaryContainerDark = Color(0xFFB0B2FF)

val SecondaryLight = SecondaryDark
val OnSecondaryLight = OnSecondaryDark
val SecondaryContainerLight = SecondaryContainerDark
val OnSecondaryContainerLight = OnSecondaryContainerDark

val TertiaryDark = Color(0xFFFFB2B7)
val OnTertiaryDark = Color(0xFF67001B)
val TertiaryContainerDark = Color(0xFFFF7886)
val OnTertiaryContainerDark = Color(0xFF780021)

val TertiaryLight = TertiaryDark
val OnTertiaryLight = OnTertiaryDark
val TertiaryContainerLight = TertiaryContainerDark
val OnTertiaryContainerLight = OnTertiaryContainerDark

val OutlineDark = Color(0xFF86948A)
val OutlineVariantDark = Color(0x1FFFFFFF)

val OutlineLight = OutlineDark
val OutlineVariantLight = OutlineVariantDark

val FocusBlue = Color(0xFF4EDEA3)

// Legacy accent color constants
val AccentCyber = Color(0xFF6366F1)
val AccentMidnight = Color(0xFF06B6D4)
val AccentSunset = Color(0xFFF59E0B)
val AccentNebula = Color(0xFFEF4444)
val AccentFrosted = Color(0xFFFFFFFF)

// ==========================================
// 5. LIQUID & GLOW GRADIENTS
// ==========================================
// FAB Gradient: from-tertiary-container (#ff7886) to secondary-container (#8b5cf6 / #3131c0)
val FabGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF7886), Color(0xFF8B5CF6))
)

val FabAmbientGlow = Color(0x668B5CF6)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x1F4EDEA3), Color(0x15C0C1FF), Color(0x10FF7886))
)

val LiquidGlassReflection = Brush.linearGradient(
    colors = listOf(Color(0x33FFFFFF), Color(0x05FFFFFF), Color(0x1AFFFFFF))
)

val HeaderGradientText = Brush.linearGradient(
    colors = listOf(Color(0xFF4EDEA3), Color(0xFF6FFBBE))
)

