import re

path = "/app/applet/app/src/main/java/com/example/data/ProjectRepository.kt"
with open(path, "r") as f:
    content = f.read()

target = """    fun duplicateProject(name: String) {
        // DocumentFile doesn't support recursive copy easily without reading/writing.
        // For simplicity, we just create a new project and copy top level files.
        val projectsFolder = getProjectsFolder() ?: return
        val srcDir = projectsFolder.findFile(name) ?: return
        val newName = "${name}_copy"
        if (projectsFolder.findFile(newName) != null) return
        val destDir = projectsFolder.createDirectory(newName) ?: return
        
        srcDir.listFiles().filter { it.isFile }.forEach { file ->
            val newFile = destDir.createFile(file.type ?: "text/plain", file.name ?: "unnamed")
            if (newFile != null) {
                try {
                    context.contentResolver.openInputStream(file.uri)?.use { input ->
                        context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }"""

rep = """    suspend fun duplicateProject(name: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val projectsFolder = getProjectsFolder() ?: return@withContext
        val srcDir = projectsFolder.findFile(name) ?: return@withContext
        val newName = "${name}_copy"
        if (projectsFolder.findFile(newName) != null) return@withContext
        val destDir = projectsFolder.createDirectory(newName) ?: return@withContext
        
        fun copyDirectory(source: androidx.documentfile.provider.DocumentFile, destination: androidx.documentfile.provider.DocumentFile) {
            source.listFiles().forEach { file ->
                if (file.isDirectory) {
                    val newDir = destination.createDirectory(file.name ?: "unnamed")
                    if (newDir != null) {
                        copyDirectory(file, newDir)
                    }
                } else if (file.isFile) {
                    val newFile = destination.createFile(file.type ?: "text/plain", file.name ?: "unnamed")
                    if (newFile != null) {
                        try {
                            context.contentResolver.openInputStream(file.uri)?.use { input ->
                                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        copyDirectory(srcDir, destDir)
    }"""

content = content.replace(target, rep)
with open(path, "w") as f:
    f.write(content)
