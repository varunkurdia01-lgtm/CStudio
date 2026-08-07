import sys

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

import re

# We will replace the zip/unzip logic with a simple Toast directing them to File Manager.
content = re.sub(
    r'val rootDir = File\(context\.filesDir, "CStudioProjects"\).*?Toast\.makeText\(context, "Exported to \$\{backupFile\.name\}", Toast\.LENGTH_LONG\)\.show\(\)',
    'Toast.makeText(context, "Workspace is accessible via File Manager. You can copy the folder to backup.", Toast.LENGTH_LONG).show()',
    content, flags=re.DOTALL
)

content = re.sub(
    r'val backupFile = File\(context\.getExternalFilesDir\(null\), "CStudio_Backup\.zip"\).*?Toast\.makeText\(context, "Imported successfully!", Toast\.LENGTH_LONG\)\.show\(\).*?else \{.*?Toast\.makeText\(context, "No backup found in external files\.", Toast\.LENGTH_LONG\)\.show\(\).*?\}',
    'Toast.makeText(context, "To import, copy your files into the selected Workspace folder using File Manager.", Toast.LENGTH_LONG).show()',
    content, flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
