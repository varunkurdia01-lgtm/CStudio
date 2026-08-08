import sys

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "localContent = viewModel.updateCode(it)",
    "localContent = viewModel.updateCode(localContent, it)"
)

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "w") as f:
    f.write(content)
