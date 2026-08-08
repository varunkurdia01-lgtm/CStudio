import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "fun updateCode(newValue: TextFieldValue) {",
    "fun updateCode(newValue: TextFieldValue): TextFieldValue {"
)

content = content.replace(
    "val activeFile = getActiveFile() ?: return",
    "val activeFile = getActiveFile() ?: return newValue"
)

content = content.replace(
    """        // Force recomposition
        val newList = openFiles.toMutableList()
        newList[activeFileIndex] = activeFile
        openFiles = newList""",
    ""
)

content = content.replace(
    """        if (settingsRepository.autoSave) {
            autoSaveJob?.cancel()
            autoSaveJob = viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, finalValue.text)
            }
        }
    }""",
    """        if (settingsRepository.autoSave) {
            autoSaveJob?.cancel()
            autoSaveJob = viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, finalValue.text)
            }
        }
        return finalValue
    }"""
)

content = content.replace(
    """    fun undo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.undoStack.isNotEmpty()) {
            activeFile.redoStack.add(activeFile.content)
            activeFile.content = activeFile.undoStack.removeLast()
            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile
            openFiles = newList
        }
    }""",
    """    fun undo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.undoStack.isNotEmpty()) {
            activeFile.redoStack.add(activeFile.content)
            activeFile.content = activeFile.undoStack.removeLast()
            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile.copy(content = activeFile.content)
            openFiles = newList
        }
    }"""
)

content = content.replace(
    """    fun redo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.redoStack.isNotEmpty()) {
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = activeFile.redoStack.removeLast()
            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile
            openFiles = newList
        }
    }""",
    """    fun redo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.redoStack.isNotEmpty()) {
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = activeFile.redoStack.removeLast()
            val newList = openFiles.toMutableList()
            newList[activeFileIndex] = activeFile.copy(content = activeFile.content)
            openFiles = newList
        }
    }"""
)

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
