with open('/app/applet/app/src/main/java/com/example/CStudioApp.kt', 'r') as f:
    content = f.read()
    
content = content.replace("import androidx.compose.material.icons.outlined.LibraryBooks", "import androidx.compose.material.icons.automirrored.outlined.LibraryBooks")

with open('/app/applet/app/src/main/java/com/example/CStudioApp.kt', 'w') as f:
    f.write(content)
