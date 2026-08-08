import sys

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("text = \"Current Path:\\n${settings.workspaceUri ?: \\\"Not set\\\"}\",", "text = \"Current Path:\\n${settings.workspaceUri ?: \\\"Not set\\\"}\",")
# Let's just fix it properly by searching for that block and replacing it
# Wait, I can just use python re.sub

import re
content = re.sub(r'text = "Current Path:\\n\$\{settings\.workspaceUri \?: \\?"Not set\\?"\}",', 'text = "Current Path:\\n" + (settings.workspaceUri ?: "Not set"),', content)
content = re.sub(r'text = "Current Path:\\n\$\{settings\.workspaceUri \?: "Not set"\}",', 'text = "Current Path:\\n" + (settings.workspaceUri ?: "Not set"),', content)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
