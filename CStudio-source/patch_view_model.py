import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

# Replace updateCode method completely
# We'll locate it via start and end
start_idx = content.find("    fun updateCode(newValue: TextFieldValue): TextFieldValue {")
end_idx = content.find("    private fun insertAtCursor(tfv: TextFieldValue, text: String): TextFieldValue {")

new_update_code = """    fun updateCode(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        val activeFile = getActiveFile() ?: return newValue

        var finalValue = newValue
        val textChanged = newValue.text != oldValue.text
        
        if (textChanged) {
            val lengthDiff = newValue.text.length - oldValue.text.length
            val cursor = newValue.selection.start
            
            if (lengthDiff == 1 && cursor > 0) {
                val addedChar = newValue.text[cursor - 1]
                
                if (cursor < newValue.text.length && addedChar == newValue.text[cursor]) {
                    if (addedChar in listOf(')', ']', '}', '"', '\\'')) {
                        finalValue = TextFieldValue(
                            text = oldValue.text,
                            selection = androidx.compose.ui.text.TextRange(cursor),
                            composition = newValue.composition
                        )
                    }
                }
                
                if (finalValue == newValue) {
                    if (addedChar == '{') finalValue = insertAfterCursor(newValue, "}")
                    else if (addedChar == '[') finalValue = insertAfterCursor(newValue, "]")
                    else if (addedChar == '(') finalValue = insertAfterCursor(newValue, ")")
                    else if (addedChar == '"') finalValue = insertAfterCursor(newValue, "\\\"")
                    else if (addedChar == '\\'') finalValue = insertAfterCursor(newValue, "'")
                    else if (addedChar == '\\n') {
                        val lines = newValue.text.substring(0, cursor).split("\\n")
                        if (lines.size >= 2) {
                            val prevLine = lines[lines.size - 2]
                            val indent = prevLine.takeWhile { it == ' ' || it == '\\t' }
                            var extraIndent = ""
                            if (prevLine.trimEnd().endsWith("{")) {
                                extraIndent = " ".repeat(settingsRepository.tabSize)
                            }
                            if (indent.isNotEmpty() || extraIndent.isNotEmpty()) {
                                finalValue = insertAtCursor(newValue, indent + extraIndent)
                                val newCursor = finalValue.selection.start
                                if (newCursor < finalValue.text.length && finalValue.text[newCursor] == '}') {
                                    val textWithClosingBraceMoved = finalValue.text.substring(0, newCursor) + "\\n" + indent + finalValue.text.substring(newCursor)
                                    finalValue = TextFieldValue(
                                        text = textWithClosingBraceMoved,
                                        selection = finalValue.selection,
                                        composition = finalValue.composition
                                    )
                                }
                            }
                        }
                    } else if (addedChar == '}') {
                        val lines = newValue.text.substring(0, cursor).split("\\n")
                        val currentLine = lines.last()
                        if (currentLine.trim() == "}") {
                            val spacesToRemove = settingsRepository.tabSize
                            if (currentLine.length > 1 && currentLine.startsWith(" ")) {
                                val toRemove = minOf(spacesToRemove, currentLine.length - 1)
                                val newText = newValue.text.substring(0, cursor - toRemove - 1) + "}" + newValue.text.substring(cursor)
                                finalValue = TextFieldValue(
                                    text = newText,
                                    selection = androidx.compose.ui.text.TextRange(cursor - toRemove),
                                    composition = newValue.composition
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (activeFile.content.text != finalValue.text) {
            activeFile.undoStack.add(activeFile.content)
            if (activeFile.undoStack.size > 50) activeFile.undoStack.removeAt(0)
            activeFile.redoStack.clear()
        }

        activeFile.content = finalValue

        if (settingsRepository.autoSave && textChanged) {
            autoSaveJob?.cancel()
            autoSaveJob = viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, finalValue.text)
            }
        }
        return finalValue
    }

"""

content = content[:start_idx] + new_update_code + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
