import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'r') as f:
    content = f.read()

# Add compileStatus
content = content.replace("var isCompiling by mutableStateOf(false)",
                          "var isCompiling by mutableStateOf(false)\n        private set\n    var isCompileSuccess by mutableStateOf<Boolean?>(null)")

# Reset in compileAndRun
content = content.replace("isCompiling = true",
                          "isCompiling = true\n        isCompileSuccess = null")

# Update in compileAndRun
success_str = """                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = if (response.stderr.isNotEmpty()) response.stderr else response.stdout
                    isCompileSuccess = false
                } else if (response.stderr.isNotEmpty()) {
                    output = response.stderr
                    isCompileSuccess = false
                } else {
                    output = "${response.stdout}\\n\\n[Execution time: ${response.executionTimeMs}ms]"
                    isCompileSuccess = true
                }"""

content = re.sub(r'                if \(isSystemError\) \{.*?(?=            \} catch)', success_str, content, flags=re.DOTALL)

# In catch block
catch_str = """            } catch (e: Exception) {
                val systemErrors = listOf("OCI runtime error", "crun:", "clone: Resource temporarily unavailable", "container startup failed", "runtime unavailable")
                val isSystemError = systemErrors.any { e.message?.contains(it, ignoreCase = true) == true }
                
                if (isSystemError) {
                    output = "Execution service is temporarily unavailable. The remote execution environment could not start your program. Please try again in a few moments."
                    executionErrorDetails = e.message
                } else {
                    output = "Error: ${e.message}"
                }
                isCompileSuccess = false"""

content = re.sub(r'            \} catch \(e: Exception\) \{.*?(?=            \} finally \{)', catch_str, content, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'w') as f:
    f.write(content)
