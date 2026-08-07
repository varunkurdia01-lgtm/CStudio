import os

files_to_fix = [
    '/app/applet/app/src/main/java/com/example/CStudioApp.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt'
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()
        
    if "import androidx.compose.material.icons.automirrored" not in content:
        content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.*\nimport androidx.compose.material.icons.automirrored.outlined.*")
        
    with open(filepath, 'w') as f:
        f.write(content)
