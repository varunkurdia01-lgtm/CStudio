import sys

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    """        return WorkspaceFile(file.name ?: fileName, projectName, file.lastModified())
    }""",
    """        return WorkspaceFile(fileName, projectName, file.lastModified())
    }"""
)

with open("app/src/main/java/com/example/data/ProjectRepository.kt", "w") as f:
    f.write(content)
