import re
import sys

# 1. CStudioApp.kt
path1 = "/app/applet/app/src/main/java/com/example/CStudioApp.kt"
with open(path1, "r") as f:
    c1 = f.read()

if "import kotlinx.coroutines.launch" not in c1:
    c1 = c1.replace("package com.example", "package com.example\n\nimport kotlinx.coroutines.launch")

with open(path1, "w") as f:
    f.write(c1)

# 2. FilesScreen.kt
path2 = "/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt"
with open(path2, "r") as f:
    c2 = f.read()

if "import kotlinx.coroutines.launch" not in c2:
    c2 = c2.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport kotlinx.coroutines.launch")

with open(path2, "w") as f:
    f.write(c2)

# 3. EditorViewModel.kt
path3 = "/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt"
with open(path3, "r") as f:
    c3 = f.read()

old_open = """    fun openFile(projectName: String, fileName: String) {
        flushSave()
        val existingIndex = openFiles.indexOfFirst { it.projectName == projectName && it.fileName == fileName }
        if (existingIndex != -1) {
            activeFileIndex = existingIndex
        } else {
            val content = projectRepository.readFile(projectName, fileName)
            val newFile = OpenFile(projectName, fileName, TextFieldValue(content))
            openFiles = openFiles + newFile
            activeFileIndex = openFiles.size - 1
        }
        saveTabsState()
    }"""
    
new_open = """    fun openFile(projectName: String, fileName: String) {
        flushSave()
        val existingIndex = openFiles.indexOfFirst { it.projectName == projectName && it.fileName == fileName }
        if (existingIndex != -1) {
            activeFileIndex = existingIndex
        } else {
            viewModelScope.launch {
                val content = projectRepository.readFile(projectName, fileName)
                val newFile = OpenFile(projectName, fileName, TextFieldValue(content))
                openFiles = openFiles + newFile
                activeFileIndex = openFiles.size - 1
                saveTabsState()
            }
        }
    }"""
    
c3 = c3.replace(old_open, new_open)
with open(path3, "w") as f:
    f.write(c3)

print("Final patch done")
