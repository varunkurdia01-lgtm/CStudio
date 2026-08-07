import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    '    var output by mutableStateOf("")\n        private set',
    '    var output by mutableStateOf("")\n        private set\n\n    var executionErrorDetails by mutableStateOf<String?>(null)\n        private set'
)

new_compile = """    fun compileAndRun(stdin: String = "") {
        val activeFile = getActiveFile() ?: return
        val code = activeFile.content.text
        if (code.isBlank()) return
            
        isCompiling = true
        output = "Compiling...\\n"
        executionErrorDetails = null

        viewModelScope.launch {
            try {
                val request = CompileRequest(code = code, stdin = stdin)
                val response = ServiceProvider.compilerService.compileCode(request)
                
                val systemErrors = listOf("OCI runtime error", "crun:", "clone: Resource temporarily unavailable", "container startup failed", "runtime unavailable")
                val isSystemError = systemErrors.any { response.stderr.contains(it, ignoreCase = true) || response.stdout.contains(it, ignoreCase = true) }

                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = if (response.stderr.isNotEmpty()) response.stderr else response.stdout
                } else if (response.stderr.isNotEmpty()) {
                    output = response.stderr
                } else {
                    output = "${response.stdout}\\n\\n[Execution time: ${response.executionTimeMs}ms]"
                }
            } catch (e: Exception) {
                val systemErrors = listOf("OCI runtime error", "crun:", "clone: Resource temporarily unavailable", "container startup failed", "runtime unavailable")
                val isSystemError = systemErrors.any { e.message?.contains(it, ignoreCase = true) == true }
                
                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = e.message
                } else {
                    output = "Error: ${e.message}"
                }
            } finally {
                isCompiling = false
            }
        }
    }"""

import re
content = re.sub(r'    fun compileAndRun\(stdin: String = ""\) \{.*?        \}[\s]*\}', new_compile, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
