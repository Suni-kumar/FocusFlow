sed -i 's/\.androidx\.compose\.ui\.zIndex\.zIndex(zIndex)/\.zIndex(zIndex)/g' app/src/main/java/com/example/ui/components/GlassCard.kt
sed -i 's/package com.example.ui.components/package com.example.ui.components\nimport androidx.compose.ui.zIndex.zIndex/g' app/src/main/java/com/example/ui/components/GlassCard.kt
