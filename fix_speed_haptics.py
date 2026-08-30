import re

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'r') as f:
    content = f.read()

# Replace Haptics mechanism back to LocalView
content = content.replace('val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current', 'val haptic = androidx.compose.ui.platform.LocalView.current')
content = content.replace('androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress', 'android.view.HapticFeedbackConstants.VIRTUAL_KEY')
# Also handle the one in LiquidGlassCard
content = content.replace('haptic.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)', 'haptic.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)')

# Speed up the expanding animation
content = content.replace('targetValue = if (isExpanding) 12f', 'targetValue = if (isExpanding) 8f')
content = content.replace('if (isExpanding) androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)', 'if (isExpanding) androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.LinearEasing)')

content = content.replace('kotlinx.coroutines.delay(180)', 'kotlinx.coroutines.delay(60)')
content = content.replace('kotlinx.coroutines.delay(200)', 'kotlinx.coroutines.delay(100)')

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'w') as f:
    f.write(content)

