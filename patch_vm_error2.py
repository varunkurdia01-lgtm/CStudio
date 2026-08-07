import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('output = "Compiling..."', 'output = "Compiling...\\n"')
content = content.replace('output = "${response.stdout}[Execution time: ${response.executionTimeMs}ms]"', 'output = "${response.stdout}\\n\\n[Execution time: ${response.executionTimeMs}ms]"')

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
