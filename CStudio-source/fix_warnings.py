import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    content = content.replace('Icons.Outlined.LibraryBooks', 'Icons.AutoMirrored.Outlined.LibraryBooks')
    content = content.replace('Icons.Filled.ArrowBack', 'Icons.AutoMirrored.Filled.ArrowBack')
    content = content.replace('Icons.Default.ArrowBack', 'Icons.AutoMirrored.Filled.ArrowBack')
    content = content.replace('Icons.Outlined.InsertDriveFile', 'Icons.AutoMirrored.Outlined.InsertDriveFile')
    
    # Fix KeyboardOptions
    content = content.replace('autoCorrect = false', 'autoCorrectEnabled = false')

    with open(filepath, 'w') as f:
        f.write(content)

fix_file('/app/applet/app/src/main/java/com/example/CStudioApp.kt')
fix_file('/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt')
fix_file('/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt')
fix_file('/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt')
fix_file('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt')
