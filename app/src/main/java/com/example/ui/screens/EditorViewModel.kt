package com.example.ui.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.CompileRequest
import com.example.api.ServiceProvider
import com.example.data.ProjectRepository
import com.example.data.SettingsRepository
import kotlinx.coroutines.launch

data class OpenFile(
    val projectName: String,
    val fileName: String,
    var content: TextFieldValue,
    val undoStack: MutableList<TextFieldValue> = androidx.compose.runtime.mutableStateListOf(),
    val redoStack: MutableList<TextFieldValue> = androidx.compose.runtime.mutableStateListOf()
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    val projectRepository = ProjectRepository(application)
    val settingsRepository = SettingsRepository(application)

    var openFiles by mutableStateOf<List<OpenFile>>(emptyList())
        private set

    var activeFileIndex by mutableStateOf(-1)
        private set
        
    var output by mutableStateOf("")
        private set

    var executionErrorDetails by mutableStateOf<String?>(null)
        private set

    var isCompiling by mutableStateOf(false)
        private set
    var isCompileSuccess by mutableStateOf<Boolean?>(null)
        private set
        
    var isSearchVisible by mutableStateOf(false)
        private set
        
    var searchQuery by mutableStateOf("")
        private set
        
    var replaceQuery by mutableStateOf("")
        private set
        
    init {
        // Load default project file
        val savedTabs = settingsRepository.getOpenTabsList()
        if (savedTabs.isNotEmpty()) {
            viewModelScope.launch {
                savedTabs.forEach { tab ->
                    val pName = tab.projectName
                    val fName = tab.fileName
                    val content = projectRepository.readFile(pName, fName)
                    if (content.isNotEmpty() || projectRepository.getProjectFiles(pName).any { it.name == fName }) {
                        val newFile = OpenFile(pName, fName, TextFieldValue(content))
                        openFiles = openFiles + newFile
                    }
                }
                val activeIndex = settingsRepository.activeTabIndex
                if (activeIndex in openFiles.indices) {
                    activeFileIndex = activeIndex
                } else if (openFiles.isNotEmpty()) {
                    activeFileIndex = 0
                }
            }
        }
        
        if (openFiles.isEmpty()) {
            val projects = projectRepository.getProjects()
            if (projects.isNotEmpty()) {
                val defaultProj = projects.first().name
                val files = projectRepository.getProjectFiles(defaultProj)
                if (files.isNotEmpty()) {
                    openFile(defaultProj, files.first().name)
                }
            }
        }
    }
    
    private fun saveTabsState() {
        val tabsList = openFiles.map { com.example.data.TabState(it.projectName, it.fileName) }
        settingsRepository.saveOpenTabsList(tabsList)
        settingsRepository.activeTabIndex = activeFileIndex
    }

    fun flushSave() {
        autoSaveJob?.cancel()
        val activeFile = getActiveFile() ?: return
        if (settingsRepository.autoSave) {
            viewModelScope.launch {
                projectRepository.writeFile(activeFile.projectName, activeFile.fileName, activeFile.content.text)
            }
        }
    }

    fun openFile(projectName: String, fileName: String) {
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
    }
    
    fun closeFile(index: Int) {
        if (index == activeFileIndex) {
            flushSave()
        }
        if (index in openFiles.indices) {
            val newList = openFiles.toMutableList()
            newList.removeAt(index)
            openFiles = newList
            if (activeFileIndex >= openFiles.size) {
                activeFileIndex = openFiles.size - 1
            }
            saveTabsState()
        }
    }

    fun onFileDeleted(projectName: String, fileName: String) {
        val index = openFiles.indexOfFirst { it.projectName == projectName && it.fileName == fileName }
        if (index != -1) {
            closeFile(index)
        }
    }

    fun onProjectDeleted(projectName: String) {
        val indicesToRemove = openFiles.indices.filter { openFiles[it].projectName == projectName }.reversed()
        indicesToRemove.forEach { closeFile(it) }
    }

    fun onProjectRenamed(oldName: String, newName: String) {
        var changed = false
        val newList = openFiles.map { 
            if (it.projectName == oldName) {
                changed = true
                it.copy(projectName = newName)
            } else it
        }
        if (changed) {
            openFiles = newList
            saveTabsState()
        }
    }

    fun onFileRenamed(projectName: String, oldFileName: String, newFileName: String) {
        var changed = false
        val newList = openFiles.map { 
            if (it.projectName == projectName && it.fileName == oldFileName) {
                changed = true
                it.copy(fileName = newFileName)
            } else it
        }
        if (changed) {
            openFiles = newList
            saveTabsState()
        }
    }

    fun getActiveFile(): OpenFile? {
        return if (activeFileIndex in openFiles.indices) openFiles[activeFileIndex] else null
    }

    private var autoSaveJob: kotlinx.coroutines.Job? = null
    private var undoJob: kotlinx.coroutines.Job? = null
    
    fun updateCode(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        val activeFile = getActiveFile() ?: return newValue

        var finalValue = newValue
        val textChanged = newValue.text != oldValue.text
        
        if (textChanged) {
            val lengthDiff = newValue.text.length - oldValue.text.length
            val cursor = newValue.selection.start
            
            if (lengthDiff == 1 && cursor > 0) {
                val addedChar = newValue.text[cursor - 1]
                
                if (cursor < newValue.text.length && addedChar == newValue.text[cursor]) {
                    if (addedChar in listOf(')', ']', '}', '"', '\'')) {
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
                    else if (addedChar == '"') finalValue = insertAfterCursor(newValue, "\"")
                    else if (addedChar == '\'') finalValue = insertAfterCursor(newValue, "'")
                    else if (addedChar == '\n') {
                        val lines = newValue.text.substring(0, cursor).split("\n")
                        if (lines.size >= 2) {
                            val prevLine = lines[lines.size - 2]
                            val indent = prevLine.takeWhile { it == ' ' || it == '\t' }
                            var extraIndent = ""
                            if (prevLine.trimEnd().endsWith("{")) {
                                extraIndent = " ".repeat(settingsRepository.tabSize)
                            }
                            if (indent.isNotEmpty() || extraIndent.isNotEmpty()) {
                                finalValue = insertAtCursor(newValue, indent + extraIndent)
                                val newCursor = finalValue.selection.start
                                if (newCursor < finalValue.text.length && finalValue.text[newCursor] == '}') {
                                    val textWithClosingBraceMoved = finalValue.text.substring(0, newCursor) + "\n" + indent + finalValue.text.substring(newCursor)
                                    finalValue = TextFieldValue(
                                        text = textWithClosingBraceMoved,
                                        selection = finalValue.selection,
                                        composition = finalValue.composition
                                    )
                                }
                            }
                        }
                    } else if (addedChar == '}') {
                        val lines = newValue.text.substring(0, cursor).split("\n")
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
            if (undoJob == null || undoJob?.isActive != true) {
                activeFile.undoStack.add(activeFile.content)
                if (activeFile.undoStack.size > 50) activeFile.undoStack.removeAt(0)
                activeFile.redoStack.clear()
            }
            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                kotlinx.coroutines.delay(750)
            }
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

    private fun insertAtCursor(tfv: TextFieldValue, text: String): TextFieldValue {
        val newText = tfv.text.substring(0, tfv.selection.start) + text + tfv.text.substring(tfv.selection.start)
        return TextFieldValue(newText, androidx.compose.ui.text.TextRange(tfv.selection.start + text.length), tfv.composition)
    }

    private fun insertAfterCursor(tfv: TextFieldValue, text: String): TextFieldValue {
        val newText = tfv.text.substring(0, tfv.selection.start) + text + tfv.text.substring(tfv.selection.start)
        return TextFieldValue(newText, tfv.selection, tfv.composition)
    }
    
    
    private fun triggerContentUpdate(activeFile: OpenFile) {
        val newList = openFiles.toMutableList()
        newList[activeFileIndex] = activeFile.copy(content = activeFile.content)
        openFiles = newList
    }

    fun undo(): TextFieldValue? {
        val activeFile = getActiveFile() ?: return null
        undoJob?.cancel()
        if (activeFile.undoStack.isNotEmpty()) {
            activeFile.redoStack.add(activeFile.content)
            val newContent = activeFile.undoStack.removeLast()
            activeFile.content = newContent
            triggerContentUpdate(activeFile)
            return newContent
        }
        return null
    }
    
    fun redo(): TextFieldValue? {
        val activeFile = getActiveFile() ?: return null
        undoJob?.cancel()
        if (activeFile.redoStack.isNotEmpty()) {
            activeFile.undoStack.add(activeFile.content)
            val newContent = activeFile.redoStack.removeLast()
            activeFile.content = newContent
            triggerContentUpdate(activeFile)
            return newContent
        }
        return null
    }
    
    fun toggleSearch() {
        isSearchVisible = !isSearchVisible
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateReplaceQuery(query: String) {
        replaceQuery = query
    }
    

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
                viewModelScope.launch { projectRepository.writeFile(activeFile.projectName, activeFile.fileName, newText) }
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
                viewModelScope.launch { projectRepository.writeFile(activeFile.projectName, activeFile.fileName, newText) }
            }
        }
    }
    fun compileAndRun(stdin: String = "") {
        val activeFile = getActiveFile() ?: return
        val code = activeFile.content.text
        if (code.isBlank()) return
            
        isCompiling = true
        isCompileSuccess = null
        output = "Compiling...\n"
        executionErrorDetails = null

        viewModelScope.launch {
            try {
                val request = CompileRequest(
                    code = code, 
                    language = if (activeFile.fileName.endsWith(".cpp") || activeFile.fileName.endsWith(".cc") || activeFile.fileName.endsWith(".cxx")) "cpp" else "c",
                    stdin = stdin, 
                    compiler = settingsRepository.compiler
                )
                val service = if (settingsRepository.compiler == "LOCAL COMPILER TEST") {
                    ServiceProvider.getLocalCompilerService(getApplication())
                } else {
                    ServiceProvider.compilerService
                }
                val response = service.compileCode(request)
                
                val systemErrors = listOf("OCI runtime error", "crun:", "clone: Resource temporarily unavailable", "container startup failed", "runtime unavailable")
                val isSystemError = systemErrors.any { response.stderr.contains(it, ignoreCase = true) || response.stdout.contains(it, ignoreCase = true) }

                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = if (response.stderr.isNotEmpty()) response.stderr else response.stdout
                    isCompileSuccess = false
                } else if (response.stderr.isNotEmpty()) {
                    output = response.stderr
                    isCompileSuccess = false
                } else {
                    output = "${response.stdout}\n\n[Execution time: ${response.executionTimeMs}ms]"
                    isCompileSuccess = true
                }            } catch (e: Exception) {
                val systemErrors = listOf("OCI runtime error", "crun:", "clone: Resource temporarily unavailable", "container startup failed", "runtime unavailable")
                val isSystemError = systemErrors.any { e.message?.contains(it, ignoreCase = true) == true }
                
                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = e.message
                } else {
                    output = "Error: ${e.message}"
                }
                isCompileSuccess = false            } finally {
                isCompiling = false
            }
        }
    }
}
