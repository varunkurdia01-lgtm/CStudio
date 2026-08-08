with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'r') as f:
    content = f.read()

old_undo = """    fun undo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.undoStack.isNotEmpty()) {
            activeFile.redoStack.add(activeFile.content)
            activeFile.content = activeFile.undoStack.removeLast()
            triggerContentUpdate(activeFile)
        }
    }"""

new_undo = """    fun undo() {
        val activeFile = getActiveFile() ?: return
        undoJob?.cancel()
        if (activeFile.undoStack.isNotEmpty()) {
            activeFile.redoStack.add(activeFile.content)
            activeFile.content = activeFile.undoStack.removeLast()
            triggerContentUpdate(activeFile)
        }
    }"""

content = content.replace(old_undo, new_undo)

old_redo = """    fun redo() {
        val activeFile = getActiveFile() ?: return
        if (activeFile.redoStack.isNotEmpty()) {
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = activeFile.redoStack.removeLast()
            triggerContentUpdate(activeFile)
        }
    }"""

new_redo = """    fun redo() {
        val activeFile = getActiveFile() ?: return
        undoJob?.cancel()
        if (activeFile.redoStack.isNotEmpty()) {
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = activeFile.redoStack.removeLast()
            triggerContentUpdate(activeFile)
        }
    }"""

content = content.replace(old_redo, new_redo)

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'w') as f:
    f.write(content)
