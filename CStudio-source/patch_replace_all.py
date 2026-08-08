import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    """            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile
            openFiles = newList""",
    """            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile.copy(content = activeFile.content)
            openFiles = newList"""
)

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
