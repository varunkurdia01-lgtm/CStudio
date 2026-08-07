import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

triggerContentUpdateStr = """
    private fun triggerContentUpdate(activeFile: OpenFile) {
        val newList = openFiles.toMutableList()
        newList[activeFileIndex] = activeFile.copy(content = activeFile.content)
        openFiles = newList
    }
"""

if "private fun triggerContentUpdate" not in content:
    content = content.replace(
        "fun undo() {",
        triggerContentUpdateStr + "\n    fun undo() {"
    )

content = content.replace(
    "val newList = openFiles.toMutableList()\n            newList[activeFileIndex] = activeFile.copy(content = activeFile.content)\n            openFiles = newList",
    "triggerContentUpdate(activeFile)"
)


search_replace_str = """
    var isCaseSensitive by mutableStateOf(false)
        private set

    fun toggleCaseSensitive() {
        isCaseSensitive = !isCaseSensitive
    }

    fun findNext() {
        val activeFile = getActiveFile() ?: return
        if (searchQuery.isEmpty()) return
        val text = activeFile.content.text
        val cursor = activeFile.content.selection.end
        val idx = text.indexOf(searchQuery, cursor, ignoreCase = !isCaseSensitive)
        if (idx != -1) {
            activeFile.content = androidx.compose.ui.text.input.TextFieldValue(text, androidx.compose.ui.text.TextRange(idx, idx + searchQuery.length))
        } else {
            val idx2 = text.indexOf(searchQuery, 0, ignoreCase = !isCaseSensitive)
            if (idx2 != -1) {
                activeFile.content = androidx.compose.ui.text.input.TextFieldValue(text, androidx.compose.ui.text.TextRange(idx2, idx2 + searchQuery.length))
            }
        }
        triggerContentUpdate(activeFile)
    }

    fun findPrevious() {
        val activeFile = getActiveFile() ?: return
        if (searchQuery.isEmpty()) return
        val text = activeFile.content.text
        val cursor = activeFile.content.selection.start
        val searchEnd = maxOf(0, cursor - 1)
        var idx = text.lastIndexOf(searchQuery, searchEnd, ignoreCase = !isCaseSensitive)
        if (idx != -1) {
            activeFile.content = androidx.compose.ui.text.input.TextFieldValue(text, androidx.compose.ui.text.TextRange(idx, idx + searchQuery.length))
        } else {
            val idx2 = text.lastIndexOf(searchQuery, text.length, ignoreCase = !isCaseSensitive)
            if (idx2 != -1) {
                activeFile.content = androidx.compose.ui.text.input.TextFieldValue(text, androidx.compose.ui.text.TextRange(idx2, idx2 + searchQuery.length))
            }
        }
        triggerContentUpdate(activeFile)
    }

    fun replaceCurrent() {
        val activeFile = getActiveFile() ?: return
        if (searchQuery.isEmpty()) return
        val text = activeFile.content.text
        val selection = activeFile.content.selection
        if (selection.start != selection.end && text.substring(selection.start, selection.end).equals(searchQuery, ignoreCase = !isCaseSensitive)) {
            val newText = text.substring(0, selection.start) + replaceQuery + text.substring(selection.end)
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(selection.start + replaceQuery.length))
            triggerContentUpdate(activeFile)
            if (settingsRepository.autoSave) {
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, newText)
            }
            findNext()
        } else {
            findNext()
        }
    }

    fun replaceAll() {
        val activeFile = getActiveFile() ?: return
        if (searchQuery.isEmpty()) return
        val newText = activeFile.content.text.replace(searchQuery.toRegex(if (isCaseSensitive) setOf() else setOf(RegexOption.IGNORE_CASE)), replaceQuery)
        if (newText != activeFile.content.text) {
            activeFile.undoStack.add(activeFile.content)
            activeFile.content = androidx.compose.ui.text.input.TextFieldValue(newText)
            triggerContentUpdate(activeFile)
            if (settingsRepository.autoSave) {
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, newText)
            }
        }
    }
"""

start_idx = content.find("    fun replaceAll() {")
end_idx = content.find("    fun compileAndRun(stdin: String = \"\") {")

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + search_replace_str + content[end_idx:]
else:
    print("Could not find replaceAll or compileAndRun")

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
