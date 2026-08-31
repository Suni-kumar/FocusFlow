package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.model.AccentTheme

val LocalAccentTheme = staticCompositionLocalOf { AccentTheme.BIO_MATRIX }
val Local3DGlassEnabled = staticCompositionLocalOf { true }

@Composable
fun SepFolTheme(
    darkTheme: Boolean = true,
    accentTheme: AccentTheme = AccentTheme.BIO_MATRIX,
    is3DGlassEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkColors = darkColorScheme(
        primary = accentTheme.primaryColor,
        onPrimary = accentTheme.buttonTextColor,
        primaryContainer = accentTheme.secondaryColor.copy(alpha = 0.25f),
        onPrimaryContainer = accentTheme.primaryColor,
        secondary = accentTheme.secondaryColor,
        onSecondary = Color(0xFF0B0B12),
        secondaryContainer = accentTheme.secondaryColor.copy(alpha = 0.18f),
        onSecondaryContainer = accentTheme.secondaryColor,
        tertiary = accentTheme.accentGlowColor,
        onTertiary = Color.White,
        tertiaryContainer = accentTheme.accentGlowColor.copy(alpha = 0.25f),
        onTertiaryContainer = accentTheme.accentGlowColor,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceContainerHighestDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        outline = accentTheme.primaryColor.copy(alpha = 0.35f),
        outlineVariant = OutlineVariantDark
    )

    val lightColors = lightColorScheme(
        primary = accentTheme.secondaryColor,
        onPrimary = Color.White,
        primaryContainer = accentTheme.primaryColor.copy(alpha = 0.22f),
        onPrimaryContainer = accentTheme.secondaryColor,
        secondary = accentTheme.primaryColor,
        onSecondary = Color.White,
        secondaryContainer = accentTheme.secondaryColor.copy(alpha = 0.12f),
        onSecondaryContainer = accentTheme.secondaryColor,
        tertiary = accentTheme.accentGlowColor,
        onTertiary = Color.White,
        tertiaryContainer = accentTheme.accentGlowColor.copy(alpha = 0.15f),
        onTertiaryContainer = accentTheme.accentGlowColor,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        outline = accentTheme.secondaryColor.copy(alpha = 0.35f),
        outlineVariant = OutlineVariantLight
    )

    val colorScheme = if (darkTheme) darkColors else lightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var ctx = view.context
            while (ctx is android.content.ContextWrapper) {
                if (ctx is Activity) break
                ctx = ctx.baseContext
            }
            val activity = ctx as? Activity
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAccentTheme provides accentTheme,
        Local3DGlassEnabled provides is3DGlassEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
