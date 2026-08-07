with open('/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt', 'r') as f:
    lines = f.readlines()

for i in range(len(lines)):
    if 'if (newProjectName.isNotBlank()) {' in lines[i]:
        # Replace the block
        end_idx = i
        while 'showCreateDialog = false' not in lines[end_idx]:
            end_idx += 1
        
        replacement = """                    if (newProjectName.isNotBlank()) {
                        val name = newProjectName
                        scope.launch {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                repository.createProject(name)
                                repository.createFile(name, "main.c", "#include <stdio.h>\\n\\nint main() {\\n    return 0;\\n}")
                            }
                            projects = repository.getProjects()
                        }
                    }
                    """
        lines[i:end_idx] = [replacement]
        break

with open('/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt', 'w') as f:
    f.writelines(lines)
