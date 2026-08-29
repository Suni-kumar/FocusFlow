package com.example.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.model.AccentTheme

val LocalAccentTheme = staticCompositionLocalOf { AccentTheme.BIO_MATRIX }

@Composable
fun SepFolTheme(
    darkTheme: Boolean = true,
    accentTheme: AccentTheme = AccentTheme.BIO_MATRIX,
    content: @Composable () -> Unit
) {
    // Dynamic animated color transitions for theme switching (300ms ease-out)
    val animatedPrimary by animateColorAsState(
        targetValue = accentTheme.primaryColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "themePrimary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = accentTheme.secondaryColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "themeSecondary"
    )
    val animatedTertiary by animateColorAsState(
        targetValue = accentTheme.accentGlowColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "themeTertiary"
    )

    val colorScheme = darkColorScheme(
        primary = animatedPrimary,
        onPrimary = accentTheme.buttonTextColor,
        primaryContainer = animatedPrimary.copy(alpha = 0.20f),
        onPrimaryContainer = Color.White,
        secondary = animatedSecondary,
        onSecondary = Color.White,
        secondaryContainer = animatedSecondary.copy(alpha = 0.20f),
        onSecondaryContainer = Color.White,
        tertiary = animatedTertiary,
        onTertiary = Color.White,
        tertiaryContainer = animatedTertiary.copy(alpha = 0.20f),
        onTertiaryContainer = Color.White,
        background = CanvasVoidDark,
        onBackground = OnSurfaceDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceContainerDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark
    )

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
        LocalAccentTheme provides accentTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
