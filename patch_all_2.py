import re

# 1. CStudioApp.kt
path1 = "/app/applet/app/src/main/java/com/example/CStudioApp.kt"
with open(path1, "r") as f:
    c1 = f.read()

c1 = c1.replace(
    'val navController = rememberNavController()',
    'val navController = rememberNavController()\n    val scope = androidx.compose.runtime.rememberCoroutineScope()'
)
c1 = c1.replace(
    'repo.createFile(safeName, "main.c", "// $lessonName\\n#include <stdio.h>\\n\\nint main() {\\n    return 0;\\n}")',
    'scope.launch { repo.createFile(safeName, "main.c", "// $lessonName\\n#include <stdio.h>\\n\\nint main() {\\n    return 0;\\n}") }'
)
with open(path1, "w") as f:
    f.write(c1)

# 2. FilesScreen.kt
path2 = "/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt"
with open(path2, "r") as f:
    c2 = f.read()

c2 = c2.replace(
    'var projects by remember { mutableStateOf(repository.getProjects()) }',
    'val scope = androidx.compose.runtime.rememberCoroutineScope()\n    var projects by remember { mutableStateOf(repository.getProjects()) }'
)

c2 = c2.replace(
    'repository.renameFile(proj, file, renameFileName)\n                        onFileRenamed(proj, file, renameFileName)\n                        projects = repository.getProjects()',
    'scope.launch {\n                            repository.renameFile(proj, file, renameFileName)\n                            onFileRenamed(proj, file, renameFileName)\n                            projects = repository.getProjects()\n                        }'
)

c2 = c2.replace(
    'repository.createFile(newProjectName, "main.c", "#include <stdio.h>\\n\\nint main() {\\n    return 0;\\n}")\n                        projects = repository.getProjects()',
    'scope.launch {\n                            repository.createFile(newProjectName, "main.c", "#include <stdio.h>\\n\\nint main() {\\n    return 0;\\n}")\n                            projects = repository.getProjects()\n                        }'
)

c2 = c2.replace(
    'repository.deleteFile(project.name, file.name)\n                                            onFileDeleted(project.name, file.name)\n                                            projects = repository.getProjects() // Refresh',
    'scope.launch {\n                                                repository.deleteFile(project.name, file.name)\n                                                onFileDeleted(project.name, file.name)\n                                                projects = repository.getProjects() // Refresh\n                                            }'
)

c2 = c2.replace(
    'repository.createFile(project.name, newFileName)\n                                        newFileName = ""\n                                        projects = repository.getProjects()',
    'scope.launch {\n                                            repository.createFile(project.name, newFileName)\n                                            newFileName = ""\n                                            projects = repository.getProjects()\n                                        }'
)

with open(path2, "w") as f:
    f.write(c2)

# 3. EditorViewModel.kt
path3 = "/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt"
with open(path3, "r") as f:
    c3 = f.read()

target_init = """        val savedTabs = settingsRepository.getOpenTabsList()
        if (savedTabs.isNotEmpty()) {
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
        }"""
        
replacement_init = """        val savedTabs = settingsRepository.getOpenTabsList()
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
        }"""
        
c3 = c3.replace(target_init, replacement_init)
with open(path3, "w") as f:
    f.write(c3)

print("Patch 2 done")
