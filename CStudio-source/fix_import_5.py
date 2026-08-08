with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()
    
content = content.replace("import androidx.compose.material.icons.filled.ArrowBack", "import androidx.compose.material.icons.automirrored.filled.ArrowBack")

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
