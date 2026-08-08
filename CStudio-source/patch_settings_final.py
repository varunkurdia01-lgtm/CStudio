import sys

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'text = "Current Path:\n" \+ \(settings\.workspaceUri \?: "Not set"\),', 'text = "Current Path:\\n" + (settings.workspaceUri ?: "Not set"),', content)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
