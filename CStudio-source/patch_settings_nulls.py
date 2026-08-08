import re

path = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"
with open(path, "r") as f:
    content = f.read()

target1 = "var currentDir = workspaceDir"
replacement1 = "var currentDir: DocumentFile = workspaceDir"

content = content.replace(target1, replacement1)

with open(path, "w") as f:
    f.write(content)
