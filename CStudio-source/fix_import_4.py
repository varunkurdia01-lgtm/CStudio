with open('/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt', 'r') as f:
    content = f.read()
    
content = content.replace("import androidx.compose.material.icons.filled.ArrowBack", "import androidx.compose.material.icons.automirrored.filled.ArrowBack")

with open('/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt', 'w') as f:
    f.write(content)
