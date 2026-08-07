import sys

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    """        val file = projDir.createFile("text/plain", fileName) ?: return null""",
    """        val mimeType = when {
            fileName.endsWith(".c") -> "text/x-c"
            fileName.endsWith(".h") -> "text/x-h"
            else -> "text/plain"
        }
        val file = projDir.createFile(mimeType, fileName) ?: return null
        if (file.name != null && file.name != fileName) {
            file.renameTo(fileName)
        }"""
)

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "w") as f:
    f.write(content)
