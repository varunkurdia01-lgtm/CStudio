import sys

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    """    fun renameFile(projectName: String, oldName: String, newName: String) {
        val projectsFolder = getProjectsFolder() ?: return""",
    """    fun renameFile(projectName: String, oldName: String, newName: String) {
        var finalNewName = newName
        if (!finalNewName.contains(".")) {
            finalNewName += ".c"
        }
        val projectsFolder = getProjectsFolder() ?: return"""
)

content = content.replace(
    """        if (projDir.findFile(newName) == null) {
            file.renameTo(newName)
        }""",
    """        if (projDir.findFile(finalNewName) == null) {
            file.renameTo(finalNewName)
        }"""
)

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "w") as f:
    f.write(content)
