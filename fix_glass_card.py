import re

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'r') as f:
    content = f.read()

# Replace Haptics back to LocalHapticFeedback
content = content.replace('val haptic = androidx.compose.ui.platform.LocalView.current', 'val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current')
content = content.replace('haptic.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)', 'haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)')
content = content.replace('haptic.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)', 'haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)')

# Inject CoroutineScope and Expansion logic in GlassCard
glass_card_replacement = """    var isPressed by remember { mutableStateOf(false) }
    var isExpanding by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var touchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedY = (touchPosition.y / componentSize.height) - 0.5f
            -normalizedY * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed && !isExpanding && touchPosition != Offset.Unspecified && componentSize != IntSize.Zero) {
            val normalizedX = (touchPosition.x / componentSize.width) - 0.5f
            normalizedX * 15f
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanding) 12f else if (isPressed) 0.94f else 1f,
        animationSpec = if (isExpanding) androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing) else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val zIndex by animateFloatAsState(targetValue = if (isExpanding || isPressed) 100f else 0f, label = "zIndex")

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            onClick = { 
                if (onClick != null) {
                    coroutineScope.kotlinx.coroutines.launch {
                        isExpanding = true
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        kotlinx.coroutines.delay(180)
                        onClick.invoke()
                        kotlinx.coroutines.delay(200)
                        isExpanding = false
                    }
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onLongClick?.invoke()
            }
        )
    } else Modifier"""

content = re.sub(
    r'    var isPressed by remember \{ mutableStateOf\(false\) \}.*?    \} else Modifier',
    glass_card_replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'w') as f:
    f.write(content)
