with open('/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()
    
content = content.replace("import androidx.compose.material.icons.outlined.InsertDriveFile", "import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile")

with open('/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
