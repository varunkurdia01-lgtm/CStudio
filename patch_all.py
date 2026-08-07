import re
import os

# 1. HomeScreen.kt
home_path = "/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt"
with open(home_path, "r") as f:
    home_content = f.read()

home_replacement = """        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (name.endsWith(".c")) "C" else "TXT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(path, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }"""
        
home_target = """        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (name.endsWith(".c")) "C" else "TXT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(path, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }"""

home_content = home_content.replace(home_target, home_replacement)
with open(home_path, "w") as f:
    f.write(home_content)

# 2. EditorScreen.kt
editor_path = "/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt"
with open(editor_path, "r") as f:
    editor_content = f.read()

editor_target_row = """                    // Editor Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""
editor_replacement_row = """                    // Editor Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""

editor_content = editor_content.replace(editor_target_row, editor_replacement_row)

editor_target_text = """                                    Text(file.fileName, color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)"""
editor_replacement_text = """                                    Text(file.fileName, color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)"""
editor_content = editor_content.replace(editor_target_text, editor_replacement_text)

editor_target_topappbar = """                        Text(activeFile?.projectName ?: "CStudio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)"""
editor_replacement_topappbar = """                        Text(activeFile?.projectName ?: "CStudio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)"""
editor_content = editor_content.replace(editor_target_topappbar, editor_replacement_topappbar)

with open(editor_path, "w") as f:
    f.write(editor_content)

# 3. SettingsScreen.kt
settings_path = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"
with open(settings_path, "r") as f:
    settings_content = f.read()

settings_content = settings_content.replace('"Current Path:\\\\n" +', '"Current Path:\\n" +')
with open(settings_path, "w") as f:
    f.write(settings_content)

print("Patch 1 done")
