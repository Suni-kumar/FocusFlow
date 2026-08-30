import re

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'r') as f:
    content = f.read()

content = content.replace("coroutineScope.kotlinx.coroutines.launch", "coroutineScope.launch")
content = content.replace("kotlinx.coroutines.delay", "kotlinx.coroutines.delay")
content = content.replace("package com.example.ui.components", "package com.example.ui.components\n\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.delay")

with open('app/src/main/java/com/example/ui/components/GlassCard.kt', 'w') as f:
    f.write(content)
