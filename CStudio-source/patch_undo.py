with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'r') as f:
    content = f.read()

old_logic = """        if (activeFile.content.text != finalValue.text) {
            val oldValue = activeFile.content
            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                activeFile.undoStack.add(oldValue)
                if (activeFile.undoStack.size > 30) activeFile.undoStack.removeAt(0)
                activeFile.redoStack.clear()
            }
        }"""

new_logic = """        if (activeFile.content.text != finalValue.text) {
            if (undoJob == null || undoJob?.isActive != true) {
                activeFile.undoStack.add(activeFile.content)
                if (activeFile.undoStack.size > 50) activeFile.undoStack.removeAt(0)
                activeFile.redoStack.clear()
            }
            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                kotlinx.coroutines.delay(750)
            }
        }"""

content = content.replace(old_logic, new_logic)

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'w') as f:
    f.write(content)
