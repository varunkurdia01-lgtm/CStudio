import sys

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    """    fun createFile(projectName: String, fileName: String, content: String = ""): WorkspaceFile? {
        val projectsFolder = getProjectsFolder() ?: return null""",
    """    fun createFile(projectName: String, fileName: String, content: String = ""): WorkspaceFile? {
        var finalName = fileName
        if (!finalName.contains(".")) {
            finalName += ".c"
        }
        val projectsFolder = getProjectsFolder() ?: return null"""
)

# And replace `fileName` with `finalName` in that function:
# Wait, I'll just do it explicitly:
content = content.replace("if (projDir.findFile(fileName) != null) return null", "if (projDir.findFile(finalName) != null) return null")
content = content.replace("fileName.endsWith(\".c\")", "finalName.endsWith(\".c\")")
content = content.replace("fileName.endsWith(\".h\")", "finalName.endsWith(\".h\")")
content = content.replace("projDir.createFile(mimeType, fileName)", "projDir.createFile(mimeType, finalName)")
content = content.replace("file.name != fileName", "file.name != finalName")
content = content.replace("file.renameTo(fileName)", "file.renameTo(finalName)")
content = content.replace("WorkspaceFile(fileName, projectName, file.lastModified())", "WorkspaceFile(finalName, projectName, file.lastModified())")

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "w") as f:
    f.write(content)
