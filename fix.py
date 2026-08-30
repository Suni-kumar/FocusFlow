import re

with open('app/src/main/java/com/example/ui/SepFolApp.kt', 'r') as f:
    content = f.read()

# I want to fix the transitionSpec block. 
# It currently has:
# transitionSpec = {
# ... my injected code ...
# /*                            fadeIn(animationSpec = tween(durationMillis = 100))
#                                 .togetherWith(
#                                     fadeOut(animationSpec = tween(durationMillis = 80))
#                                 )
#                         },
#                         label = "screenTransition"

new_code = """transitionSpec = {
                            val isForward = targetState.first != ScreenState.MAIN_WORKSPACE && initialState.first == ScreenState.MAIN_WORKSPACE
                            val isBackward = targetState.first == ScreenState.MAIN_WORKSPACE && initialState.first != ScreenState.MAIN_WORKSPACE

                            if (isForward) {
                                (androidx.compose.animation.scaleIn(initialScale = 0.85f, animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200))) togetherWith (androidx.compose.animation.scaleOut(targetScale = 1.15f, animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)))
                            } else if (isBackward) {
                                (androidx.compose.animation.scaleIn(initialScale = 1.15f, animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200))) togetherWith (androidx.compose.animation.scaleOut(targetScale = 0.85f, animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)))
                            } else {
                                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) togetherWith androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(100))
                            }
                        },
                        label = "screenTransition\""""

# Let's use a regex to replace everything from `transitionSpec = {` down to `label = "screenTransition"`
content = re.sub(r'transitionSpec = \{.*label = "screenTransition"', new_code, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/SepFolApp.kt', 'w') as f:
    f.write(content)
