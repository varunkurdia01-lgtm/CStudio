import re

path = "/app/applet/app/src/main/java/com/example/ui/screens/FilesScreen.kt"
with open(path, "r") as f:
    content = f.read()

target = """                                        onClick = {
                                            repository.duplicateProject(project.name)
                                            projects = repository.getProjects()
                                            showMenu = false
                                        }"""
rep = """                                        onClick = {
                                            scope.launch {
                                                repository.duplicateProject(project.name)
                                                projects = repository.getProjects()
                                            }
                                            showMenu = false
                                        }"""

content = content.replace(target, rep)
with open(path, "w") as f:
    f.write(content)
